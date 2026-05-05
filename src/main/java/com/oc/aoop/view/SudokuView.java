package com.oc.aoop.view;

import com.oc.aoop.controller.SudokuController;
import com.oc.aoop.model.SudokuModelInterface;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

/**
 * Swing GUI View for Sudoku.
 * This class displays the board and sends user interactions to the Controller.
 */
@SuppressWarnings("deprecation")
public class SudokuView extends JFrame implements Observer {

    private static final int CELL_SIZE = 48;

    private final SudokuModelInterface model;
    private final SudokuController controller;

    private final JTextField[][] cellFields = new JTextField[SudokuModelInterface.BOARD_SIZE][SudokuModelInterface.BOARD_SIZE];

    private final JButton eraseButton = new JButton("Erase");
    private final JButton undoButton = new JButton("Undo");
    private final JButton hintButton = new JButton("Hint");
    private final JButton resetButton = new JButton("Reset");
    private final JButton newGameButton = new JButton("New Game");

    private final JCheckBox validationCheckBox = new JCheckBox("Validation Feedback");
    private final JCheckBox hintCheckBox = new JCheckBox("Hint");
    private final JCheckBox randomPuzzleCheckBox = new JCheckBox("Random Puzzle");

    private int selectedRow = -1;
    private int selectedCol = -1;

    private boolean updatingFromModel;
    private boolean completionMessageShown;

    public SudokuView(SudokuModelInterface model, SudokuController controller) {
        this.model = model;
        this.controller = controller;

        model.addObserver(this);
        controller.setView(this);

        createControls();
        update(null, null);
    }

    public void setSelectedCell(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        refreshBoard();
    }

    public void clearSelectedCell() {
        selectedRow = -1;
        selectedCol = -1;
        refreshBoard();
    }

    public void setEditingControlsEnabled(boolean enabled) {
        eraseButton.setEnabled(enabled);
    }

    public void setHintButtonEnabled(boolean enabled) {
        hintButton.setEnabled(enabled);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    @Override
    public void update(Observable observable, Object argument) {
        updatingFromModel = true;

        validationCheckBox.setSelected(model.isValidationFeedbackEnabled());
        hintCheckBox.setSelected(model.isHintEnabled());
        randomPuzzleCheckBox.setSelected(model.isRandomPuzzleEnabled());

        updatingFromModel = false;

        refreshBoard();

        if (model.isBoardComplete() && !completionMessageShown) {
            completionMessageShown = true;
            JOptionPane.showMessageDialog(this, "Congratulations! The Sudoku puzzle is correctly completed.");
        }

        if (!model.isBoardComplete()) {
            completionMessageShown = false;
        }
    }

    private void createControls() {
        setTitle("AOOP Sudoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(createBoardPanel(), BorderLayout.CENTER);
        add(createSidePanel(), BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createBoardPanel() {
        JPanel boardPanel = new JPanel(new GridLayout(9, 9));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int row = 0; row < SudokuModelInterface.BOARD_SIZE; row++) {
            for (int col = 0; col < SudokuModelInterface.BOARD_SIZE; col++) {
                JTextField cellField = createCellField(row, col);
                cellFields[row][col] = cellField;
                boardPanel.add(cellField);
            }
        }

        return boardPanel;
    }

    private JTextField createCellField(int row, int col) {
        JTextField field = new JTextField();
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setFont(new Font("Arial", Font.PLAIN, 22));
        field.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
        field.setEditable(false);
        field.setFocusable(true);
        field.setBorder(createCellBorder(row, col));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                controller.selectCell(row, col);
            }
        });

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                handleCellKeyPress(event, row, col);
            }
        });

        return field;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        sidePanel.add(createButtonPanel());
        sidePanel.add(createNumberKeyboardPanel());
        sidePanel.add(createFlagPanel());

        return sidePanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5));

        eraseButton.addActionListener(event -> controller.eraseSelectedCell());
        undoButton.addActionListener(event -> controller.undo());
        hintButton.addActionListener(event -> controller.hint());
        resetButton.addActionListener(event -> controller.reset());
        newGameButton.addActionListener(event -> controller.newGame());

        buttonPanel.add(eraseButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(hintButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(newGameButton);

        return buttonPanel;
    }

    private JPanel createNumberKeyboardPanel() {
        JPanel keyboardPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        keyboardPanel.setBorder(BorderFactory.createTitledBorder("Numbers"));

        for (int value = 1; value <= 9; value++) {
            int digit = value;
            JButton numberButton = new JButton(String.valueOf(digit));
            numberButton.addActionListener(event -> controller.enterValue(digit));
            keyboardPanel.add(numberButton);
        }

        return keyboardPanel;
    }

    private JPanel createFlagPanel() {
        JPanel flagPanel = new JPanel(new GridLayout(3, 1));
        flagPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        validationCheckBox.addActionListener(event -> {
            if (!updatingFromModel) {
                controller.setValidationFeedbackEnabled(validationCheckBox.isSelected());
            }
        });

        hintCheckBox.addActionListener(event -> {
            if (!updatingFromModel) {
                controller.setHintEnabled(hintCheckBox.isSelected());
            }
        });

        randomPuzzleCheckBox.addActionListener(event -> {
            if (!updatingFromModel) {
                controller.setRandomPuzzleEnabled(randomPuzzleCheckBox.isSelected());
            }
        });

        flagPanel.add(validationCheckBox);
        flagPanel.add(hintCheckBox);
        flagPanel.add(randomPuzzleCheckBox);

        return flagPanel;
    }

    private void handleCellKeyPress(KeyEvent event, int row, int col) {
        int keyCode = event.getKeyCode();

        if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
            controller.enterValue(keyCode - KeyEvent.VK_0);
        } else if (keyCode >= KeyEvent.VK_NUMPAD1 && keyCode <= KeyEvent.VK_NUMPAD9) {
            controller.enterValue(keyCode - KeyEvent.VK_NUMPAD0);
        } else if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
            controller.eraseSelectedCell();
        } else if (keyCode == KeyEvent.VK_UP) {
            focusCell(Math.max(0, row - 1), col);
        } else if (keyCode == KeyEvent.VK_DOWN) {
            focusCell(Math.min(8, row + 1), col);
        } else if (keyCode == KeyEvent.VK_LEFT) {
            focusCell(row, Math.max(0, col - 1));
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            focusCell(row, Math.min(8, col + 1));
        }
    }

    private void focusCell(int row, int col) {
        cellFields[row][col].requestFocusInWindow();
        controller.selectCell(row, col);
    }

    private void refreshBoard() {
        for (int row = 0; row < SudokuModelInterface.BOARD_SIZE; row++) {
            for (int col = 0; col < SudokuModelInterface.BOARD_SIZE; col++) {
                refreshCell(row, col);
            }
        }
    }

    private void refreshCell(int row, int col) {
        JTextField field = cellFields[row][col];
        int value = model.getCellValue(row, col);

        field.setText(value == SudokuModelInterface.EMPTY_CELL ? "" : String.valueOf(value));
        field.setBorder(createCellBorder(row, col));

        if (model.isFixedCell(row, col)) {
            field.setFont(new Font("Arial", Font.BOLD, 22));
            field.setBackground(new Color(220, 220, 220));
        } else {
            field.setFont(new Font("Arial", Font.PLAIN, 22));
            field.setBackground(Color.WHITE);
        }

        if (model.isValidationFeedbackEnabled() && model.hasConflictAt(row, col)) {
            field.setBackground(new Color(255, 190, 190));
        }

        if (row == selectedRow && col == selectedCol) {
            field.setBackground(new Color(210, 230, 255));
        }
    }

    private javax.swing.border.Border createCellBorder(int row, int col) {
        int top = row % 3 == 0 ? 3 : 1;
        int left = col % 3 == 0 ? 3 : 1;
        int bottom = row == 8 ? 3 : 1;
        int right = col == 8 ? 3 : 1;

        return BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK);
    }
}