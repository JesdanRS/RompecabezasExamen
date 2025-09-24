package com.example.rompecabezasexamen;

/**
 * Modelo de datos para representar un jugador en la base de datos
 */
public class Player {
    private int id;
    private String name;
    private long createdAt;

    // Constructor vacío
    public Player() {
    }

    // Constructor con parámetros
    public Player(String name) {
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor completo
    public Player(int id, String name, long createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
