package com.uvu.eric;

import javafx.event.ActionEvent;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.Random;


public class Ball extends Circle {
    public static final double defaultRadius = 20;
    private Random random = new Random();
    private static final double defaultMaxSpeed = 3;
    //private double x = radius, y = radius;
    private double dx = 1, dy = 1;


    //Note: Ball placement should be random, but bounded by parent.
    //Note: Ball speed should be random.
    //Note: Ball direction should be random.

    public Ball(Color fillColor, double xCenter, double yCenter)
    {
        super();
        setFill(fillColor);
        setRadius(defaultRadius);
        Random random = new Random();
        xCenter = xCenter < defaultRadius ?
                defaultRadius : xCenter;
        yCenter = yCenter < defaultRadius ?
                defaultRadius : yCenter;
        setCenterX(xCenter);
        setCenterY(yCenter);
        dx = random.nextDouble() * defaultMaxSpeed;
        dy = random.nextDouble() * defaultMaxSpeed;
        if(random.nextBoolean()) dx = -dx;
        if(random.nextBoolean()) dy = -dy;
    }
    protected void Move() {
        //Each movement has a chance to move the ball to a random location. Location is bounded by parent.
        if (random.nextDouble() < 0.01)
        {
            setCenterX(random.nextDouble() * getParent().getLayoutBounds().getWidth() -  getRadius());
            setCenterY(random.nextDouble()* getParent().getLayoutBounds().getHeight() - getRadius());
            setCenterX(getCenterX() < getRadius() ? getRadius():getCenterX());
            setCenterY(getCenterY() < getRadius() ? getRadius(): getCenterY());
        }

        // Check boundaries
        if (getCenterX() < getRadius() || getCenterX() > parentProperty().get().getLayoutBounds().getWidth() - getRadius()) {
            dx *= -1; // Change ball move direction
        }
        if (getCenterY() < getRadius() || getCenterY() > parentProperty().get().getLayoutBounds().getHeight() - getRadius()) {
            dy *= -1; // Change ball move direction
        }
        // Adjust ball position
        setCenterX(getCenterX() + dx);
        setCenterY(getCenterY() + dy);

    }
}
