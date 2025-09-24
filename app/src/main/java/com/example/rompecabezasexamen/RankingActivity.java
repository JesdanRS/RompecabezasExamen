package com.example.rompecabezasexamen;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Actividad para mostrar el ranking de jugadores
 * 
 * Funcionalidades:
 * - Visualización de registros ordenados por diferentes criterios
 * - Filtros por nombre, tiempo, movimientos y puntuación
 * - Búsqueda por nombre de jugador
 * - Limpieza de registros con confirmación
 * - Interfaz moderna con RecyclerView y chips de filtro
 * - Estado vacío cuando no hay registros
 */
public class RankingActivity extends AppCompatActivity {
    
    // Views principales
    private RecyclerView rvRanking;
    private LinearLayout llEmptyState;
    private ChipGroup chipGroupFilters;
    private TextInputEditText etPlayerFilter;
    private ImageButton btnBack;
    private ImageButton btnClear;
    
    // Chips de filtro
    private Chip chipAll;
    private Chip chipByScore;
    private Chip chipByTime;
    private Chip chipByMoves;
    
    // Adapter y datos
    private RankingAdapter adapter;
    private List<GameRecord> allRecords;
    private List<GameRecord> filteredRecords;
    
    // Base de datos
    private DatabaseHelper dbHelper;
    
    // Estado actual del filtro
    private FilterType currentFilter = FilterType.ALL;
    private String currentPlayerFilter = "";
    
    /**
     * Enum para tipos de filtro
     */
    public enum FilterType {
        ALL, BY_SCORE, BY_TIME, BY_MOVES
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        
        // Inicializar componentes
        initializeComponents();
        
        // Configurar vistas
        initViews();
        
        // Configurar listeners
        setupListeners();
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Cargar datos
        loadRankingData();
    }
    
    /**
     * Inicializa los componentes necesarios
     */
    private void initializeComponents() {
        dbHelper = DatabaseHelper.getInstance(this);
        allRecords = new ArrayList<>();
        filteredRecords = new ArrayList<>();
    }
    
    /**
     * Inicializa las vistas del layout
     */
    private void initViews() {
        rvRanking = findViewById(R.id.rv_ranking);
        llEmptyState = findViewById(R.id.ll_empty_state);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        etPlayerFilter = findViewById(R.id.et_player_filter);
        btnBack = findViewById(R.id.btn_back);
        btnClear = findViewById(R.id.btn_clear);
        
        // Obtener chips individuales
        chipAll = findViewById(R.id.chip_all);
        chipByScore = findViewById(R.id.chip_by_score);
        chipByTime = findViewById(R.id.chip_by_time);
        chipByMoves = findViewById(R.id.chip_by_moves);
    }
    
    /**
     * Configura los listeners de los elementos de la UI
     */
    private void setupListeners() {
        // Botón volver
        btnBack.setOnClickListener(v -> finish());
        
        // Botón limpiar registros
        btnClear.setOnClickListener(v -> confirmClearRanking());
        
        // Listener para cambios en los chips de filtro
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                FilterType newFilter = FilterType.ALL;
                
                if (checkedId == R.id.chip_by_score) {
                    newFilter = FilterType.BY_SCORE;
                } else if (checkedId == R.id.chip_by_time) {
                    newFilter = FilterType.BY_TIME;
                } else if (checkedId == R.id.chip_by_moves) {
                    newFilter = FilterType.BY_MOVES;
                }
                
                if (newFilter != currentFilter) {
                    currentFilter = newFilter;
                    applyFilters();
                }
            }
        });
        
        // Listener para filtro de texto por jugador
        etPlayerFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newFilter = s.toString().trim();
                if (!newFilter.equals(currentPlayerFilter)) {
                    currentPlayerFilter = newFilter;
                    applyFilters();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    /**
     * Configura el RecyclerView con su adapter
     */
    private void setupRecyclerView() {
        adapter = new RankingAdapter(filteredRecords);
        rvRanking.setLayoutManager(new LinearLayoutManager(this));
        rvRanking.setAdapter(adapter);
        
        // Agregar decoración para espaciado
        rvRanking.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(
            this, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));
    }
    
    /**
     * Carga los datos del ranking desde la base de datos
     */
    private void loadRankingData() {
        try {
            // Cargar todos los registros (por defecto ordenados por puntuación)
            allRecords = dbHelper.getAllGameRecords();
            
            // Aplicar filtros iniciales
            applyFilters();
            
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_database), Toast.LENGTH_LONG).show();
            allRecords = new ArrayList<>();
            updateUI();
        }
    }
    
    /**
     * Aplica los filtros seleccionados a los datos
     */
    private void applyFilters() {
        List<GameRecord> sourceRecords;
        
        // Aplicar filtro de ordenamiento
        switch (currentFilter) {
            case BY_SCORE:
                sourceRecords = dbHelper.getAllGameRecords(); // Ya ordenado por puntuación
                break;
            case BY_TIME:
                sourceRecords = dbHelper.getGameRecordsByTime();
                break;
            case BY_MOVES:
                sourceRecords = dbHelper.getGameRecordsByMoves();
                break;
            default:
                sourceRecords = dbHelper.getAllGameRecords();
                break;
        }
        
        // Aplicar filtro de nombre de jugador si existe
        if (currentPlayerFilter.isEmpty()) {
            filteredRecords = new ArrayList<>(sourceRecords);
        } else {
            filteredRecords = new ArrayList<>();
            String filterLowerCase = currentPlayerFilter.toLowerCase();
            
            for (GameRecord record : sourceRecords) {
                if (record.getPlayerName().toLowerCase().contains(filterLowerCase)) {
                    filteredRecords.add(record);
                }
            }
        }
        
        // Actualizar UI
        updateUI();
    }
    
    /**
     * Actualiza la interfaz de usuario
     */
    private void updateUI() {
        if (filteredRecords.isEmpty()) {
            rvRanking.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvRanking.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            
            // Actualizar adapter
            adapter.updateData(filteredRecords);
        }
        
        // Habilitar/deshabilitar botón de limpiar
        btnClear.setEnabled(!allRecords.isEmpty());
    }
    
    /**
     * Confirma la limpieza del ranking
     */
    private void confirmClearRanking() {
        if (allRecords.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_records), Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_title);
        builder.setMessage(R.string.confirm_clear_ranking);
        builder.setIcon(R.drawable.ic_delete);
        
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            clearAllRecords();
        });
        
        builder.setNegativeButton(R.string.no, null);
        
        builder.show();
    }
    
    /**
     * Limpia todos los registros del ranking
     */
    private void clearAllRecords() {
        try {
            dbHelper.clearAllData();
            allRecords.clear();
            filteredRecords.clear();
            updateUI();
            
            Toast.makeText(this, "Todos los registros han sido eliminados", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_database), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando se regresa a la actividad
        loadRankingData();
    }
}

/**
 * Adapter para el RecyclerView del ranking
 */
class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {
    
    private List<GameRecord> records;
    
    public RankingAdapter(List<GameRecord> records) {
        this.records = records;
    }
    
    /**
     * Actualiza los datos del adapter
     */
    public void updateData(List<GameRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }
    
    @Override
    public RankingViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
        View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(RankingViewHolder holder, int position) {
        GameRecord record = records.get(position);
        holder.bind(record, position + 1);
    }
    
    @Override
    public int getItemCount() {
        return records.size();
    }
    
    /**
     * ViewHolder para items del ranking
     */
    static class RankingViewHolder extends RecyclerView.ViewHolder {
        
        private android.widget.ImageView ivRankMedal;
        private android.widget.TextView tvRankNumber;
        private android.widget.TextView tvPlayerName;
        private android.widget.TextView tvDifficulty;
        private android.widget.TextView tvScore;
        private android.widget.TextView tvTime;
        private android.widget.TextView tvMoves;
        
        public RankingViewHolder(View itemView) {
            super(itemView);
            
            ivRankMedal = itemView.findViewById(R.id.iv_rank_medal);
            tvRankNumber = itemView.findViewById(R.id.tv_rank_number);
            tvPlayerName = itemView.findViewById(R.id.tv_player_name);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvMoves = itemView.findViewById(R.id.tv_moves);
        }
        
        /**
         * Vincula los datos del registro con las vistas
         */
        public void bind(GameRecord record, int position) {
            // Configurar medalla según posición
            configureMedal(position);
            
            // Configurar textos
            tvRankNumber.setText(String.valueOf(position));
            tvPlayerName.setText(record.getPlayerName());
            tvDifficulty.setText(itemView.getContext().getString(R.string.difficulty_label, 
                record.getDifficultyString()));
            tvScore.setText(String.valueOf(record.getScore()));
            tvTime.setText(record.getFormattedTime());
            tvMoves.setText(record.getMoves() + " movimientos");
        }
        
        /**
         * Configura la medalla según la posición en el ranking
         */
        private void configureMedal(int position) {
            android.content.Context context = itemView.getContext();
            
            switch (position) {
                case 1: // Oro
                    ivRankMedal.setImageResource(R.drawable.ic_medal_gold);
                    ivRankMedal.setColorFilter(context.getColor(R.color.rank_gold));
                    break;
                case 2: // Plata
                    ivRankMedal.setImageResource(R.drawable.ic_medal_silver);
                    ivRankMedal.setColorFilter(context.getColor(R.color.rank_silver));
                    break;
                case 3: // Bronce
                    ivRankMedal.setImageResource(R.drawable.ic_medal_bronze);
                    ivRankMedal.setColorFilter(context.getColor(R.color.rank_bronze));
                    break;
                default: // Posición numérica
                    ivRankMedal.setImageResource(R.drawable.ic_rank_number);
                    ivRankMedal.setColorFilter(context.getColor(R.color.text_secondary));
                    break;
            }
        }
    }
}
