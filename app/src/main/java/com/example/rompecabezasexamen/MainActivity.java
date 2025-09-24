package com.example.rompecabezasexamen;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Actividad principal de la aplicación
 * 
 * Funcionalidades:
 * - Registro y validación del nombre del jugador
 * - Navegación al juego y ranking
 * - Gestión de permisos para cámara y almacenamiento
 * - Diálogos de ayuda
 * - Persistencia del nombre del jugador en SharedPreferences
 */
public class MainActivity extends AppCompatActivity {
    
    // Códigos de request para permisos
    private static final int PERMISSION_REQUEST_CAMERA = 100;
    private static final int PERMISSION_REQUEST_STORAGE = 101;
    
    // Preferencias compartidas
    private static final String PREFS_NAME = "PuzzleGamePrefs";
    private static final String KEY_PLAYER_NAME = "player_name";
    private static final String KEY_FIRST_TIME = "first_time";
    
    // Views
    private TextInputEditText etPlayerName;
    private TextInputLayout tilPlayerName;
    private MaterialButton btnStartGame;
    private MaterialButton btnViewRanking;
    private MaterialButton btnHelp;
    
    // Base de datos
    private DatabaseHelper dbHelper;
    
    // Estado
    private String currentPlayerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Configurar window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Inicializar base de datos
        dbHelper = DatabaseHelper.getInstance(this);
        
        // Inicializar vistas
        initViews();
        
        // Configurar listeners
        setupListeners();
        
        // Cargar datos guardados
        loadSavedData();
        
        // Mostrar ayuda si es la primera vez
        checkFirstTime();
    }
    
    /**
     * Inicializa las vistas del layout
     */
    private void initViews() {
        etPlayerName = findViewById(R.id.et_player_name);
        tilPlayerName = findViewById(R.id.til_player_name);
        btnStartGame = findViewById(R.id.btn_start_game);
        btnViewRanking = findViewById(R.id.btn_view_ranking);
        btnHelp = findViewById(R.id.btn_help);
        
        // Inicialmente deshabilitar el botón de iniciar juego
        btnStartGame.setEnabled(false);
    }
    
    /**
     * Configura los listeners de los elementos de la UI
     */
    private void setupListeners() {
        // Listener para validación del nombre en tiempo real
        etPlayerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePlayerName(s.toString().trim());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Botón iniciar juego
        btnStartGame.setOnClickListener(v -> {
            if (validateAndSavePlayerName()) {
                showDifficultySelectionDialog();
            }
        });
        
        // Botón ver ranking
        btnViewRanking.setOnClickListener(v -> {
            Intent intent = new Intent(this, RankingActivity.class);
            startActivity(intent);
        });
        
        // Botón ayuda
        btnHelp.setOnClickListener(v -> showHelpDialog());
        
    }
    
    /**
     * Valida el nombre del jugador en tiempo real
     */
    private void validatePlayerName(String name) {
        currentPlayerName = name;
        
        if (name.isEmpty()) {
            tilPlayerName.setError(null);
            btnStartGame.setEnabled(false);
            return;
        }
        
        if (name.length() < 2) {
            tilPlayerName.setError(getString(R.string.error_name_too_short));
            btnStartGame.setEnabled(false);
            return;
        }
        
        // Nombre válido
        tilPlayerName.setError(null);
        btnStartGame.setEnabled(true);
    }
    
    /**
     * Valida y guarda el nombre del jugador
     */
    private boolean validateAndSavePlayerName() {
        String name = currentPlayerName.trim();
        
        if (name.isEmpty()) {
            tilPlayerName.setError(getString(R.string.error_name_empty));
            etPlayerName.requestFocus();
            return false;
        }
        
        if (name.length() < 2) {
            tilPlayerName.setError(getString(R.string.error_name_too_short));
            etPlayerName.requestFocus();
            return false;
        }
        
        // Guardar en SharedPreferences
        savePlayerName(name);
        
        // Crear o obtener jugador en la base de datos
        if (!dbHelper.playerExists(name)) {
            Player player = new Player(name);
            dbHelper.insertPlayer(player);
        }
        
        return true;
    }
    
    /**
     * Muestra el diálogo de selección de dificultad
     */
    private void showDifficultySelectionDialog() {
        String[] difficulties = {
            getString(R.string.difficulty_2x2),
            getString(R.string.difficulty_3x3),
            getString(R.string.difficulty_4x4),
            getString(R.string.difficulty_5x5)
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_difficulty);
        builder.setItems(difficulties, (dialog, which) -> {
            int size = which + 2; // 2x2, 3x3, 4x4, 5x5
            showImageSelectionDialog(size);
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
    
    /**
     * Muestra el diálogo de selección de imagen
     */
    private void showImageSelectionDialog(int puzzleSize) {
        String[] options = {
            getString(R.string.take_photo),
            getString(R.string.select_from_gallery),
            getString(R.string.use_default_image)
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_image);
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Tomar foto
                    if (checkCameraPermission()) {
                        startPuzzleActivity(puzzleSize, PuzzleActivity.IMAGE_SOURCE_CAMERA);
                    }
                    break;
                case 1: // Seleccionar de galería
                    if (checkStoragePermission()) {
                        startPuzzleActivity(puzzleSize, PuzzleActivity.IMAGE_SOURCE_GALLERY);
                    }
                    break;
                case 2: // Imagen predeterminada
                    startPuzzleActivity(puzzleSize, PuzzleActivity.IMAGE_SOURCE_DEFAULT);
                    break;
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
    
    /**
     * Inicia la actividad del puzzle
     */
    private void startPuzzleActivity(int size, int imageSource) {
        Intent intent = new Intent(this, PuzzleActivity.class);
        intent.putExtra(PuzzleActivity.EXTRA_PLAYER_NAME, currentPlayerName);
        intent.putExtra(PuzzleActivity.EXTRA_PUZZLE_SIZE, size);
        intent.putExtra(PuzzleActivity.EXTRA_IMAGE_SOURCE, imageSource);
        startActivity(intent);
    }
    
    /**
     * Verifica y solicita permisos de cámara
     */
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                PERMISSION_REQUEST_CAMERA);
            return false;
        }
        return true;
    }
    
    /**
     * Verifica y solicita permisos de almacenamiento
     */
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            String[] permissions = {
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            };
            
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) 
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 
                        PERMISSION_REQUEST_STORAGE);
                    return false;
                }
            }
        } else {
            // Android 12 y anteriores
            String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) 
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 
                        PERMISSION_REQUEST_STORAGE);
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.error_camera_permission), 
                        Toast.LENGTH_LONG).show();
                }
                break;
                
            case PERMISSION_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.error_storage_permission), 
                        Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    
    /**
     * Muestra el diálogo de ayuda
     */
    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.help_title);
        builder.setMessage(R.string.help_content);
        builder.setPositiveButton(R.string.got_it, null);
        builder.show();
    }
    
    
    /**
     * Carga los datos guardados del jugador
     */
    private void loadSavedData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedName = prefs.getString(KEY_PLAYER_NAME, "");
        
        if (!savedName.isEmpty()) {
            etPlayerName.setText(savedName);
            etPlayerName.setSelection(savedName.length()); // Cursor al final
        }
    }
    
    /**
     * Guarda el nombre del jugador en SharedPreferences
     */
    private void savePlayerName(String name) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_PLAYER_NAME, name).apply();
    }
    
    /**
     * Verifica si es la primera vez que se abre la app
     */
    private void checkFirstTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(KEY_FIRST_TIME, true);
        
        if (isFirstTime) {
            // Marcar que ya no es la primera vez
            prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply();
            
            // Mostrar diálogo de bienvenida
            showWelcomeDialog();
        }
    }
    
    /**
     * Muestra el diálogo de bienvenida para nuevos usuarios
     */
    private void showWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Bienvenido a Puzzle Master!");
        builder.setMessage("¿Listo para el desafío? Crea puzzles deslizantes con tus propias imágenes y compite por los mejores tiempos y puntuaciones.");
        builder.setPositiveButton("¡Empezar!", null);
        builder.setNeutralButton(R.string.menu_help, (dialog, which) -> showHelpDialog());
        builder.show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Revalidar el nombre cuando se regresa a la actividad
        validatePlayerName(currentPlayerName);
    }
}