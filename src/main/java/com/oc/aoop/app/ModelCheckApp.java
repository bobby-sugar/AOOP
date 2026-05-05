package com.oc.aoop.app;

import com.oc.aoop.model.SudokuModel;

public class ModelCheckApp {

    public static void main(String[] args) {
        SudokuModel model = new SudokuModel();

        System.out.println("First cell value: " + model.getCellValue(0, 0));
        System.out.println("Board valid: " + model.isBoardValid());
        System.out.println("Board complete: " + model.isBoardComplete());
        System.out.println("Invariant: " + model.invariant());
    }
}