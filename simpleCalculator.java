import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class simpleCalculator extends Application {

    private TextField display;
    private double num1 = 0;
    private double memory = 0;
    private String operator = "";
    private boolean startNewInput = true;
    private ListView<String> historyList;

    @Override
    public void start(Stage primaryStage) {
        display = new TextField();
        display.setEditable(false);
        display.setAlignment(Pos.CENTER_RIGHT);
        display.setPrefHeight(50);
        display.setStyle("-fx-font-size: 18");

        GridPane buttonGrid = new GridPane();
        buttonGrid.setVgap(10);
        buttonGrid.setHgap(10);
        buttonGrid.setPadding(new Insets(10));

        String[][] buttons = {
                {"7", "8", "9", "/"},
                {"4", "5", "6", "*"},
                {"1", "2", "3", "-"},
                {"0", ".", "=", "+"},
                {"C", "←", "±", "🌙"},
                {"MC", "M+", "M-", "MR"}
        };

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String text = buttons[row][col];
                Button button = new Button(text);
                button.setPrefSize(60, 60);
                button.setStyle("-fx-font-size: 14");
                button.setOnAction(e -> handleButton(text));
                buttonGrid.add(button, col, row);
            }
        }

        VBox leftPane = new VBox(10, display, buttonGrid);
        leftPane.setPadding(new Insets(10));

        historyList = new ListView<>();
        historyList.setPrefWidth(200);
        historyList.setStyle("-fx-font-size: 14");

        Label historyLabel = new Label("History");
        historyLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        VBox historyPane = new VBox(10, historyLabel, historyList);
        historyPane.setPadding(new Insets(10));

        HBox root = new HBox(leftPane, historyPane);
        Scene scene = new Scene(root, 580, 450);

        // Keyboard support
        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            if (code.isDigitKey() || code == KeyCode.PERIOD) {
                handleButton(code.getName());
            } else if (code == KeyCode.ENTER) {
                handleButton("=");
            } else if (code == KeyCode.BACK_SPACE) {
                handleButton("←");
            } else if (code == KeyCode.ADD) {
                handleButton("+");
            } else if (code == KeyCode.SUBTRACT) {
                handleButton("-");
            } else if (code == KeyCode.MULTIPLY) {
                handleButton("*");
            } else if (code == KeyCode.DIVIDE) {
                handleButton("/");
            }
        });

        // Load history and memory from DB on startup
        loadHistoryFromDB();
        loadMemoryFromDB();

        primaryStage.setTitle("Calculator with MySQL Memory + History");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void handleButton(String text) {
        switch (text) {
            case "C" -> {
                display.clear();
                operator = "";
                startNewInput = true;
            }
            case "←" -> {
                String current = display.getText();
                if (!current.isEmpty()) {
                    display.setText(current.substring(0, current.length() - 1));
                }
            }
            case "±" -> {
                if (!display.getText().isEmpty()) {
                    double val = Double.parseDouble(display.getText());
                    display.setText(String.valueOf(-val));
                }
            }
            case "=" -> {
                if (!operator.isEmpty()) {
                    double num2 = Double.parseDouble(display.getText());
                    double result = calculate(num1, num2, operator);
                    String historyEntry = num1 + " " + operator + " " + num2 + " = " + result;
                    historyList.getItems().add(0, historyEntry); // add at top

                    display.setText(String.valueOf(result));
                    startNewInput = true;
                    operator = "";

                    // Save history to DB
                    DBUtil.saveHistory(num1 + " " + operator + " " + num2, String.valueOf(result));
                }
            }
            case "+", "-", "*", "/" -> {
                if (!display.getText().isEmpty()) {
                    num1 = Double.parseDouble(display.getText());
                    operator = text;
                    startNewInput = true;
                }
            }
            case "." -> {
                if (startNewInput) {
                    display.setText("0.");
                    startNewInput = false;
                } else if (!display.getText().contains(".")) {
                    display.appendText(".");
                }
            }
            case "🌙" -> toggleDarkMode();
            case "MC" -> {
                memory = 0;
                DBUtil.saveMemory(memory);
            }
            case "M+" -> {
                if (!display.getText().isEmpty()) {
                    memory += Double.parseDouble(display.getText());
                    DBUtil.saveMemory(memory);
                }
            }
            case "M-" -> {
                if (!display.getText().isEmpty()) {
                    memory -= Double.parseDouble(display.getText());
                    DBUtil.saveMemory(memory);
                }
            }
            case "MR" -> display.setText(String.valueOf(memory));
            default -> {
                if (startNewInput) {
                    display.setText(text);
                    startNewInput = false;
                } else {
                    display.appendText(text);
                }
            }
        }
    }

    private double calculate(double a, double b, String op) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> (b != 0) ? a / b : 0;
            default -> 0;
        };
    }

    private void toggleDarkMode() {
        boolean isDark = display.getStyle().contains("white");
        String bgColor = isDark ? "#ffffff" : "#222222";
        String textColor = isDark ? "black" : "white";

        display.setStyle("-fx-control-inner-background: " + bgColor + "; -fx-text-fill: " + textColor + "; -fx-font-size: 18");

        historyList.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; -fx-font-size: 14");
    }

    private void loadHistoryFromDB() {
        List<String> history = DBUtil.loadHistory();
        historyList.getItems().addAll(history);
    }

    private void loadMemoryFromDB() {
        memory = DBUtil.loadMemory();
    }
}