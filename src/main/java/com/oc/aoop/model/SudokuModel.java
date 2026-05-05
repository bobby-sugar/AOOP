package com.oc.aoop.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

/**
 * Model for the Sudoku game.
 * This class stores all game state and enforces Sudoku rules.
 */
@SuppressWarnings("deprecation")
public class SudokuModel extends Observable implements SudokuModelInterface {

    private final List<int[][]> puzzles = new ArrayList<>();
    private final Random random = new Random();

    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    private int[][] initialBoard = new int[BOARD_SIZE][BOARD_SIZE];
    private int[][] solutionBoard = new int[BOARD_SIZE][BOARD_SIZE];

    private Move lastMove;

    private boolean validationFeedbackEnabled = true;
    private boolean hintEnabled = true;
    private boolean randomPuzzleEnabled = false;

    public SudokuModel() {
        loadPuzzlesFromFile();
        loadPuzzle(selectPuzzleIndex());
        assert invariant() : "Model invariant failed after construction";
    }

    public boolean invariant() {
        return hasExpectedShape(board)
                && hasExpectedShape(initialBoard)
                && hasExpectedShape(solutionBoard)
                && allValuesAreInRange(board)
                && allValuesAreInRange(initialBoard)
                && allValuesAreInRange(solutionBoard)
                && fixedCellsHaveNotChanged();
    }

    @Override
    public int getCellValue(int row, int col) {
        assert isValidPosition(row, col) : "row and col must be between 0 and 8";
        return board[row][col];
    }

    @Override
    public boolean isFixedCell(int row, int col) {
        assert isValidPosition(row, col) : "row and col must be between 0 and 8";
        return initialBoard[row][col] != EMPTY_CELL;
    }

    @Override
    public boolean isEditableCell(int row, int col) {
        assert isValidPosition(row, col) : "row and col must be between 0 and 8";
        return !isFixedCell(row, col);
    }

    @Override
    public boolean setCellValue(int row, int col, int value) {
        assert invariant() : "Invariant must hold before setting a cell";

        if (!isValidPosition(row, col) || !isValidDigit(value) || initialBoard[row][col] != EMPTY_CELL) {
            return false;
        }

        int oldValue = board[row][col];

        if (oldValue == value) {
            return true;
        }

        board[row][col] = value;
        lastMove = new Move(row, col, oldValue, value);
        notifyModelChanged();

        assert board[row][col] == value : "Cell value was not updated";
        assert invariant() : "Invariant must hold after setting a cell";

        return true;
    }

    @Override
    public boolean clearCell(int row, int col) {
        assert invariant() : "Invariant must hold before clearing a cell";

        if (!isValidPosition(row, col) || initialBoard[row][col] != EMPTY_CELL) {
            return false;
        }

        int oldValue = board[row][col];

        if (oldValue == EMPTY_CELL) {
            return true;
        }

        board[row][col] = EMPTY_CELL;
        lastMove = new Move(row, col, oldValue, EMPTY_CELL);
        notifyModelChanged();

        assert board[row][col] == EMPTY_CELL : "Cell was not cleared";
        assert invariant() : "Invariant must hold after clearing a cell";

        return true;
    }

    @Override
    public boolean undo() {
        assert invariant() : "Invariant must hold before undo";

        if (lastMove == null) {
            return false;
        }

        if (initialBoard[lastMove.row][lastMove.col] != EMPTY_CELL) {
            return false;
        }

        board[lastMove.row][lastMove.col] = lastMove.previousValue;
        lastMove = null;
        notifyModelChanged();

        assert invariant() : "Invariant must hold after undo";

        return true;
    }

    @Override
    public boolean giveHint() {
        assert invariant() : "Invariant must hold before giving a hint";

        if (!hintEnabled) {
            return false;
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (initialBoard[row][col] == EMPTY_CELL && board[row][col] == EMPTY_CELL) {
                    int hintValue = solutionBoard[row][col];

                    if (!isValidDigit(hintValue)) {
                        return false;
                    }

                    board[row][col] = hintValue;
                    lastMove = new Move(row, col, EMPTY_CELL, hintValue);
                    notifyModelChanged();

                    assert invariant() : "Invariant must hold after giving a hint";
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void reset() {
        assert invariant() : "Invariant must hold before reset";

        board = copyOf(initialBoard);
        lastMove = null;
        notifyModelChanged();

        assert invariant() : "Invariant must hold after reset";
    }

    @Override
    public void newGame() {
        assert invariant() : "Invariant must hold before new game";

        loadPuzzle(selectPuzzleIndex());
        notifyModelChanged();

        assert invariant() : "Invariant must hold after new game";
    }

    @Override
    public boolean isBoardValid() {
        return isBoardValid(board);
    }

    @Override
    public boolean isBoardComplete() {
        if (!isBoardValid()) {
            return false;
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == EMPTY_CELL) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean hasConflictAt(int row, int col) {
        if (!isValidPosition(row, col)) {
            return false;
        }

        int value = board[row][col];

        if (value == EMPTY_CELL) {
            return false;
        }

        for (int c = 0; c < BOARD_SIZE; c++) {
            if (c != col && board[row][c] == value) {
                return true;
            }
        }

        for (int r = 0; r < BOARD_SIZE; r++) {
            if (r != row && board[r][col] == value) {
                return true;
            }
        }

        int boxStartRow = row - row % 3;
        int boxStartCol = col - col % 3;

        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                if ((r != row || c != col) && board[r][c] == value) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isValidationFeedbackEnabled() {
        return validationFeedbackEnabled;
    }

    @Override
    public void setValidationFeedbackEnabled(boolean enabled) {
        validationFeedbackEnabled = enabled;
        notifyModelChanged();
    }

    @Override
    public boolean isHintEnabled() {
        return hintEnabled;
    }

    @Override
    public void setHintEnabled(boolean enabled) {
        hintEnabled = enabled;
        notifyModelChanged();
    }

    @Override
    public boolean isRandomPuzzleEnabled() {
        return randomPuzzleEnabled;
    }

    @Override
    public void setRandomPuzzleEnabled(boolean enabled) {
        randomPuzzleEnabled = enabled;
        notifyModelChanged();
    }

    private void loadPuzzlesFromFile() {
        Path path = Paths.get("puzzles.txt");

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            while ((line = reader.readLine()) != null) {
                String puzzleText = line.trim();

                if (!puzzleText.isEmpty()) {
                    puzzles.add(parsePuzzle(puzzleText));
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read puzzles.txt from the project root.", exception);
        }

        if (puzzles.isEmpty()) {
            throw new IllegalStateException("puzzles.txt does not contain any valid puzzles.");
        }
    }

    private int[][] parsePuzzle(String puzzleText) {
        if (puzzleText.length() != BOARD_SIZE * BOARD_SIZE) {
            throw new IllegalArgumentException("Each puzzle must contain exactly 81 digits.");
        }

        int[][] puzzle = new int[BOARD_SIZE][BOARD_SIZE];

        for (int index = 0; index < puzzleText.length(); index++) {
            char ch = puzzleText.charAt(index);

            if (!Character.isDigit(ch)) {
                throw new IllegalArgumentException("Puzzle contains a non-digit character.");
            }

            int row = index / BOARD_SIZE;
            int col = index % BOARD_SIZE;
            puzzle[row][col] = Character.getNumericValue(ch);
        }

        return puzzle;
    }

    private int selectPuzzleIndex() {
        if (randomPuzzleEnabled) {
            return random.nextInt(puzzles.size());
        }

        return 0;
    }

    private void loadPuzzle(int puzzleIndex) {
        int[][] selectedPuzzle = puzzles.get(puzzleIndex);

        initialBoard = copyOf(selectedPuzzle);
        board = copyOf(selectedPuzzle);
        solutionBoard = copyOf(selectedPuzzle);

        if (!solveBoard(solutionBoard)) {
            solutionBoard = copyOf(selectedPuzzle);
        }

        lastMove = null;
    }

    private boolean solveBoard(int[][] workingBoard) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (workingBoard[row][col] == EMPTY_CELL) {
                    for (int value = 1; value <= 9; value++) {
                        if (canPlaceValue(workingBoard, row, col, value)) {
                            workingBoard[row][col] = value;

                            if (solveBoard(workingBoard)) {
                                return true;
                            }

                            workingBoard[row][col] = EMPTY_CELL;
                        }
                    }

                    return false;
                }
            }
        }

        return isBoardValid(workingBoard);
    }

    private boolean canPlaceValue(int[][] workingBoard, int row, int col, int value) {
        for (int c = 0; c < BOARD_SIZE; c++) {
            if (workingBoard[row][c] == value) {
                return false;
            }
        }

        for (int r = 0; r < BOARD_SIZE; r++) {
            if (workingBoard[r][col] == value) {
                return false;
            }
        }

        int boxStartRow = row - row % 3;
        int boxStartCol = col - col % 3;

        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                if (workingBoard[r][c] == value) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isBoardValid(int[][] checkedBoard) {
        return rowsAreValid(checkedBoard)
                && columnsAreValid(checkedBoard)
                && boxesAreValid(checkedBoard);
    }

    private boolean rowsAreValid(int[][] checkedBoard) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            boolean[] seen = new boolean[BOARD_SIZE + 1];

            for (int col = 0; col < BOARD_SIZE; col++) {
                int value = checkedBoard[row][col];

                if (value != EMPTY_CELL) {
                    if (seen[value]) {
                        return false;
                    }

                    seen[value] = true;
                }
            }
        }

        return true;
    }

    private boolean columnsAreValid(int[][] checkedBoard) {
        for (int col = 0; col < BOARD_SIZE; col++) {
            boolean[] seen = new boolean[BOARD_SIZE + 1];

            for (int row = 0; row < BOARD_SIZE; row++) {
                int value = checkedBoard[row][col];

                if (value != EMPTY_CELL) {
                    if (seen[value]) {
                        return false;
                    }

                    seen[value] = true;
                }
            }
        }

        return true;
    }

    private boolean boxesAreValid(int[][] checkedBoard) {
        for (int boxRow = 0; boxRow < BOARD_SIZE; boxRow += 3) {
            for (int boxCol = 0; boxCol < BOARD_SIZE; boxCol += 3) {
                if (!boxIsValid(checkedBoard, boxRow, boxCol)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean boxIsValid(int[][] checkedBoard, int boxStartRow, int boxStartCol) {
        boolean[] seen = new boolean[BOARD_SIZE + 1];

        for (int row = boxStartRow; row < boxStartRow + 3; row++) {
            for (int col = boxStartCol; col < boxStartCol + 3; col++) {
                int value = checkedBoard[row][col];

                if (value != EMPTY_CELL) {
                    if (seen[value]) {
                        return false;
                    }

                    seen[value] = true;
                }
            }
        }

        return true;
    }

    private void notifyModelChanged() {
        setChanged();
        notifyObservers();
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private boolean isValidDigit(int value) {
        return value >= 1 && value <= 9;
    }

    private boolean hasExpectedShape(int[][] array) {
        if (array == null || array.length != BOARD_SIZE) {
            return false;
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            if (array[row] == null || array[row].length != BOARD_SIZE) {
                return false;
            }
        }

        return true;
    }

    private boolean allValuesAreInRange(int[][] array) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int value = array[row][col];

                if (value < EMPTY_CELL || value > BOARD_SIZE) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean fixedCellsHaveNotChanged() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (initialBoard[row][col] != EMPTY_CELL && board[row][col] != initialBoard[row][col]) {
                    return false;
                }
            }
        }

        return true;
    }

    private int[][] copyOf(int[][] original) {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];

        for (int row = 0; row < BOARD_SIZE; row++) {
            System.arraycopy(original[row], 0, copy[row], 0, BOARD_SIZE);
        }

        return copy;
    }

    private static class Move {

        private final int row;
        private final int col;
        private final int previousValue;
        private final int newValue;

        private Move(int row, int col, int previousValue, int newValue) {
            this.row = row;
            this.col = col;
            this.previousValue = previousValue;
            this.newValue = newValue;
        }
    }
}