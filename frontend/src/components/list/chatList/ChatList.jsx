import { useEffect, useState } from "react";
import "./chatList.css";
import AddUser from "./addUser/addUser";
import { useUserStore } from "../../../lib/userStore";
import { useChatStore } from "../../../lib/chatStore";
import { getUserChats, findUserById } from "../../../lib/mockData";
import { markConversationAsRead } from "../../chat/chatApi";
import axios from "axios";

const API_URL = "http://localhost:8081/api/v1/conversation";

const ChatList = () => {
  const [chats, setChats] = useState([]);
  const [addMode, setAddMode] = useState(false);
  const [input, setInput] = useState("");

  const { currentUser } = useUserStore();
  const { chatId, changeChat, refreshFlag } = useChatStore();
  const { isUserOnline } = useUserStore();

  // Để fetchConversations có thể gọi lại, đưa nó ra ngoài useEffect
  const fetchConversations = async () => {
    try {
      const token = localStorage.getItem("accessToken");
      const res = await axios.get(`${API_URL}/`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const conversations = res.data.data || [];
      const userId = currentUser.id;
      // Map conversation đúng tên thuộc tính backend
      const chatsWithUsers = conversations.map(conv => {
        // Lấy participant khác user hiện tại (chat 1-1)
        const other = conv.participants.find(p => p.userId !== userId) || conv.participants[0];
        return {
          id: conv.id,
          name: conv.name,
          type: conv.type,
          convAvatar: conv.convAvatar,
          participants: conv.participants,
          updatedAt: conv.updatedAt,
          lastActive: conv.lastActive,
          lastMessage: conv.lastMessagePreview || "", // lấy từ BE
          unreadCount: conv.unreadCount || 0, // Số tin nhắn chưa đọc
          // Thông tin user để hiển thị (participant khác user hiện tại)
          user: {
            userId: other.userId,
            displayName: other.displayName,
            avatarUrl: other.avatarUrl || "./avatar.png",
            isAdmin: other.isAdmin,
            blocked: [] // Đảm bảo luôn có thuộc tính blocked
          },
          isSeen: (conv.unreadCount || 0) === 0 // Đánh dấu đã đọc nếu không có tin nhắn chưa đọc
        };
      });
      setChats(chatsWithUsers);
    } catch (err) {
      setChats([]);
    }
  };

  useEffect(() => {
    fetchConversations();
  }, [currentUser.id, refreshFlag]); // Thêm refreshFlag vào dependency

  const handleSelect = async (chat) => {
    // Mark as read nếu có unread messages
    if (chat.unreadCount > 0) {
      try {
        await markConversationAsRead(chat.id);
        // Update local state to reflect read status
        setChats(prevChats => 
          prevChats.map(c => 
            c.id === chat.id 
              ? { ...c, isSeen: true, unreadCount: 0 }
              : c
          )
        );
      } catch (err) {
        console.error('Failed to mark as read:', err);
      }
    }
    
    setChats(prevChats => 
      prevChats.map(c => 
        c.id === chat.id 
          ? { ...c, isSeen: true }
          : c
      )
    );
    changeChat(chat.id, chat.user, {
      id: chat.id,
      name: chat.name,
      type: chat.type,
      avatarUrl: chat.convAvatar,
      participants: chat.participants
    });
  };

  const filteredChats = chats.filter((c) =>
    c.name.toLowerCase().includes(input.toLowerCase())
  );

  return (
    <div className="chatList">
      <div className="search">
        <div className="searchBar">
          <img src="./search.png" alt="" />
          <input
            type="text"
            placeholder="Search"
            onChange={(e) => setInput(e.target.value)}
          />
        </div>
        <img
          src={addMode ? "./minus.png" : "./plus.png"}
          alt=""
          className="add"
          onClick={() => setAddMode((prev) => !prev)}
        />
      </div>
      <div className="user-list">
        {filteredChats.map((chat) => (
          <div
            className={`item ${(chat.unreadCount > 0 && chat.id !== chatId) ? "unread" : ""}`}
            key={chat.id}
            onClick={() => handleSelect(chat)}
            style={{
              backgroundColor: (chat.unreadCount > 0 && chat.id !== chatId)
                ? "rgba(73, 158, 243, 0.2)" 
                : (chat?.isSeen ? "transparent" : "#5183fe"),
              border: (chat.unreadCount > 0 && chat.id !== chatId) ? "1px solid rgba(73, 158, 243, 0.3)" : "",
              boxShadow: (chat.unreadCount > 0 && chat.id !== chatId) ? "0 0 8px rgba(73, 158, 243, 0.2)" : ""
            }}
          >
            <div style={{ position: 'relative' }}>
              <img
                src={chat.convAvatar || "./avatar.png"}
                alt=""
              />
              {chat.user && isUserOnline(chat.user.userId) && chat.type === "DIRECT" && (
                <div style={{
                  position: 'absolute',
                  bottom: 0,
                  right: 0,
                  width: '12px',
                  height: '12px',
                  backgroundColor: '#4caf50',
                  borderRadius: '50%',
                  border: '2px solid #23283a'
                }}></div>
              )}
            </div>
            <div className="texts">
              <div className="top-row">
                <span className="chat-name">{chat.name}</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  {(chat.unreadCount > 0 && chat.id !== chatId) && (
                    <div className="unread-badge">
                      {chat.unreadCount}
                    </div>
                  )}
                  <span className="last-active">{
                    chat.lastActive ? (() => {
                      const d = new Date(chat.lastActive);
                      const now = new Date();
                      const isToday = d.getDate() === now.getDate() && d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
                      return isToday
                        ? d.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
                        : d.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit" });
                    })() : ""
                  }</span>
                </div>
              </div>
              <div className="bottom-row">
                <p className="last-message" style={{ 
                  fontWeight: (chat.unreadCount > 0 && chat.id !== chatId) ? '600' : '400',
                  color: (chat.unreadCount > 0 && chat.id !== chatId) ? '#ffffff' : '#bfc7d5'
                }}>{chat.lastMessage}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
      {addMode && <AddUser onClose={() => setAddMode(false)} onCreated={() => {
        setAddMode(false);
        // Gọi lại fetchConversations để reload danh sách
        // Ta sẽ tạo một hàm fetchConversations ở ngoài useEffect để có thể gọi lại
        fetchConversations();
      }} />}
    </div>
  );
};

export default ChatList;
