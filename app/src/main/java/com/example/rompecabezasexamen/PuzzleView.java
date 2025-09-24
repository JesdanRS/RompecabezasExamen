package com.example.rompecabezasexamen;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.Nullable;

/**
 * Vista personalizada para renderizar y manejar la interacción del puzzle deslizante
 * 
 * Características:
 * - Renderizado dinámico de piezas con imagen de fondo
 * - Animaciones suaves para movimientos de piezas
 * - Detección de toques y gestos para mover piezas
 * - Escalado automático según el tamaño del dispositivo
 * - Efectos visuales modernos con sombras y bordes redondeados
 */
public class PuzzleView extends View {
    
    // Configuración visual
    private static final int PIECE_MARGIN = 4;           // Margen entre piezas
    private static final int PIECE_RADIUS = 12;          // Radio de esquinas redondeadas
    private static final int SHADOW_OFFSET = 6;          // Desplazamiento de sombra
    private static final int ANIMATION_DURATION = 200;   // Duración de animaciones en ms
    
    // Estado del puzzle
    private int[][] puzzleBoard;      // Configuración actual del tablero
    private int puzzleSize;           // Tamaño del puzzle (n x n)
    private int emptyRow, emptyCol;   // Posición del espacio vacío
    
    // Imagen del puzzle
    private Bitmap originalImage;     // Imagen original completa
    private Bitmap[] pieceBitmaps;    // Array de imágenes de cada pieza
    
    // Dimensiones y posicionamiento
    private float pieceSize;          // Tamaño de cada pieza en pixels
    private float boardStartX;        // Posición X donde inicia el tablero
    private float boardStartY;        // Posición Y donde inicia el tablero
    private float boardSize;          // Tamaño total del tablero
    
    // Objetos de dibujo
    private Paint piecePaint;         // Paint para las piezas
    private Paint shadowPaint;        // Paint para las sombras
    private Paint borderPaint;        // Paint para los bordes
    private Paint emptyPaint;         // Paint para el espacio vacío
    private Paint numberPaint;        // Paint para números (modo debug)
    
    // Animación
    private ValueAnimator currentAnimator;
    private float animationProgress = 0f;
    private int animatingPiece = -1;
    private float animStartX, animStartY, animEndX, animEndY;
    
    // Configuración
    private boolean showNumbers = false;    // Mostrar números en lugar de imagen
    private boolean animationsEnabled = true;
    
    // Listener para movimientos
    private OnPieceMoveListener moveListener;
    
    /**
     * Interface para notificar movimientos de piezas
     */
    public interface OnPieceMoveListener {
        void onPieceMove(int fromRow, int fromCol, int toRow, int toCol);
        void onPuzzleCompleted();
    }
    
    // Constructores
    public PuzzleView(Context context) {
        super(context);
        init();
    }
    
    public PuzzleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public PuzzleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    /**
     * Inicialización de la vista
     */
    private void init() {
        // Configurar paints
        piecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        piecePaint.setFilterBitmap(true);
        
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.argb(100, 0, 0, 0));
        
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        
        emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setColor(Color.argb(50, 255, 255, 255));
        emptyPaint.setStyle(Paint.Style.FILL);
        
        numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        numberPaint.setColor(Color.WHITE);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTypeface(Typeface.DEFAULT_BOLD);
        numberPaint.setShadowLayer(4, 2, 2, Color.BLACK);
    }
    
    /**
     * Configura el puzzle con una nueva imagen y tamaño
     */
    public void setPuzzle(Bitmap image, int size) {
        android.util.Log.d("PUZZLE_VIEW", "setPuzzle llamado - Imagen: " + 
            (image != null ? "SÍ" : "NO") + ", Tamaño: " + size);
        
        this.originalImage = image;
        this.puzzleSize = size;
        this.puzzleBoard = new int[size][size];
        
        // Inicializar tablero resuelto
        initializeSolvedBoard();
        
        // Crear piezas de la imagen
        createPieceBitmaps();
        
        // Configurar modo según si tenemos imágenes
        this.showNumbers = (pieceBitmaps == null || pieceBitmaps.length == 0);
        android.util.Log.d("PUZZLE_VIEW", "Modo configurado - showNumbers: " + showNumbers);
        
        // Recalcular dimensiones ahora que tenemos puzzleSize
        if (getWidth() > 0 && getHeight() > 0) {
            calculateDimensions(getWidth(), getHeight());
        }
        
        // Solicitar redibujo
        invalidate();
        
        android.util.Log.d("PUZZLE_VIEW", "Puzzle configurado correctamente");
    }
    
    /**
     * Configura el estado del tablero
     */
    public void setBoardState(int[][] board) {
        if (board.length != puzzleSize) return;
        
        for (int i = 0; i < puzzleSize; i++) {
            System.arraycopy(board[i], 0, puzzleBoard[i], 0, puzzleSize);
        }
        
        // Encontrar posición del espacio vacío
        findEmptyPosition();
        invalidate();
    }
    
    /**
     * Inicializa el tablero en estado resuelto
     */
    private void initializeSolvedBoard() {
        int value = 1;
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                if (i == puzzleSize - 1 && j == puzzleSize - 1) {
                    puzzleBoard[i][j] = 0; // Espacio vacío
                    emptyRow = i;
                    emptyCol = j;
                } else {
                    puzzleBoard[i][j] = value++;
                }
            }
        }
    }
    
    /**
     * Crea las imágenes de cada pieza a partir de la imagen original
     */
    private void createPieceBitmaps() {
        if (originalImage == null) {
            android.util.Log.e("PUZZLE_VIEW", "Error: originalImage es null en createPieceBitmaps");
            return;
        }
        
        android.util.Log.d("PUZZLE_VIEW", "Creando piezas - Imagen original: " + 
            originalImage.getWidth() + "x" + originalImage.getHeight() + 
            ", Tamaño puzzle: " + puzzleSize);
        
        pieceBitmaps = new Bitmap[puzzleSize * puzzleSize];
        
        // Crear bitmap cuadrado de la imagen original
        Bitmap squareImage = createSquareBitmap(originalImage);
        int imageSize = squareImage.getWidth();
        int pieceImageSize = imageSize / puzzleSize;
        
        android.util.Log.d("PUZZLE_VIEW", "Imagen cuadrada: " + imageSize + 
            "x" + imageSize + ", Tamaño pieza: " + pieceImageSize);
        
        // Cortar cada pieza
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                int value = i * puzzleSize + j + 1;
                if (value <= puzzleSize * puzzleSize - 1) { // Excluir el espacio vacío
                    int x = j * pieceImageSize;
                    int y = i * pieceImageSize;
                    
                    try {
                        Bitmap piece = Bitmap.createBitmap(squareImage, x, y, 
                                                         pieceImageSize, pieceImageSize);
                        pieceBitmaps[value - 1] = piece;
                        android.util.Log.d("PUZZLE_VIEW", "Pieza " + value + " creada: " + 
                            piece.getWidth() + "x" + piece.getHeight());
                    } catch (Exception e) {
                        android.util.Log.e("PUZZLE_VIEW", "Error creando pieza " + value + ": " + e.getMessage());
                    }
                }
            }
        }
        
        android.util.Log.d("PUZZLE_VIEW", "Piezas creadas: " + pieceBitmaps.length);
    }
    
    /**
     * Convierte cualquier imagen en un bitmap cuadrado
     */
    private Bitmap createSquareBitmap(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        
        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);
        return Bitmap.createScaledBitmap(squared, 800, 800, true);
    }
    
    /**
     * Encuentra la posición del espacio vacío
     */
    private void findEmptyPosition() {
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                if (puzzleBoard[i][j] == 0) {
                    emptyRow = i;
                    emptyCol = j;
                    return;
                }
            }
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        android.util.Log.d("PUZZLE_VIEW", "onSizeChanged - w: " + w + ", h: " + h + ", puzzleSize: " + puzzleSize);
        
        // Solo calcular dimensiones si el puzzle está configurado
        if (puzzleSize > 0) {
            calculateDimensions(w, h);
        } else {
            android.util.Log.w("PUZZLE_VIEW", "onSizeChanged llamado antes de configurar puzzleSize, ignorando");
        }
        
        android.util.Log.d("PUZZLE_VIEW", "onSizeChanged completado");
    }
    
    /**
     * Calcula las dimensiones del puzzle
     */
    private void calculateDimensions(int w, int h) {
        // Calcular dimensiones del tablero
        int padding = 40;
        int availableSize = Math.min(w, h) - (padding * 2);
        boardSize = availableSize;
        pieceSize = (boardSize - (PIECE_MARGIN * (puzzleSize + 1))) / puzzleSize;
        
        boardStartX = (w - boardSize) / 2f;
        boardStartY = (h - boardSize) / 2f;
        
        android.util.Log.d("PUZZLE_VIEW", "Dimensiones calculadas - boardSize: " + boardSize + 
            ", pieceSize: " + pieceSize + ", boardStartX: " + boardStartX + ", boardStartY: " + boardStartY);
        
        // Actualizar tamaño de texto
        numberPaint.setTextSize(pieceSize * 0.4f);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        android.util.Log.d("PUZZLE_VIEW", "onDraw llamado - puzzleBoard: " + (puzzleBoard != null ? "SÍ" : "NO") + 
            ", puzzleSize: " + puzzleSize + ", showNumbers: " + showNumbers);
        
        if (puzzleBoard == null) {
            android.util.Log.w("PUZZLE_VIEW", "puzzleBoard es null, no dibujando nada");
            return;
        }
        
        // Dibujar fondo del tablero
        drawBackground(canvas);
        
        // Dibujar cada pieza
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                int value = puzzleBoard[i][j];
                
                if (value == 0) {
                    // Dibujar espacio vacío
                    drawEmptySpace(canvas, i, j);
                } else if (value == animatingPiece && currentAnimator != null) {
                    // Dibujar pieza animándose
                    drawAnimatingPiece(canvas, value);
                } else {
                    // Dibujar pieza normal
                    drawPiece(canvas, value, i, j);
                }
            }
        }
        
        android.util.Log.d("PUZZLE_VIEW", "onDraw completado");
    }
    
    /**
     * Dibuja el fondo del tablero
     */
    private void drawBackground(Canvas canvas) {
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.argb(30, 255, 255, 255));
        
        RectF bgRect = new RectF(boardStartX, boardStartY, 
                               boardStartX + boardSize, boardStartY + boardSize);
        canvas.drawRoundRect(bgRect, PIECE_RADIUS * 2, PIECE_RADIUS * 2, bgPaint);
    }
    
    /**
     * Dibuja una pieza del puzzle
     */
    private void drawPiece(Canvas canvas, int value, int row, int col) {
        android.util.Log.d("PUZZLE_VIEW", "drawPiece llamado - value: " + value + ", row: " + row + ", col: " + col);
        
        float x = boardStartX + PIECE_MARGIN + col * (pieceSize + PIECE_MARGIN);
        float y = boardStartY + PIECE_MARGIN + row * (pieceSize + PIECE_MARGIN);
        
        android.util.Log.d("PUZZLE_VIEW", "Posición pieza - x: " + x + ", y: " + y + ", pieceSize: " + pieceSize);
        
        // Dibujar sombra
        RectF shadowRect = new RectF(x + SHADOW_OFFSET, y + SHADOW_OFFSET, 
                                   x + pieceSize + SHADOW_OFFSET, y + pieceSize + SHADOW_OFFSET);
        canvas.drawRoundRect(shadowRect, PIECE_RADIUS, PIECE_RADIUS, shadowPaint);
        
        // Dibujar pieza
        RectF pieceRect = new RectF(x, y, x + pieceSize, y + pieceSize);
        
        // Verificar si tenemos imagen de la pieza
        boolean hasPieceImage = pieceBitmaps != null && 
                               value > 0 && 
                               value <= pieceBitmaps.length && 
                               pieceBitmaps[value - 1] != null;
        
        android.util.Log.d("PUZZLE_VIEW", "Pieza " + value + " - hasPieceImage: " + hasPieceImage + ", showNumbers: " + showNumbers);
        
        if (showNumbers || !hasPieceImage) {
            // Modo números o sin imagen
            piecePaint.setColor(getColorForPiece(value));
            canvas.drawRoundRect(pieceRect, PIECE_RADIUS, PIECE_RADIUS, piecePaint);
            
            // Dibujar número
            float textX = x + pieceSize / 2;
            float textY = y + pieceSize / 2 + numberPaint.getTextSize() / 3;
            canvas.drawText(String.valueOf(value), textX, textY, numberPaint);
            
            android.util.Log.d("PUZZLE_VIEW", "Pieza " + value + " dibujada en modo números");
            
            // Debug: mostrar información de la pieza
            if (!hasPieceImage) {
                android.util.Log.w("PUZZLE_VIEW", "Pieza " + value + " sin imagen - usando modo números");
            }
        } else {
            // Modo imagen
            canvas.save();
            
            // Crear máscara redondeada
            Path clipPath = new Path();
            clipPath.addRoundRect(pieceRect, PIECE_RADIUS, PIECE_RADIUS, Path.Direction.CW);
            canvas.clipPath(clipPath);
            
            // Dibujar imagen de la pieza
            canvas.drawBitmap(pieceBitmaps[value - 1], null, pieceRect, piecePaint);
            
            canvas.restore();
            
            android.util.Log.d("PUZZLE_VIEW", "Pieza " + value + " dibujada en modo imagen");
        }
        
        // Dibujar borde
        canvas.drawRoundRect(pieceRect, PIECE_RADIUS, PIECE_RADIUS, borderPaint);
    }
    
    /**
     * Dibuja una pieza que se está animando
     */
    private void drawAnimatingPiece(Canvas canvas, int value) {
        float currentX = animStartX + (animEndX - animStartX) * animationProgress;
        float currentY = animStartY + (animEndY - animStartY) * animationProgress;
        
        RectF pieceRect = new RectF(currentX, currentY, currentX + pieceSize, currentY + pieceSize);
        
        if (showNumbers || pieceBitmaps == null || pieceBitmaps[value - 1] == null) {
            piecePaint.setColor(getColorForPiece(value));
            canvas.drawRoundRect(pieceRect, PIECE_RADIUS, PIECE_RADIUS, piecePaint);
            
            float textX = currentX + pieceSize / 2;
            float textY = currentY + pieceSize / 2 + numberPaint.getTextSize() / 3;
            canvas.drawText(String.valueOf(value), textX, textY, numberPaint);
        } else {
            canvas.save();
            Path clipPath = new Path();
            clipPath.addRoundRect(pieceRect, PIECE_RADIUS, PIECE_RADIUS, Path.Direction.CW);
            canvas.clipPath(clipPath);
            canvas.drawBitmap(pieceBitmaps[value - 1], null, pieceRect, piecePaint);
            canvas.restore();
        }
        
        canvas.drawRoundRect(pieceRect, PIECE_RADIUS, PIECE_RADIUS, borderPaint);
    }
    
    /**
     * Dibuja el espacio vacío
     */
    private void drawEmptySpace(Canvas canvas, int row, int col) {
        float x = boardStartX + PIECE_MARGIN + col * (pieceSize + PIECE_MARGIN);
        float y = boardStartY + PIECE_MARGIN + row * (pieceSize + PIECE_MARGIN);
        
        RectF emptyRect = new RectF(x, y, x + pieceSize, y + pieceSize);
        canvas.drawRoundRect(emptyRect, PIECE_RADIUS, PIECE_RADIUS, emptyPaint);
    }
    
    /**
     * Genera un color único para cada pieza
     */
    private int getColorForPiece(int value) {
        float hue = (value * 360f) / (puzzleSize * puzzleSize);
        return Color.HSVToColor(new float[]{hue, 0.7f, 0.9f});
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (currentAnimator != null && currentAnimator.isRunning()) {
                return true; // Ignorar toques durante animación
            }
            
            // Convertir coordenadas del toque a posición del tablero
            int[] boardPos = getTouchedPosition(event.getX(), event.getY());
            if (boardPos != null) {
                int row = boardPos[0];
                int col = boardPos[1];
                
                // Intentar mover la pieza
                if (canMovePiece(row, col)) {
                    movePiece(row, col);
                    return true;
                }
            }
        }
        
        return super.onTouchEvent(event);
    }
    
    /**
     * Convierte coordenadas de pantalla a posición del tablero
     */
    private int[] getTouchedPosition(float touchX, float touchY) {
        if (touchX < boardStartX || touchX > boardStartX + boardSize ||
            touchY < boardStartY || touchY > boardStartY + boardSize) {
            return null;
        }
        
        int col = (int) ((touchX - boardStartX - PIECE_MARGIN) / (pieceSize + PIECE_MARGIN));
        int row = (int) ((touchY - boardStartY - PIECE_MARGIN) / (pieceSize + PIECE_MARGIN));
        
        if (row >= 0 && row < puzzleSize && col >= 0 && col < puzzleSize) {
            return new int[]{row, col};
        }
        
        return null;
    }
    
    /**
     * Verifica si una pieza se puede mover
     */
    private boolean canMovePiece(int row, int col) {
        // La pieza debe ser adyacente al espacio vacío
        return (Math.abs(row - emptyRow) == 1 && col == emptyCol) ||
               (Math.abs(col - emptyCol) == 1 && row == emptyRow);
    }
    
    /**
     * Mueve una pieza al espacio vacío
     */
    private void movePiece(int fromRow, int fromCol) {
        if (animationsEnabled) {
            animatePieceMove(fromRow, fromCol, emptyRow, emptyCol);
        } else {
            executeMove(fromRow, fromCol, emptyRow, emptyCol);
        }
    }
    
    /**
     * Anima el movimiento de una pieza
     */
    private void animatePieceMove(int fromRow, int fromCol, int toRow, int toCol) {
        animatingPiece = puzzleBoard[fromRow][fromCol];
        
        // Calcular posiciones de inicio y fin
        animStartX = boardStartX + PIECE_MARGIN + fromCol * (pieceSize + PIECE_MARGIN);
        animStartY = boardStartY + PIECE_MARGIN + fromRow * (pieceSize + PIECE_MARGIN);
        animEndX = boardStartX + PIECE_MARGIN + toCol * (pieceSize + PIECE_MARGIN);
        animEndY = boardStartY + PIECE_MARGIN + toRow * (pieceSize + PIECE_MARGIN);
        
        // Crear animador
        currentAnimator = ValueAnimator.ofFloat(0f, 1f);
        currentAnimator.setDuration(ANIMATION_DURATION);
        currentAnimator.setInterpolator(new DecelerateInterpolator());
        
        currentAnimator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        
        currentAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                executeMove(fromRow, fromCol, toRow, toCol);
                animatingPiece = -1;
                currentAnimator = null;
            }
        });
        
        currentAnimator.start();
    }
    
    /**
     * Ejecuta el movimiento de la pieza
     */
    private void executeMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Intercambiar piezas
        int temp = puzzleBoard[fromRow][fromCol];
        puzzleBoard[fromRow][fromCol] = puzzleBoard[toRow][toCol];
        puzzleBoard[toRow][toCol] = temp;
        
        // Actualizar posición del espacio vacío
        emptyRow = fromRow;
        emptyCol = fromCol;
        
        invalidate();
        
        // Notificar el movimiento
        if (moveListener != null) {
            moveListener.onPieceMove(fromRow, fromCol, toRow, toCol);
            
            // Verificar si el puzzle está completo
            if (isPuzzleCompleted()) {
                moveListener.onPuzzleCompleted();
            }
        }
    }
    
    /**
     * Verifica si el puzzle está completado
     */
    private boolean isPuzzleCompleted() {
        int expectedValue = 1;
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                if (i == puzzleSize - 1 && j == puzzleSize - 1) {
                    if (puzzleBoard[i][j] != 0) return false;
                } else {
                    if (puzzleBoard[i][j] != expectedValue) return false;
                    expectedValue++;
                }
            }
        }
        return true;
    }
    
    // Métodos públicos para control externo
    
    public void setOnPieceMoveListener(OnPieceMoveListener listener) {
        this.moveListener = listener;
    }
    
    public void setShowNumbers(boolean showNumbers) {
        this.showNumbers = showNumbers;
        invalidate();
    }
    
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }
    
    public int[][] getCurrentBoard() {
        int[][] copy = new int[puzzleSize][puzzleSize];
        for (int i = 0; i < puzzleSize; i++) {
            System.arraycopy(puzzleBoard[i], 0, copy[i], 0, puzzleSize);
        }
        return copy;
    }
    
    public boolean isAnimating() {
        return currentAnimator != null && currentAnimator.isRunning();
    }
    
    /**
     * Método de debug para verificar el estado del puzzle
     */
    public void debugPuzzleState() {
        android.util.Log.d("PUZZLE_DEBUG", "=== ESTADO DEL PUZZLE ===");
        android.util.Log.d("PUZZLE_DEBUG", "Tamaño: " + puzzleSize);
        android.util.Log.d("PUZZLE_DEBUG", "Imagen original: " + (originalImage != null ? "SÍ" : "NO"));
        if (originalImage != null) {
            android.util.Log.d("PUZZLE_DEBUG", "Dimensiones imagen: " + originalImage.getWidth() + "x" + originalImage.getHeight());
        }
        android.util.Log.d("PUZZLE_DEBUG", "Piezas creadas: " + (pieceBitmaps != null ? pieceBitmaps.length : "NULL"));
        if (pieceBitmaps != null) {
            for (int i = 0; i < pieceBitmaps.length; i++) {
                android.util.Log.d("PUZZLE_DEBUG", "Pieza " + (i + 1) + ": " + (pieceBitmaps[i] != null ? "SÍ" : "NO"));
            }
        }
        android.util.Log.d("PUZZLE_DEBUG", "Tablero actual:");
        if (puzzleBoard != null) {
            for (int i = 0; i < puzzleSize; i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < puzzleSize; j++) {
                    row.append(puzzleBoard[i][j]).append(" ");
                }
                android.util.Log.d("PUZZLE_DEBUG", "Fila " + i + ": " + row.toString());
            }
        }
        android.util.Log.d("PUZZLE_DEBUG", "========================");
    }
}
