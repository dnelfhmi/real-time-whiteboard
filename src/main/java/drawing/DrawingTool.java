package drawing;

import javafx.scene.canvas.GraphicsContext;

/**
 * The DrawingTool interface defines a method for drawing shapes on a canvas.
 */
public interface DrawingTool {

    void draw(GraphicsContext gc, double startX, double startY, double endX, double endY);
}

