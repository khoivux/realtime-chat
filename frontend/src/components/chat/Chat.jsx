import React, { useEffect, useRef, useState } from "react";
import "./chat.css";
import EmojiPicker from "emoji-picker-react";
import { useChatStore } from "../../lib/chatStore";
import { useUserStore } from "../../lib/userStore";
import { formatTime, truncateReplyPreview } from "./chatUtils";
import MessageItem from "./MessageItem";
import {
  fetchMessages,
  fetchConversation,
  sendMessage,
  searchUser as searchUserAPI,
  addUserToConversation,
  removeUserFromConversation,
  deleteMessage,
  editMessage,
  logout,
  markConversationAsRead
} from "./chatApi";

// Component riêng biệt cho SeenByAvatars với React.memo để tránh re-render
const SeenByAvatars = React.memo(({ messageId, isOwnMessage, conversation, currentUserId }) => {
  // Helper function được move vào component
  const getUsersWhoSeenMessage = (messageId) => {
    if (!conversation || !conversation.participants) return [];
    
    // Chỉ hiển thị seen avatars trong group chat (>= 2 người) hoặc direct chat với người đang online
    if (conversation.participants.length < 2) return [];
    
    return conversation.participants.filter(participant => 
      participant.lastSeenMessage === messageId && 
      participant.userId !== currentUserId // Không hiển thị avatar của chính mình
    );
  };
  
  const seenByUsers = getUsersWhoSeenMessage(messageId);
  
  if (seenByUsers.length === 0) return null;
  
  return (
    <div 
      className="seen-by-avatars" 
      style={{
        display: 'flex',
        gap: '2px',
        marginTop: '4px',
        // Giống Messenger: tất cả seen avatars đều nằm bên phải
        justifyContent: 'flex-end',
        alignItems: 'center',
        // Padding đồng nhất cho cả tin nhắn own và other
        paddingLeft: '20px', // Padding từ bên trái
        paddingRight: '20px', // Padding từ bên phải để không sát lề
        marginBottom: '4px',
        width: '100%', // Đảm bảo full width để justify-content hoạt động
        // Force isolation from parent styles
        pointerEvents: 'auto',
        position: 'relative',
        zIndex: 10
      }}
      onMouseEnter={(e) => {
        e.stopPropagation();
        e.preventDefault();
      }}
      onMouseLeave={(e) => {
        e.stopPropagation(); 
        e.preventDefault();
      }}
      onClick={(e) => {
        e.stopPropagation();
        e.preventDefault();
      }}
    >
      {seenByUsers.map((user, index) => (
        <div
          key={user.userId}
          style={{
            width: '16px',
            height: '16px',
            borderRadius: '50%',
            overflow: 'hidden',
            border: '1.5px solid #fff',
            marginLeft: index > 0 ? '-6px' : '0', // Overlap avatars slightly
            zIndex: seenByUsers.length - index + 10,
            position: 'relative',
            boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
            cursor: 'pointer',
            transition: 'transform 0.2s ease, filter 0.2s ease',
            filter: 'brightness(0.9)',
            transform: 'scale(1)',
            pointerEvents: 'auto'
          }}
          title={`Đã xem bởi ${user.displayName}`}
          onMouseEnter={(e) => {
            e.stopPropagation();
            e.preventDefault();
            e.currentTarget.style.transform = 'scale(1.3)';
            e.currentTarget.style.filter = 'brightness(1.1)';
            e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.3)';
          }}
          onMouseLeave={(e) => {
            e.stopPropagation();
            e.preventDefault();
            e.currentTarget.style.transform = 'scale(1)';
            e.currentTarget.style.filter = 'brightness(0.9)';
            e.currentTarget.style.boxShadow = '0 1px 3px rgba(0,0,0,0.2)';
          }}
          onClick={(e) => {
            e.stopPropagation();
            e.preventDefault();
          }}
        >
          <img 
            src={user.avatarUrl || "./avatar.png"} 
            alt={user.displayName}
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              pointerEvents: 'none' // Let parent handle events
            }}
          />
        </div>
      ))}
      {seenByUsers.length > 3 && (
        <span 
          style={{ 
            fontSize: '11px', 
            color: '#666', 
            marginLeft: '4px',
            fontWeight: '500',
            pointerEvents: 'none'
          }}
        >
          +{seenByUsers.length - 3}
        </span>
      )}
    </div>
  );
});

const Chat = () => {
  const [chat, setChat] = useState();
  const [open, setOpen] = useState(false);
  const [text, setText] = useState("");
  const [img, setImg] = useState({
    file: null,
    url: "",
  });
  const [hoveredMsgId, setHoveredMsgId] = useState(null);
  const [currentConversation, setCurrentConversation] = useState(null);
  const [showScrollBtn, setShowScrollBtn] = useState(false); // Hiện nút scroll xuống cuối
  const [showAddUser, setShowAddUser] = useState(false);
  const [searchUser, setSearchUser] = useState("");
  const [searchResult, setSearchResult] = useState(null);
  const [adding, setAdding] = useState(false);
  const [removing, setRemoving] = useState(null);
  const [showMembers, setShowMembers] = useState(false);
  const [searching, setSearching] = useState(false); // Add loading state for search
  const [menuMsgId, setMenuMsgId] = useState(null);
  const [editingMsgId, setEditingMsgId] = useState(null);
  const [editText, setEditText] = useState("");
  const [deletingMsgId, setDeletingMsgId] = useState(null);
  const [replyToMsg, setReplyToMsg] = useState(null); // NEW: track message being replied to
  const [isGlobalStompConnected, setIsGlobalStompConnected] = useState(false);
  
  // Pagination states
  const [nextOffset, setNextOffset] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [switchingConversation, setSwitchingConversation] = useState(false); // Track conversation switching
  
  const { currentUser } = useUserStore();
  const { chatId, user, conversation: conversationFromStore, isCurrentUserBlocked, isReceiverBlocked, triggerRefresh } = useChatStore();
  const { isUserOnline } = useUserStore();

  const endRef = useRef(null);
  const centerRef = useRef(null); // Ref cho container của messages

    // Luôn scroll xuống cuối khi có tin nhắn mới (chỉ khi user ở gần cuối)
  useEffect(() => {
    if (!chat?.messages || chat?.messages.length === 0) return;
    
    const centerDiv = centerRef.current;
    if (centerDiv) {
      // Kiểm tra xem user có đang ở gần cuối không (trong vòng 100px)
      const isNearBottom = centerDiv.scrollHeight - centerDiv.scrollTop - centerDiv.clientHeight < 100;
      
      // Chỉ auto-scroll khi user ở gần cuối (đang theo dõi tin nhắn mới)
      if (isNearBottom) {
        // Scroll ngay lập tức xuống cuối, không animation
        centerDiv.scrollTop = centerDiv.scrollHeight;
      }
      
      // Update scroll button visibility
      const isAtBottom = centerDiv.scrollHeight - centerDiv.scrollTop - centerDiv.clientHeight < 40;
      setShowScrollBtn(!isAtBottom);
    }
  }, [chat?.messages]);

  // Scroll button visibility handler
  useEffect(() => {
    const centerDiv = centerRef.current;
    if (!centerDiv) return;
    
    const handleScrollForButton = () => {
      const isAtBottom = centerDiv.scrollHeight - centerDiv.scrollTop - centerDiv.clientHeight < 40;
      setShowScrollBtn(!isAtBottom);
    };
    
    centerDiv.addEventListener('scroll', handleScrollForButton);
    return () => centerDiv.removeEventListener('scroll', handleScrollForButton);
  }, [chatId]); // Chỉ setup lại khi chatId thay đổi


  // Lắng nghe tin nhắn từ global WebSocket connection
  useEffect(() => {
    if (!chatId || !currentUser) return;
    
    const handleNewChatMessage = (event) => {
      const { topic, message: rawMsg } = event.detail;
      const conversationId = topic.replace('/topic/conversations/', '');
      
      // Chỉ xử lý tin nhắn cho conversation hiện tại
      if (conversationId !== chatId) return;
      
      console.log('[CHAT] Nhận tin nhắn real-time cho conversation:', conversationId, rawMsg);
      
      const newMsg = {
        ...rawMsg,
        mine: rawMsg.sender?.userId === currentUser?.id
      };
      
      if (newMsg.deleted) {
        console.log('[STOMP] Nhận tin nhắn đã bị thu hồi:', newMsg);
      }
      
      setChat(prev => {
        if (!prev?.messages) return { messages: [newMsg], nextOffset: 0, hasMore: true };
        
        // Nếu là tin nhắn bị xóa, xóa khỏi danh sách
        if (newMsg.deleted) {
          return { 
            ...prev, 
            messages: prev.messages.filter(m => m.id !== newMsg.id) 
          };
        }
        
        const idx = prev.messages.findIndex(m => m.id === newMsg.id);
        if (idx !== -1) {
          // Nếu là tin nhắn đã tồn tại (edit), cập nhật
          const updated = [...prev.messages];
          updated[idx] = { ...updated[idx], ...newMsg };
          return { ...prev, messages: updated };
        }
        
        // Nếu là tin nhắn mới, thêm vào cuối và tự động mark as read
        const updatedChat = { 
          ...prev, 
          messages: [...prev.messages, newMsg] 
        };
        
        // Tự động mark conversation as read khi nhận tin nhắn mới
        // (chỉ khi user đang active trong conversation này)
        setTimeout(() => {
          markConversationAsRead(chatId)
            .then(() => {
              // Refresh conversation data để cập nhật lastSeenMessage
              return fetchConversation(chatId);
            })
            .then(setCurrentConversation)
            .catch(console.error);
        }, 500);
        
        return updatedChat;
      });
    };

    // Handle seen status updates (NEW)
    const handleSeenStatusUpdate = (event) => {
      const { topic, message: seenData } = event.detail;
      const conversationId = topic.replace('/topic/conversations/', '').replace('/seen', '');
      
      // Chỉ xử lý cho conversation hiện tại
      if (conversationId !== chatId) return;
      
      console.log('[CHAT] Nhận cập nhật seen status real-time:', seenData);
      
      // Cập nhật conversation state với thông tin seen mới
      setCurrentConversation(prev => {
        if (!prev) return prev;
        
        return {
          ...prev,
          participants: prev.participants.map(p => {
            // Tìm participant tương ứng trong seenData
            const updatedParticipant = seenData.participants?.find(sp => sp.userId === p.userId);
            if (updatedParticipant) {
              return {
                ...p,
                lastSeenMessage: updatedParticipant.lastSeenMessage
              };
            }
            return p;
          })
        };
      });
    };
    
    // Lắng nghe custom event từ global STOMP
    window.addEventListener('newChatMessage', handleNewChatMessage);
    window.addEventListener('seenStatusUpdate', handleSeenStatusUpdate); // NEW
    
    return () => {
      window.removeEventListener('newChatMessage', handleNewChatMessage);
      window.removeEventListener('seenStatusUpdate', handleSeenStatusUpdate); // NEW
    };
  }, [chatId, currentUser]);

  // Theo dõi trạng thái global STOMP connection
  useEffect(() => {
    const checkGlobalConnection = () => {
      const globalStomp = window.globalStompClient;
      const isConnected = globalStomp && globalStomp.client && globalStomp.client.connected;
      setIsGlobalStompConnected(isConnected);
    };
    
    // Check ngay lập tức
    checkGlobalConnection();
    
    // Check định kỳ mỗi 2 giây
    const interval = setInterval(checkGlobalConnection, 2000);
    
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (chatId && currentUser) {
      console.log('[CHAT] Switching to conversation:', chatId);
      setSwitchingConversation(true);
      
      // Clear state hoàn toàn khi chuyển conversation
      setChat(null);
      setLoading(true);
      setLoadingMore(false); // Reset loading more state
      setNextOffset(0);
      setHasMore(true);
      
      // Tạm thời disable scroll events để tránh trigger load more khi đang switch
      const centerDiv = centerRef.current;
      if (centerDiv) {
        centerDiv.scrollTop = 0; // Reset scroll position
      }
      
      fetchMessages(chatId, currentUser, 0, 10)
        .then((data) => {
          console.log('[CHAT] Initial load for', chatId, ':', {
            messagesCount: data.messages.length,
            nextOffset: data.nextOffset,
            hasMore: data.hasMore
          });
          
          // Force set states để đảm bảo không có race condition
          setChat(data);
          setNextOffset(data.nextOffset);
          setHasMore(data.hasMore);
          
          // Scroll xuống cuối ngay lập tức khi mở conversation mới
          setTimeout(() => {
            const centerDiv = centerRef.current;
            if (centerDiv) {
              centerDiv.scrollTop = centerDiv.scrollHeight;
              console.log('[CHAT] Scrolled to bottom for conversation:', chatId);
            }
            
            // Mark conversation as read sau khi load xong
            markConversationAsRead(chatId)
              .then(() => {
                // Refresh conversation data để cập nhật lastSeenMessage
                return fetchConversation(chatId);
              })
              .then(setCurrentConversation)
              .catch(console.error);
          }, 50);
        })
        .catch((error) => {
          console.error('[CHAT] Failed to load initial messages:', error);
          setChat({ messages: [], nextOffset: 0, hasMore: false });
        })
        .finally(() => {
          setLoading(false);
          setSwitchingConversation(false);
        });
    } else {
      // Nếu không có chatId, clear chat
      setChat(null);
      setNextOffset(0);
      setHasMore(false);
      setLoadingMore(false);
      setSwitchingConversation(false);
    }
  }, [chatId, currentUser]);

  useEffect(() => {
    if (chatId) {
      fetchConversation(chatId)
        .then(setCurrentConversation)
        .catch(() => setCurrentConversation(null));
    }
  }, [chatId]);

  // Auto search with debounce
  useEffect(() => {
    if (!searchUser.trim()) {
      setSearchResult(null);
      setSearching(false);
      return;
    }

    setSearching(true);
    const timeoutId = setTimeout(() => {
      performSearch(searchUser);
    }, 500); // Debounce 500ms

    return () => clearTimeout(timeoutId);
  }, [searchUser]);

  const performSearch = async (searchTerm) => {
    console.log("[SEARCH] Auto searching for:", searchTerm);
    
    try {
      console.log("[SEARCH] Calling searchUserAPI with term:", searchTerm);
      const result = await searchUserAPI(searchTerm);
      console.log("[SEARCH] Search result:", result);
      
      // API trả về array, lấy phần tử đầu tiên nếu có
      if (result && Array.isArray(result) && result.length > 0) {
        console.log("[SEARCH] Found user:", result[0]);
        setSearchResult(result[0]);
      } else {
        console.log("[SEARCH] No user found in result");
        setSearchResult(null);
      }
    } catch (err) {
      console.error("[SEARCH] Search error:", err);
      setSearchResult(null);
    } finally {
      setSearching(false);
    }
  };

  const handleEmoji = (e) => {
    setText((prev) => prev + e.emoji);
    setOpen(false);
  };

  // Load more messages function
  const loadMoreMessages = async () => {
    if (!hasMore || loadingMore || !chatId || switchingConversation) {
      console.log('[CHAT] Skip load more:', { hasMore, loadingMore, chatId, switchingConversation });
      return;
    }
    
    console.log('[CHAT] Loading more messages for conversation:', chatId, 'at offset:', nextOffset);
    setLoadingMore(true);
    
    try {
      const data = await fetchMessages(chatId, currentUser, nextOffset, 10);
      
      console.log('[CHAT] Load more result:', {
        conversationId: chatId,
        currentOffset: nextOffset,
        newMessagesCount: data.messages.length,
        newNextOffset: data.nextOffset,
        newHasMore: data.hasMore
      });
      
      // Nếu không có tin nhắn mới, set hasMore = false
      if (data.messages.length === 0) {
        console.log('[CHAT] No more messages to load for:', chatId);
        setHasMore(false);
        return;
      }
      
      // Prepend older messages to the beginning
      setChat(prevChat => {
        // Kiểm tra nếu conversation đã đổi trong lúc loading
        if (!prevChat || !prevChat.messages) {
          console.log('[CHAT] Chat state cleared while loading, returning new data');
          return data;
        }
        
        // Kiểm tra duplicate messages bằng ID
        const existingIds = new Set(prevChat.messages.map(m => m.id));
        const newMessages = data.messages.filter(m => !existingIds.has(m.id));
        
        console.log('[CHAT] Filtering messages:', {
          existingCount: prevChat.messages.length,
          fetchedCount: data.messages.length,
          newCount: newMessages.length
        });
        
        if (newMessages.length === 0) {
          // Nếu tất cả messages đều duplicate, stop loading
          console.log('[CHAT] All messages are duplicates, stopping pagination');
          setHasMore(false);
          return prevChat;
        }
        
        const updatedChat = {
          messages: [...newMessages, ...prevChat.messages], // Older messages go to front
          nextOffset: data.nextOffset,
          hasMore: data.hasMore
        };
        
        console.log('[CHAT] Updated chat:', {
          totalMessages: updatedChat.messages.length,
          nextOffset: updatedChat.nextOffset,
          hasMore: updatedChat.hasMore
        });
        
        return updatedChat;
      });
      
      setNextOffset(data.nextOffset);
      setHasMore(data.hasMore);
      
    } catch (error) {
      console.error('[CHAT] Failed to load more messages:', error);
      setHasMore(false); // Stop trying to load on error
    } finally {
      setLoadingMore(false);
    }
  };

  // Infinite scroll handler
  const handleScroll = (e) => {
    // Không làm gì nếu đang loading initial data hoặc chưa có chat hoặc đang switching
    if (loading || !chat || !chat.messages || chat.messages.length === 0 || switchingConversation) {
      return;
    }
    
    const { scrollTop } = e.target;
    
    // Khi scroll gần đến đầu (còn 50px nữa là tới đầu) và vẫn còn tin nhắn để load
    if (scrollTop <= 50 && hasMore && !loadingMore && chatId) {
      console.log('[CHAT] Triggering load more at scroll position:', scrollTop);
      
      // Lưu scroll position hiện tại trước khi load
      const scrollHeight = e.target.scrollHeight;
      
      loadMoreMessages().then(() => {
        // Sau khi load xong, điều chỉnh scroll position để không bị jump
        requestAnimationFrame(() => {
          if (e.target) { // Check if element still exists
            const newScrollHeight = e.target.scrollHeight;
            const scrollDiff = newScrollHeight - scrollHeight;
            e.target.scrollTop = scrollTop + scrollDiff;
            console.log('[CHAT] Adjusted scroll position after load more');
          }
        });
      });
    }
  };

  const handleImg = (e) => {
    if (e.target.files[0]) {
      setImg({
        file: e.target.files[0],
        url: URL.createObjectURL(e.target.files[0]),
      });
    }
  };

  const handleSend = async () => {
    // Allow sending if there's text or image
    if (!text.trim() && (!img || !img.file)) return;
    
    // Sử dụng global STOMP connection
    const globalStomp = window.globalStompClient;
    if (!globalStomp || !globalStomp.client || !globalStomp.client.connected) {
      console.error('[STOMP] Global connection chưa sẵn sàng, không thể gửi tin nhắn');
      return;
    }
    
    try {
      // Tạo đối tượng giả để tương thích với sendMessage function
      const stompServiceRef = { current: globalStomp };
      await sendMessage({chatId, text, replyToMsg, img, stompServiceRef, currentUser });
      triggerRefresh(); // Thông báo chat list reload khi gửi tin nhắn
    } catch (err) {
      console.error('[STOMP] ❌ Lỗi gửi tin nhắn:', err);
      alert('Lỗi khi gửi tin nhắn: ' + err.message);
    } finally {
      setImg({ file: null, url: "" });
      setText("");
      setReplyToMsg(null);
      setTimeout(() => {
        const centerDiv = centerRef.current;
        if (centerDiv) {
          centerDiv.scrollTop = centerDiv.scrollHeight;
        }
        setShowScrollBtn(false);
      }, 50);
    }
  };

  const handleAddUser = async (userId) => {
    setAdding(true);
    try {
      const participantRequest = {
        conversationId: chatId,
        userId: userId
      };
      
      await addUserToConversation(participantRequest);
      
      setShowAddUser(false);
      setSearchUser("");
      setSearchResult(null);
      if (chatId) {
        const conv = await fetchConversation(chatId);
        setCurrentConversation(conv);
      }
    } finally {
      setAdding(false);
    }
  };

  const handleRemoveUser = async (userId) => {
    setRemoving(userId);
    try {
      const participantRequest = {
        conversationId: chatId,
        userId: userId
      };
      
      await removeUserFromConversation(participantRequest);
      
      if (chatId) {
        const conv = await fetchConversation(chatId);
        setCurrentConversation(conv);
      }
    } finally {
      setRemoving(null);
    }
  };

  const handleDeleteMsg = async (msgId) => {
    setDeletingMsgId(msgId);
    try {
      await deleteMessage(msgId);
    } finally {
      setDeletingMsgId(null);
      setMenuMsgId(null);
    }
  };

  const handleStartEdit = (msg) => {
    setEditingMsgId(msg.id);
    setEditText(msg.message);
    setMenuMsgId(null);
  };

  const handleSaveEdit = async (msgId) => {
    try {
      await editMessage(msgId, editText);
    } finally {
      setEditingMsgId(null);
      setEditText("");
    }
  };

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {}
  };

  // Check if should show seen avatars for this message (chỉ show cho một số tin nhắn cuối)
  const shouldShowSeenAvatars = (messageIndex, totalMessages) => {
    // Chỉ hiển thị seen avatars cho 10 tin nhắn cuối cùng
    return messageIndex >= totalMessages - 10;
  };

  return (
    <div className="chat">
      <div className="top">
        <div className="conv-info">
          <div style={{ position: 'relative', display: 'inline-block' }}>
            <img src={(conversationFromStore?.convAvatar || conversationFromStore?.avatarUrl || currentConversation?.convAvatar) || "./avatar.png"} alt="avatar" className="conv-avatar" />
            {(conversationFromStore?.type || currentConversation?.type) === "DIRECT" && user && isUserOnline(user.userId) && (
              <div style={{
                position: 'absolute',
                bottom: 2,
                right: 2,
                width: '12px',
                height: '12px',
                backgroundColor: '#4caf50',
                borderRadius: '50%',
                border: '2px solid #1a1a1a'
              }}></div>
            )}
          </div>
          <div className="conv-details">
            <span className="conv-name">{(conversationFromStore?.name || currentConversation?.name) || "Conversation"}</span>
            {(conversationFromStore?.type || currentConversation?.type) === "DIRECT" && user && (
              <span className="conv-status" style={{ 
                fontSize: '12px', 
                color: isUserOnline(user.userId) ? '#4caf50' : '#999',
                display: 'block'
              }}>
                {isUserOnline(user.userId) ? 'Online' : 'Offline'}
              </span>
            )}
          </div>
          {(conversationFromStore?.type || currentConversation?.type) !== "DIRECT" && (
            <>
              <button className="add-user-btn" onClick={() => setShowAddUser(true)}>+</button>
              <button className="members-btn" onClick={() => setShowMembers(true)}>
                <img src="./more.png" alt="Thành viên" style={{ width: 20, height: 20, verticalAlign: 'middle' }} /> 
              </button>
            </>
          )}
        </div>
        <div className="icons">
          <img src="./phone.png" alt="" />
          <img src="./video.png" alt="" />
          <img src="./info.png" alt="" />
        </div>
      </div>
      {/* Modal thêm thành viên */}
      {showAddUser && (
        <div className="modal-overlay">
          <div className="modal-add-user">
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <input 
                type="text" 
                placeholder="Nhập username để tìm kiếm..." 
                value={searchUser} 
                onChange={e => setSearchUser(e.target.value)}
                style={{ flex: 1 }}
              />
              {searching && (
                <span style={{ color: '#999', fontSize: '12px' }}>Đang tìm...</span>
              )}
              <button type="button" onClick={() => { 
                setShowAddUser(false); 
                setSearchUser(""); 
                setSearchResult(null); 
                setSearching(false);
              }}>Đóng</button>
            </div>
            {console.log("[SEARCH] Current searchResult state:", searchResult)}
            {console.log("[SEARCH] searchUser value:", searchUser)}
            {console.log("[SEARCH] searchResult type:", typeof searchResult)}
            {searchResult && (
              <div style={{ marginTop: 12, display: 'flex', alignItems: 'center', padding: '8px', background: 'rgba(17, 25, 40, 0.3)', borderRadius: '8px' }}>
                <img src={searchResult.avatarUrl || "./avatar.png"} alt="avatar" style={{ width: 40, height: 40, borderRadius: "50%" }} />
                <span style={{ marginLeft: 12, flex: 1 }}>{searchResult.displayName || searchResult.username}</span>
                <button 
                  disabled={adding} 
                  onClick={() => handleAddUser(searchResult.id)} 
                  style={{ 
                    marginLeft: 12, 
                    padding: '6px 12px', 
                    background: '#5183fe', 
                    color: 'white', 
                    border: 'none', 
                    borderRadius: '4px',
                    cursor: adding ? 'not-allowed' : 'pointer'
                  }}
                >
                  {adding ? "Đang thêm..." : "Thêm vào nhóm"}
                </button>
              </div>
            )}
            {!searchResult && searchUser && !searching && (
              <div style={{ marginTop: 12, color: '#999', textAlign: 'center', padding: '12px' }}>
                Không tìm thấy người dùng với tên "{searchUser}"
              </div>
            )}
          </div>
        </div>
      )}
      {/* Modal danh sách thành viên */}
      {showMembers && (
        <div className="modal-overlay" onClick={() => setShowMembers(false)}>
          <div className="modal-members" onClick={e => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h4>Thành viên nhóm</h4>
              <button onClick={() => setShowMembers(false)} style={{ background: 'none', border: 'none', fontSize: 20, color: '#fff', cursor: 'pointer' }}>&times;</button>
            </div>
            {conversationFromStore?.participants?.map((p) => (
              <div key={p.userId} className="member-item">
                <div className="member-info">
                  <div style={{ position: 'relative' }}>
                    <img src={p.avatarUrl || "./avatar.png"} alt="avatar" className="msg-avatar" />
                    {isUserOnline(p.userId) && (
                      <div style={{
                        position: 'absolute',
                        bottom: 0,
                        right: 0,
                        width: '10px',
                        height: '10px',
                        backgroundColor: '#4caf50',
                        borderRadius: '50%',
                        border: '2px solid #23283a'
                      }}></div>
                    )}
                  </div>
                  <span className="msg-displayName">{p.displayName}</span>
                </div>
                <button 
                  disabled={removing === p.userId} 
                  onClick={() => handleRemoveUser(p.userId)} 
                  className="icon-remove-btn"
                  title="Xóa thành viên"
                >
                  &times;
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
      <div 
        className="center" 
        ref={centerRef}
        onScroll={handleScroll}
        style={{ position: 'relative', overflowY: 'auto' }}
      >
        {/* Loading indicator khi đang load more */}
        {loadingMore && (
          <div className="loading-more-indicator">
            <div className="loading-spinner"></div>
            <span>Loading older messages...</span>
          </div>
        )}
        
        {loading ? (
          <div className="loading-messages">Loading messages...</div>
        ) : chat && chat.messages ? (
          chat.messages.map((message, index) => (
            <div key={message.id} className="message-with-seen">
              <MessageItem
                message={message}
                hoveredMsgId={hoveredMsgId}
                setHoveredMsgId={setHoveredMsgId}
                setReplyToMsg={setReplyToMsg}
                handleStartEdit={handleStartEdit}
                handleDeleteMsg={handleDeleteMsg}
                editingMsgId={editingMsgId}
                editText={editText}
                setEditText={setEditText}
                handleSaveEdit={handleSaveEdit}
                setEditingMsgId={setEditingMsgId}
                deletingMsgId={deletingMsgId}
              />
              {shouldShowSeenAvatars(index, chat.messages.length) && (
                <SeenByAvatars 
                  messageId={message.id} 
                  isOwnMessage={message.mine}
                  conversation={conversationFromStore || currentConversation}
                  currentUserId={currentUser?.id}
                />
              )}
            </div>
          ))
        ) : null}
        <div ref={endRef}></div>
        {/* Nút scroll xuống cuối */}
        {showScrollBtn && (
          <button
            onClick={() => {
              const centerDiv = centerRef.current;
              if (centerDiv) {
                centerDiv.scrollTop = centerDiv.scrollHeight;
              }
              setShowScrollBtn(false);
            }}
            style={{
              position: 'fixed',
              bottom: 100,
              right: 40,
              zIndex: 100,
              width: 44,
              height: 44,
              borderRadius: '50%',
              background: '#5183fe',
              border: 'none',
              boxShadow: '0 2px 8px 0 rgba(0,0,0,0.18)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              color: '#fff',
              fontSize: 28
            }}
            title="Cuộn xuống cuối"
          >
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
          </button>
        )}
      </div>
      {/* NEW: Reply preview above input */}
      {replyToMsg && (
        <div className="reply-preview-bar" style={{ background: "rgba(81,131,254,0.12)", borderLeft: "3px solid #5183fe", padding: "8px 12px", borderRadius: 6, margin: "8px 16px 0 16px", display: "flex", alignItems: "center", gap: 8 }}>
          <span style={{ fontWeight: 500 }}>{replyToMsg.sender?.displayName || ""}</span>: <span style={{ color: "#b3c0d1" }}>{truncateReplyPreview(replyToMsg.message)}</span>
          <button onClick={() => setReplyToMsg(null)} style={{ marginLeft: "auto", background: "none", border: "none", color: "#5183fe", cursor: "pointer", fontSize: 16 }}>&times;</button>
        </div>
      )}
      
      {/* Image preview above input */}
      {img.url && (
        <div style={{ 
          position: 'relative',
          margin: '8px 16px 0 16px',
          display: 'inline-block',
          border: '2px dashed #5183fe',
          borderRadius: '8px',
          padding: '8px',
          background: 'rgba(81,131,254,0.1)'
        }}>
          <img 
            src={img.url} 
            alt="Preview" 
            style={{ 
              maxWidth: '200px', 
              maxHeight: '200px', 
              borderRadius: '4px',
              display: 'block'
            }} 
          />
          <button
            onClick={() => setImg({ file: null, url: "" })}
            style={{
              position: 'absolute',
              top: '-8px',
              right: '-8px',
              background: '#f44336',
              color: 'white',
              border: 'none',
              borderRadius: '50%',
              width: '24px',
              height: '24px',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
            title="Remove image"
          >
            ×
          </button>
          <div style={{ 
            textAlign: 'center', 
            marginTop: '4px', 
            fontSize: '12px', 
            color: '#5183fe' 
          }}>
            Ảnh sẽ được gửi
          </div>
        </div>
      )}

      <div className="bottom">
        <div className="icons">
          <label htmlFor="file">
            <img src="./img.png" alt="" />
          </label>
          <input
            type="file"
            id="file"
            style={{ display: "none" }}
            onChange={handleImg}
          />
          <img src="./camera.png" alt="" />
          <img src="./mic.png" alt="" />
        </div>
        <input
          type="text"
          placeholder={
            isCurrentUserBlocked || isReceiverBlocked
              ? "You cannot send a message"
              : "Type a message..."
          }
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={e => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              handleSend();
            }
          }}
          disabled={isCurrentUserBlocked || isReceiverBlocked}
        />
        <div className="emoji">
          <img
            src="./emoji.png"
            alt=""
            onClick={() => setOpen((prev) => !prev)}
          />
          <div className="picker">
            <EmojiPicker open={open} onEmojiClick={handleEmoji} />
          </div>
        </div>
        <button
          className="sendButton"
          onClick={handleSend}
          disabled={!isGlobalStompConnected || (text === "" && (!img || !img.file))}
        >
          Send
        </button>
      </div>
      <div style={{ position: 'absolute', top: 8, right: 16, fontSize: 12, color: isGlobalStompConnected ? '#4caf50' : '#f44336' }}>
        {isGlobalStompConnected ? 'Đã kết nối realtime' : 'Mất kết nối realtime'}
      </div>
    </div>
  );
};

export default Chat;
