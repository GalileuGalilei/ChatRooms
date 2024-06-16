const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('api', {
  sendUsername: (username) => 
    {
      ipcRenderer.send('send-username', username);
    }
});

window.addEventListener('DOMContentLoaded', () => {
  ipcRenderer.on('update-chat-rooms', (event, chatRoomNames) => {
    const chatroomDiv = document.findElementById('chatrooms');
    alert("dfsdf")
    if (chatroomDiv) {
      chatroomDiv.innerHTML = ''; // Clear existing content
      chatRoomNames.forEach(name => {
        const div = document.createElement('div');
        div.textContent = name;
        chatroomDiv.appendChild(div);
      });
    } else {
      console.error('Chatroom div not found');
    }
  })});

ipcRenderer.on('update-messages', (event, message) => {
    const messagesDiv = document.querySelector('.messages');
    if (messagesDiv) {
        const div = document.createElement('div');
        div.textContent = message;
        messagesDiv.appendChild(div);
    } else {
        console.error('Messages div not found');
    }
    });


