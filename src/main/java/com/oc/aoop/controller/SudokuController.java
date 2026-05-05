package com.oc.aoop.controller;

import com.oc.aoop.model.SudokuModelInterface;
import com.oc.aoop.view.SudokuView;

/**
 * Controller for the Sudoku GUI.
 * It converts user interactions into valid Model operations.
 */
public class SudokuController {

    private final SudokuModelInterface model;
    private SudokuView view;

    private int selectedRow = -1;
    private int selectedCol = -1;

    public SudokuController(SudokuModelInterface model) {
        this.model = model;
    }

    public void setView(SudokuView view) {
        this.view = view;
        updateViewControls();
    }

    public void selectCell(int row, int col) {
        selectedRow = row;
        selectedCol = col;

        if (view != null) {
            view.setSelectedCell(row, col);
        }

        updateViewControls();
    }

    public void enterValue(int value) {
        if (!hasSelectedCell()) {
            showMessage("Please select a cell first.");
            return;
        }

        if (value < 1 || value > 9) {
            showMessage("Only digits 1-9 are valid.");
            return;
        }

        if (model.isFixedCell(selectedRow, selectedCol)) {
            showMessage("This is a pre-filled cell and cannot be changed.");
            return;
        }

        boolean changed = model.setCellValue(selectedRow, selectedCol, value);

        if (!changed) {
            showMessage("The move was rejected.");
            return;
        }

        if (model.isValidationFeedbackEnabled()
                && model.hasConflictAt(selectedRow, selectedCol)) {
            showMessage("Invalid move: this value creates a duplicate.");
        }

        updateViewControls();
    }

    public void eraseSelectedCell() {
        if (!hasSelectedCell()) {
            showMessage("Please select a cell first.");
            return;
        }

        if (model.isFixedCell(selectedRow, selectedCol)) {
            showMessage("Pre-filled cells cannot be erased.");
            return;
        }

        boolean cleared = model.clearCell(selectedRow, selectedCol);

        if (!cleared) {
            showMessage("The selected cell could not be erased.");
        }

        updateViewControls();
    }

    public void undo() {
        boolean undone = model.undo();

        if (!undone) {
            showMessage("There is no move to undo.");
        }

        updateViewControls();
    }

    public void hint() {
        if (!model.isHintEnabled()) {
            showMessage("Hints are disabled.");
            return;
        }

        boolean hinted = model.giveHint();

        if (!hinted) {
            showMessage("No hint was added.");
        }

        updateViewControls();
    }

    public void reset() {
        model.reset();
        updateViewControls();
    }

    public void newGame() {
        model.newGame();
        selectedRow = -1;
        selectedCol = -1;

        if (view != null) {
            view.clearSelectedCell();
        }

        updateViewControls();
    }

    public void setValidationFeedbackEnabled(boolean enabled) {
        model.setValidationFeedbackEnabled(enabled);
        updateViewControls();
    }

    public void setHintEnabled(boolean enabled) {
        model.setHintEnabled(enabled);
        updateViewControls();
    }

    public void setRandomPuzzleEnabled(boolean enabled) {
        model.setRandomPuzzleEnabled(enabled);
        updateViewControls();
    }

    private boolean hasSelectedCell() {
        return selectedRow >= 0
                && selectedRow < SudokuModelInterface.BOARD_SIZE
                && selectedCol >= 0
                && selectedCol < SudokuModelInterface.BOARD_SIZE;
    }

    private boolean selectedCellIsEditable() {
        return hasSelectedCell() && model.isEditableCell(selectedRow, selectedCol);
    }

    private void updateViewControls() {
        if (view != null) {
            view.setEditingControlsEnabled(selectedCellIsEditable());
            view.setHintButtonEnabled(model.isHintEnabled());
        }
    }

    private void showMessage(String message) {
        if (view != null) {
            view.showMessage(message);
        }
    }
}