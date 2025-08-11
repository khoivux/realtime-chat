// MessageItem.jsx - New version with separate image and text bubbles
import React from "react";
import { formatTime, truncateReplyPreview } from "./chatUtils";
import { useUserStore } from "../../lib/userStore";

const MessageItem = ({
  message,
  hoveredMsgId,
  setHoveredMsgId,
  setReplyToMsg,
  handleStartEdit,
  handleDeleteMsg,
  editingMsgId,
  editText,
  setEditText,
  handleSaveEdit,
  setEditingMsgId,
  deletingMsgId
}) => {
  const { isUserOnline } = useUserStore();
  
  if (!message || message.deleted) return null;

  // Component for displaying images
  const ImageDisplay = ({ mediaUrl, onClick, style = {} }) => (
    <div style={{ marginBottom: 8, ...style }}>
      <img 
        src={mediaUrl} 
        alt="Shared image" 
        style={{ 
          maxWidth: '400px', 
          maxHeight: '500px',
          width: 'auto',
          height: 'auto',
          borderRadius: '12px',
          cursor: 'pointer',
          boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
          display: 'block'
        }}
        onClick={onClick}
      />
    </div>
  );

  // Component for text bubble
  const TextBubble = ({ className, children, style = {} }) => (
    <div className={className} style={{ position: 'relative', ...style }}>
      {children}
    </div>
  );

  // Tin nhắn không phải của mình
  if (!message.mine) {
    return (
      <div
        className="msg-hover-area"
        style={{ display: 'block', marginBottom: 8 }}
        onMouseEnter={() => setHoveredMsgId(message.id)}
        onMouseLeave={() => setHoveredMsgId(null)}
      >
        <div className="msg-sender" style={{ display: 'flex', alignItems: 'center', gap: 8, marginLeft: 2, marginBottom: 2 }}>
          <div style={{ position: 'relative' }}>
            <img src={message.sender.avatarUrl || "./avatar.png"} alt="avatar" className="msg-avatar" style={{ width: 32, height: 32 }} />
            {isUserOnline(message.sender.userId) && (
              <div style={{
                position: 'absolute',
                bottom: 0,
                right: 0,
                width: '8px',
                height: '8px',
                backgroundColor: '#4caf50',
                borderRadius: '50%',
                border: '1px solid #23283a'
              }}></div>
            )}
          </div>
          <span className="msg-displayName">{message.sender.displayName}</span>
        </div>
        
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', maxWidth: '70%', marginLeft: 40 }}>
          {/* Reply preview */}
          {message.parent && (
            <div className="reply-preview-in-msg" style={{ 
              background: "rgba(81,131,254,0.12)", 
              borderLeft: "3px solid #5183fe", 
              padding: "6px 10px", 
              borderRadius: 6, 
              marginBottom: 8, 
              fontSize: 13, 
              color: "#b3c0d1",
              alignSelf: 'stretch',
              maxWidth: '100%'
            }}>
              <span style={{ fontWeight: 500 }}>{message.parent.sender?.displayName || ""}</span>: {truncateReplyPreview(message.parent.message)}
            </div>
          )}
          
          {/* Image without bubble */}
          {(message.type === 'IMAGE' || message.mediaUrl) && (
            <ImageDisplay 
              mediaUrl={message.mediaUrl} 
              onClick={() => window.open(message.mediaUrl, '_blank')}
              style={{ alignSelf: 'flex-start' }}
            />
          )}
          
          {/* Text message with bubble - only if there's text */}
          {message.message && message.message.trim() && (
            <TextBubble className="message">
              {editingMsgId === message.id ? (
                <div className="edit-msg-box">
                  <input
                    value={editText}
                    onChange={e => setEditText(e.target.value)}
                    onKeyDown={e => {
                      if (e.key === "Enter" && !e.shiftKey) handleSaveEdit(message.id);
                      if (e.key === "Escape") setEditingMsgId(null);
                    }}
                    autoFocus
                  />
                  <button onClick={() => handleSaveEdit(message.id)}>Lưu</button>
                  <button onClick={() => setEditingMsgId(null)}>Hủy</button>
                </div>
              ) : (
                <div style={{ display: 'inline-block', position: 'relative', verticalAlign: 'middle' }}>
                  <p className="msg-content" style={{ display: 'inline-block', position: 'relative', margin: 0 }}>
                    {message.message}
                    {hoveredMsgId === message.id && (
                      <span style={{ 
                        position: 'absolute', 
                        left: 0, 
                        bottom: '-28px', 
                        background: 'rgba(30,34,44,0.95)', 
                        color: '#b3c0d1', 
                        fontSize: 13, 
                        padding: '3px 10px', 
                        borderRadius: 6, 
                        whiteSpace: 'nowrap', 
                        zIndex: 10, 
                        boxShadow: '0 2px 8px 0 rgba(0,0,0,0.12)' 
                      }}>
                        {formatTime(message.createdAt)}
                      </span>
                    )}
                  </p>
                </div>
              )}
            </TextBubble>
          )}
          
          {message.img && <img src={message.img} alt="" />} {/* Keep legacy support */}
        </div>
      </div>
    );
  }

  // Tin nhắn của mình
  return (
    <div
      style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', marginBottom: 8 }}
      onMouseEnter={() => setHoveredMsgId(message.id)}
      onMouseLeave={() => setHoveredMsgId(null)}
    >
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', maxWidth: '70%', marginRight: 16 }}>
        {/* Reply preview */}
        {message.parent && (
          <div className="reply-preview-in-msg" style={{ 
            background: "rgba(81,131,254,0.12)", 
            borderLeft: "3px solid #5183fe", 
            padding: "6px 10px", 
            borderRadius: 6, 
            marginBottom: 8, 
            fontSize: 13, 
            color: "#b3c0d1",
            alignSelf: 'stretch',
            maxWidth: '100%'
          }}>
            <span style={{ fontWeight: 500 }}>{message.parent.sender?.displayName || ""}</span>: {truncateReplyPreview(message.parent.message)}
          </div>
        )}
        
        {/* Image without bubble */}
        {(message.type === 'IMAGE' || message.mediaUrl) && (
          <ImageDisplay 
            mediaUrl={message.mediaUrl} 
            onClick={() => window.open(message.mediaUrl, '_blank')}
            style={{ alignSelf: 'flex-end' }}
          />
        )}
        
        {/* Text message with bubble - only if there's text */}
        {message.message && message.message.trim() && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            {/* 3 icon sát bên trái bubble tin nhắn, chỉ hiện khi hover */}
            {message.mine && hoveredMsgId === message.id && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <button className="msg-action-btn" title="Trả lời" onClick={() => setReplyToMsg(message)} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 2 }}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#eaf6ff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="9 17 4 12 9 7"/><line x1="4" y1="12" x2="20" y2="12"/></svg>
                </button>
                <button className="msg-action-btn" title="Chỉnh sửa" onClick={() => handleStartEdit(message)} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 2 }}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#eaf6ff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19.5 3 21l1.5-4L16.5 3.5z"/></svg>
                </button>
                <button className="msg-action-btn" title="Xóa" onClick={() => handleDeleteMsg(message.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 2 }} disabled={deletingMsgId === message.id}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#eaf6ff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
                </button>
              </div>
            )}
            
            <TextBubble className="message own">
              {editingMsgId === message.id ? (
                <div className="edit-msg-box">
                  <input
                    value={editText}
                    onChange={e => setEditText(e.target.value)}
                    onKeyDown={e => {
                      if (e.key === "Enter" && !e.shiftKey) handleSaveEdit(message.id);
                      if (e.key === "Escape") setEditingMsgId(null);
                    }}
                    autoFocus
                  />
                  <button onClick={() => handleSaveEdit(message.id)}>Lưu</button>
                  <button onClick={() => setEditingMsgId(null)}>Hủy</button>
                </div>
              ) : (
                <div style={{ display: 'inline-block', position: 'relative', verticalAlign: 'middle' }}>
                  <p className="msg-own-content" style={{ display: 'inline-block', position: 'relative', margin: 0 }}>
                    {message.deleted
                      ? (
                        <span style={{ color: '#b3c0d1', fontStyle: 'italic' }}>
                          Bạn đã thu hồi tin nhắn
                        </span>
                      )
                      : message.message}
                    {hoveredMsgId === message.id && (
                      <span style={{ 
                        position: 'absolute', 
                        right: 0, 
                        bottom: '-28px', 
                        background: 'rgba(30,34,44,0.95)', 
                        color: '#b3c0d1', 
                        fontSize: 13, 
                        padding: '3px 10px', 
                        borderRadius: 6, 
                        whiteSpace: 'nowrap', 
                        zIndex: 10, 
                        boxShadow: '0 2px 8px 0 rgba(0,0,0,0.12)' 
                      }}>
                        {formatTime(message.createdAt)}
                      </span>
                    )}
                  </p>
                </div>
              )}
            </TextBubble>
          </div>
        )}
        
        {message.img && <img src={message.img} alt="" />} {/* Keep legacy support */}
      </div>
    </div>
  );
};

export default MessageItem;
