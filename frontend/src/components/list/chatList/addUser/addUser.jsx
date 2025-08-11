import "./addUser.css";
import { useState, useEffect } from "react";
import { useUserStore } from "../../../../lib/userStore";
import axios from "axios";

const AddUser = ({ onClose, onCreated }) => {
  const [mode, setMode] = useState("direct"); // "direct" | "group"
  const [input, setInput] = useState("");
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [creatingId, setCreatingId] = useState(null);
  
  // For group creation
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [creatingGroup, setCreatingGroup] = useState(false);
  
  const { currentUser } = useUserStore();

  useEffect(() => {
    if (!input) {
      setResults([]);
      return;
    }
    const fetchUsers = async () => {
      setLoading(true);
      try {
        const token = localStorage.getItem("accessToken");
        const res = await axios.get(`http://localhost:8081/api/v1/users/?name=${encodeURIComponent(input)}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setResults(res.data.data.filter(u => u.id !== currentUser.id));
      } catch (err) {
        setResults([]);
      } finally {
        setLoading(false);
      }
    };
    const timeout = setTimeout(fetchUsers, 300); // debounce
    return () => clearTimeout(timeout);
  }, [input, currentUser.id]);

  const handleCreateDirect = async (userId) => {
    setCreatingId(userId);
    try {
      const token = localStorage.getItem("accessToken");
      await axios.post("http://localhost:8081/api/v1/conversation/", {
        participantIds: [currentUser.id, userId],
        type: "DIRECT"
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (onCreated) onCreated();
    } catch (err) {
      alert("Tạo hội thoại thất bại!");
    } finally {
      setCreatingId(null);
    }
  };

  const handleCreateGroup = async () => {
    if (selectedUsers.length === 0) {
      alert("Vui lòng chọn ít nhất 1 thành viên!");
      return;
    }
    
    setCreatingGroup(true);
    try {
      const token = localStorage.getItem("accessToken");
      await axios.post("http://localhost:8081/api/v1/conversation/", {
        participantIds: [currentUser.id, ...selectedUsers.map(u => u.id)],
        type: "GROUP"
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (onCreated) onCreated();
    } catch (err) {
      alert("Tạo nhóm thất bại!");
    } finally {
      setCreatingGroup(false);
    }
  };

  const toggleSelectUser = (user) => {
    setSelectedUsers(prev => {
      const isSelected = prev.find(u => u.id === user.id);
      if (isSelected) {
        return prev.filter(u => u.id !== user.id);
      } else {
        return [...prev, user];
      }
    });
  };

  return (
    <div className="addUser-backdrop" onClick={(e) => { 
      if (e.target === e.currentTarget) onClose(); 
    }}>
      <div className="addUser">
        {/* Header với nút đóng */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <h3 style={{ margin: 0, color: '#fff', fontSize: 18 }}>
            {mode === 'direct' ? 'Tạo cuộc trò chuyện' : 'Tạo nhóm chat'}
          </h3>
        <button 
          onClick={onClose}
          style={{ 
            background: 'none', 
            border: 'none', 
            color: '#fff', 
            cursor: 'pointer',
            fontSize: 24,
            padding: 0,
            width: 30,
            height: 30,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderRadius: '50%',
            transition: 'background-color 0.2s ease'
          }}
          onMouseEnter={(e) => e.target.style.backgroundColor = 'rgba(255,255,255,0.1)'}
          onMouseLeave={(e) => e.target.style.backgroundColor = 'transparent'}
          title="Đóng"
        >
          ×
        </button>
      </div>

      {/* Mode selection tabs */}
      <div className="mode-tabs" style={{ display: 'flex', marginBottom: 16, borderRadius: 8, overflow: 'hidden', backgroundColor: '#1a1a1a' }}>
        <button 
          onClick={() => setMode('direct')}
          style={{ 
            flex: 1, 
            padding: '12px 16px', 
            border: 'none', 
            backgroundColor: mode === 'direct' ? '#5183fe' : 'transparent',
            color: '#fff',
            cursor: 'pointer',
            fontWeight: mode === 'direct' ? 600 : 400
          }}
        >
          Chat 1-1
        </button>
        <button 
          onClick={() => setMode('group')}
          style={{ 
            flex: 1, 
            padding: '12px 16px', 
            border: 'none', 
            backgroundColor: mode === 'group' ? '#5183fe' : 'transparent',
            color: '#fff',
            cursor: 'pointer',
            fontWeight: mode === 'group' ? 600 : 400
          }}
        >
          Tạo nhóm
        </button>
      </div>

      {mode === 'group' && selectedUsers.length > 0 && (
        <div style={{ marginBottom: 16 }}>
          <div style={{ color: '#aaa', fontSize: 12, marginBottom: 8 }}>
            Đã chọn {selectedUsers.length} thành viên:
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
            {selectedUsers.map(user => (
              <div key={user.id} style={{ 
                display: 'flex', 
                alignItems: 'center', 
                gap: 6,
                padding: '4px 8px',
                backgroundColor: '#5183fe',
                borderRadius: 16,
                fontSize: 12
              }}>
                <img 
                  src={user.avatarUrl || "./avatar.png"} 
                  alt="avatar" 
                  style={{ width: 20, height: 20, borderRadius: '50%' }} 
                />
                <span>{user.displayName || user.username}</span>
                <button 
                  onClick={() => toggleSelectUser(user)}
                  style={{ 
                    background: 'none', 
                    border: 'none', 
                    color: '#fff', 
                    cursor: 'pointer',
                    padding: 0,
                    marginLeft: 2
                  }}
                >
                  ×
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      <input
        type="text"
        placeholder={mode === 'group' ? "Tìm kiếm thành viên để thêm vào nhóm..." : "Nhập tên hoặc username để tìm kiếm..."}
        value={input}
        onChange={e => setInput(e.target.value)}
        style={{ 
          width: '100%', 
          padding: 12, 
          borderRadius: 8, 
          border: "1px solid #444",
          backgroundColor: '#23283a',
          color: '#fff'
        }}
      />
      
      {loading && <div style={{ color: '#aaa', marginTop: 8 }}>Đang tìm kiếm...</div>}
      
      {results.length > 0 && (
        <div className="user-search-list" style={{ marginTop: 10, background: "#23283a", borderRadius: 8, padding: 8, maxHeight: 220, overflowY: "auto" }}>
          {results.map(user => {
            const isSelected = selectedUsers.find(u => u.id === user.id);
            return (
              <div key={user.id} className="user-search-item" style={{ 
                display: "flex", 
                alignItems: "center", 
                gap: 12, 
                marginBottom: 8,
                padding: 8,
                borderRadius: 8,
                backgroundColor: mode === 'group' && isSelected ? 'rgba(81, 131, 254, 0.2)' : 'transparent',
                border: mode === 'group' && isSelected ? '1px solid rgba(81, 131, 254, 0.4)' : '1px solid transparent'
              }}>
                <img src={user.avatarUrl || "./avatar.png"} alt="avatar" style={{ width: 36, height: 36, borderRadius: "50%", objectFit: "cover" }} />
                <div style={{ flex: 1 }}>
                  <div style={{ color: "#fff", fontWeight: 500 }}>{user.displayName || user.username}</div>
                  <div style={{ color: "#aaa", fontSize: 13 }}>@{user.username}</div>
                </div>
                {mode === 'direct' ? (
                  <button 
                    onClick={() => handleCreateDirect(user.id)} 
                    disabled={creatingId === user.id} 
                    style={{ 
                      padding: "6px 14px", 
                      borderRadius: 6, 
                      background: "#5183fe", 
                      color: "#fff", 
                      border: "none", 
                      cursor: "pointer" 
                    }}
                  >
                    {creatingId === user.id ? "Đang tạo..." : "Tạo hội thoại"}
                  </button>
                ) : (
                  <button 
                    onClick={() => toggleSelectUser(user)} 
                    style={{ 
                      padding: "6px 14px", 
                      borderRadius: 6, 
                      background: isSelected ? "#28a745" : "#5183fe", 
                      color: "#fff", 
                      border: "none", 
                      cursor: "pointer" 
                    }}
                  >
                    {isSelected ? "✓ Đã chọn" : "Chọn"}
                  </button>
                )}
              </div>
            );
          })}
        </div>
      )}
      
      {!loading && input && results.length === 0 && (
        <div style={{ color: '#aaa', marginTop: 8 }}>Không tìm thấy người dùng phù hợp.</div>
      )}

      {mode === 'group' && (
        <div style={{ marginTop: 16, display: 'flex', gap: 8 }}>
          <button 
            onClick={handleCreateGroup}
            disabled={creatingGroup || selectedUsers.length === 0}
            style={{ 
              flex: 1,
              padding: "12px 16px", 
              borderRadius: 8, 
              background: creatingGroup || selectedUsers.length === 0 ? "#666" : "#28a745", 
              color: "#fff", 
              border: "none", 
              cursor: creatingGroup || selectedUsers.length === 0 ? "not-allowed" : "pointer",
              fontWeight: 600
            }}
          >
            {creatingGroup ? "Đang tạo nhóm..." : `Tạo nhóm (${selectedUsers.length + 1} thành viên)`}
          </button>
        </div>
      )}
      </div>
    </div>
  );
};

export default AddUser;
