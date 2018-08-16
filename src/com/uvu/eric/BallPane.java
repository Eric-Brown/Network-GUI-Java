package com.uvu.eric;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.event.TextEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BallPane extends Pane implements IObservableBallPane{
    private volatile ArrayList<Ball> balls = new ArrayList<Ball>();
    public ArrayList<Ball> BallList()
    {
        return balls;
    }
    private LinkedList<IBallPaneObserver> observers = new LinkedList<>();
    private Timeline animation;
    private ColorPicker picker = new ColorPicker();
    private Random random = new Random();
    private TextField commandEntry = new TextField();
    public String LastCommand = "";
    public void AddBall()
    {
        Ball toAdd= new Ball(picker.getValue(), random.nextDouble() * getWidth() - Ball.defaultRadius,
                random.nextDouble() * getHeight() - Ball.defaultRadius);
        getChildren().add(toAdd);
        toAdd.toBack();
        balls.add(toAdd);
    }
    public synchronized void AddBall(Color ballColor)
    {
        Ball toAdd= new Ball(ballColor, random.nextDouble() * getWidth() - Ball.defaultRadius,
                random.nextDouble() * getHeight() - Ball.defaultRadius);
        getChildren().add(toAdd);
        toAdd.toBack();
        balls.add(toAdd);
    }
    public BallPane(boolean hasControls)
    {
        ConfigureAnimation();
        balls.get(0).setFill(Color.RED);
        balls.get(0).setCenterX(300);
        if(hasControls) getChildren().add(InitializeControls());
    }

    private void OnButtonPress()
    {
        LastCommand = commandEntry.getText();
        NotifyObservers();
    }

    public BallPane() {
        ConfigureAnimation();
        getChildren().add(InitializeControls());
    }

    private void ConfigureAnimation() {
        balls.add(new Ball(Color.GREEN, Ball.defaultRadius, Ball.defaultRadius));
        balls.stream().forEach((ball)->{getChildren().add(ball);});

        // Create an animation for moving the ball
        animation = new Timeline(
                new KeyFrame(Duration.millis(50), (event)->balls.stream().forEach((e)->e.Move()))
        );
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play(); // Start animation
    }

    private VBox InitializeControls() {
        Button addBallBtn = new Button("Add Ball");
        addBallBtn.setOnAction(event -> AddBall());
        //Button testBallBtn = new Button("test");
        //testBallBtn.setOnAction(event -> AddBall(Color.BLACK));
        Button enterCommandBtn = new Button("Send Command");
        enterCommandBtn.setOnAction(event -> OnButtonPress());
        Label commandLabel = new Label("Enter Commands:");
        commandLabel.setPadding(new Insets(2,5,0,2));
        VBox StackedControls = new VBox();
        HBox UpperControls = new HBox();
        UpperControls.getChildren().addAll(addBallBtn, picker);
        HBox LowerControls = new HBox();
        LowerControls.getChildren().addAll(commandLabel, commandEntry, enterCommandBtn);
        StackedControls.getChildren().addAll(UpperControls, LowerControls);
        return StackedControls;
    }


    public void play() {
        animation.play();
    }

    public void pause() {
        animation.pause();
    }

    public void increaseSpeed() {
        animation.setRate(animation.getRate() + 0.1);
    }

    public void decreaseSpeed() {
        animation.setRate(
                animation.getRate() > 0 ? animation.getRate() - 0.1 : 0);
    }

    public DoubleProperty rateProperty() {
        return animation.rateProperty();
    }


    @Override
    public void RegisterObserver(IBallPaneObserver observer) {
        observers.add(observer);
    }

    @Override
    public void UnregisterObserver(IBallPaneObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void NotifyObservers() {
        observers.forEach((o)->o.Update(this));
    }
}