package it.unibs.pajc.ui.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GridRenderer {

    private static final double GRID_SIZE = 50;
    private static final Color GRID_COLOR = Color.rgb(50, 50, 70, 0.5);
    private static final Color AXIS_COLOR = Color.rgb(100, 100, 120, 0.8);

    public void render(GraphicsContext gc, double width, double height) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);

        for (double x = 0; x < width; x += GRID_SIZE) {
            gc.strokeLine(x, 0, x, height);
        }

        for (double y = 0; y < height; y += GRID_SIZE) {
            gc.strokeLine(0, y, width, y);
        }

        gc.setStroke(AXIS_COLOR);
        gc.setLineWidth(1);
        gc.strokeLine(0, height / 2, width, height / 2);
        gc.strokeLine(width / 2, 0, width / 2, height);
    }
}
