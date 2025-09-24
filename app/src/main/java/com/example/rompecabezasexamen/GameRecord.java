package com.example.rompecabezasexamen;

/**
 * Modelo de datos para representar un registro de juego completado
 */
public class GameRecord {
    private int id;
    private String playerName;
    private int difficulty; // 2, 3, 4, 5 (tamaño del grid)
    private long timeInMillis; // Tiempo en milisegundos
    private int moves; // Número de movimientos
    private int score; // Puntuación calculada
    private long completedAt; // Timestamp de cuando se completó

    // Constructor vacío
    public GameRecord() {
    }

    // Constructor con parámetros
    public GameRecord(String playerName, int difficulty, long timeInMillis, int moves, int score) {
        this.playerName = playerName;
        this.difficulty = difficulty;
        this.timeInMillis = timeInMillis;
        this.moves = moves;
        this.score = score;
        this.completedAt = System.currentTimeMillis();
    }

    // Constructor completo
    public GameRecord(int id, String playerName, int difficulty, long timeInMillis, 
                     int moves, int score, long completedAt) {
        this.id = id;
        this.playerName = playerName;
        this.difficulty = difficulty;
        this.timeInMillis = timeInMillis;
        this.moves = moves;
        this.score = score;
        this.completedAt = completedAt;
    }

    // Método para calcular la puntuación basada en tiempo y movimientos
    public static int calculateScore(int difficulty, long timeInSeconds, int moves) {
        // Puntuación base según dificultad
        int baseScore = difficulty * 1000;
        
        // Penalización por tiempo (menos puntos si toma más tiempo)
        int timePenalty = (int) (timeInSeconds * difficulty);
        
        // Penalización por movimientos (menos puntos por más movimientos)
        int movesPenalty = moves * difficulty * 10;
        
        // Calcular puntuación final (mínimo 100 puntos)
        int finalScore = Math.max(100, baseScore - timePenalty - movesPenalty);
        
        return finalScore;
    }

    // Método para obtener el tiempo formateado
    public String getFormattedTime() {
        long seconds = timeInMillis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    // Método para obtener la dificultad como string
    public String getDifficultyString() {
        return difficulty + "x" + difficulty;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "GameRecord{" +
                "id=" + id +
                ", playerName='" + playerName + '\'' +
                ", difficulty=" + difficulty +
                ", timeInMillis=" + timeInMillis +
                ", moves=" + moves +
                ", score=" + score +
                ", completedAt=" + completedAt +
                '}';
    }
}
