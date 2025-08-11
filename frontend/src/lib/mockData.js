// Mock data for testing UI without Firebase

export const mockUsers = [
  {
    id: "user1",
    username: "John Doe",
    email: "john@example.com",
    avatar: "https://randomuser.me/api/portraits/men/1.jpg",
    blocked: []
  },
  {
    id: "user2",
    username: "Jane Smith",
    email: "jane@example.com",
    avatar: "https://randomuser.me/api/portraits/women/2.jpg",
    blocked: []
  },
  {
    id: "user3",
    username: "Mike Johnson",
    email: "mike@example.com",
    avatar: "https://randomuser.me/api/portraits/men/3.jpg",
    blocked: []
  },
  {
    id: "user4",
    username: "Sarah Wilson",
    email: "sarah@example.com",
    avatar: "https://randomuser.me/api/portraits/women/4.jpg",
    blocked: []
  },
  {
    id: "user5",
    username: "David Brown",
    email: "david@example.com",
    avatar: "https://randomuser.me/api/portraits/men/5.jpg",
    blocked: []
  },
  {
    id: "user6",
    username: "Emily Clark",
    email: "emily@example.com",
    avatar: "https://randomuser.me/api/portraits/women/6.jpg",
    blocked: []
  },
  {
    id: "user7",
    username: "Chris Lee",
    email: "chris@example.com",
    avatar: "https://randomuser.me/api/portraits/men/7.jpg",
    blocked: []
  },
  {
    id: "user8",
    username: "Anna Scott",
    email: "anna@example.com",
    avatar: "https://randomuser.me/api/portraits/women/8.jpg",
    blocked: []
  },
  {
    id: "user9",
    username: "Tom Harris",
    email: "tom@example.com",
    avatar: "https://randomuser.me/api/portraits/men/9.jpg",
    blocked: []
  },
  {
    id: "user10",
    username: "Olivia King",
    email: "olivia@example.com",
    avatar: "https://randomuser.me/api/portraits/women/10.jpg",
    blocked: []
  },
  {
    id: "user11",
    username: "Jack White",
    email: "jack@example.com",
    avatar: "https://randomuser.me/api/portraits/men/11.jpg",
    blocked: []
  },
  {
    id: "user12",
    username: "Sophia Green",
    email: "sophia@example.com",
    avatar: "https://randomuser.me/api/portraits/women/12.jpg",
    blocked: []
  }
];

export const mockChats = [
  {
    chatId: "chat1",
    messages: [
      {
        senderId: "user1",
        text: "Hey, how are you doing?",
        createdAt: new Date(Date.now() - 3600000), // 1 hour ago
        id: "msg1"
      },
      {
        senderId: "user2", 
        text: "I'm doing great! How about you?",
        createdAt: new Date(Date.now() - 3000000), // 50 minutes ago
        id: "msg2"
      },
      {
        senderId: "user1",
        text: "Pretty good! Want to grab coffee later?",
        createdAt: new Date(Date.now() - 2400000), // 40 minutes ago
        id: "msg3"
      }
    ]
  },
  {
    chatId: "chat2",
    messages: [
      {
        senderId: "user1",
        text: "Did you finish the project?",
        createdAt: new Date(Date.now() - 7200000), // 2 hours ago
        id: "msg4"
      },
      {
        senderId: "user3",
        text: "Almost done! Will send it by EOD",
        createdAt: new Date(Date.now() - 6000000), // 1.5 hours ago
        id: "msg5"
      }
    ]
  },
  {
    chatId: "chat3", 
    messages: [
      {
        senderId: "user4",
        text: "Happy birthday! 🎉",
        createdAt: new Date(Date.now() - 86400000), // 1 day ago
        id: "msg6"
      },
      {
        senderId: "user1",
        text: "Thank you so much! 😊",
        createdAt: new Date(Date.now() - 82800000), // 23 hours ago
        id: "msg7"
      }
    ]
  },
  {
    chatId: "chat4",
    messages: [
      {
        senderId: "user1",
        text: "Hey Emily!",
        createdAt: new Date(Date.now() - 7200000),
        id: "msg8"
      },
      {
        senderId: "user6",
        text: "Hi John! How are you?",
        createdAt: new Date(Date.now() - 7100000),
        id: "msg9"
      }
    ]
  },
  {
    chatId: "chat5",
    messages: [
      {
        senderId: "user1",
        text: "Hey Chris!",
        createdAt: new Date(Date.now() - 6500000),
        id: "msg10"
      },
      {
        senderId: "user7",
        text: "Hello John!",
        createdAt: new Date(Date.now() - 6400000),
        id: "msg11"
      }
    ]
  },
  {
    chatId: "chat6",
    messages: [
      {
        senderId: "user1",
        text: "Hi Anna!",
        createdAt: new Date(Date.now() - 6000000),
        id: "msg12"
      },
      {
        senderId: "user8",
        text: "Hi John!",
        createdAt: new Date(Date.now() - 5900000),
        id: "msg13"
      }
    ]
  },
  {
    chatId: "chat7",
    messages: [
      {
        senderId: "user1",
        text: "Hey Tom!",
        createdAt: new Date(Date.now() - 5500000),
        id: "msg14"
      },
      {
        senderId: "user9",
        text: "Hey!",
        createdAt: new Date(Date.now() - 5400000),
        id: "msg15"
      }
    ]
  },
  {
    chatId: "chat8",
    messages: [
      {
        senderId: "user1",
        text: "Hi Olivia!",
        createdAt: new Date(Date.now() - 5000000),
        id: "msg16"
      },
      {
        senderId: "user10",
        text: "Hi John!",
        createdAt: new Date(Date.now() - 4900000),
        id: "msg17"
      }
    ]
  },
  {
    chatId: "chat9",
    messages: [
      {
        senderId: "user1",
        text: "Hey Jack!",
        createdAt: new Date(Date.now() - 4500000),
        id: "msg18"
      },
      {
        senderId: "user11",
        text: "Hey John!",
        createdAt: new Date(Date.now() - 4400000),
        id: "msg19"
      }
    ]
  },
  {
    chatId: "chat10",
    messages: [
      {
        senderId: "user1",
        text: "Hi Sophia!",
        createdAt: new Date(Date.now() - 4000000),
        id: "msg20"
      },
      {
        senderId: "user12",
        text: "Hi John!",
        createdAt: new Date(Date.now() - 3900000),
        id: "msg21"
      }
    ]
  }
];

export const mockUserChats = {
  user1: [
    {
      chatId: "chat1",
      receiverId: "user2",
      lastMessage: "Pretty good! Want to grab coffee later?",
      updatedAt: Date.now() - 2400000,
      isSeen: true
    },
    {
      chatId: "chat2", 
      receiverId: "user3",
      lastMessage: "Almost done! Will send it by EOD",
      updatedAt: Date.now() - 6000000,
      isSeen: false
    },
    {
      chatId: "chat3",
      receiverId: "user4", 
      lastMessage: "Thank you so much! 😊",
      updatedAt: Date.now() - 82800000,
      isSeen: true
    },
    {
      chatId: "chat4",
      receiverId: "user6",
      lastMessage: "Hi John! How are you?",
      updatedAt: Date.now() - 7100000,
      isSeen: true
    },
    {
      chatId: "chat5",
      receiverId: "user7",
      lastMessage: "Hello John!",
      updatedAt: Date.now() - 6400000,
      isSeen: true
    },
    {
      chatId: "chat6",
      receiverId: "user8",
      lastMessage: "Hi John!",
      updatedAt: Date.now() - 5900000,
      isSeen: true
    },
    {
      chatId: "chat7",
      receiverId: "user9",
      lastMessage: "Hey!",
      updatedAt: Date.now() - 5400000,
      isSeen: true
    },
    {
      chatId: "chat8",
      receiverId: "user10",
      lastMessage: "Hi John!",
      updatedAt: Date.now() - 4900000,
      isSeen: true
    },
    {
      chatId: "chat9",
      receiverId: "user11",
      lastMessage: "Hey John!",
      updatedAt: Date.now() - 4400000,
      isSeen: true
    },
    {
      chatId: "chat10",
      receiverId: "user12",
      lastMessage: "Hi John!",
      updatedAt: Date.now() - 3900000,
      isSeen: true
    }
  ]
};

// Current user (for testing)
export const currentUser = mockUsers[0];

// Helper functions
export const findUserById = (userId) => {
  return mockUsers.find(user => user.id === userId);
};

export const findChatById = (chatId) => {
  return mockChats.find(chat => chat.chatId === chatId);
};

export const getUserChats = (userId) => {
  return mockUserChats[userId] || [];
}; 