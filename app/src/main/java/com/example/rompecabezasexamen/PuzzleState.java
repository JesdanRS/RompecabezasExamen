package com.example.rompecabezasexamen;

import java.util.Arrays;

/**
 * Representa un estado del puzzle deslizante para el algoritmo A*
 * Cada estado contiene la configuración actual del tablero y métodos
 * para calcular la heurística de Manhattan
 */
public class PuzzleState implements Comparable<PuzzleState> {
    private int[][] board;           // Configuración actual del tablero
    private int size;                // Tamaño del tablero (n x n)
    private int emptyRow, emptyCol;  // Posición del espacio vacío
    private int gCost;               // Costo desde el estado inicial
    private int hCost;               // Heurística (distancia Manhattan)
    private int fCost;               // Costo total (g + h)
    private PuzzleState parent;      // Estado padre para reconstruir la solución
    private String move;             // Movimiento que llevó a este estado
    
    /**
     * Constructor para crear un estado del puzzle
     */
    public PuzzleState(int[][] board, int gCost, PuzzleState parent, String move) {
        this.size = board.length;
        this.board = new int[size][size];
        this.gCost = gCost;
        this.parent = parent;
        this.move = move;
        
        // Copiar el tablero y encontrar la posición vacía
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.board[i][j] = board[i][j];
                if (board[i][j] == 0) {
                    this.emptyRow = i;
                    this.emptyCol = j;
                }
            }
        }
        
        // Calcular la heurística y el costo total
        this.hCost = calculateManhattanDistance();
        this.fCost = this.gCost + this.hCost;
    }
    
    /**
     * Calcula la distancia Manhattan para cada pieza
     * La distancia Manhattan es la suma de las distancias horizontales y verticales
     * desde la posición actual hasta la posición objetivo
     */
    private int calculateManhattanDistance() {
        int distance = 0;
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = board[i][j];
                if (value != 0) { // Ignorar el espacio vacío
                    // Calcular la posición objetivo para este valor
                    int targetRow = (value - 1) / size;
                    int targetCol = (value - 1) % size;
                    
                    // Sumar la distancia Manhattan
                    distance += Math.abs(i - targetRow) + Math.abs(j - targetCol);
                }
            }
        }
        
        return distance;
    }
    
    /**
     * Verifica si este estado es el estado objetivo (puzzle resuelto)
     */
    public boolean isGoal() {
        int expectedValue = 1;
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == size - 1 && j == size - 1) {
                    // La última posición debe estar vacía (0)
                    if (board[i][j] != 0) return false;
                } else {
                    // Las demás posiciones deben tener el valor esperado
                    if (board[i][j] != expectedValue) return false;
                    expectedValue++;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Genera todos los estados vecinos posibles moviendo el espacio vacío
     */
    public java.util.List<PuzzleState> getNeighbors() {
        java.util.List<PuzzleState> neighbors = new java.util.ArrayList<>();
        
        // Direcciones posibles: arriba, abajo, izquierda, derecha
        int[] rowMoves = {-1, 1, 0, 0};
        int[] colMoves = {0, 0, -1, 1};
        String[] moveNames = {"ARRIBA", "ABAJO", "IZQUIERDA", "DERECHA"};
        
        for (int i = 0; i < 4; i++) {
            int newRow = emptyRow + rowMoves[i];
            int newCol = emptyCol + colMoves[i];
            
            // Verificar si el movimiento es válido
            if (isValidPosition(newRow, newCol)) {
                // Crear nuevo tablero con el movimiento
                int[][] newBoard = copyBoard();
                
                // Intercambiar el espacio vacío con la pieza
                newBoard[emptyRow][emptyCol] = newBoard[newRow][newCol];
                newBoard[newRow][newCol] = 0;
                
                // Crear nuevo estado
                PuzzleState neighbor = new PuzzleState(newBoard, gCost + 1, this, moveNames[i]);
                neighbors.add(neighbor);
            }
        }
        
        return neighbors;
    }
    
    /**
     * Verifica si una posición es válida en el tablero
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }
    
    /**
     * Crea una copia del tablero actual
     */
    private int[][] copyBoard() {
        int[][] copy = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, size);
        }
        return copy;
    }
    
    /**
     * Compara estados basándose en el costo F para la cola de prioridad
     */
    @Override
    public int compareTo(PuzzleState other) {
        if (this.fCost != other.fCost) {
            return Integer.compare(this.fCost, other.fCost);
        }
        // Si los costos F son iguales, priorizar menor costo H
        return Integer.compare(this.hCost, other.hCost);
    }
    
    /**
     * Verifica si dos estados son iguales comparando sus tableros
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PuzzleState that = (PuzzleState) obj;
        return Arrays.deepEquals(this.board, that.board);
    }
    
    /**
     * Genera un hash code basado en el tablero
     */
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
    
    /**
     * Representación en string del estado para debugging
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sb.append(String.format("%3d", board[i][j]));
            }
            sb.append("\n");
        }
        sb.append("gCost: ").append(gCost).append(", hCost: ").append(hCost).append(", fCost: ").append(fCost);
        return sb.toString();
    }
    
    // Getters
    public int[][] getBoard() {
        return copyBoard();
    }
    
    public int getSize() {
        return size;
    }
    
    public int getEmptyRow() {
        return emptyRow;
    }
    
    public int getEmptyCol() {
        return emptyCol;
    }
    
    public int getGCost() {
        return gCost;
    }
    
    public int getHCost() {
        return hCost;
    }
    
    public int getFCost() {
        return fCost;
    }
    
    public PuzzleState getParent() {
        return parent;
    }
    
    public String getMove() {
        return move;
    }
}
