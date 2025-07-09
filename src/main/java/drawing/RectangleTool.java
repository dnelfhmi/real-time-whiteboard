package drawing;

import javafx.scene.canvas.GraphicsContext;

public class RectangleTool implements DrawingTool {

    @Override
    public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY), Math.abs(endX - startX), Math.abs(endY - startY));
    }
}
