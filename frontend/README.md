# React Chat App - Mock Data Version

Đây là phiên bản chat app sử dụng mock data để test giao diện mà không cần Firebase backend.

## Tính năng

- ✅ Giao diện chat hoàn chỉnh
- ✅ Danh sách chat với mock data
- ✅ Gửi tin nhắn (simulated)
- ✅ Upload ảnh (simulated)
- ✅ Tìm kiếm user
- ✅ Block/unblock user
- ✅ Responsive design

## Mock Data

Project sử dụng dữ liệu giả trong `src/lib/mockData.js`:

- **Users**: 5 users mẫu với avatar và thông tin
- **Chats**: 3 cuộc hội thoại mẫu với tin nhắn
- **Messages**: Tin nhắn mẫu với timestamp

## Cách sử dụng

1. **Cài đặt dependencies:**
   ```bash
   npm install
   ```

2. **Chạy development server:**
   ```bash
   npm run dev
   ```

3. **Truy cập ứng dụng:**
   - Mở browser tại `http://localhost:5173`
   - App sẽ tự động đăng nhập với user mẫu "John Doe"

## Test các tính năng

### Chat List
- Xem danh sách chat với 3 cuộc hội thoại
- Click vào chat để mở
- Tìm kiếm user theo tên

### Chat Interface
- Xem tin nhắn mẫu
- Gửi tin nhắn mới (sẽ hiển thị trong session)
- Upload ảnh (sẽ hiển thị mock URL)
- Sử dụng emoji picker

### Add User
- Tìm kiếm user: "Jane Smith", "Mike Johnson", "Sarah Wilson", "David Brown"
- Click "Add User" để simulate thêm user

### User Detail
- Xem thông tin user
- Block/unblock user
- Logout (sẽ reload page)

## Cấu trúc Mock Data

```javascript
// Users
mockUsers = [
  { id: "user1", username: "John Doe", avatar: "...", blocked: [] },
  // ...
]

// Chats
mockChats = [
  { chatId: "chat1", messages: [...] },
  // ...
]

// User Chats
mockUserChats = {
  user1: [
    { chatId: "chat1", receiverId: "user2", lastMessage: "...", isSeen: true },
    // ...
  ]
}
```

## Lưu ý

- Tất cả dữ liệu chỉ tồn tại trong session
- Không có persistent storage
- Upload ảnh trả về URL mẫu
- Authentication được simulate

## Dependencies đã loại bỏ

- ❌ Firebase
- ❌ Firebase Auth
- ❌ Firestore
- ❌ Firebase Storage

## Dependencies còn lại

- ✅ React
- ✅ Zustand (state management)
- ✅ Emoji Picker
- ✅ React Toastify
- ✅ Vite
