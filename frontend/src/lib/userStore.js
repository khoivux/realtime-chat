import { create } from "zustand";
import axios from "axios";

export const useUserStore = create((set, get) => ({
  currentUser: localStorage.getItem("accessToken") ? null : null, // Sẽ fetch sau khi login
  isLoading: false,
  onlineUsers: [], // Thêm state lưu danh sách user online
  fetchUserInfo: async (username) => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      set({ currentUser: null, isLoading: false });
      return;
    }
    set({ isLoading: true });
    try {
      const res = await axios.get(`http://localhost:8081/api/v1/users/${username}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      set({ currentUser: res.data.data, isLoading: false });
    } catch (err) {
      set({ currentUser: null, isLoading: false });
    }
  },
  fetchOnlineUsers: async () => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      set({ onlineUsers: [] });
      return;
    }
    try {
      const res = await axios.get("http://localhost:8081/api/v1/users/online", {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      set({ onlineUsers: res.data.data || [] });
    } catch (err) {
      set({ onlineUsers: [] });
    }
  },
  // Real-time handlers cho online status
  addOnlineUser: (userId) => {
    const { onlineUsers } = get();
    if (!onlineUsers.includes(userId)) {
      set({ onlineUsers: [...onlineUsers, userId] });
      console.log(`[UserStore] User ${userId} is now ONLINE`);
    }
  },
  removeOnlineUser: (userId) => {
    const { onlineUsers } = get();
    const updatedUsers = onlineUsers.filter(id => id !== userId);
    if (updatedUsers.length !== onlineUsers.length) {
      set({ onlineUsers: updatedUsers });
      console.log(`[UserStore] User ${userId} is now OFFLINE`);
    }
  },
  isUserOnline: (userId) => {
    const { onlineUsers } = get();
    return onlineUsers.includes(userId);
  },
  updateCurrentUser: (userData) => {
    set({ currentUser: userData });
  },
}));
