import { BrowserRouter, Routes, Route } from "react-router-dom";
import { useEffect, useRef } from "react";
import Chat from "./components/chat/Chat";
import Detail from "./components/detail/Detail";
import List from "./components/list/List";
import Login from "./components/login/Login";
import Notification from "./components/notification/Notification";
import { useUserStore } from "./lib/userStore";
import { useChatStore } from "./lib/chatStore";
import { createGlobalStompConnection } from "./lib/stompService";
import { markConversationAsRead } from "./components/chat/chatApi";
import axios from "axios";

// Component xử lý redirect sau khi đăng nhập OAuth2
function Oauth2Redirect() {
  const navigate = window.location;
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");
    const username = params.get("username");
    if (token) {
      localStorage.setItem("accessToken", token);
      if (username) localStorage.setItem("username", username);
      window.location.href = "/";
    } else {
      window.location.href = "/login";
    }
  }, []);
  return <div>Đang đăng nhập...</div>;
}

// Custom hook kết nối STOMP toàn cục
function useGlobalStomp(currentUser) {
  const stompRef = useRef(null);
  const { triggerRefresh } = useChatStore();
  const { fetchOnlineUsers } = useUserStore();
  
  useEffect(() => {
    if (!currentUser) return;
    let unsubscribed = false;
    // Lấy danh sách conversation của user
    const fetchAndConnect = async () => {
      const token = localStorage.getItem("accessToken");
      if (!token) return;
      try {
        const res = await axios.get("http://localhost:8081/api/v1/conversation/", {
          headers: { Authorization: `Bearer ${token}` }
        });
        const conversations = res.data.data || [];
        const topics = conversations.map(conv => `/topic/conversations/${conv.id}`);
        // Thêm topic để lắng nghe cập nhật conversation (name, avatar)
        const conversationUpdateTopics = conversations.map(conv => `/topic/conversation-update/${conv.id}`);
        topics.push(...conversationUpdateTopics);
        // Thêm topic để lắng nghe conversation mới của user hiện tại
        const currentUser = JSON.parse(localStorage.getItem("currentUser"));
        if (currentUser?.id) {
          const newConversationTopic = `/topic/conversations/${currentUser.id}`;
          topics.push(newConversationTopic);
          console.log('[WEBSOCKET] Added new conversation topic:', newConversationTopic);
        }
        // Thêm topic để lắng nghe thay đổi trạng thái online
        topics.push('/topic/online-status');
        
        console.log('[GLOBAL-STOMP] Subscribing to topics:', topics);
        
        // Kết nối STOMP và subscribe tất cả topic
        stompRef.current = createGlobalStompConnection({
          token,
          topics,
          onMessage: (data, topic) => {
            console.log('[STOMP] Nhận message từ topic:', topic, 'data:', data);
            if (topic === '/topic/online-status') {
              console.log('[ONLINE-STATUS] Nhận notification:', data);
              // Cập nhật danh sách user online dựa trên notification từ backend
              const { type, userId } = data;
              const { addOnlineUser, removeOnlineUser } = useUserStore.getState();
              
              if (type === 'USER_ONLINE') {
                addOnlineUser(userId);
              } else if (type === 'USER_OFFLINE') {
                removeOnlineUser(userId);
              }
            } else if (topic.startsWith('/topic/conversation-update/')) {
              // Cập nhật thông tin conversation (name, avatar)
              console.log('[CONVERSATION-UPDATE] Nhận cập nhật conversation:', topic, data);
              
              // Lấy conversationId từ topic
              const conversationId = topic.split('/').pop();
              
              // Cập nhật conversation trong chatStore nếu đang xem conversation này
              const { chatId, updateConversation } = useChatStore.getState();
              if (chatId === conversationId) {
                updateConversation(data);
              }
              
              // Trigger refresh chat list để cập nhật tên và avatar trong danh sách
              triggerRefresh();
              
            } else if (topic.startsWith('/topic/conversations/')) {
              console.log('[WEBSOCKET] 📨 Received message on topic:', topic);
              console.log('[WEBSOCKET] 📄 Message data:', data);
              
              // Lấy ID từ topic (có thể là conversationId hoặc userId)
              const topicId = topic.split('/').pop();
              const currentUser = JSON.parse(localStorage.getItem("currentUser"));
              
              console.log('[WEBSOCKET] 🔍 Topic ID:', topicId);
              console.log('[WEBSOCKET] 👤 Current User ID:', currentUser?.id);
              console.log('[WEBSOCKET] 🧮 Are they equal?:', currentUser?.id === topicId);
              
              // Nếu topicId trùng với userId hiện tại => đây là conversation mới hoặc cập nhật chat list
              if (currentUser?.id === topicId) {
                console.log('[WEBSOCKET] � Message for current user:', topicId);
                
                // Nếu là string "CHAT_LIST_UPDATE" thì chỉ cần refresh chat list
                if (data === "CHAT_LIST_UPDATE") {
                  console.log('[CHAT-LIST-UPDATE] � Received chat list update signal');
                  triggerRefresh();
                  console.log('[CHAT-LIST-UPDATE] ✅ Triggered chat list refresh');
                  return;
                }
                
                // Nếu là object conversation thì đây là conversation mới
                if (typeof data === 'object' && data.id) {
                  console.log('[CONVERSATION-NEW] 🎉 Nhận conversation mới từ topic:', topic);
                  console.log('[CONVERSATION-NEW] 📄 Conversation data:', data);
                  
                  // Subscribe to new conversation's messages and updates
                  if (stompRef.current?.client?.connected) {
                    const newConvId = data.id;
                    
                    console.log('[SUBSCRIPTION] 🔔 Subscribing to new conversation topics:', newConvId);
                    
                    // Subscribe to messages from this conversation
                    stompRef.current.client.subscribe(`/topic/conversations/${newConvId}`, (message) => {
                      const messageData = JSON.parse(message.body);
                      console.log('[CHAT] Message from new conversation:', newConvId, messageData);
                      
                      // Broadcast message để Chat component có thể nhận
                      window.dispatchEvent(new CustomEvent('newChatMessage', {
                        detail: { topic: `/topic/conversations/${newConvId}`, message: messageData }
                      }));
                      
                      // Tự động mark as read nếu đang xem conversation này
                      const { chatId } = useChatStore.getState();
                      if (chatId === newConvId) {
                        console.log('[AUTO-MARK-READ] User đang xem conversation, mark as read:', newConvId);
                        markConversationAsRead(newConvId).catch(err => 
                          console.error('[AUTO-MARK-READ] Failed:', err)
                        );
                      }
                      
                      triggerRefresh(); // Refresh chat list khi có tin nhắn mới
                    });
                    
                    // Subscribe to conversation updates
                    stompRef.current.client.subscribe(`/topic/conversation-update/${newConvId}`, (message) => {
                      const updateData = JSON.parse(message.body);
                      console.log('[CONVERSATION-UPDATE] Update from new conversation:', newConvId, updateData);
                      
                      // Cập nhật conversation trong chatStore nếu đang xem conversation này
                      const { chatId, updateConversation } = useChatStore.getState();
                      if (chatId === newConvId) {
                        updateConversation(updateData);
                      }
                      
                      triggerRefresh(); // Refresh chat list
                    });
                  }
                  
                  // Trigger refresh chat list để hiển thị conversation mới
                  triggerRefresh();
                  console.log('[CONVERSATION-NEW] ✅ Triggered chat list refresh and subscribed to new conversation');
                }
              } else {
                // Đây là tin nhắn chat từ conversation
                console.log('[CHAT] Nhận tin nhắn từ conversation:', topic, data);
                
                // Broadcast message để Chat component có thể nhận
                window.dispatchEvent(new CustomEvent('newChatMessage', {
                  detail: { topic, message: data }
                }));
                
                // Kiểm tra nếu user đang xem conversation này thì tự động mark as read
                const { chatId } = useChatStore.getState();
                if (chatId === topicId) {
                  console.log('[AUTO-MARK-READ] User đang xem conversation, mark as read:', topicId);
                  markConversationAsRead(topicId).catch(err => 
                    console.error('[AUTO-MARK-READ] Failed:', err)
                  );
                }
                
                triggerRefresh(); // Tự động reload chat list khi có tin nhắn mới
              }
            }
          },
          onConnect: () => {
            console.log('[GLOBAL-STOMP] Connected successfully');
            // Fetch danh sách user online khi kết nối thành công
            fetchOnlineUsers();
          },
        });
      } catch (e) { console.warn("Không thể kết nối STOMP toàn cục:", e); }
    };
    fetchAndConnect();
    return () => {
      unsubscribed = true;
      if (stompRef.current && stompRef.current.disconnect) {
        stompRef.current.disconnect();
      }
    };
  }, [currentUser, triggerRefresh, fetchOnlineUsers]);
  
  // Expose global stomp client để Chat component có thể gửi tin nhắn
  useEffect(() => {
    window.globalStompClient = stompRef.current;
  }, [stompRef.current]);
}

const App = () => {
  const { currentUser, isLoading, fetchUserInfo } = useUserStore();
  const { chatId } = useChatStore();

  useGlobalStomp(currentUser); // Kết nối STOMP toàn cục khi user đăng nhập

  useEffect(() => {
    // Lấy username từ localStorage để fetch user info
    const username = localStorage.getItem("username");
    if (username) {
      fetchUserInfo(username);
    }
  }, [fetchUserInfo]);

  if (isLoading) return <div className="loading">Loading...</div>;

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/oauth2/redirect" element={<Oauth2Redirect />} />
        <Route path="/" element={
          <div className="container">
            {currentUser ? (
              <>
                <List />
                {chatId && <Chat />}
                {chatId && <Detail />}
              </>
            ) : (
              <Login />
            )}
            <Notification />
          </div>
        } />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
