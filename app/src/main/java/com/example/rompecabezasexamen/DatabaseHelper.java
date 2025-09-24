package com.example.rompecabezasexamen;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper de base de datos SQLite para manejar jugadores y registros de juego
 * Implementa patrón Singleton para una única instancia de la base de datos
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    // Configuración de la base de datos
    private static final String DATABASE_NAME = "puzzle_game.db";
    private static final int DATABASE_VERSION = 1;
    
    // Tabla de jugadores
    private static final String TABLE_PLAYERS = "players";
    private static final String COLUMN_PLAYER_ID = "id";
    private static final String COLUMN_PLAYER_NAME = "name";
    private static final String COLUMN_PLAYER_CREATED_AT = "created_at";
    
    // Tabla de registros de juego
    private static final String TABLE_GAME_RECORDS = "game_records";
    private static final String COLUMN_RECORD_ID = "id";
    private static final String COLUMN_RECORD_PLAYER_NAME = "player_name";
    private static final String COLUMN_RECORD_DIFFICULTY = "difficulty";
    private static final String COLUMN_RECORD_TIME = "time_in_millis";
    private static final String COLUMN_RECORD_MOVES = "moves";
    private static final String COLUMN_RECORD_SCORE = "score";
    private static final String COLUMN_RECORD_COMPLETED_AT = "completed_at";
    
    // Instancia singleton
    private static DatabaseHelper instance;
    
    // Constructor privado para singleton
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    // Método para obtener instancia singleton
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de jugadores
        String createPlayersTable = "CREATE TABLE " + TABLE_PLAYERS + "(" +
                COLUMN_PLAYER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_PLAYER_NAME + " TEXT NOT NULL UNIQUE," +
                COLUMN_PLAYER_CREATED_AT + " INTEGER NOT NULL" +
                ")";
        
        // Crear tabla de registros de juego
        String createRecordsTable = "CREATE TABLE " + TABLE_GAME_RECORDS + "(" +
                COLUMN_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_RECORD_PLAYER_NAME + " TEXT NOT NULL," +
                COLUMN_RECORD_DIFFICULTY + " INTEGER NOT NULL," +
                COLUMN_RECORD_TIME + " INTEGER NOT NULL," +
                COLUMN_RECORD_MOVES + " INTEGER NOT NULL," +
                COLUMN_RECORD_SCORE + " INTEGER NOT NULL," +
                COLUMN_RECORD_COMPLETED_AT + " INTEGER NOT NULL" +
                ")";
        
        db.execSQL(createPlayersTable);
        db.execSQL(createRecordsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Eliminar tablas existentes si la versión de la base de datos se actualiza
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);
        onCreate(db);
    }
    
    // ===== MÉTODOS PARA JUGADORES =====
    
    /**
     * Insertar un nuevo jugador en la base de datos
     */
    public long insertPlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_PLAYER_NAME, player.getName());
        values.put(COLUMN_PLAYER_CREATED_AT, player.getCreatedAt());
        
        long id = db.insert(TABLE_PLAYERS, null, values);
        db.close();
        return id;
    }
    
    /**
     * Obtener jugador por nombre
     */
    public Player getPlayerByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Player player = null;
        
        Cursor cursor = db.query(TABLE_PLAYERS,
                new String[]{COLUMN_PLAYER_ID, COLUMN_PLAYER_NAME, COLUMN_PLAYER_CREATED_AT},
                COLUMN_PLAYER_NAME + "=?",
                new String[]{name}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            player = new Player(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_NAME)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_CREATED_AT))
            );
            cursor.close();
        }
        
        db.close();
        return player;
    }
    
    /**
     * Verificar si un jugador existe por nombre
     */
    public boolean playerExists(String name) {
        return getPlayerByName(name) != null;
    }
    
    /**
     * Obtener todos los jugadores
     */
    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_PLAYERS, null, null, null, null, null, 
                COLUMN_PLAYER_CREATED_AT + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Player player = new Player(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_NAME)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_CREATED_AT))
                );
                players.add(player);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return players;
    }
    
    // ===== MÉTODOS PARA REGISTROS DE JUEGO =====
    
    /**
     * Insertar un nuevo registro de juego
     */
    public long insertGameRecord(GameRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_RECORD_PLAYER_NAME, record.getPlayerName());
        values.put(COLUMN_RECORD_DIFFICULTY, record.getDifficulty());
        values.put(COLUMN_RECORD_TIME, record.getTimeInMillis());
        values.put(COLUMN_RECORD_MOVES, record.getMoves());
        values.put(COLUMN_RECORD_SCORE, record.getScore());
        values.put(COLUMN_RECORD_COMPLETED_AT, record.getCompletedAt());
        
        long id = db.insert(TABLE_GAME_RECORDS, null, values);
        db.close();
        return id;
    }
    
    /**
     * Obtener todos los registros ordenados por puntuación descendente
     */
    public List<GameRecord> getAllGameRecords() {
        return getGameRecords("ORDER BY " + COLUMN_RECORD_SCORE + " DESC");
    }
    
    /**
     * Obtener registros filtrados por nombre de jugador
     */
    public List<GameRecord> getGameRecordsByPlayer(String playerName) {
        return getGameRecords("WHERE " + COLUMN_RECORD_PLAYER_NAME + " = '" + playerName + 
                "' ORDER BY " + COLUMN_RECORD_SCORE + " DESC");
    }
    
    /**
     * Obtener registros ordenados por tiempo ascendente
     */
    public List<GameRecord> getGameRecordsByTime() {
        return getGameRecords("ORDER BY " + COLUMN_RECORD_TIME + " ASC");
    }
    
    /**
     * Obtener registros ordenados por movimientos ascendente
     */
    public List<GameRecord> getGameRecordsByMoves() {
        return getGameRecords("ORDER BY " + COLUMN_RECORD_MOVES + " ASC");
    }
    
    /**
     * Método privado para obtener registros con filtros/ordenamiento personalizado
     */
    private List<GameRecord> getGameRecords(String whereOrderClause) {
        List<GameRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_GAME_RECORDS + " " + whereOrderClause;
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                GameRecord record = new GameRecord(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_PLAYER_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_DIFFICULTY)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RECORD_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_MOVES)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_SCORE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RECORD_COMPLETED_AT))
                );
                records.add(record);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return records;
    }
    
    /**
     * Obtener el mejor puntaje para una dificultad específica
     */
    public GameRecord getBestScoreForDifficulty(int difficulty) {
        SQLiteDatabase db = this.getReadableDatabase();
        GameRecord bestRecord = null;
        
        String query = "SELECT * FROM " + TABLE_GAME_RECORDS + 
                " WHERE " + COLUMN_RECORD_DIFFICULTY + " = ? " +
                " ORDER BY " + COLUMN_RECORD_SCORE + " DESC LIMIT 1";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(difficulty)});
        
        if (cursor != null && cursor.moveToFirst()) {
            bestRecord = new GameRecord(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_PLAYER_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_DIFFICULTY)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RECORD_TIME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_MOVES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_SCORE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RECORD_COMPLETED_AT))
            );
            cursor.close();
        }
        
        db.close();
        return bestRecord;
    }
    
    /**
     * Limpiar todos los datos de la base de datos
     */
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GAME_RECORDS, null, null);
        db.delete(TABLE_PLAYERS, null, null);
        db.close();
    }
}
