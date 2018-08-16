package com.uvu.eric;



import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import jdk.nashorn.internal.scripts.JO;

import java.io.*;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class BounceBallControl extends Application implements IBallPaneObserver{
    private final static double DEFAULT_BOUNDARIES = 600;
    private final static String DISCONNECT_MSG = "disconnect";
    private volatile BallPane ballPane = new BallPane(); // Create a ball pane
    private SocketChannel server;

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        Scene primaryScene = CreateScene(true);
        primaryScene.setFill(Color.LIGHTGRAY);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }
    private Scene CreateScene(boolean sceneHasControls)
    {
        // Pause and resume animation
        ballPane.setOnMousePressed(e -> ballPane.pause());
        ballPane.setOnMouseReleased(e -> ballPane.play());

        // Increase and decrease animation
        ballPane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DIGIT1) {
                ballPane.increaseSpeed();
            }
            else if (e.getCode() == KeyCode.DIGIT2) {
                ballPane.decreaseSpeed();
            }
        });
        ballPane.requestFocus();
        ballPane.RegisterObserver(this);

        ballPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        return new Scene(ballPane, DEFAULT_BOUNDARIES, DEFAULT_BOUNDARIES);
    }


    private void InvokeCommands(String commandPrompt)
    {
                /*
        Messages are as follows:
        Commands to the server should be structured text, not RMI. (10 pts)
        The server maintains a table of (name, connection) routing pairs. It does NOT keep any global state information about the Balls or where they are. (10 pts)
         */
        Scanner input = new Scanner(commandPrompt);
        String result = input.next();
        boolean quit = false;
        switch (result)
        {
            case "connect":
                //connect: send "connect <name> <server ip> <server port>
                //The name used above should be included in every other command sent to the server. (10 pts)
                if(server == null)
                {
                    if (commandPrompt.split("\\s").length < 4) JOptionPane.showMessageDialog(new JFrame(), "Not enough arguments for this command.");
                    String name = input.next();
                    String address = input.next();
                    int port = input.nextInt();
                    try {
                        InetSocketAddress serverLocation = new InetSocketAddress(address, port);
                        server = SocketChannel.open(serverLocation);
                        ByteBuffer greg = ByteBuffer.allocate(name.getBytes().length);
                        greg.put(name.getBytes());
                        greg.flip();
                        server.write(greg);
                        Task readingTask = new Task<Void>(){

                            @Override
                            protected Void call()  {
                                Supplier<String> socketInput = ()->{
                                    try{
                                        ByteBuffer temp = ByteBuffer.allocate(1024);
                                        server.read(temp);
                                        temp.flip();
                                        return new String(temp.array()).trim();
                                    }
                                    catch (IOException ex)
                                    {
                                        JOptionPane.showMessageDialog(new JFrame(), "Error reading from server.");
                                        return null;
                                    }
                                };
                                Stream<String> stream = Stream.generate(socketInput);
                                stream.forEach((s)->{
                                    if (s.equals("ball")) Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            ballPane.AddBall(Color.BLACK);
                                        }
                                    });
                                });
                                return null;
                            }
                        };
                        new Thread(readingTask).start();
                    }
                    catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(new JFrame(), "Could not add server. Please check that all info entered was correct.");
                        server = null;
                    }
                    catch (IllegalArgumentException ex)
                    {
                        JOptionPane.showMessageDialog(new JFrame(), "Invalid arguments were used. Please review your command.");
                        server = null;
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(new JFrame(), "Please disconnect before reconnecting.");
                }
                break;
            case "disconnect":
                quit = true;
                //disconnect: send "disconnect <name>" to the server. (10 pts)
                //If a client window is killed, all Balls it contains disappear with it. Clients are NOT required to be persistent if/when restarted. (10 pts)
                if(server != null)
                {
                    if(server.isOpen()) {
                        String name = input.next();
                        if(name != null) {
                            ByteBuffer temp = ByteBuffer.allocate(DISCONNECT_MSG.getBytes().length + name.getBytes().length + " ".getBytes().length);
                            temp.put(DISCONNECT_MSG.getBytes());
                            temp.put(" ".getBytes());
                            temp.put(name.getBytes());
                            temp.flip();
                            try {
                                server.write(temp);
                            }
                            catch (IOException ex)
                            {
                                JOptionPane.showMessageDialog(new JFrame(), "Could not send disconnect to server.");
                            }
                            server = null;
                        }
                    }
                    else {
                        server = null;
                    }
                }
                else JOptionPane.showMessageDialog(new JFrame(), "Not currently connected to a server.");
                break;
            case "ball":
                //ball: send a ball to a random destination client that is different from the source. If only one client is connected, drop the message (destroy the ball). (10 pts)
                ByteBuffer temp = ByteBuffer.allocate("ball".getBytes().length);
                temp.put("ball".getBytes());
                temp.flip();
                try{
                    server.write(temp);
                }
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(new JFrame(), "Could not send ball to server.");
                }
                break;
            default:
                JOptionPane.showMessageDialog(new JFrame(), "Command not recognized.");
                break;
        }
    }

    /**
     * The main method is only needed for the IDE with limited
     * JavaFX support. Not needed for running from the command line.
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void Update(BallPane subject) {
        InvokeCommands(subject.LastCommand);
    }
}