package com.oc.aoop.app;

import com.oc.aoop.controller.SudokuController;
import com.oc.aoop.model.SudokuModel;
import com.oc.aoop.model.SudokuModelInterface;
import com.oc.aoop.view.SudokuView;

import javax.swing.SwingUtilities;

/**
 * Main class for the GUI version of Sudoku.
 */
public class SudokuGUIApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SudokuModelInterface model = new SudokuModel();
            SudokuController controller = new SudokuController(model);
            new SudokuView(model, controller);
        });
    }
}