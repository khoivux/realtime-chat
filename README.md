# REALTIME CHAT 💬  
A full-featured real-time chat application supporting 1-on-1 and group conversations with real-time presence, push notifications, and admin management.

---

## Features

- 🔐 **Authentication & Authorization**
  - JWT-based authentication
  - Email verification
  - Social login using OAuth2 (Google, Facebook, etc.)

- 💬 **Real-time Messaging**
  - 1-on-1 and group chat
  - Text and image messages
  - WebSocket-based real-time communication

- 👥 **User Presence & Session Management**
  - Online/offline user status
  - Token storage and session tracking with Redis

- 🔔 **Push Notifications**
  - Real-time push notifications via Firebase Cloud Messaging (FCM)

- 🛠 **Admin Dashboard**
  - User management
  - Statistics for users and messages

- ☁ **Media Management**
  - Image upload and storage using Cloudinary

---

## Tech Stack

### Backend
- Spring Boot
- Spring Security
- Spring Data JPA
- RESTful APIs
- WebSocket (STOMP + SockJS)
- JWT & OAuth2

### Databases & Cache
- MongoDB 
- Redis (token storage & user presence)

### DevOps & Services
- Docker
- Firebase Cloud Messaging (FCM)
- Cloudinary
