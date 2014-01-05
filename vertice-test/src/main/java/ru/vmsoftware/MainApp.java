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
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    private Point from, to, pos;
    private Canvas canvas;
    private double angle;

    private void render() {
        final GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(Color.BLACK);
        ctx.setStroke(Color.WHITE);

        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (pos != null) {
            ctx.strokeText("Pos: " + pos, 0, 10);
        }

        Point drawnVec = null;
        if (from != null && to != null) {
            drawnVec = to.sub(from);

            ctx.strokeText("From: " + from, 0, 25);
            ctx.strokeText("To: " + to, 0, 40);
            ctx.strokeText("Drawn vector: " + drawnVec, 0, 55);
            ctx.strokeLine(from.get(Point.X), from.get(Point.Y), to.get(Point.X), to.get(Point.Y));
        }

        if (pos == null || drawnVec == null) {
            return;
        }

        final Point currentVec = pos.sub(from);
        ctx.strokeText("Current vector: " + currentVec, 0, 70);

        final float cosineOfAngle = drawnVec.cosineOfAngle(currentVec);
        ctx.strokeText("Cosine of angle: " + cosineOfAngle, 0, 85);

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
