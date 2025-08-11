import { useState, useEffect } from "react";
import { useChatStore } from "../../lib/chatStore";
import { useUserStore } from "../../lib/userStore";
import { fetchSharedPhotos, removeUserFromConversation } from "../chat/chatApi";
import GroupEdit from "../groupEdit/GroupEdit";
import "./detail.css";

const Detail = () => {
  const { chatId, user, conversation, isCurrentUserBlocked, isReceiverBlocked, changeBlock, resetChat, updateConversation, refreshFlag } =
    useChatStore();
  const { currentUser } = useUserStore();
  const [isGroupEditOpen, setIsGroupEditOpen] = useState(false);
  const [sharedPhotos, setSharedPhotos] = useState([]);
  const [showPhotos, setShowPhotos] = useState(false);
  const [loadingPhotos, setLoadingPhotos] = useState(false);
  const [isLeaving, setIsLeaving] = useState(false);

  // Load shared photos when conversation changes or when messages refresh
  useEffect(() => {
    if (chatId) {
      loadSharedPhotos();
    }
  }, [chatId, refreshFlag]); // Add refreshFlag to dependencies

  const loadSharedPhotos = async () => {
    if (!chatId) return;
    
    setLoadingPhotos(true);
    try {
      const photos = await fetchSharedPhotos(chatId);
      setSharedPhotos(photos);
    } catch (error) {
      console.error("Failed to load shared photos:", error);
      setSharedPhotos([]);
    } finally {
      setLoadingPhotos(false);
    }
  };

  const togglePhotos = () => {
    setShowPhotos(!showPhotos);
  };

  const downloadImage = async (imageUrl, filename) => {
    try {
      // Fetch image data
      const response = await fetch(imageUrl);
      if (!response.ok) throw new Error('Failed to fetch image');
      
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      
      // Create temporary link and trigger download
      const link = document.createElement('a');
      link.href = url;
      link.download = filename || 'shared-image.jpg';
      document.body.appendChild(link);
      link.click();
      
      // Cleanup
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Download failed:', error);
      // Fallback: open image in new tab
      window.open(imageUrl, '_blank');
    }
  };

  const handleBlock = async () => {
    if (!user) return;

    try {
      // Simulate blocking/unblocking user
      console.log(`Blocking/unblocking user: ${user.username}`);
      changeBlock();
    } catch (err) {
      console.log(err);
    }
  };

  const handleLogout = () => {
    // Simulate logout
    console.log("Logging out...");
    resetChat();
    // In a real app, this would redirect to login
    window.location.reload();
  };

  const handleGroupEdit = () => {
    setIsGroupEditOpen(true);
  };

  const handleGroupUpdate = (updatedConversation) => {
    // Cập nhật conversation trong chat store (cho Detail và Chat area)
    updateConversation(updatedConversation);
    
    // Trigger refresh chat list để cập nhật tên và avatar trong danh sách
    const { triggerRefresh } = useChatStore.getState();
    triggerRefresh();
    
    console.log("Group updated successfully:", updatedConversation);
  };

  const handleLeaveGroup = async () => {
    if (!isGroupChat || !currentUser?.id || !chatId) {
      console.error("Cannot leave group: missing required data");
      return;
    }

    // Confirm before leaving
    if (!window.confirm("Bạn có chắc chắn muốn rời khỏi nhóm này?")) {
      return;
    }

    setIsLeaving(true);
    try {
      console.log("Leaving group:", chatId, "User:", currentUser.id);
      
      const participantRequest = {
        conversationId: chatId,
        userId: currentUser.id
      };
      
      await removeUserFromConversation(participantRequest);
      
      // Reset chat sau khi rời nhóm thành công
      resetChat();
      
      // Trigger refresh để cập nhật chat list
      const { triggerRefresh } = useChatStore.getState();
      triggerRefresh();
      
      console.log("Successfully left the group");
    } catch (error) {
      console.error("Failed to leave group:", error);
      alert("Không thể rời khỏi nhóm. Vui lòng thử lại.");
    } finally {
      setIsLeaving(false);
    }
  };

  const isGroupChat = conversation && conversation.type === "GROUP";
  const displayInfo = isGroupChat 
    ? {
        name: conversation.name,
        avatar: conversation.convAvatar || conversation.avatarUrl || "./avatar.png"
      }
    : {
        name: user?.displayName || user?.username,
        avatar: user?.avatar || user?.avatarUrl || "./avatar.png"
      };

  // Check if current user is admin of the group
  const isAdmin = isGroupChat && conversation.participants
    ?.find(p => p.userId === currentUser.id)?.isAdmin;

  // Debug logs
  console.log("Debug Detail component:", {
    conversation,
    isGroupChat,
    isAdmin,
    currentUserId: currentUser?.id,
    participants: conversation?.participants
  });

  return (
    <div className="detail">
      <div className="user">
        <img src={displayInfo.avatar} alt="" />
        <h2>{displayInfo.name}</h2>
        {isGroupChat ? (
          <div className="group-info">
            <p>{conversation.participants?.length || 0} thành viên</p>
            <div className="group-buttons">
              <button className="edit-group-btn" onClick={handleGroupEdit}>
                <img src="/edit.png" alt="Edit" />
                Chỉnh sửa nhóm
              </button>
              <button 
                className="leave-group-btn" 
                onClick={handleLeaveGroup}
                disabled={isLeaving}
              >
                <img src="/minus.png" alt="Leave" />
                {isLeaving ? "Đang rời..." : "Rời khỏi nhóm"}
              </button>
            </div>
          </div>
        ) : (
          <p></p>
        )}
      </div>
      <div className="info">
      
        <div className="option">
          {/* <div className="title">
            <span>Chat Settings</span>
            <img src="./arrowUp.png" alt="" />
          </div> */}
        </div>
        <div className="option">
          <div className="title" onClick={togglePhotos} style={{ cursor: 'pointer' }}>
            <span>Shared photos ({sharedPhotos.length})</span>
            <img 
              src={showPhotos ? "./arrowUp.png" : "./arrowDown.png"} 
              alt="" 
              style={{ transform: showPhotos ? 'rotate(0deg)' : 'rotate(0deg)' }}
            />
          </div>
          {showPhotos && (
            <div className="photos">
              {loadingPhotos ? (
                <div style={{ textAlign: 'center', padding: '20px', color: '#999' }}>
                  Loading photos...
                </div>
              ) : sharedPhotos.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '20px', color: '#999' }}>
                  No photos shared yet
                </div>
              ) : (
                sharedPhotos.map((photo) => (
                  <div key={photo.id} className="photoItem">
                    <div className="photoDetail">
                      <img
                        src={photo.mediaUrl}
                        alt={photo.message || "Shared photo"}
                        style={{ cursor: 'pointer' }}
                        onClick={() => window.open(photo.mediaUrl, '_blank')}
                      />
                      <span title={`Sent by ${photo.sender?.displayName} on ${new Date(photo.createdAt).toLocaleDateString()}`}>
                        {photo.message || `Photo from ${photo.sender?.displayName}`}
                      </span>
                    </div>
                    <button 
                      onClick={() => downloadImage(
                        photo.mediaUrl, 
                        `photo-${photo.sender?.displayName}-${new Date(photo.createdAt).toISOString().split('T')[0]}.jpg`
                      )}
                      style={{ 
                        background: 'none', 
                        border: 'none', 
                        cursor: 'pointer', 
                        padding: 0,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                      title="Download image"
                    >
                      <img src="./download.png" alt="Download" className="icon" />
                    </button>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
        {/* <div className="option">
          <div className="title">
            <span>Shared Files</span>
            <img src="./arrowUp.png" alt="" />
          </div>
        </div> */}
        {/* <button onClick={handleBlock}>
          {isCurrentUserBlocked
            ? "You are Blocked!"
            : isReceiverBlocked
            ? "User blocked"
            : "Block User"}
        </button>
        <button className="logout" onClick={handleLogout}>
          Logout
        </button> */}
      </div>

      {/* Group Edit Modal */}
      {isGroupEditOpen && (
        <GroupEdit
          isOpen={isGroupEditOpen}
          onClose={() => setIsGroupEditOpen(false)}
          conversation={conversation}
          onUpdate={handleGroupUpdate}
        />
      )}
    </div>
  );
};

export default Detail;
