const { app, BrowserWindow } = require('electron');
const path = require('path');
const net = require('net');

let mainWindow;

function createWindow () {
  mainWindow = new BrowserWindow({
    width: 800,
    height: 600,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: true, // Make sure to disable Node.js integration
    }
  });

  mainWindow.loadFile('index.html');
}

app.on('ready', createWindow);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});

// Setup TCP socket server
let clientSocket = net.connect(3000, "26.3.100.186");

/*
const server = net.createServer((socket) => {
    console.log('Client connected');
    clientSocket = socket;

  socket.on('data', (data) => {
    let input = data.toString().split(':');
    let command = input[0];
    let argument = input[1];

    if (command === 'rooms') 
    {
        console.log(`Creating chat room: ${argument}`);
        chatRoomNames = argument.split(',');
        mainWindow.webContents.send('update-chat-rooms', chatRoomNames);
        return;
    }

    if (command === 'message') 
    {
        console.log(`Received message: ${argument}`);
        mainWindow.webContents.send('update-messages', argument);
        return;
    }


  });

  socket.on('end', () => {
    console.log('Client disconnected');

//socket.pipe(socket);
  
  });
});
*/
let ipcMain = require('electron').ipcMain;
ipcMain.on('send-username', (event, data) => {
    console.log(`Received username: ${data}`);
    //send "name:username" to the server
    mainWindow.webContents.send('update-chat-rooms', ['chat1', 'chat2']);
    clientSocket.write(`name:${data}`);
    clientSocket.write('\n');
    
    
});
