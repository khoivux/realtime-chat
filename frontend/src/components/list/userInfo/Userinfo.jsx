import "./userInfo.css"
import { useUserStore } from "../../../lib/userStore";
import { useState } from "react";
import ProfileEdit from "../../profileEdit/ProfileEdit";
import axios from "axios";

const Userinfo = () => {

  const { currentUser } = useUserStore();
  const { isUserOnline } = useUserStore();
  const [showProfileEdit, setShowProfileEdit] = useState(false);

  const handleLogout = async () => {
    try {
      const token = localStorage.getItem("accessToken");
      await axios.post("http://localhost:8081/api/v1/auth/logout", { token }, {
        headers: { Authorization: `Bearer ${token}` }
      });
    } catch (err) {}
    localStorage.removeItem("accessToken");
    localStorage.removeItem("username");
    window.location.reload();
  };

  return (
    <div className='userInfo'>
      <div className="user">
        <img src={currentUser.avatarUrl || "./avatar.png"} alt="" />
        <div className="info">
          <span>{currentUser?.displayName}</span>
          <span className="online-status" style={{
            fontSize: '12px',
            color: isUserOnline(currentUser?.id) ? '#4caf50' : '#999',
            marginLeft: '8px'
          }}>
          </span>
        </div>
      </div>
      <div className="icons">
        <img src="./more.png" alt="" />
        <img 
          src="./edit.png" 
          alt="" 
          onClick={() => setShowProfileEdit(true)}
          style={{ cursor: 'pointer' }}
          title="Chỉnh sửa thông tin"
        />

        <button className="logout-btn" onClick={handleLogout} title="Đăng xuất">
          {/* Icon SVG logout */}
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>

      <ProfileEdit 
        isOpen={showProfileEdit}
        onClose={() => setShowProfileEdit(false)}
      />
    </div>
  )
}

export default Userinfo