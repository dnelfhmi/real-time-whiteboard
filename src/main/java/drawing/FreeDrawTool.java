package drawing;

import javafx.scene.canvas.GraphicsContext;

/**
 * The FreeDrawTool class implements the DrawingTool interface to provide
 * functionality for freehand drawing on a canvas.
 */
public class FreeDrawTool implements DrawingTool {

    /**
     * Draws a freehand line on the canvas.
     *
     * @param gc the GraphicsContext to draw on
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param endX the ending X coordinate
     * @param endY the ending Y coordinate
     */
    @Override
    public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.lineTo(endX, endY);
        gc.stroke();
        gc.closePath();
    }
}