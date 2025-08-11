// src/components/chat/chatApi.js
import axios from "axios";
import upload from "../../lib/upload"; // Import upload function

const BASE_URL = "http://localhost:8081/api/v1";

const getAuthHeaders = () => ({
  'Authorization': `Bearer ${localStorage.getItem("accessToken")}`,
  'Content-Type': 'application/json'
});

/* ------------------ Message APIs ------------------ */

// Lấy tin nhắn theo conversationId với pagination
export const fetchMessages = async (conversationId, currentUser, offset = 0, limit = 10) => {
  const res = await axios.get(`${BASE_URL}/chat/${conversationId}`, {
    headers: getAuthHeaders(),
    params: { offset, limit }
  });

  const response = res.data.data;
  // Backend trả về tin nhắn mới nhất trước (descending), cần reverse để hiển thị đúng
  const messages = response.content.reverse().map(msg => ({
    ...msg,
    mine: msg.sender?.userId === currentUser?.id
  }));

  return { 
    messages,
    nextOffset: response.nextOffset,
    // hasMore = true nếu: số message = limit VÀ nextOffset > offset hiện tại
    hasMore: response.content.length === limit && response.nextOffset > offset
  };
};

// Lấy danh sách hình ảnh đã chia sẻ trong conversation
export const fetchSharedPhotos = async (conversationId) => {
  try {
    const res = await axios.get(`${BASE_URL}/chat/${conversationId}`, {
      headers: getAuthHeaders(),
      params: { 
        offset: 0, 
        limit: 1000 // Lấy nhiều tin nhắn để tìm hình ảnh
      }
    });

    const messages = res.data.data.content;
    
    // Filter messages that have images
    const imageMessages = messages
      .filter(msg => msg.type === 'IMAGE' && msg.mediaUrl)
      .map(msg => ({
        id: msg.id,
        mediaUrl: msg.mediaUrl,
        message: msg.message || '', // Caption
        createdAt: msg.createdAt,
        sender: msg.sender
      }))
      .reverse(); // Hiển thị ảnh mới nhất trước

    return imageMessages;
  } catch (error) {
    console.error("Error fetching shared photos:", error);
    return [];
  }
};

// Gửi tin nhắn bằng WebSocket (gửi text/image/reply)
export const sendMessage = async ({ chatId, text, replyToMsg, img, stompServiceRef, currentUser }) => {
  let mediaUrl = null;
  let messageType = "TEXT";
  let messageContent = text;

  // If there's an image, upload it first
  if (img && img.file) {
    try {
      console.log("Uploading image for message...");
      mediaUrl = await upload(img.file);
      messageType = "IMAGE";
      console.log("Image uploaded successfully:", mediaUrl);
      
      // For image messages, the text can be empty or used as caption
      if (!text.trim()) {
        messageContent = ""; // Empty message for image-only
      }
    } catch (error) {
      console.error("Failed to upload image:", error);
      throw new Error("Failed to upload image");
    }
  }

  const message = {
    conversationId: chatId,
    message: messageContent,
    parentId: replyToMsg?.id || null,
    senderId: currentUser?.id,
    mediaUrl: mediaUrl,
    type: messageType
  };

  console.log("Sending message:", message);

  const client = stompServiceRef.current?.client;

  if (!client) {
    console.error("❌ STOMP client is undefined");
    return;
  }

  if (typeof client.publish !== 'function') {
    console.error("❌ STOMP client.publish is not a function", client);
    return;
  }

  // Gửi tin nhắn qua WebSocket
  client.publish({
    destination: "/app/chat.sendMessage",
    body: JSON.stringify(message),
  });

  // Mark conversation as read khi gửi tin nhắn
  try {
    await markConversationAsRead(chatId);
  } catch (error) {
    console.error("❌ Failed to mark conversation as read:", error);
  }
};

// Xoá tin nhắn
export const deleteMessage = async (messageId) => {
  const res = await axios.delete(`${BASE_URL}/chat/${messageId}`, {
    headers: getAuthHeaders()
  });
  return res.data;
};

// Chỉnh sửa tin nhắn
export const editMessage = async (messageId, newMessage) => {
  const res = await axios.patch(
    `${BASE_URL}/chat/`,
    { id: messageId, message: newMessage },
    { headers: getAuthHeaders() }
  );
  return res.data.data;
};

/* ------------------ Conversation APIs ------------------ */

// Lấy chi tiết 1 conversation
export const fetchConversation = async (conversationId) => {
  const res = await axios.get(`${BASE_URL}/conversation/${conversationId}`, {
    headers: getAuthHeaders()
  });
  return res.data.data;
};

// Mark conversation as read
export const markConversationAsRead = async (conversationId) => {
  await axios.patch(`${BASE_URL}/conversation/${conversationId}/mark-as-read`, {}, {
    headers: getAuthHeaders()
  });
};

// Thêm người dùng vào group chat
export const addUserToConversation = async (participantRequest) => {
  await axios.patch(`${BASE_URL}/conversation/add-user`, participantRequest, {
    headers: getAuthHeaders()
  });
};

// Xóa người dùng khỏi group chat
export const removeUserFromConversation = async (participantRequest) => {
  await axios.patch(`${BASE_URL}/conversation/remove-user`, participantRequest, {
    headers: getAuthHeaders()
  });
};

/* ------------------ User APIs ------------------ */

// Tìm người dùng theo tên
export const searchUser = async (name) => {
  console.log("[API] searchUser called with name:", name);
  console.log("[API] Request URL:", `${BASE_URL}/users/`);
  console.log("[API] Request params:", { name });
  console.log("[API] Auth headers:", getAuthHeaders());
  
  try {
    const res = await axios.get(`${BASE_URL}/users/`, {
      params: { name },
      headers: getAuthHeaders()
    });
    console.log("[API] searchUser response:", res.data);
    return res.data.data;
  } catch (error) {
    console.error("[API] searchUser error:", error);
    console.error("[API] Error response:", error.response?.data);
    throw error;
  }
};

/* ------------------ Auth ------------------ */

// Xoá accessToken khi logout
export const logout = () => {
  localStorage.removeItem("accessToken");
};
