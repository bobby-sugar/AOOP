package com.oc.aoop.cli;

import com.oc.aoop.model.SudokuModel;
import com.oc.aoop.model.SudokuModelInterface;

import java.util.Scanner;

/**
 * Command-line version of the Sudoku game.
 * This class directly uses the Model and does not use a separate View or Controller.
 */
public class SudokuCLIApp {

    private final SudokuModelInterface model;
    private final Scanner scanner;

    public SudokuCLIApp() {
        model = new SudokuModel();
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        SudokuCLIApp app = new SudokuCLIApp();
        app.run();
    }

    private void run() {
        System.out.println("Welcome to AOOP Sudoku CLI.");
        System.out.println("Type 'help' to see available commands.");
        printBoard();

        boolean running = true;

        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            switch (command) {
                case "help":
                    printHelp();
                    break;

                case "show":
                    printBoard();
                    break;

                case "set":
                    handleSet(parts);
                    break;

                case "clear":
                    handleClear(parts);
                    break;

                case "undo":
                    handleUndo(parts);
                    break;

                case "hint":
                    handleHint(parts);
                    break;

                case "reset":
                    handleReset(parts);
                    break;

                case "new":
                    handleNewGame(parts);
                    break;

                case "quit":
                case "exit":
                    running = false;
                    System.out.println("Goodbye.");
                    break;

                default:
                    System.out.println("Unknown command. Type 'help' to see available commands.");
                    break;
            }
        }
    }

    private void printHelp() {
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  show                 Display the current Sudoku board.");
        System.out.println("  set row col value    Set an editable cell. Example: set 1 3 9");
        System.out.println("  clear row col        Clear an editable cell. Example: clear 1 3");
        System.out.println("  undo                 Undo the most recent user action.");
        System.out.println("  hint                 Reveal one correct value if hints are enabled.");
        System.out.println("  reset                Restore the current puzzle to its initial state.");
        System.out.println("  new                  Start a new game.");
        System.out.println("  quit                 Exit the program.");
        System.out.println();
        System.out.println("Rows and columns are numbered from 1 to 9.");
        System.out.println("Pre-filled cells cannot be changed.");
        System.out.println();
    }

    private void handleSet(String[] parts) {
        if (parts.length != 4) {
            System.out.println("Usage: set row col value. Example: set 1 3 9");
            return;
        }

        Integer row = parseNumber(parts[1]);
        Integer col = parseNumber(parts[2]);
        Integer value = parseNumber(parts[3]);

        if (row == null || col == null || value == null) {
            System.out.println("Row, column and value must be numbers.");
            return;
        }

        int modelRow = row - 1;
        int modelCol = col - 1;

        if (!isUserPositionValid(row, col)) {
            System.out.println("Row and column must be between 1 and 9.");
            return;
        }

        if (value < 1 || value > 9) {
            System.out.println("Only digits 1-9 are valid inputs.");
            return;
        }

        if (model.isFixedCell(modelRow, modelCol)) {
            System.out.println("This is a pre-filled cell and cannot be modified.");
            return;
        }

        boolean changed = model.setCellValue(modelRow, modelCol, value);

        if (!changed) {
            System.out.println("The move was rejected.");
            return;
        }

        printBoard();

        if (model.isValidationFeedbackEnabled() && model.hasConflictAt(modelRow, modelCol)) {
            System.out.println("Invalid move: this value creates a duplicate in its row, column or 3x3 box.");
        }

        checkCompletion();
    }

    private void handleClear(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Usage: clear row col. Example: clear 1 3");
            return;
        }

        Integer row = parseNumber(parts[1]);
        Integer col = parseNumber(parts[2]);

        if (row == null || col == null) {
            System.out.println("Row and column must be numbers.");
            return;
        }

        int modelRow = row - 1;
        int modelCol = col - 1;

        if (!isUserPositionValid(row, col)) {
            System.out.println("Row and column must be between 1 and 9.");
            return;
        }

        if (model.isFixedCell(modelRow, modelCol)) {
            System.out.println("This is a pre-filled cell and cannot be cleared.");
            return;
        }

        boolean cleared = model.clearCell(modelRow, modelCol);

        if (cleared) {
            printBoard();
        } else {
            System.out.println("The cell could not be cleared.");
        }
    }

    private void handleUndo(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Usage: undo");
            return;
        }

        boolean undone = model.undo();

        if (undone) {
            System.out.println("Last move undone.");
            printBoard();
        } else {
            System.out.println("There is no move to undo.");
        }
    }

    private void handleHint(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Usage: hint");
            return;
        }

        boolean hinted = model.giveHint();

        if (hinted) {
            System.out.println("A hint has been added.");
            printBoard();
            checkCompletion();
        } else {
            System.out.println("No hint was added. Hints may be disabled or there may be no empty editable cells.");
        }
    }

    private void handleReset(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Usage: reset");
            return;
        }

        model.reset();
        System.out.println("Puzzle reset.");
        printBoard();
    }

    private void handleNewGame(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Usage: new");
            return;
        }

        model.newGame();
        System.out.println("New game loaded.");
        printBoard();
    }

    private void checkCompletion() {
        if (model.isBoardComplete()) {
            System.out.println("Congratulations! The Sudoku puzzle is correctly completed.");
        }
    }

    private void printBoard() {
        System.out.println();
        System.out.println("    1 2 3   4 5 6   7 8 9");
        System.out.println("  +-------+-------+-------+");

        for (int row = 0; row < SudokuModelInterface.BOARD_SIZE; row++) {
            System.out.print((row + 1) + " | ");

            for (int col = 0; col < SudokuModelInterface.BOARD_SIZE; col++) {
                int value = model.getCellValue(row, col);

                if (value == SudokuModelInterface.EMPTY_CELL) {
                    System.out.print(". ");
                } else {
                    System.out.print(value + " ");
                }

                if ((col + 1) % 3 == 0) {
                    System.out.print("| ");
                }
            }

            System.out.println();

            if ((row + 1) % 3 == 0) {
                System.out.println("  +-------+-------+-------+");
            }
        }

        System.out.println();
    }

    private Integer parseNumber(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isUserPositionValid(int row, int col) {
        return row >= 1
                && row <= SudokuModelInterface.BOARD_SIZE
                && col >= 1
                && col <= SudokuModelInterface.BOARD_SIZE;
    }
}