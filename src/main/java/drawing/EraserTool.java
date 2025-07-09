package drawing;

import javafx.scene.canvas.GraphicsContext;

/**
 * The EraserTool class implements the DrawingTool interface to provide
 * functionality for erasing parts of a canvas.
 */
public class EraserTool implements DrawingTool {

    private final double eraserSize;

    /**
     * Constructs a new EraserTool with the specified eraser size.
     *
     * @param eraserSize the size of the eraser
     */
    public EraserTool(double eraserSize) {
        this.eraserSize = eraserSize;
    }

    /**
     * Erases a part of the canvas.
     *
     * @param gc the GraphicsContext to draw on
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param endX the ending X coordinate
     * @param endY the ending Y coordinate
     */
    @Override
    public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        gc.clearRect(endX - eraserSize / 2, endY - eraserSize / 2, eraserSize, eraserSize);
    }
}
