package com.example.snakegameproject2;
/*Project
Names: Deema/Mawaddah/Shumokh
Date:13 May 2023
The code represents a SnakeGame. 
The game consists of a snake that moves around the 
screen and tries to eat food. The snake grows in 
length each time it eats food. The game ends if 
the snake collides with the walls or itself. The 
speed of the snake increases based on the score.
 */

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.ImageView;


public class SnakeGame extends Application {

    // constants
    private static int SPEED = 5;
    private static final int WIDTH = 25; //number of corners
    private static final int HEIGHT = 25;
    private static final int CORNER_SIZE = 25;//each corner is 25 pixel
    private static final int MAX_FOOD_COLOR = 3;

    // variables
    private final List<Corner> snake = new ArrayList<>(); //instance
    private Dir direction = Dir.LEFT;
    private boolean gameOver = false;
    private final Random random = new Random(); //instance

    //choose X, and Y randomly to place the first food
    private int foodX = random.nextInt(WIDTH - 5) + 1;
    private int foodY = random.nextInt(HEIGHT - 5) + 1;
    private int foodColor;

    //4 instances
    private Image redAppleImage;
    private Image greenAppleImage;
    private Image yellowAppleImage;
    private Canvas canvas;

    //specifies the direction of the snake's movement
    public enum Dir {
        LEFT, RIGHT, UP, DOWN
    }

    //represent a single "cell" in the game board
    public static class Corner {
        int x;
        int y;

        public Corner(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // method to speed up the snake depending on the score
    public static class Speedup {
        // A private integer scoreThreshold to store the score at which the speed will be increased
        public int scoreThreshold;

        // A constructor for the Speedup class that takes an integer scoreThreshold and assigns it to the instance variable
        public Speedup(int scoreThreshold) {
            this.scoreThreshold = scoreThreshold;
        }

        // A method called increaseSpeed that takes an integer score as an input parameter
        public void increaseSpeed(int scoreThreshold) {
            switch (scoreThreshold) {
                // If the score is equal to 10, set the speed to 7
                case 3 -> SPEED = 7;
                case 6 -> SPEED = 10;
                case 8 -> SPEED = 14;

                // If the score does not match any of the cases above, do nothing
                default -> {
                }
            }
        }
    }

    // Create an instance of the Speedup class with a scoreThreshold of 10
    public Speedup speedup = new Speedup(3);


    @Override
    public void start(Stage primaryStage) {
        // First stage: Creates the start screen
        StackPane root1 = new StackPane();
        root1.setStyle("-fx-background-color: green;");
        //gives the actual size of the game board in pixels, 25 cells in width and each of 25 pixel
        Scene startScene = new Scene(root1, WIDTH * CORNER_SIZE, HEIGHT * CORNER_SIZE);


        //Borderpane that has the start button and snake image
        BorderPane borderPane = new BorderPane();
        Button startButton = new Button("Start");
        startButton.setStyle("-fx-background-color: yellow; -fx-text-fill: green; -fx-font-size: 30px;");
        startButton.setOnAction(e -> startGame());
        borderPane.setCenter(startButton);
        ImageView snakeIcon = new ImageView(new Image("snake_icon.png"));
        borderPane.setTop(snakeIcon);
        BorderPane.setAlignment(snakeIcon, Pos.CENTER);
        BorderPane.setMargin(snakeIcon, new Insets(20, 0, 0, 0));//20 pixels below the top edge of the BorderPane.
        root1.getChildren().add(borderPane);
        root1.setAlignment(Pos.CENTER);

        Image logoImage = new Image("logo.png");
        ImageView logoImageView = new ImageView(logoImage);
        // Set the scene of the primary stage to the start scene
        primaryStage.setScene(startScene);
        primaryStage.setTitle("SNAKE GAME");
        primaryStage.getIcons().add(logoImage);
        primaryStage.show();
    }

    // A method called startGame that takes a Stage object called primaryStage as an input parameter
    private void startGame() {
        //Second stage: The snake game implementation
        Stage primaryStage2 = new Stage();
        greenAppleImage = new Image("greenApple.png");
        redAppleImage = new Image("redApple.png");
        yellowAppleImage = new Image("yellowApple.png");
        VBox root = new VBox();
        canvas = new Canvas(WIDTH * CORNER_SIZE, HEIGHT * CORNER_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, WIDTH * CORNER_SIZE, HEIGHT * CORNER_SIZE);

        // Add the key event listener
        scene.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case UP -> direction = Dir.UP;
                case DOWN -> direction = Dir.DOWN;
                case LEFT -> direction = Dir.LEFT;
                case RIGHT -> direction = Dir.RIGHT;
            }
        });

        //starting snake has 3 cells
        //snake is set at the center of the game board.
        snake.add(new Corner(WIDTH / 2, HEIGHT / 2));
        snake.add(new Corner(WIDTH / 2, HEIGHT / 2));
        snake.add(new Corner(WIDTH / 2, HEIGHT / 2));

        primaryStage2.setScene(scene);
        primaryStage2.setTitle("SNAKE GAME");
        primaryStage2.show();

        /*updating the frames of the snake's movement on the screen at a certain rate,
         which gives the illusion of a smooth animation*/
        new AnimationTimer() {
            long lastTick = 0;

            @Override
            public void handle(long now) {
                if (canvas == null) {
                    // canvas is not yet initialized, wait for the next iteration
                    return;
                }

                if (lastTick == 0) {
                    lastTick = now;
                    tick(canvas.getGraphicsContext2D());
                    return;
                }

                if (now - lastTick > 1000000000 / SPEED) {
                    lastTick = now;
                    tick(canvas.getGraphicsContext2D());
                }
            }
        }.start();

    }

    private void tick(GraphicsContext gc) { //method signature for a private method named tick. It takes a GraphicsContext object gc as a parameter.
        speedup.increaseSpeed(snake.size() - 3);
        if (gameOver) { //if gameOver is true

            //try and catch is for the audio. If it didn't work, it will not stop the game from the execution, instead it will generate an error or exception
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                        new File("src/main/resources/crying.aiff"));
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // add game-over image and score
            Image backgroundImage = new Image("gameOver.png");
            gc.drawImage(backgroundImage, 0, 0, WIDTH * CORNER_SIZE, HEIGHT * CORNER_SIZE);
            gc.setFill(Color.RED);
            gc.setFont(new Font("Times New Roman", 50));
            gc.fillText("Game Over", 190, 335);
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Times New Roman", 30));
            gc.fillText("Score: " + (snake.size() - 3), 270, 364);

            // add restart button
            gc.setFill(Color.RED);
            gc.fillRect(249, 375, 130, 40);
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Times New Roman", 20));
            gc.fillText("RESTART", 270, 403);

            // check if restart button is clicked
            canvas.setOnMouseClicked(event -> {
                double x = event.getX();
                double y = event.getY();
                if (x >= 240 && x <= 360 && y >= 350 && y <= 400) {
                    restart();//call the restart function
                }
            });

            return;

        } else if (snake.size() - 3 == 10) { //if the user won
            Image backgroundImage = new Image("win.png");
            gc.drawImage(backgroundImage, 0, 0, WIDTH * CORNER_SIZE, HEIGHT * CORNER_SIZE);
            gc.setFill(Color.LIGHTGREEN);
            gc.setFont(new Font("Times New Roman", 50));
            gc.fillText("Congratulation", 170, 335);
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Times New Roman", 30));
            gc.fillText("You collected 10 scores", 170, 374); //display the score

            //try and catch is for the audio. If it didn't work, it will not stop the game from the execution, instead it will generate an exeption
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                        new File("src/main/resources/hooray.wav"));
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // add restart button
            gc.setFill(Color.LIGHTGREEN);
            gc.fillRect(249, 379, 130, 40);
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Times New Roman", 20));
            gc.fillText("RESTART", 270, 405);

            // check if restart button is clicked
            canvas.setOnMouseClicked(event -> {
                double x = event.getX();
                double y = event.getY();
                if (x >= 240 && x <= 360 && y >= 350 && y <= 400) {
                    restart();
                }
            });
            return;
        }

        // move the snake
        Corner head = snake.get(0);
        int x = head.x;
        int y = head.y;

        switch (direction) {//moving the snake by changing its X, and Y
            case UP -> y--;
            case DOWN -> y++;
            case LEFT -> x--;
            case RIGHT -> x++;
        }

        // check for collision with the walls or with itself
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) { // If the snake collides with the wall
            gameOver = true;
        } else {
            for (Corner c : snake) {
                if (c.x == x && c.y == y) {// If the snake collides with itself
                    gameOver = true;
                    break;
                }
            }
        }

        // check for collision with the food
        if (x == foodX && y == foodY) {
            //if snake's x == food's x and Their Ys == The snake ate the food
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                        new File("src/main/resources/apple.wav"));
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            // remove the current food by place it outside the x and y
            foodX = -1;
            foodY = -1;

            // add a new food at a random position
            while (foodX == -1 && foodY == -1) {
                int newX = random.nextInt(WIDTH - 5) + 1;//generate new x and y
                int newY = random.nextInt(HEIGHT - 5) + 1;
                boolean collision = false;

                for (Corner c : snake) {
                    if (c.x == newX && c.y == newY) { //check if the new food's place it in the same of any cell of the snake body
                        collision = true;//to prevent the food from being placed on top of the snake's body.
                        break;
                    }
                }

                if (!collision) {//if collision is FALSE
                    foodX = newX;//the generated x and y will be used
                    foodY = newY;
                    foodColor = random.nextInt(MAX_FOOD_COLOR);//will randomly choose one of the three images
                }
            }

            // add a new segment to the snake
            snake.add(0, new Corner(x, y));//new x and y to add a cell to the snake

        } else {
            // move the snake
            snake.remove(snake.size() - 1);//remove the head
            snake.add(0, new Corner(x, y));/*create the head's cell again
                                        in the new x and y so the snake moves*/
        }

        // clear the canvas and draw the snake and the food
        gc.clearRect(0, 0, WIDTH * CORNER_SIZE, HEIGHT * CORNER_SIZE);
        Image backgroundImage = new Image("background.png");
        gc.drawImage(backgroundImage, 0, 0, WIDTH * CORNER_SIZE, HEIGHT * CORNER_SIZE);


        // Set the fill color for the snake
        gc.setFill(Color.LIGHTGREEN);

        // Draw each corner of the snake
        for (int i = snake.size() - 1; i >= 0; i--) {
            //the body wOut the head |the body| subtract one to change all the body's cell's colors
            Corner c = snake.get(i);
            // If this is the head of the snake, set a different fill color
            if (i == 0) {
                gc.setFill(Color.DARKGREEN);
            }                                                  //width           height
            gc.fillRect(c.x * CORNER_SIZE, c.y * CORNER_SIZE, CORNER_SIZE - 1, CORNER_SIZE - 1);
            // Reset the fill color to the default color for the snake
            gc.setFill(Color.LIGHTGREEN);
        }

        // draw food
        gc.drawImage(redAppleImage, foodX * CORNER_SIZE, foodY * CORNER_SIZE, CORNER_SIZE, CORNER_SIZE);

        switch (foodColor) {
            case 0 -> gc.drawImage(redAppleImage, foodX * CORNER_SIZE, foodY * CORNER_SIZE, CORNER_SIZE, CORNER_SIZE);
            case 1 ->gc.drawImage(greenAppleImage, (foodX * CORNER_SIZE), (foodY * CORNER_SIZE), CORNER_SIZE, CORNER_SIZE);
            case 2 ->gc.drawImage(yellowAppleImage, foodX * CORNER_SIZE, foodY * CORNER_SIZE, CORNER_SIZE, CORNER_SIZE);
        }
    }

    private void restart() {
        // reset game variables
        snake.clear();
        snake.add(new Corner(WIDTH / 2, HEIGHT / 2));
        snake.add(new Corner(WIDTH / 2, HEIGHT / 2));
        snake.add(new Corner(WIDTH / 2, HEIGHT / 2));
        direction = Dir.LEFT;
        gameOver = false;
        SPEED = 5;

        // remove the mouse click listener from the canvas
        canvas.setOnMouseClicked(null);
    }

    public static void main(String[] args) {
        launch(args);
    }
}