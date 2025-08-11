import { useState, useEffect } from "react";
import { useChatStore } from "../../lib/chatStore";
import { useUserStore } from "../../lib/userStore";
import upload from "../../lib/upload";
import axios from "axios";
import "./groupEdit.css";

const GroupEdit = ({ isOpen, onClose, conversation, onUpdate }) => {
  const [avatar, setAvatar] = useState({ file: null, url: conversation?.avatarUrl || "" });
  const [name, setName] = useState(conversation?.name || "");
  const [loading, setLoading] = useState(false);
  const { currentUser } = useUserStore();

  useEffect(() => {
    if (conversation) {
      setName(conversation.name || "");
      setAvatar({ file: null, url: conversation.avatarUrl || "" });
    }
  }, [conversation]);

  const handleAvatar = (e) => {
    if (e.target.files[0]) {
      setAvatar({
        file: e.target.files[0],
        url: URL.createObjectURL(e.target.files[0])
      });
    }
  };

  const validateForm = () => {
    if (!name.trim()) {
      alert("Tên nhóm không được để trống");
      return false;
    }
    if (name.trim().length < 2) {
      alert("Tên nhóm phải có ít nhất 2 ký tự");
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setLoading(true);
    
    console.log("=== DEBUGGING GROUP UPDATE ===");
    console.log("Original conversation:", conversation);
    console.log("conversation.id type:", typeof conversation.id);
    console.log("conversation.id value:", conversation.id);
    console.log("name to update:", name.trim());
    console.log("name type:", typeof name.trim());
    
    try {
      let avatarUrl = conversation.avatarUrl;
      console.log("Initial avatarUrl:", avatarUrl);
      console.log("Initial avatarUrl type:", typeof avatarUrl);
      
      // Upload new avatar if selected
      if (avatar.file) {
        console.log("Uploading avatar file...");
        try {
          const uploadResult = await upload(avatar.file);
          console.log("Avatar upload result:", uploadResult);
          console.log("Avatar upload result type:", typeof uploadResult);
          console.log("Avatar upload result JSON:", JSON.stringify(uploadResult, null, 2));
          avatarUrl = uploadResult;
          console.log("Avatar uploaded successfully:", avatarUrl);
        } catch (uploadError) {
          console.error("Error uploading avatar:", uploadError);
          alert("Lỗi khi tải lên ảnh đại diện");
          setLoading(false);
          return;
        }
      }
      
      console.log("Updating conversation with data:", {
        conversationId: conversation.id,
        name: name.trim(),
        avatarUrl
      });
      
      const requestData = {
        conversationId: conversation.id,
        name: name.trim(),
        avatarUrl
      };
      
      console.log("Request data being sent:", JSON.stringify(requestData, null, 2));
      console.log("Field types:", {
        conversationId: typeof requestData.conversationId,
        name: typeof requestData.name,
        avatarUrl: typeof requestData.avatarUrl
      });
      console.log("avatarUrl value:", requestData.avatarUrl);
      
      // Update conversation
      const response = await axios.put(
        "http://localhost:8081/api/v1/conversation/",
        requestData,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
            "Content-Type": "application/json"
          }
        }
      );
      
      console.log("API Response:", response.data);
      
      // Kiểm tra response success - có thể backend trả về code khác hoặc không có code
      if (response.data.code === 1000 || response.status === 200 || response.data.message?.includes("thành công")) {
        // Update conversation data in parent component
        const updatedConversation = {
          ...conversation,
          name: name.trim(),
          avatarUrl,
          convAvatar: avatarUrl // Cập nhật cả convAvatar cho Chat header
        };
        
        console.log("Updating conversation:", updatedConversation);
        onUpdate(updatedConversation);
        onClose(); // Tự động đóng popup khi thành công
      } else {
        alert("Cập nhật thông tin nhóm thất bại: " + (response.data.message || "Lỗi không xác định"));
      }
    } catch (error) {
      console.error("Error updating group:", error);
      console.error("Error response:", error.response?.data);
      console.error("Error status:", error.response?.status);
      console.error("Error headers:", error.response?.headers);
      
      if (error.response?.data?.message) {
        alert("Lỗi: " + error.response.data.message);
      } else {
        alert("Cập nhật thông tin nhóm thất bại");
      }
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="group-edit-overlay">
      <div className="group-edit-modal">
        <div className="group-edit-header">
          <h3>Chỉnh sửa thông tin nhóm</h3>
          <button className="close-btn" onClick={onClose} disabled={loading}>×</button>
        </div>
        
        <form onSubmit={handleSubmit} className="group-edit-form">
          <div className="avatar-section">
            <div className="avatar-preview">
              <img 
                src={avatar.url || "/avatar.png"} 
                alt="Group Avatar" 
                className="avatar-img"
              />
              <label htmlFor="group-avatar-upload" className="avatar-upload-btn">
                <img src="/camera.png" alt="Camera" />
              </label>
              <input
                type="file"
                id="group-avatar-upload"
                accept="image/*"
                onChange={handleAvatar}
                disabled={loading}
                hidden
              />
            </div>
          </div>
          
          <div className="form-group">
            <label htmlFor="group-name">Tên nhóm</label>
            <input
              type="text"
              id="group-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Nhập tên nhóm..."
              disabled={loading}
              maxLength={50}
            />
          </div>
          
          <div className="form-buttons">
            <button 
              type="button" 
              className="cancel-btn"
              onClick={onClose}
              disabled={loading}
            >
              Hủy
            </button>
            <button 
              type="submit" 
              className="save-btn"
              disabled={loading}
            >
              {loading ? "Đang lưu..." : "Lưu thay đổi"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default GroupEdit;
