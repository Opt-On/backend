package com.opton.spring_boot.audit;

/**
 * Constraint satisfaction solver. Stores a matrix where columns represent
 * constraints and rows represent items that satisfy constraints. Each
 * element is either zero for a non-match or non-zero for a match, with
 * higher values representing a higher preference for that match. Finds
 * a maximum covering of constraints where an item can satisfy at most one
 * constraint.
 */
public class CourseAllocator {
    private final int rows; // Number of items (rows)
    private final int cols; // Number of constraints (columns)
    private final int[][] setArray; // Matrix storing match priorities
    private final int[] rowCount; // Count of matches per row
    private final int[] colCount; // Count of matches per column

    /**
     * Creates an empty matrix and initializes row and column counts.
     *
     * @param rows Number of rows (items)
     * @param cols Number of columns (constraints)
     * @throws IllegalArgumentException If rows or cols are non-positive
     */
    public CourseAllocator(int rows, int cols) {
        if (rows < 0 || cols < 0) {
            throw new IllegalArgumentException("Rows and columns must be positive.");
        }
        this.rows = rows;
        this.cols = cols;
        this.setArray = new int[rows][cols];
        this.rowCount = new int[rows];
        this.colCount = new int[cols];
    }

    /**
     * Sets a match with the given priority between an item (row) and a constraint (column).
     *
     * @param row      The row index (item)
     * @param col      The column index (constraint)
     * @param priority The priority of the match (higher values indicate higher preference)
     * @throws IllegalArgumentException If row or col is out of bounds
     */
    public void set(int row, int col, int priority) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IllegalArgumentException("Row or column index out of bounds.");
        }
        setArray[row][col] = 1 + priority; // Priority is stored as 1 + priority
        rowCount[row]++;
        colCount[col]++;
    }

    /**
     * Solves the constraint satisfaction problem by selecting one element from each row
     * to maximize column coverage. Ensures that each item satisfies at most one constraint.
     */
    public void solve() {
        boolean solved;
        do {
            boolean changes;
            do {
                changes = false;

                // Iterate through columns to find uniquely satisfied constraints
                for (int col = 0; col < cols; col++) {
                    if (colCount[col] == 1) {
                        int row = firstRow(col);
                        if (rowCount[row] != 1) {
                            // Find the column with the highest priority for this row
                            int maxCol = firstMaxCol(row, col, false);
                            selectElement(row, maxCol);
                            changes = true;
                        }
                    }
                }

                // Iterate through rows to find uniquely matched items
                for (int row = 0; row < rows; row++) {
                    if (rowCount[row] == 1) {
                        int col = firstCol(row);
                        if (colCount[col] != 1) {
                            // Find the row with the highest priority for this column
                            int maxRow = firstMaxRow(row, col, true);
                            selectElement(maxRow, col);
                            changes = true;
                        }
                    }
                }
            } while (changes);

            // Break ties for columns with multiple matches
            solved = true;
            for (int col = 0; col < cols && solved; col++) {
                if (colCount[col] > 1) {
                    int row = firstMaxRow(firstRow(col), col, false);
                    selectElement(row, col);
                    solved = false;
                }
            }
        } while (!solved);
    }

    /**
     * Retrieves the first non-zero element from a row.
     *
     * @param row The row index
     * @return The column index of the first non-zero element
     */
    public int element(int row) {
        return firstCol(row);
    }

    /**
     * Finds the first non-zero element in a column.
     *
     * @param col The column index
     * @return The row index of the first non-zero element
     */
    public int firstRow(int col) {
        for (int row = 0; row < rows; row++) {
            if (setArray[row][col] >= 1) {
                return row;
            }
        }
        return -1; // No match found
    }

    /**
     * Finds the first row with the maximum value for a given column.
     *
     * @param maxRow      The current row with the maximum value
     * @param col         The column index
     * @param rowCountOne If true, only consider rows with a count of 1
     * @return The row index with the maximum value
     */
    private int firstMaxRow(int maxRow, int col, boolean rowCountOne) {
        for (int row = 0; row < rows; row++) {
            if ((setArray[row][col] > setArray[maxRow][col]) &&
                (!rowCountOne || rowCount[row] == 1)) {
                maxRow = row;
            }
        }
        return maxRow;
    }

    /**
     * Finds the first non-zero element in a row.
     *
     * @param row The row index
     * @return The column index of the first non-zero element
     */
    private int firstCol(int row) {
        for (int col = 0; col < cols; col++) {
            if (setArray[row][col] >= 1) {
                return col;
            }
        }
        return -1; // No match found
    }

    /**
     * Finds the first column with the maximum value for a given row.
     *
     * @param row         The row index
     * @param maxCol      The current column with the maximum value
     * @param colCountOne If true, only consider columns with a count of 1
     * @return The column index with the maximum value
     */
    private int firstMaxCol(int row, int maxCol, boolean colCountOne) {
        for (int col = 0; col < cols; col++) {
            if ((setArray[row][col] > setArray[row][maxCol]) &&
                (!colCountOne || colCount[col] == 1)) {
                maxCol = col;
            }
        }
        return maxCol;
    }

    /**
     * Selects an element from the matrix by zeroing all other elements in the same row or column.
     *
     * @param row The row index of the selected element
     * @param col The column index of the selected element
     */
    private void selectElement(int row, int col) {
        // Remove other column matches for the selected row
        for (int j = 0; j < cols && rowCount[row] > 1; j++) {
            if (setArray[row][j] >= 1 && j != col) {
                setArray[row][j] = 0;
                colCount[j]--;
                rowCount[row]--;
            }
        }

        // Remove other row matches for the selected column
        for (int i = 0; i < rows && colCount[col] > 1; i++) {
            if (setArray[i][col] >= 1 && i != row) {
                setArray[i][col] = 0;
                colCount[col]--;
                rowCount[i]--;
            }
        }
    }
}