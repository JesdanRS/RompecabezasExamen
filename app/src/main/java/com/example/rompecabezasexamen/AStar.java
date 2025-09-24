package com.example.rompecabezasexamen;

import java.util.*;

/**
 * Implementación del algoritmo A* para resolver puzzles deslizantes
 * 
 * El algoritmo A* es un algoritmo de búsqueda informada que encuentra
 * el camino más corto desde un estado inicial hasta un estado objetivo.
 * 
 * Funcionamiento:
 * 1. Mantiene una lista abierta (openSet) de estados por explorar
 * 2. Mantiene una lista cerrada (closedSet) de estados ya explorados
 * 3. Para cada estado calcula f(n) = g(n) + h(n) donde:
 *    - g(n) es el costo real desde el inicio
 *    - h(n) es la heurística (distancia Manhattan)
 * 4. Siempre explora el estado con menor f(n)
 * 5. Termina cuando encuentra el estado objetivo
 */
public class AStar {
    
    private static final int MAX_ITERATIONS = 50000; // Límite de iteraciones
    private PriorityQueue<PuzzleState> openSet;      // Estados por explorar
    private Set<PuzzleState> closedSet;              // Estados ya explorados
    private List<String> solutionMoves;             // Secuencia de movimientos de la solución
    private int iterations;                          // Contador de iteraciones
    private boolean solutionFound;                   // Bandera de solución encontrada
    
    /**
     * Constructor del algoritmo A*
     */
    public AStar() {
        this.openSet = new PriorityQueue<>();
        this.closedSet = new HashSet<>();
        this.solutionMoves = new ArrayList<>();
        this.iterations = 0;
        this.solutionFound = false;
    }
    
    /**
     * Resuelve el puzzle usando el algoritmo A*
     * 
     * @param initialBoard Configuración inicial del tablero
     * @return Lista de movimientos para resolver el puzzle (null si no hay solución)
     */
    public List<String> solvePuzzle(int[][] initialBoard) {
        // Verificar si el puzzle ya está resuelto
        PuzzleState initialState = new PuzzleState(initialBoard, 0, null, null);
        if (initialState.isGoal()) {
            return new ArrayList<>(); // Ya está resuelto
        }
        
        // Verificar si el puzzle es resoluble
        if (!isSolvable(initialBoard)) {
            return null; // Puzzle no tiene solución
        }
        
        // Reiniciar estructuras de datos
        openSet.clear();
        closedSet.clear();
        solutionMoves.clear();
        iterations = 0;
        solutionFound = false;
        
        // Agregar estado inicial al conjunto abierto
        openSet.add(initialState);
        
        // Bucle principal del algoritmo A*
        while (!openSet.isEmpty() && iterations < MAX_ITERATIONS) {
            iterations++;
            
            // Obtener el estado con menor costo F
            PuzzleState current = openSet.poll();
            
            // Agregar al conjunto cerrado
            closedSet.add(current);
            
            // Verificar si alcanzamos el objetivo
            if (current.isGoal()) {
                solutionFound = true;
                reconstructPath(current);
                return solutionMoves;
            }
            
            // Explorar estados vecinos
            for (PuzzleState neighbor : current.getNeighbors()) {
                // Saltar si ya está en el conjunto cerrado
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                
                // Si no está en el conjunto abierto, agregarlo
                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                } else {
                    // Si ya está en el conjunto abierto, verificar si este camino es mejor
                    updateNeighborIfBetter(neighbor);
                }
            }
        }
        
        // No se encontró solución dentro del límite de iteraciones
        return null;
    }
    
    /**
     * Reconstruye el camino de la solución desde el estado objetivo
     * hasta el estado inicial siguiendo los padres
     */
    private void reconstructPath(PuzzleState goalState) {
        List<String> path = new ArrayList<>();
        PuzzleState current = goalState;
        
        // Seguir la cadena de padres hasta el estado inicial
        while (current.getParent() != null) {
            path.add(current.getMove());
            current = current.getParent();
        }
        
        // Invertir la lista para obtener el orden correcto
        Collections.reverse(path);
        solutionMoves = path;
    }
    
    /**
     * Actualiza un vecino en el conjunto abierto si se encuentra un mejor camino
     */
    private void updateNeighborIfBetter(PuzzleState neighbor) {
        for (PuzzleState state : openSet) {
            if (state.equals(neighbor) && neighbor.getGCost() < state.getGCost()) {
                openSet.remove(state);
                openSet.add(neighbor);
                break;
            }
        }
    }
    
    /**
     * Verifica si un puzzle es resoluble
     * 
     * Un puzzle deslizante es resoluble si:
     * - Para grid impar: número de inversiones es par
     * - Para grid par: 
     *   - Si el espacio vacío está en fila par (desde abajo): inversiones impares
     *   - Si el espacio vacío está en fila impar (desde abajo): inversiones pares
     */
    public boolean isSolvable(int[][] board) {
        int size = board.length;
        int[] flatBoard = flattenBoard(board);
        int inversions = countInversions(flatBoard);
        
        if (size % 2 == 1) {
            // Grid impar: resoluble si inversiones es par
            return inversions % 2 == 0;
        } else {
            // Grid par: lógica más compleja
            int emptyRowFromBottom = size - findEmptyRow(board);
            
            if (emptyRowFromBottom % 2 == 0) {
                return inversions % 2 == 1;
            } else {
                return inversions % 2 == 0;
            }
        }
    }
    
    /**
     * Aplana el tablero 2D en un array 1D excluyendo el espacio vacío
     */
    private int[] flattenBoard(int[][] board) {
        List<Integer> flatList = new ArrayList<>();
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0) {
                    flatList.add(board[i][j]);
                }
            }
        }
        
        return flatList.stream().mapToInt(i -> i).toArray();
    }
    
    /**
     * Cuenta el número de inversiones en el array
     * Una inversión ocurre cuando un número mayor aparece antes que uno menor
     */
    private int countInversions(int[] array) {
        int inversions = 0;
        
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (array[i] > array[j]) {
                    inversions++;
                }
            }
        }
        
        return inversions;
    }
    
    /**
     * Encuentra la fila donde está el espacio vacío (0-indexado)
     */
    private int findEmptyRow(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Genera una configuración mezclada resoluble del puzzle
     */
    public int[][] generateSolvablePuzzle(int size, int shuffleMoves) {
        // Crear puzzle resuelto
        int[][] solved = createSolvedPuzzle(size);
        
        // Mezclar haciendo movimientos aleatorios válidos
        PuzzleState current = new PuzzleState(solved, 0, null, null);
        Random random = new Random();
        
        for (int i = 0; i < shuffleMoves; i++) {
            List<PuzzleState> neighbors = current.getNeighbors();
            if (!neighbors.isEmpty()) {
                current = neighbors.get(random.nextInt(neighbors.size()));
            }
        }
        
        return current.getBoard();
    }
    
    /**
     * Crea un puzzle resuelto de tamaño específico
     */
    private int[][] createSolvedPuzzle(int size) {
        int[][] puzzle = new int[size][size];
        int value = 1;
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == size - 1 && j == size - 1) {
                    puzzle[i][j] = 0; // Espacio vacío en la última posición
                } else {
                    puzzle[i][j] = value++;
                }
            }
        }
        
        return puzzle;
    }
    
    /**
     * Obtiene información sobre la última ejecución del algoritmo
     */
    public String getExecutionInfo() {
        return String.format("Iteraciones: %d, Solución encontrada: %s, Movimientos: %d",
                iterations, solutionFound, solutionMoves.size());
    }
    
    // Getters para información adicional
    public int getIterations() {
        return iterations;
    }
    
    public boolean isSolutionFound() {
        return solutionFound;
    }
    
    public List<String> getSolutionMoves() {
        return new ArrayList<>(solutionMoves);
    }
    
    public int getSolutionLength() {
        return solutionMoves.size();
    }
}
