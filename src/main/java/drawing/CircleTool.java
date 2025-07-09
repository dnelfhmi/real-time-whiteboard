package drawing;

import javafx.scene.canvas.GraphicsContext;

/**
 * The CircleTool class implements the DrawingTool interface to provide
 * functionality for drawing circles on a canvas.
 */
public class CircleTool implements DrawingTool {

    /**
     * Draws a circle on the canvas.
     *
     * @param gc the GraphicsContext to draw on
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param endX the ending X coordinate
     * @param endY the ending Y coordinate
     */
    @Override
    public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
        gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
    }
}
