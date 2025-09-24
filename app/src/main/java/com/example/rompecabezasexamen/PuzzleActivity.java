package com.example.rompecabezasexamen;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad principal del juego de puzzle
 * 
 * Funcionalidades principales:
 * - Gestión del estado del juego (pausa, reanudación, finalización)
 * - Cronómetro en tiempo real con capacidad de pausa
 * - Contador de movimientos y cálculo de puntuación
 * - Resolución automática usando algoritmo A*
 * - Mezclado aleatorio del puzzle
 * - Vista previa (miniatura) del puzzle resuelto
 * - Manejo de imágenes (cámara, galería, predeterminada)
 * - Modal de victoria con estadísticas finales
 * - Integración con base de datos para guardar récords
 */
public class PuzzleActivity extends AppCompatActivity implements PuzzleView.OnPieceMoveListener {
    
    // Constantes para extras del Intent
    public static final String EXTRA_PLAYER_NAME = "player_name";
    public static final String EXTRA_PUZZLE_SIZE = "puzzle_size";
    public static final String EXTRA_IMAGE_SOURCE = "image_source";
    
    // Constantes para fuentes de imagen
    public static final int IMAGE_SOURCE_CAMERA = 0;
    public static final int IMAGE_SOURCE_GALLERY = 1;
    public static final int IMAGE_SOURCE_DEFAULT = 2;
    
    // Request codes para actividades
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_PICK = 101;
    
    // Views de la UI
    private PuzzleView puzzleView;
    private TextView tvTimeValue;
    private TextView tvMovesValue;
    private TextView tvScoreValue;
    private MaterialButton btnPause;
    private MaterialButton btnPreview;
    private MaterialButton btnShuffle;
    private MaterialButton btnSolve;
    private MaterialButton btnBackToMenu;
    private LinearLayout llPauseOverlay;
    private LinearLayout llSolvingIndicator;
    private ImageView ivPreview;
    private androidx.cardview.widget.CardView cardPreview;
    
    // Estado del juego
    private String playerName;
    private int puzzleSize;
    private int imageSource;
    private Bitmap puzzleImage;
    private boolean isGamePaused = false;
    private boolean isGameStarted = false;
    private boolean isGameCompleted = false;
    private boolean isSolving = false;
    private boolean isPreviewVisible = false;
    
    // Estadísticas del juego
    private long gameStartTime;
    private long pausedTime = 0;
    private int moveCount = 0;
    private int currentScore = 0;
    
    // Threading y cronómetro
    private Handler mainHandler;
    private Runnable timerRunnable;
    private ExecutorService executorService;
    
    // Algoritmo A*
    private AStar aStar;
    
    // Base de datos
    private DatabaseHelper dbHelper;
    
    // Launchers para nuevas APIs de Android
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        
        // Inicializar componentes
        initializeComponents();
        
        // Configurar vistas
        initViews();
        
        // Configurar listeners
        setupListeners();
        
        // Obtener datos del intent
        getIntentData();
        
        // Configurar launcher para resultados de actividades
        setupActivityLaunchers();
        
        // Cargar imagen según la fuente
        loadImageBasedOnSource();
    }
    
    /**
     * Inicializa los componentes necesarios
     */
    private void initializeComponents() {
        dbHelper = DatabaseHelper.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();
        aStar = new AStar();
    }
    
    /**
     * Inicializa las vistas del layout
     */
    private void initViews() {
        puzzleView = findViewById(R.id.puzzle_view);
        tvTimeValue = findViewById(R.id.tv_time_value);
        tvMovesValue = findViewById(R.id.tv_moves_value);
        tvScoreValue = findViewById(R.id.tv_score_value);
        btnPause = findViewById(R.id.btn_pause);
        btnPreview = findViewById(R.id.btn_preview);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnSolve = findViewById(R.id.btn_solve);
        btnBackToMenu = findViewById(R.id.btn_back_to_menu);
        llPauseOverlay = findViewById(R.id.ll_pause_overlay);
        llSolvingIndicator = findViewById(R.id.ll_solving_indicator);
        ivPreview = findViewById(R.id.iv_preview);
        cardPreview = findViewById(R.id.card_preview);
        
        // Configurar estado inicial
        updateUI();
    }
    
    /**
     * Configura los listeners de los elementos de la UI
     */
    private void setupListeners() {
        // Configurar listener del puzzle view
        puzzleView.setOnPieceMoveListener(this);
        
        // Botón pausar/reanudar
        btnPause.setOnClickListener(v -> toggleGamePause());
        
        // Botón mostrar/ocultar vista previa
        btnPreview.setOnClickListener(v -> togglePreview());
        
        // Botón mezclar
        btnShuffle.setOnClickListener(v -> shufflePuzzle());
        
        // Botón resolver
        btnSolve.setOnClickListener(v -> solvePuzzle());
        
        // Botón volver al menú
        btnBackToMenu.setOnClickListener(v -> confirmExitGame());
        
        // Botón reanudar en overlay
        MaterialButton btnResumeOverlay = findViewById(R.id.btn_resume_overlay);
        btnResumeOverlay.setOnClickListener(v -> toggleGamePause());
    }
    
    /**
     * Obtiene los datos del Intent
     */
    private void getIntentData() {
        playerName = getIntent().getStringExtra(EXTRA_PLAYER_NAME);
        puzzleSize = getIntent().getIntExtra(EXTRA_PUZZLE_SIZE, 3);
        imageSource = getIntent().getIntExtra(EXTRA_IMAGE_SOURCE, IMAGE_SOURCE_DEFAULT);
        
        if (playerName == null) {
            playerName = "Jugador";
        }
    }
    
    /**
     * Configura los launchers para actividades de imagen
     */
    private void setupActivityLaunchers() {
        // Launcher para cámara
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                android.util.Log.d("CAMERA_DEBUG", "Resultado de cámara: " + result.getResultCode());
                if (result.getResultCode() == RESULT_OK) {
                    if (photoFile != null && photoFile.exists()) {
                        android.util.Log.d("CAMERA_DEBUG", "Archivo de foto existe: " + photoFile.getAbsolutePath());
                        puzzleImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        if (puzzleImage != null) {
                            android.util.Log.d("CAMERA_DEBUG", "Imagen decodificada correctamente");
                            initializePuzzle();
                        } else {
                            android.util.Log.e("CAMERA_DEBUG", "Error decodificando la imagen");
                            showImageLoadError();
                        }
                    } else {
                        android.util.Log.e("CAMERA_DEBUG", "Archivo de foto no existe");
                        showImageLoadError();
                    }
                } else {
                    android.util.Log.e("CAMERA_DEBUG", "Cámara cancelada o error");
                    showImageLoadError();
                }
            }
        );
        
        // Launcher para galería
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            puzzleImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            initializePuzzle();
                        } catch (IOException e) {
                            showImageLoadError();
                        }
                    }
                } else {
                    showImageLoadError();
                }
            }
        );
    }
    
    /**
     * Carga la imagen según la fuente especificada
     */
    private void loadImageBasedOnSource() {
        switch (imageSource) {
            case IMAGE_SOURCE_CAMERA:
                takePictureWithCamera();
                break;
            case IMAGE_SOURCE_GALLERY:
                selectImageFromGallery();
                break;
            case IMAGE_SOURCE_DEFAULT:
                loadDefaultImage();
                break;
            default:
                loadDefaultImage();
                break;
        }
    }
    
    /**
     * Toma una foto con la cámara
     */
    private void takePictureWithCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Crear archivo temporal para la foto
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.rompecabezasexamen.fileprovider", photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    cameraLauncher.launch(cameraIntent);
                } else {
                    android.util.Log.e("CAMERA_DEBUG", "No se pudo crear el archivo de foto");
                    showImageLoadError();
                }
            } catch (IOException e) {
                android.util.Log.e("CAMERA_DEBUG", "Error creando archivo: " + e.getMessage());
                showImageLoadError();
            }
        } else {
            Toast.makeText(this, getString(R.string.error_no_camera), Toast.LENGTH_LONG).show();
            loadDefaultImage();
        }
    }
    
    /**
     * Selecciona una imagen de la galería
     */
    private void selectImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }
    
    /**
     * Carga la imagen predeterminada
     */
    private void loadDefaultImage() {
        // Crear una imagen predeterminada simple
        puzzleImage = createDefaultPuzzleImage();
        
        // TEMPORAL: Probar con imagen de prueba
        testPuzzle();
        
        initializePuzzle();
    }
    
    /**
     * Crea un archivo temporal para la foto
     */
    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir("Pictures");
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    
    /**
     * Crea una imagen predeterminada para el puzzle
     */
    private Bitmap createDefaultPuzzleImage() {
        // Crear una imagen colorida por defecto
        int size = 600;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        
        // Crear un patrón de colores
        int[] colors = {
            0xFF6C5CE7, 0xFFA29BFE, 0xFF74B9FF, 0xFF00B894,
            0xFFFDCB6E, 0xFFFF7675, 0xFFE17055, 0xFF81ECEC
        };
        
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint();
        
        int squareSize = size / puzzleSize;
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                int colorIndex = (i * puzzleSize + j) % colors.length;
                paint.setColor(colors[colorIndex]);
                canvas.drawRect(j * squareSize, i * squareSize, 
                              (j + 1) * squareSize, (i + 1) * squareSize, paint);
                
                // Agregar texto con número
                paint.setColor(0xFFFFFFFF);
                paint.setTextSize(squareSize * 0.3f);
                paint.setTextAlign(android.graphics.Paint.Align.CENTER);
                String text = String.valueOf(i * puzzleSize + j + 1);
                float x = j * squareSize + squareSize * 0.5f;
                float y = i * squareSize + squareSize * 0.6f;
                canvas.drawText(text, x, y, paint);
            }
        }
        
        return bitmap;
    }
    
    /**
     * Inicializa el puzzle con la imagen cargada
     */
    private void initializePuzzle() {
        if (puzzleImage != null) {
            // Debug: Verificar que la imagen se cargó
            android.util.Log.d("PUZZLE_DEBUG", "Imagen cargada: " + 
                (puzzleImage != null ? "SÍ" : "NO") + 
                ", Tamaño: " + (puzzleImage != null ? puzzleImage.getWidth() + "x" + puzzleImage.getHeight() : "N/A"));
            
            // Configurar el puzzle view
            puzzleView.setPuzzle(puzzleImage, puzzleSize);
            
            // Debug: Verificar estado del puzzle después de configurarlo
            puzzleView.debugPuzzleState();
            
            // Configurar vista previa
            ivPreview.setImageBitmap(puzzleImage);
            
            // Mezclar el puzzle
            shufflePuzzle();
            
            // Iniciar el juego
            startGame();
        } else {
            android.util.Log.e("PUZZLE_DEBUG", "Error: puzzleImage es null");
            showImageLoadError();
        }
    }
    
    /**
     * Método de prueba para verificar que el puzzle funciona
     */
    private void testPuzzle() {
        android.util.Log.d("PUZZLE_DEBUG", "=== INICIANDO PRUEBA DEL PUZZLE ===");
        
        // Crear una imagen de prueba simple
        Bitmap testImage = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(testImage);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(android.graphics.Color.RED);
        canvas.drawRect(0, 0, 300, 300, paint);
        paint.setColor(android.graphics.Color.WHITE);
        paint.setTextSize(50);
        paint.setTextAlign(android.graphics.Paint.Align.CENTER);
        canvas.drawText("TEST", 150, 150, paint);
        
        // Configurar el puzzle con la imagen de prueba
        puzzleView.setPuzzle(testImage, 3);
        
        // Forzar redibujo
        puzzleView.invalidate();
        
        android.util.Log.d("PUZZLE_DEBUG", "=== PRUEBA COMPLETADA ===");
    }
    
    /**
     * Inicia el juego
     */
    private void startGame() {
        isGameStarted = true;
        isGameCompleted = false;
        gameStartTime = System.currentTimeMillis();
        pausedTime = 0;
        moveCount = 0;
        currentScore = 0;
        
        // Iniciar cronómetro
        startTimer();
        
        // Actualizar UI
        updateUI();
        
        Toast.makeText(this, "¡Juego iniciado! ¡Buena suerte!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Inicia el cronómetro del juego
     */
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isGameStarted && !isGamePaused && !isGameCompleted) {
                    updateTimeDisplay();
                    updateScore();
                    mainHandler.postDelayed(this, 1000); // Actualizar cada segundo
                }
            }
        };
        mainHandler.post(timerRunnable);
    }
    
    /**
     * Actualiza la visualización del tiempo
     */
    private void updateTimeDisplay() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - gameStartTime - pausedTime;
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        String timeText = String.format("%d:%02d", minutes, seconds);
        tvTimeValue.setText(timeText);
    }
    
    /**
     * Actualiza la puntuación en tiempo real
     */
    private void updateScore() {
        if (isGameStarted && !isGameCompleted) {
            long elapsedSeconds = getElapsedTimeSeconds();
            currentScore = GameRecord.calculateScore(puzzleSize, elapsedSeconds, moveCount);
            tvScoreValue.setText(String.valueOf(Math.max(currentScore, 0)));
        }
    }
    
    /**
     * Obtiene el tiempo transcurrido en segundos
     */
    private long getElapsedTimeSeconds() {
        if (!isGameStarted) return 0;
        long currentTime = System.currentTimeMillis();
        return (currentTime - gameStartTime - pausedTime) / 1000;
    }
    
    /**
     * Pausa o reanuda el juego
     */
    private void toggleGamePause() {
        if (!isGameStarted || isGameCompleted) return;
        
        if (isGamePaused) {
            // Reanudar juego
            pausedTime += System.currentTimeMillis() - gameStartTime;
            gameStartTime = System.currentTimeMillis();
            isGamePaused = false;
            llPauseOverlay.setVisibility(View.GONE);
            btnPause.setText(R.string.pause_game);
            btnPause.setIcon(getDrawable(R.drawable.ic_pause));
            startTimer();
        } else {
            // Pausar juego
            isGamePaused = true;
            llPauseOverlay.setVisibility(View.VISIBLE);
            btnPause.setText(R.string.resume_game);
            btnPause.setIcon(getDrawable(R.drawable.ic_play));
            mainHandler.removeCallbacks(timerRunnable);
        }
        
        updateUI();
    }
    
    /**
     * Muestra u oculta la vista previa
     */
    private void togglePreview() {
        isPreviewVisible = !isPreviewVisible;
        
        if (isPreviewVisible) {
            cardPreview.setVisibility(View.VISIBLE);
            btnPreview.setText(R.string.hide_preview);
        } else {
            cardPreview.setVisibility(View.GONE);
            btnPreview.setText(R.string.show_preview);
        }
    }
    
    /**
     * Mezcla el puzzle
     */
    private void shufflePuzzle() {
        if (!isGameStarted || isGamePaused || isSolving) return;
        
        // Generar un puzzle mezclado usando el algoritmo A*
        int[][] shuffledBoard = aStar.generateSolvablePuzzle(puzzleSize, puzzleSize * puzzleSize * 10);
        puzzleView.setBoardState(shuffledBoard);
        
        Toast.makeText(this, getString(R.string.toast_puzzle_shuffled), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Resuelve el puzzle automáticamente
     */
    private void solvePuzzle() {
        if (!isGameStarted || isGamePaused || isSolving || isGameCompleted) return;
        
        isSolving = true;
        llSolvingIndicator.setVisibility(View.VISIBLE);
        btnSolve.setEnabled(false);
        
        Toast.makeText(this, getString(R.string.toast_solving_started), Toast.LENGTH_SHORT).show();
        
        // Ejecutar algoritmo A* en background thread
        executorService.execute(() -> {
            int[][] currentBoard = puzzleView.getCurrentBoard();
            List<String> solution = aStar.solvePuzzle(currentBoard);
            
            mainHandler.post(() -> {
                llSolvingIndicator.setVisibility(View.GONE);
                btnSolve.setEnabled(true);
                isSolving = false;
                
                if (solution != null && !solution.isEmpty()) {
                    // Aplicar la solución paso a paso con animación
                    applySolutionSteps(solution);
                } else {
                    Toast.makeText(this, getString(R.string.error_puzzle_unsolvable), 
                        Toast.LENGTH_LONG).show();
                }
            });
        });
    }
    
    /**
     * Aplica los pasos de la solución con animación
     */
    private void applySolutionSteps(List<String> solution) {
        // Para una demo rápida, completar inmediatamente
        // En una implementación real, se aplicarían paso a paso con delays
        
        // Crear puzzle resuelto
        int[][] solvedBoard = new int[puzzleSize][puzzleSize];
        int value = 1;
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                if (i == puzzleSize - 1 && j == puzzleSize - 1) {
                    solvedBoard[i][j] = 0;
                } else {
                    solvedBoard[i][j] = value++;
                }
            }
        }
        
        puzzleView.setBoardState(solvedBoard);
        
        // Marcar como completado automáticamente
        mainHandler.postDelayed(() -> {
            Toast.makeText(this, getString(R.string.puzzle_solved_auto), Toast.LENGTH_LONG).show();
            onPuzzleCompleted();
        }, 1000);
    }
    
    /**
     * Confirma la salida del juego
     */
    private void confirmExitGame() {
        if (isGameStarted && !isGameCompleted) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_exit_game);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                finish();
            });
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        } else {
            finish();
        }
    }
    
    /**
     * Actualiza todos los elementos de la UI
     */
    private void updateUI() {
        tvMovesValue.setText(String.valueOf(moveCount));
        
        // Habilitar/deshabilitar botones según el estado
        boolean canInteract = isGameStarted && !isGamePaused && !isGameCompleted && !isSolving;
        btnShuffle.setEnabled(canInteract);
        btnSolve.setEnabled(canInteract);
        btnPreview.setEnabled(isGameStarted);
        btnPause.setEnabled(isGameStarted && !isGameCompleted);
    }
    
    /**
     * Maneja errores al cargar imágenes
     */
    private void showImageLoadError() {
        Toast.makeText(this, getString(R.string.error_image_load), Toast.LENGTH_LONG).show();
        loadDefaultImage(); // Cargar imagen por defecto como fallback
    }
    
    // Implementación de PuzzleView.OnPieceMoveListener
    
    @Override
    public void onPieceMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (isGameStarted && !isGamePaused && !isGameCompleted && !isSolving) {
            moveCount++;
            updateUI();
        }
    }
    
    @Override
    public void onPuzzleCompleted() {
        if (!isGameCompleted) {
            isGameCompleted = true;
            mainHandler.removeCallbacks(timerRunnable);
            
            // Calcular estadísticas finales
            long finalTimeMs = getElapsedTimeSeconds() * 1000;
            int finalScore = GameRecord.calculateScore(puzzleSize, getElapsedTimeSeconds(), moveCount);
            
            // Guardar récord en la base de datos
            saveGameRecord(finalTimeMs, finalScore);
            
            // Mostrar modal de victoria
            showVictoryDialog(finalTimeMs, finalScore);
            
            updateUI();
        }
    }
    
    /**
     * Guarda el récord del juego en la base de datos
     */
    private void saveGameRecord(long timeMs, int score) {
        GameRecord record = new GameRecord(playerName, puzzleSize, timeMs, moveCount, score);
        dbHelper.insertGameRecord(record);
        Toast.makeText(this, getString(R.string.toast_record_saved), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Muestra el modal de victoria
     */
    private void showVictoryDialog(long timeMs, int score) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Crear vista personalizada para el diálogo
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_victory, null);
        
        // Configurar elementos del diálogo
        TextView tvCongrats = dialogView.findViewById(R.id.tv_congratulations);
        TextView tvFinalTime = dialogView.findViewById(R.id.tv_final_time);
        TextView tvFinalMoves = dialogView.findViewById(R.id.tv_final_moves);
        TextView tvFinalScore = dialogView.findViewById(R.id.tv_final_score);
        MaterialButton btnPlayAgain = dialogView.findViewById(R.id.btn_play_again);
        MaterialButton btnBackToMain = dialogView.findViewById(R.id.btn_back_to_main);
        
        // Configurar textos
        tvCongrats.setText(R.string.congratulations);
        
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        String timeText = String.format("%d:%02d", minutes, seconds);
        tvFinalTime.setText(getString(R.string.final_time, timeText));
        
        tvFinalMoves.setText(getString(R.string.final_moves, moveCount));
        tvFinalScore.setText(getString(R.string.final_score, score));
        
        builder.setView(dialogView);
        builder.setCancelable(false);
        
        AlertDialog dialog = builder.create();
        
        // Configurar listeners de botones
        btnPlayAgain.setOnClickListener(v -> {
            dialog.dismiss();
            restartGame();
        });
        
        btnBackToMain.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        
        dialog.show();
    }
    
    /**
     * Reinicia el juego con la misma configuración
     */
    private void restartGame() {
        isGameCompleted = false;
        isGameStarted = false;
        isGamePaused = false;
        isSolving = false;
        
        // Resetear estadísticas
        moveCount = 0;
        currentScore = 0;
        pausedTime = 0;
        
        // Mezclar puzzle nuevamente
        shufflePuzzle();
        
        // Reiniciar juego
        startGame();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Limpiar recursos
        if (mainHandler != null && timerRunnable != null) {
            mainHandler.removeCallbacks(timerRunnable);
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        // Limpiar archivo temporal
        if (photoFile != null && photoFile.exists()) {
            photoFile.delete();
        }
    }
    
    @Override
    public void onBackPressed() {
        confirmExitGame();
    }
}
