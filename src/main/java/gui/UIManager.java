package gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Optional;
import java.util.function.Consumer;
import drawing.CanvasManager;

/**
 * The UIManager class manages the user interface for the whiteboard application.
 */
public class UIManager {
    private final WhiteboardApp app;
    private final CanvasManager canvasManager;
    private TextArea chatArea;
    private TextField chatInput;
    private ListView<String> userListView;
    private TextField textInput;

    /**
     * Constructs a new UIManager.
     *
     * @param app the WhiteboardApp instance associated with this manager
     * @param canvasManager the CanvasManager instance associated with this manager
     */
    public UIManager(WhiteboardApp app, CanvasManager canvasManager) {
        this.app = app;
        this.canvasManager = canvasManager;
    }

    /**
     * Initializes the primary stage and sets up the user interface.
     *
     * @param primaryStage the primary stage for this application
     */
    public void initialize(Stage primaryStage) {
        primaryStage.setTitle("Shared Whiteboard");

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add("style.css");

        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        VBox tools = createToolbox();
        root.setLeft(tools);

        Canvas canvas = canvasManager.getCanvas();
        root.setCenter(canvas);

        // Bind canvas size to the window size
        canvas.widthProperty().bind(scene.widthProperty().subtract(tools.widthProperty()));
        canvas.heightProperty().bind(scene.heightProperty().subtract(menuBar.getHeight()));

        // Bind the sidebar width to a fraction of the window width
        tools.prefWidthProperty().bind(scene.widthProperty().multiply(0.2));

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Shows an approval dialog for a user connection request.
     *
     * @param username the username of the user requesting connection
     * @param decisionCallback the callback to handle the user's decision
     */
    public void showApprovalDialog(String username, Consumer<Boolean> decisionCallback) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("User Connection Request");
            alert.setHeaderText("Approval Needed");
            alert.setContentText("Do you want to approve the connection for user: " + username + "?");

            ButtonType buttonTypeAccept = new ButtonType("Accept");
            ButtonType buttonTypeRefuse = new ButtonType("Refuse", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeAccept, buttonTypeRefuse);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == buttonTypeAccept) {
                decisionCallback.accept(true);
            } else {
                decisionCallback.accept(false);
            }
        });
    }

    /**
     * Creates the menu bar for the application.
     *
     * @return the created MenuBar
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem newItem = new MenuItem("New");
        newItem.setOnAction(e -> {
            try {
                app.getController().createNewBoard();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });

        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Whiteboard");
            File file = fileChooser.showOpenDialog(app.getPrimaryStage());
            if (file != null) {
                try {
                    app.getController().openBoard(file.getAbsolutePath());
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Whiteboard");
            File file = fileChooser.showSaveDialog(app.getPrimaryStage());
            if (file != null) {
                try {
                    app.getController().saveBoard(file.getAbsolutePath());
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        MenuItem saveAsItem = new MenuItem("Save As");
        saveAsItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Whiteboard As");
            File file = fileChooser.showSaveDialog(app.getPrimaryStage());
            if (file != null) {
                try {
                    app.getController().saveBoard(file.getAbsolutePath());
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        MenuItem closeItem = new MenuItem("Close");
        closeItem.setOnAction(e -> {
            try {
                app.getController().closeBoard();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });

        // Add a button or menu item for kicking out a user
        MenuItem kickUserItem = new MenuItem("Kick User");
        kickUserItem.setOnAction(e -> {
            String username = getSelectedUsername();
            if (username != null) {
                try {
                    app.getController().kickUser(username);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        fileMenu.getItems().add(kickUserItem);

        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, closeItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    /**
     * Gets the selected username from the user list.
     *
     * @return the selected username, or null if no user is selected
     */
    private String getSelectedUsername() {
        return userListView.getSelectionModel().getSelectedItem();
    }

    /**
     * Creates the toolbox for the application.
     *
     * @return the created VBox containing the toolbox
     */
    private VBox createToolbox() {
        VBox tools = new VBox(15);
        tools.setId("sidebar");
        tools.setPadding(new Insets(10));

        userListView = new ListView<>();
        userListView.getStyleClass().add("list-view");
        // Reduce the height of the active users field
        userListView.setPrefHeight(100);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        // Increase the height of the chat area
        chatArea.setPrefHeight(300);

        chatInput = new TextField();
        chatInput.setPromptText("Type a message...");
        // Set the preferred height of the chat input field
        chatInput.setPrefHeight(30);
        chatInput.setOnAction(e -> {
            String message = chatInput.getText();
            try {
                app.getController().sendMessage(message);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
            chatInput.clear();
        });

        ColorPicker colorPicker = new ColorPicker(canvasManager.getCurrentColor());
        colorPicker.getStyleClass().add("button");
        // Increase the width of the color picker
        colorPicker.setPrefWidth(150);
        colorPicker.setOnAction(e -> canvasManager.setColor(colorPicker.getValue()));

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            try {
                app.getController().clearCanvas();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });

        ComboBox<String> shapePicker = new ComboBox<>();
        shapePicker.getItems().addAll("FreeDraw", "Line", "Circle", "Rectangle", "Oval", "Eraser", "Text");
        shapePicker.setValue("FreeDraw");
        shapePicker.getStyleClass().add("combo-box");
        shapePicker.setOnAction(e -> {
            String selectedTool = shapePicker.getValue();
            canvasManager.setTool(selectedTool);
            if ("Text".equals(selectedTool)) {
                canvasManager.setTextContent(textInput.getText());
            }
        });

        Slider eraserSizeSlider = new Slider(5, 50, 10);
        eraserSizeSlider.setShowTickLabels(true);
        eraserSizeSlider.setShowTickMarks(true);
        eraserSizeSlider.getStyleClass().add("slider");
        eraserSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> canvasManager.setEraserSize(newVal.doubleValue()));

        textInput = new TextField();
        textInput.setPromptText("Enter text to write");
        textInput.setOnAction(e -> {
            if ("Text".equals(canvasManager.currentTool)) {
                canvasManager.setTextContent(textInput.getText());
            }
        });

        tools.getChildren().addAll(
                new Label("Color Picker"), colorPicker,
                clearBtn, new Label("Shapes"), shapePicker,
                new Label("Eraser Size"), eraserSizeSlider,
                new Label("Text Input"), textInput,
                new Label("Active Users"), userListView,
                new Label("Chat"), chatArea, chatInput
        );
        return tools;
    }

    /**
     * Updates the chat area with a new message.
     *
     * @param message the message to add to the chat area
     */
    public void updateChat(String message) {
        chatArea.appendText(message + "\n");
    }

    /**
     * Updates the user list with the given array of usernames.
     *
     * @param users the array of usernames to display in the user list
     */
    public void updateUserList(String[] users) {
        userListView.getItems().clear();
        userListView.getItems().addAll(users);
    }
}
