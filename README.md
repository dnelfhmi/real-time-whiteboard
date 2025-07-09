# 📝 Real-Time Shared Whiteboard

## 📖 Overview

This project is a real-time collaborative whiteboard application built with Java, JavaFX, and Java RMI. It allows multiple users to draw, chat, and collaborate on a shared canvas, with one user acting as the session manager. The application supports user approval, drawing tools, chat, and session management features.

## ✨ Features

- **Real-time Collaboration:** Multiple users can draw and interact on the same whiteboard.
- **Drawing Tools:** Free draw, line, rectangle, circle, oval, eraser, and text input.
- **Color Picker:** Choose custom colors for drawing.
- **Eraser Tool:** Adjustable eraser size for removing parts of the drawing.
- **Text Tool:** Add text annotations to the canvas.
- **User Management:** Manager approves or rejects join requests, can kick users, and sees the active user list.
- **Chat:** Built-in chat for session participants.
- **Session Management:** Create, open, save, and close whiteboard sessions (manager only).
- **File Operations:** Save/load whiteboard state to/from files.
- **Customizable UI:** Modern JavaFX interface with theming via `style.css`.

## 🏛️ Architecture

- **Java RMI:** Used for communication between server and clients.
- **Server:** Manages session state, user registration, approval, and broadcasts updates.
- **Client:** Connects to server, handles drawing, chat, and UI.
- **JavaFX:** Provides the graphical user interface.
- **Modular Design:** Separate packages for client, server, GUI, drawing tools, and common interfaces.

### 🧩 Main Components
- `server.WhiteboardServer`: Starts the RMI server and session manager.
- `client.CreateWhiteBoardLauncher`: Launches the manager client (creates a new whiteboard session).
- `client.JoinWhiteBoardLauncher`: Launches a regular client (joins an existing session).
- `gui.WhiteboardApp`, `gui.UIManager`: Main UI logic and layout.
- `drawing.CanvasManager` and tools: Drawing logic and extensible tool support.

## ⚙️ Setup

### Prerequisites
- Java 11 or higher
- Maven

### Build

```
mvn clean package
```

This will generate the following JARs in the project root:
- `whiteboardserver.jar` (server)
- `createwhiteboard.jar` (manager client)
- `joinwhiteboard.jar` (regular client)

## ▶️ Usage

### 1. Start the Server

```
java -jar whiteboardserver.jar <serverIPAddress> <serverPort>
```
Example:
```
java -jar whiteboardserver.jar 127.0.0.1 1099
```

### 2. Start the Manager Client (Create Whiteboard)

```
java -jar createwhiteboard.jar <serverIPAddress> <serverPort> <username>
```
Example:
```
java -jar createwhiteboard.jar 127.0.0.1 1099 Alice
```

### 3. Start a Regular Client (Join Whiteboard)

```
java -jar joinwhiteboard.jar <serverIPAddress> <serverPort> <username>
```
Example:
```
java -jar joinwhiteboard.jar 127.0.0.1 1099 Bob
```

- The manager must approve join requests for new users.
- Only the manager can save/load/close the session or kick users.

## Customization

- **UI Theme:** Modify `src/main/resources/style.css` to change the look and feel.
- **Logging:** Adjust `logging.properties` for log level and output.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Authors

- Daniel Fahmi (2025)

---

*For more details, see the code and comments in each package. Contributions and suggestions are welcome!*
