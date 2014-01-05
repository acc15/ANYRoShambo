package ru.vmsoftware;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    private Point from, to, pos;
    private Canvas canvas;

    private void strokeLine(GraphicsContext ctx, Point p1, Point p2) {
        ctx.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    private void renderArrow(GraphicsContext ctx, Paint paint, Point from, Point to) {

        ctx.setStroke(paint);
        ctx.setFill(paint);

        strokeLine(ctx, from, to);
        final Point v = from.sub(to).identity().mul(15);
        final Point p1 = v.rotate((float)Math.toRadians(15)).add(to);
        final Point p2 = v.rotate((float)Math.toRadians(-15)).add(to);
        ctx.fillPolygon(new double[]{to.getX(), p1.getX(), p2.getX()},
                        new double[]{to.getY(), p1.getY(), p2.getY()}, 3);

    }

    private void render() {
        final GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        ctx.setStroke(Color.WHITE);
        if (pos != null) {
            ctx.strokeText("Pos: " + pos, 0, 10);
        }

        Point drawnVec = null;
        if (from != null && to != null) {
            drawnVec = to.sub(from);
            ctx.strokeText("From: " + from, 0, 25);
            ctx.strokeText("To: " + to, 0, 40);
            ctx.strokeText("Drawn vector: " + drawnVec, 0, 55);
            renderArrow(ctx, Color.WHITE, from, to);
        }

        if (pos == null || drawnVec == null) {
            return;
        }

        final Point currentVec = pos.sub(from);
        ctx.strokeText("Current vector: " + currentVec, 0, 70);

        final float cosineOfAngle = drawnVec.cosineOfAngle(currentVec);
        ctx.strokeText("Cosine of angle: " + cosineOfAngle, 0, 85);

        renderArrow(ctx, Color.GRAY, from, pos);

    }

    private Point fromMouseEvent(MouseEvent e) {
        return Point.fromArray((float)e.getX(), (float)e.getY());
    }

    public void start(Stage stage) throws Exception {

        canvas = new Canvas(400, 400);
        canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                from = fromMouseEvent(mouseEvent);
                to = null;
                render();
            }
        });
        canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                to = fromMouseEvent(mouseEvent);
                render();
            }
        });
        canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                to = fromMouseEvent(mouseEvent);
                render();
            }
        });
        canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                pos = fromMouseEvent(mouseEvent);
                render();
            }
        });


        render();

        stage.setScene(new Scene(new Group(canvas), 400, 400));
        stage.show();
    }
}
