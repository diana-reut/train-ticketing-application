package com.ticketing.application;

public class SudokuSolverProgram {

    public static void main(String[] args) {
        // This is a program that solves Sudoku puzzles.

        // It contains a hardcoded puzzle, where '.' represents an empty space.
        char[][] board = {
                {'5', '3', '.', '.', '7', '.', '.', '.', '.'},
                {'6', '.', '.', '1', '9', '5', '.', '.', '.'},
                {'.', '9', '8', '.', '.', '.', '.', '6', '.'},
                {'8', '.', '.', '.', '6', '.', '.', '.', '3'},
                {'4', '.', '.', '8', '.', '3', '.', '.', '1'},
                {'7', '.', '.', '.', '2', '.', '.', '.', '6'},
                {'.', '6', '.', '.', '.', '.', '2', '8', '.'},
                {'.', '.', '.', '4', '1', '9', '.', '.', '5'},
                {'.', '.', '.', '.', '8', '.', '.', '7', '9'}
        };

        System.out.println("Initial Sudoku:");
        printBoard(board);

        if (solveSudoku(board)) {
            System.out.println();
            System.out.println("Solved Sudoku:");
            printBoard(board);
        } else {
            System.out.println("No solution exists for the given board.");
        }
    }

    public static boolean solveSudoku(char[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == '.') {
                    for (char digit = '1'; digit <= '9'; digit++) {
                        if (isValid(board, row, col, digit)) {
                            board[row][col] = digit;

                            if (solveSudoku(board)) {
                                return true;
                            }

                            board[row][col] = '.';
                        }
                    }
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isValid(char[][] board, int row, int col, char digit) {
        // each row contains 1-9 once
        // each column contains 1-9 once
        // each 3x3 box contains 1-9 once

        for (int i = 0; i < 9; i++) {
            if (board[row][i] == digit) {
                return false;
            }

            if (board[i][col] == digit) {
                return false;
            }

            int boxRow = 3 * (row / 3) + (i / 3);
            int boxCol = 3 * (col / 3) + (i % 3);
            if (board[boxRow][boxCol] == digit) {
                return false;
            }
        }

        return true;
    }

    private static void printBoard(char[][] board) {
        for (char[] row : board) {
            for (char cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }
}
