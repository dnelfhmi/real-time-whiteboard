package drawing;

import javafx.scene.canvas.GraphicsContext;

public class OvalTool implements DrawingTool {

    @Override
    public void draw(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        gc.strokeOval(Math.min(startX, endX), Math.min(startY, endY), Math.abs(endX - startX), Math.abs(endY - startY));
    }
}
