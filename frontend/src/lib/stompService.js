import { Client } from '@stomp/stompjs';

// Giữ trạng thái kết nối và subscription
let stompClient = null;
let currentSubscription = null;
let currentTopic = null;

export function createStompConnection({ token, topic, onMessage, onConnect, onDisconnect, onError }) {
  if (!token || !topic || !onMessage) throw new Error('Missing required params');

  // Nếu đã có client đang chạy, chỉ cần đổi topic (không tạo lại)
  if (stompClient && stompClient.connected) {
    switchTopic(topic, onMessage);
    return {
      client: stompClient,
      switchTopic: (newTopic) => switchTopic(newTopic, onMessage),
      disconnect: safeDisconnect,
    };
  }

  // Nếu chưa có client, tạo mới
  stompClient = new Client({
    brokerURL: 'ws://localhost:8081/ws',
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    onConnect: (frame) => {
      console.log('[STOMP] Kết nối thành công:', frame);
      if (onConnect) onConnect();
      switchTopic(topic, onMessage); // Sub topic ban đầu
    },
    onDisconnect: (frame) => {
      console.log('[STOMP] Đã disconnect:', frame);
      if (onDisconnect) onDisconnect();
    },
    onStompError: (frame) => {
      console.error('[STOMP] Lỗi STOMP:', frame);
      if (onError) onError(frame);
    },
    onWebSocketError: (evt) => {
      console.error('[STOMP] Lỗi WebSocket:', evt);
      if (onError) onError(evt);
    },
    reconnectDelay: 5000, // Tự reconnect nếu rớt mạng
  });

  console.log('[STOMP] Đang kích hoạt kết nối...');
  stompClient.activate();

  return {
    client: stompClient,
    switchTopic: (newTopic) => switchTopic(newTopic, onMessage),
    disconnect: safeDisconnect,
  };
}

// Hàm tạo kết nối STOMP toàn cục, cho phép lắng nghe nhiều topic
export function createGlobalStompConnection({ token, topics, onMessage, onConnect, onDisconnect, onError }) {
  if (!token || !topics || !onMessage) throw new Error('Missing required params');

  if (stompClient && stompClient.connected) {
    // Unsubscribe tất cả topic cũ
    if (currentSubscription) {
      currentSubscription.unsubscribe();
      currentSubscription = null;
    }
    // Subscribe tất cả topic mới
    topics.forEach(topic => {
      stompClient.subscribe(topic, (message) => {
        try {
          const data = JSON.parse(message.body);
          onMessage(data, topic);
        } catch (err) {
          console.error('[STOMP] Lỗi parse message:', err, message.body);
        }
      });
    });
    return {
      client: stompClient,
      disconnect: safeDisconnect,
    };
  }

  stompClient = new Client({
    brokerURL: 'ws://localhost:8081/ws',
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    onConnect: (frame) => {
      console.log('[STOMP] Kết nối toàn cục thành công:', frame);
      if (onConnect) onConnect();
      // Subscribe tất cả topic
      topics.forEach(topic => {
        stompClient.subscribe(topic, (message) => {
          try {
            const data = JSON.parse(message.body);
            onMessage(data, topic);
          } catch (err) {
            console.error('[STOMP] Lỗi parse message:', err, message.body);
          }
        });
      });
      // Xóa phần gửi tín hiệu online vì backend không có handler
    },
    onDisconnect: (frame) => {
      if (onDisconnect) onDisconnect();
    },
    onStompError: (frame) => {
      if (onError) onError(frame);
    },
    onWebSocketError: (evt) => {
      if (onError) onError(evt);
    },
    reconnectDelay: 5000,
  });
  stompClient.activate();
  return {
    client: stompClient,
    disconnect: safeDisconnect,
  };
}

// Unsubscribe topic cũ, subscribe topic mới
function switchTopic(newTopic, onMessage) {
  if (!stompClient || !stompClient.connected) {
    console.warn('[STOMP] Client chưa kết nối');
    return;
  }

  if (currentSubscription) {
    currentSubscription.unsubscribe();
    console.log('[STOMP] Đã unsubscribe topic:', currentTopic);
  }

  currentSubscription = stompClient.subscribe(newTopic, (message) => {
    try {
      const data = JSON.parse(message.body);
      console.log('[STOMP] Nhận message từ topic', newTopic, data);
      onMessage(data);
    } catch (err) {
      console.error('[STOMP] Lỗi parse message:', err, message.body);
    }
  });

  currentTopic = newTopic;
  console.log('[STOMP] Đã subscribe topic:', newTopic);
}

// Ngắt kết nối hoàn toàn (chỉ dùng khi logout/tab close)
function safeDisconnect() {
  if (currentSubscription) {
    currentSubscription.unsubscribe();
    currentSubscription = null;
  }
  if (stompClient) {
    console.log('[STOMP] Đang disconnect...');
    stompClient.deactivate();
    stompClient = null;
  }
}
