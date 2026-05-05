package com.oc.aoop.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for SudokuModel.
 * These tests focus on the Model, not the GUI or CLI.
 */
public class SudokuModelTest {

    private SudokuModel model;

    @Before
    public void setUp() {
        model = new SudokuModel();
    }

    /**
     * Scenario 1:
     * A user enters a digit into an editable empty cell.
     *
     * Precondition:
     * - The selected cell is inside the board.
     * - The selected cell is not a fixed cell.
     * - The entered value is a digit from 1 to 9.
     *
     * Expected postcondition:
     * - setCellValue returns true.
     * - The selected cell contains the entered value.
     * - The model invariant still holds.
     */
    @Test
    public void testSetEditableCellUpdatesBoard() {
        Position emptyCell = findEditableEmptyCell();
        assertNotNull("There should be at least one editable empty cell.", emptyCell);

        boolean result = model.setCellValue(emptyCell.row, emptyCell.col, 1);

        assertTrue("Editable cell should accept a valid digit.", result);
        assertEquals("The cell should contain the entered value.", 1,
                model.getCellValue(emptyCell.row, emptyCell.col));
        assertTrue("The model invariant should still hold.", model.invariant());
    }

    /**
     * Scenario 2:
     * A user tries to change a pre-filled fixed cell.
     *
     * Precondition:
     * - The selected cell is inside the board.
     * - The selected cell is fixed.
     * - The entered value is a digit from 1 to 9.
     *
     * Expected postcondition:
     * - setCellValue returns false.
     * - The fixed cell remains unchanged.
     * - The model invariant still holds.
     */
    @Test
    public void testFixedCellCannotBeModified() {
        Position fixedCell = findFixedCell();
        assertNotNull("There should be at least one fixed cell.", fixedCell);

        int originalValue = model.getCellValue(fixedCell.row, fixedCell.col);
        int attemptedValue = originalValue == 1 ? 2 : 1;

        boolean result = model.setCellValue(fixedCell.row, fixedCell.col, attemptedValue);

        assertFalse("Fixed cells must not be modified.", result);
        assertEquals("The fixed cell should keep its original value.", originalValue,
                model.getCellValue(fixedCell.row, fixedCell.col));
        assertTrue("The model invariant should still hold.", model.invariant());
    }

    /**
     * Scenario 3:
     * A user creates a temporary invalid board state by entering a duplicate value.
     *
     * Precondition:
     * - The selected cell is inside the board.
     * - The selected cell is editable and empty.
     * - The entered value is already present in the same row.
     *
     * Expected postcondition:
     * - The model allows the temporary input.
     * - hasConflictAt returns true for the changed cell.
     * - isBoardValid returns false.
     * - The model invariant still holds.
     */
    @Test
    public void testDuplicateValueCreatesConflict() {
        DuplicateScenario scenario = findDuplicateScenario();
        assertNotNull("There should be a row with an editable empty cell and an existing fixed value.", scenario);

        boolean result = model.setCellValue(scenario.emptyRow, scenario.emptyCol, scenario.duplicateValue);

        assertTrue("The model should allow temporary invalid inputs.", result);
        assertTrue("The changed cell should be marked as conflicting.",
                model.hasConflictAt(scenario.emptyRow, scenario.emptyCol));
        assertFalse("The board should not be valid after a duplicate is inserted.",
                model.isBoardValid());
        assertTrue("The model invariant should still hold.", model.invariant());
    }

    private Position findEditableEmptyCell() {
        for (int row = 0; row < SudokuModelInterface.BOARD_SIZE; row++) {
            for (int col = 0; col < SudokuModelInterface.BOARD_SIZE; col++) {
                if (model.isEditableCell(row, col)
                        && model.getCellValue(row, col) == SudokuModelInterface.EMPTY_CELL) {
                    return new Position(row, col);
                }
            }
        }

        return null;
    }

    private Position findFixedCell() {
        for (int row = 0; row < SudokuModelInterface.BOARD_SIZE; row++) {
            for (int col = 0; col < SudokuModelInterface.BOARD_SIZE; col++) {
                if (model.isFixedCell(row, col)) {
                    return new Position(row, col);
                }
            }
        }

        return null;
    }

    private DuplicateScenario findDuplicateScenario() {
        for (int row = 0; row < SudokuModelInterface.BOARD_SIZE; row++) {
            for (int emptyCol = 0; emptyCol < SudokuModelInterface.BOARD_SIZE; emptyCol++) {
                if (model.isEditableCell(row, emptyCol)
                        && model.getCellValue(row, emptyCol) == SudokuModelInterface.EMPTY_CELL) {

                    for (int valueCol = 0; valueCol < SudokuModelInterface.BOARD_SIZE; valueCol++) {
                        int value = model.getCellValue(row, valueCol);

                        if (value != SudokuModelInterface.EMPTY_CELL) {
                            return new DuplicateScenario(row, emptyCol, value);
                        }
                    }
                }
            }
        }

        return null;
    }

    private static class Position {

        private final int row;
        private final int col;

        private Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private static class DuplicateScenario {

        private final int emptyRow;
        private final int emptyCol;
        private final int duplicateValue;

        private DuplicateScenario(int emptyRow, int emptyCol, int duplicateValue) {
            this.emptyRow = emptyRow;
            this.emptyCol = emptyCol;
            this.duplicateValue = duplicateValue;
        }
    }
}