import { create } from "zustand";
import { useUserStore } from "./userStore";

export const useChatStore = create((set) => ({
  chatId: null,
  user: null,
  conversation: null, // Thêm conversation để lưu thông tin group
  isCurrentUserBlocked: false,
  isReceiverBlocked: false,
  refreshFlag: 0, // Thêm biến này để trigger reload chat list
  changeChat: (chatId, user, conversation = null) => {
    const currentUser = useUserStore.getState().currentUser;
    const userBlocked = user && Array.isArray(user.blocked) ? user.blocked : [];
    const currentUserBlocked = currentUser && Array.isArray(currentUser.blocked) ? currentUser.blocked : [];

    // CHECK IF CURRENT USER IS BLOCKED
    if (userBlocked.includes(currentUser.id)) {
      return set({
        chatId,
        user: null,
        conversation,
        isCurrentUserBlocked: true,
        isReceiverBlocked: false,
      });
    }

    // CHECK IF RECEIVER IS BLOCKED
    else if (currentUserBlocked.includes(user.userId)) {
      return set({
        chatId,
        user: user,
        conversation,
        isCurrentUserBlocked: false,
        isReceiverBlocked: true,
      });
    } else {
      return set({
        chatId,
        user,
        conversation,
        isCurrentUserBlocked: false,
        isReceiverBlocked: false,
      });
    }
  },
  triggerRefresh: () => set((state) => ({ refreshFlag: state.refreshFlag + 1 })), // Hàm này để tăng refreshFlag
  changeBlock: () => {
    set((state) => ({ ...state, isReceiverBlocked: !state.isReceiverBlocked }));
  },
  resetChat: () => {
    set({
      chatId: null,
      user: null,
      conversation: null,
      isCurrentUserBlocked: false,
      isReceiverBlocked: false,
    });
  },
  // Hàm cập nhật conversation (dùng khi edit group)
  updateConversation: (updatedConversation) => {
    set((state) => ({
      conversation: updatedConversation
    }));
  },
}));
