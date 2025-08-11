import { useState } from "react";
import "./profileEdit.css";
import axios from "axios";
import { useUserStore } from "../../lib/userStore";

const ProfileEdit = ({ isOpen, onClose }) => {
  const { currentUser, updateCurrentUser } = useUserStore();
  const [formData, setFormData] = useState({
    displayName: currentUser?.displayName || "",
    username: currentUser?.username || "",
  });
  const [avatarFile, setAvatarFile] = useState(null);
  const [avatarPreview, setAvatarPreview] = useState(currentUser?.avatarUrl || "");
  const [isLoading, setIsLoading] = useState(false);

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setAvatarFile(file);
      const reader = new FileReader();
      reader.onload = (e) => {
        setAvatarPreview(e.target.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const uploadAvatar = async () => {
    if (!avatarFile) return currentUser?.avatarUrl;

    const formData = new FormData();
    formData.append("file", avatarFile);

    try {
      const token = localStorage.getItem("accessToken");
      const response = await axios.post(
        "http://localhost:8081/api/v1/upload/",
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
          },
        }
      );
      return response.data.data; // URL của ảnh đã upload
    } catch (error) {
      console.error("Lỗi upload avatar:", error);
      throw error;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      let avatarUrl = currentUser?.avatarUrl;
      
      // Upload avatar mới nếu có
      if (avatarFile) {
        avatarUrl = await uploadAvatar();
      }

      // Cập nhật thông tin profile
      const token = localStorage.getItem("accessToken");
      const updateData = {
        username: formData.username,
        displayName: formData.displayName,
        avatarUrl: avatarUrl
      };

      const response = await axios.patch(
        "http://localhost:8081/api/v1/users/profile",
        updateData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      // Cập nhật store với dữ liệu mới
      updateCurrentUser(response.data.data);
      
      alert("Cập nhật thông tin thành công!");
      onClose();
    } catch (error) {
      console.error("Lỗi cập nhật profile:", error);
      alert("Có lỗi xảy ra khi cập nhật thông tin");
    } finally {
      setIsLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="profile-edit-overlay">
      <div className="profile-edit-modal">
        <div className="profile-edit-header">
          <h2>Chỉnh sửa thông tin cá nhân</h2>
          <button className="close-btn" onClick={onClose}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit} className="profile-edit-form">
          <div className="avatar-section">
            <div className="avatar-container">
              <img 
                src={avatarPreview || "./avatar.png"} 
                alt="Avatar" 
                className="avatar-preview"
              />
              <label htmlFor="avatar-input" className="avatar-upload-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                  <circle cx="8.5" cy="8.5" r="1.5"/>
                  <polyline points="21,15 16,10 5,21"/>
                </svg>
              </label>
              <input
                id="avatar-input"
                type="file"
                accept="image/*"
                onChange={handleAvatarChange}
                style={{ display: "none" }}
              />
            </div>
            <span className="avatar-hint">Nhấn vào ảnh để thay đổi</span>
          </div>

          <div className="form-group">
            <label htmlFor="displayName">Tên hiển thị</label>
            <input
              type="text"
              id="displayName"
              name="displayName"
              value={formData.displayName}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="username">Tên đăng nhập</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-actions">
            <button 
              type="button" 
              className="cancel-btn" 
              onClick={onClose}
              disabled={isLoading}
            >
              Hủy
            </button>
            <button 
              type="submit" 
              className="save-btn" 
              disabled={isLoading}
            >
              {isLoading ? "Đang lưu..." : "Lưu thay đổi"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProfileEdit;
