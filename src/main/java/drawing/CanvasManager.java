package drawing;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The CanvasManager class manages the drawing operations on a JavaFX Canvas.
 * It supports various drawing tools and handles user interactions with the canvas.
 */
public class CanvasManager {
    private static final Logger LOGGER = Logger.getLogger(CanvasManager.class.getName());
    private final Canvas canvas;
    private final GraphicsContext gc;
    private double startX, startY;
    public String currentTool = "FreeDraw";
    private Color currentColor = Color.BLACK;
    private double eraserSize = 10;
    private String textContent = "";
    private final Map<String, DrawingTool> tools;
    private CommandListener listener;
    private final List<String> drawingActions = new ArrayList<>();

    /**
     * Constructs a new CanvasManager.
     *
     * @param canvas the JavaFX Canvas to manage
     * @param listener the CommandListener to notify of drawing actions
     */
    public CanvasManager(Canvas canvas, CommandListener listener) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.listener = listener;
        this.tools = new HashMap<>();
        tools.put("Line", new LineTool());
        tools.put("Circle", new CircleTool());
        tools.put("Rectangle", new RectangleTool());
        tools.put("Oval", new OvalTool());
        tools.put("Eraser", new EraserTool(eraserSize));
        tools.put("FreeDraw", new FreeDrawTool());
        initDrawing();
        // Resizing the canvas to fit window
        canvas.widthProperty().addListener(evt -> drawOnResize());
        canvas.heightProperty().addListener(evt -> drawOnResize());
    }

    /**
     * Handles canvas resizing by redrawing all actions.
     */
    private void drawOnResize() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (String action : drawingActions) {
            applyDrawingAction(action);
        }
    }

    /**
     * Sets the current drawing tool.
     *
     * @param tool the name of the drawing tool to set
     */
    public void setTool(String tool) {
        this.currentTool = tool;
    }

    /**
     * Sets the text content for the text tool.
     *
     * @param text the text content to set
     */
    public void setTextContent(String text) {
        this.textContent = text;
    }

    /**
     * Sets the current drawing color.
     *
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.currentColor = color;
        gc.setStroke(currentColor);
    }

    /**
     * Sets the size of the eraser tool.
     *
     * @param size the size of the eraser
     */
    public void setEraserSize(double size) {
        this.eraserSize = size;
        tools.put("Eraser", new EraserTool(eraserSize));
    }

    /**
     * Gets the managed JavaFX Canvas.
     *
     * @return the managed Canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Gets the current drawing color.
     *
     * @return the current drawing color
     */
    public Color getCurrentColor() {
        return currentColor;
    }

    /**
     * Initializes the drawing event handlers.
     */
    private void initDrawing() {
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);
    }

    /**
     * Handles mouse pressed events on the canvas.
     *
     * @param e the MouseEvent
     */
    private void onMousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
        if ("Eraser".equals(currentTool)) {
            gc.clearRect(startX - eraserSize / 2, startY - eraserSize / 2, eraserSize, eraserSize);
        } else {
            gc.beginPath();
            gc.moveTo(startX, startY);
        }
    }

    /**
     * Handles mouse dragged events on the canvas.
     *
     * @param e the MouseEvent
     */
    private void onMouseDragged(MouseEvent e) {
        double endX = e.getX();
        double endY = e.getY();
        if ("FreeDraw".equals(currentTool)) {
            if (listener != null) {
                String drawCommand = createCommand(startX, startY, endX, endY);
                listener.onReleaseCommand(drawCommand);
            }
            startX = endX;
            startY = endY;
        } else if ("Eraser".equals(currentTool)) {
            if (listener != null) {
                String drawCommand = createCommand(startX, startY, endX, endY);
                listener.onReleaseCommand(drawCommand);
            }
            startX = endX;
            startY = endY;
        }
    }

    /**
     * Handles mouse released events on the canvas.
     *
     * @param e the MouseEvent
     */
    private void onMouseReleased(MouseEvent e) {
        double endX = e.getX();
        double endY = e.getY();
        if ("Text".equals(currentTool)) {
            if (listener != null) {
                String drawCommand = createCommand(startX, startY, endX, endY);
                listener.onReleaseCommand(drawCommand);
            }
        } else if (!"FreeDraw".equals(currentTool) && !"Eraser".equals(currentTool)) {
            DrawingTool tool = tools.get(currentTool);
            if (tool != null) {
                if (listener != null) {
                    String drawCommand = createCommand(startX, startY, endX, endY);
                    listener.onReleaseCommand(drawCommand);
                }
            }
        }
    }

    /**
     * Creates a drawing command string based on the current tool and coordinates.
     *
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param endX the ending X coordinate
     * @param endY the ending Y coordinate
     * @return the drawing command string
     */
    private String createCommand(double startX, double startY, double endX, double endY) {
        String colorHex = String.format("#%02X%02X%02X",
                (int) (currentColor.getRed() * 255),
                (int) (currentColor.getGreen() * 255),
                (int) (currentColor.getBlue() * 255));

        switch (currentTool) {
            case "FreeDraw":
                return String.format("DRAW FreeDraw %f %f %f %f %s", startX, startY, endX, endY, colorHex);
            case "Line":
                return String.format("DRAW Line %f %f %f %f %s", startX, startY, endX, endY, colorHex);
            case "Circle":
                return String.format("DRAW Circle %f %f %f %f %s", startX, startY, endX, endY, colorHex);
            case "Rectangle":
                return String.format("DRAW Rectangle %f %f %f %f %s", startX, startY, endX, endY, colorHex);
            case "Oval":
                return String.format("DRAW Oval %f %f %f %f %s", startX, startY, endX, endY, colorHex);
            case "Eraser":
                return String.format("DRAW Eraser %f %f %f %f %s %f", startX, startY, endX, endY, colorHex, eraserSize);
            case "Text":
                return String.format("DRAW Text %f %f %f %f %s \"%s\"", startX, startY, endX, endY, colorHex, textContent);
            default:
                System.out.println("Unsupported tool for command: " + currentTool);
                return null;
        }
    }

    /**
     * Updates the canvas with a given drawing command.
     *
     * @param command the drawing command to apply
     */
    public void updateCanvas(String command) {
        LOGGER.log(Level.INFO, "Updating canvas with action: {0}", command);
        // Store the drawing action
        drawingActions.add(command);
        applyDrawingAction(command);
    }

    /**
     * Applies a drawing action to the canvas.
     *
     * @param command the drawing command to apply
     */
    private void applyDrawingAction(String command) {
        String[] parts = command.split(" ");
        if (parts.length < 6) {
            LOGGER.log(Level.SEVERE, "Invalid drawing action received: {0}", command);
            return;
        }

        String toolName = parts[1];
        double startX = Double.parseDouble(parts[2]);
        double startY = Double.parseDouble(parts[3]);
        double endX = Double.parseDouble(parts[4]);
        double endY = Double.parseDouble(parts[5]);
        Color color = Color.web(parts[6]);

        gc.setStroke(color);

        if ("Eraser".equals(toolName)) {
            double eraserSize = Double.parseDouble(parts[7]);
            gc.clearRect(startX - eraserSize / 2, startY - eraserSize / 2, eraserSize, eraserSize);
        } else if ("Text".equals(toolName)) {
            if (parts.length < 8) {
                LOGGER.log(Level.SEVERE, "Text content missing in command");
                return;
            }
            String textContent = parts[7].replaceAll("\"", ""); 
            gc.fillText(textContent, startX, startY);
        } else {
            DrawingTool tool = tools.get(toolName);
            if (tool != null) {
                tool.draw(gc, startX, startY, endX, endY);
            } else {
                LOGGER.log(Level.SEVERE, "Unknown tool: {0}", toolName);
            }
        }
    }

    /**
     * Clears the canvas and removes all stored drawing actions.
     */
    public void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawingActions.clear();
    }
}

