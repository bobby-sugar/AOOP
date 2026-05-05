package com.oc.aoop.model;

import java.util.Observer;

/**
 * Interface for the Sudoku model.
 * GUI, CLI, Controller and JUnit tests should depend on this interface.
 */
public interface SudokuModelInterface {

    int BOARD_SIZE = 9;
    int EMPTY_CELL = 0;

    void addObserver(Observer observer);

    int getCellValue(int row, int col);

    boolean isFixedCell(int row, int col);

    boolean isEditableCell(int row, int col);

    boolean setCellValue(int row, int col, int value);

    boolean clearCell(int row, int col);

    boolean undo();

    boolean giveHint();

    void reset();

    void newGame();

    boolean isBoardValid();

    boolean isBoardComplete();

    boolean hasConflictAt(int row, int col);

    boolean isValidationFeedbackEnabled();

    void setValidationFeedbackEnabled(boolean enabled);

    boolean isHintEnabled();

    void setHintEnabled(boolean enabled);

    boolean isRandomPuzzleEnabled();

    void setRandomPuzzleEnabled(boolean enabled);
}