package drawing;

import javafx.scene.canvas.GraphicsContext;

/**
 * The LineTool class implements the DrawingTool interface to provide
 * functionality for drawing lines on a canvas.
 */
public class LineTool implements DrawingTool {

    /**
     * Draws a line on the canvas.
     *
     * @param gc the GraphicsContext to draw on
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param endX the ending X coordinate
     * @param endY the ending Y coordinate
     */
    @Override
    public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        gc.strokeLine(startX, startY, endX, endY);
    }
}
