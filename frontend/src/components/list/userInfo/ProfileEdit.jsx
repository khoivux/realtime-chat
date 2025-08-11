import { useState } from "react";
import axios from "axios";
import { useUserStore } from "../../../lib/userStore";
import "./profileEdit.css";

const ProfileEdit = ({ onClose, onSave }) => {
  const { currentUser, updateCurrentUser } = useUserStore();
  const [formData, setFormData] = useState({
    username: currentUser?.username || "",
    displayName: currentUser?.displayName || "",
    avatarUrl: currentUser?.avatarUrl || ""
  });
  const [loading, setLoading] = useState(false);
  const [uploadingAvatar, setUploadingAvatar] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleAvatarUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      alert('Vui lòng chọn file ảnh');
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('File ảnh không được vượt quá 5MB');
      return;
    }

    setUploadingAvatar(true);
    try {
      const token = localStorage.getItem("accessToken");
      const uploadFormData = new FormData();
      uploadFormData.append("file", file);

      const response = await axios.post(
        "http://localhost:8081/api/v1/upload/",
        uploadFormData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'multipart/form-data'
          }
        }
      );

      if (response.data.data) {
        setFormData(prev => ({
          ...prev,
          avatarUrl: response.data.data
        }));
      }
    } catch (error) {
      console.error("Upload failed:", error);
      alert("Upload ảnh thất bại. Vui lòng thử lại.");
    } finally {
      setUploadingAvatar(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.username.trim() || !formData.displayName.trim()) {
      alert("Vui lòng điền đầy đủ thông tin");
      return;
    }

    setLoading(true);
    try {
      const token = localStorage.getItem("accessToken");
      
      await axios.put(
        "http://localhost:8081/api/v1/users/profile",
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );

      // Update user store
      updateCurrentUser({
        ...currentUser,
        ...formData
      });

      onSave && onSave(formData);
      onClose();
      alert("Cập nhật thông tin thành công!");
      
    } catch (error) {
      console.error("Update failed:", error);
      if (error.response?.data?.message) {
        alert(error.response.data.message);
      } else {
        alert("Cập nhật thất bại. Vui lòng thử lại.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="profile-edit-overlay">
      <div className="profile-edit-modal">
        <div className="profile-edit-header">
          <h2>Chỉnh sửa thông tin cá nhân</h2>
          <button 
            className="close-btn" 
            onClick={onClose}
            type="button"
          >
            ×
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="profile-edit-form">
          <div className="avatar-section">
            <div className="current-avatar">
              <img 
                src={formData.avatarUrl || "./avatar.png"} 
                alt="Current avatar" 
              />
              {uploadingAvatar && (
                <div className="upload-overlay">
                  <div className="upload-spinner"></div>
                </div>
              )}
            </div>
            <div className="avatar-upload">
              <input
                type="file"
                id="avatar-upload"
                accept="image/*"
                onChange={handleAvatarUpload}
                disabled={uploadingAvatar}
                style={{ display: 'none' }}
              />
              <label 
                htmlFor="avatar-upload" 
                className={`upload-btn ${uploadingAvatar ? 'disabled' : ''}`}
              >
                {uploadingAvatar ? 'Đang tải...' : 'Thay đổi ảnh đại diện'}
              </label>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="username">Tên đăng nhập:</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              disabled={loading}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="displayName">Tên hiển thị:</label>
            <input
              type="text"
              id="displayName"
              name="displayName"
              value={formData.displayName}
              onChange={handleInputChange}
              disabled={loading}
              required
            />
          </div>

          <div className="form-actions">
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
              disabled={loading || uploadingAvatar}
            >
              {loading ? 'Đang lưu...' : 'Lưu thay đổi'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProfileEdit;
