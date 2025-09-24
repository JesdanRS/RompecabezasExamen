# 🧩 Puzzle Master - Rompecabezas Deslizante

Una aplicación moderna de rompecabezas deslizante para Android desarrollada en Java, con múltiples niveles de dificultad, algoritmo A* para resolución automática, y sistema de ranking competitivo.

## 📱 Características Principales

### 🎮 Funcionalidades de Juego
- **4 Niveles de Dificultad**: 2x2, 3x3, 4x4, y 5x5
- **Múltiples Fuentes de Imagen**: Cámara, galería, o imagen predeterminada
- **Cronómetro en Tiempo Real**: Con capacidad de pausa y reanudación
- **Contador de Movimientos**: Seguimiento preciso de cada movimiento
- **Sistema de Puntuación**: Cálculo dinámico basado en tiempo y movimientos
- **Resolución Automática**: Algoritmo A* optimizado para resolver puzzles
- **Vista Previa**: Miniatura del puzzle resuelto como guía
- **Mezclado Inteligente**: Garantiza puzzles siempre resolubles

### 🏆 Sistema de Ranking
- **Base de Datos SQLite**: Persistencia local de récords
- **Múltiples Filtros**: Por puntuación, tiempo, movimientos, o jugador
- **Búsqueda por Nombre**: Filtrado dinámico de jugadores
- **Medallas**: Oro, plata y bronce para las mejores posiciones
- **Estadísticas Detalladas**: Tiempo, movimientos y puntuación

### 🎨 Interfaz Moderna
- **Material Design 3**: Diseño moderno y elegante
- **Animaciones Fluidas**: Transiciones suaves entre movimientos
- **Paleta de Colores Vibrante**: Inspirada en gradientes modernos
- **Responsive Design**: Adaptación automática al tamaño del dispositivo
- **Dark Theme Compatible**: Preparado para modo oscuro

## 🛠️ Arquitectura Técnica

### Estructura del Proyecto

```
app/src/main/
├── java/com/example/rompecabezasexamen/
│   ├── MainActivity.java          # Pantalla principal y registro
│   ├── PuzzleActivity.java        # Actividad principal del juego
│   ├── RankingActivity.java       # Sistema de ranking
│   ├── PuzzleView.java           # Vista customizada del puzzle
│   ├── DatabaseHelper.java       # Gestión de base de datos SQLite
│   ├── AStar.java                # Algoritmo A* para resolución
│   ├── PuzzleState.java          # Estado del puzzle para A*
│   ├── Player.java               # Modelo de datos del jugador
│   └── GameRecord.java           # Modelo de datos del registro
├── res/
│   ├── layout/                   # Layouts XML
│   ├── drawable/                 # Iconos y fondos vectoriales
│   ├── values/                   # Recursos (colores, strings, estilos)
│   └── xml/                      # Configuraciones adicionales
└── AndroidManifest.xml           # Configuración de la aplicación
```

### Componentes Clave

#### 🧠 Algoritmo A* (AStar.java)
El corazón de la resolución automática del puzzle implementa una búsqueda heurística optimizada:

```java
/**
 * Algoritmo A* para resolver puzzles deslizantes
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
```

**Características del Algoritmo:**
- **Heurística Manhattan**: Calcula la distancia óptima de cada pieza a su posición final
- **Verificación de Solubilidad**: Determina si un puzzle tiene solución antes de resolver
- **Optimización de Rendimiento**: Máximo 50,000 iteraciones para evitar bloqueos
- **Generación de Puzzles**: Crea configuraciones mezcladas pero siempre resolubles

#### 🎨 Vista Personalizada (PuzzleView.java)
Componente Android customizado que maneja toda la renderización y interacción:

```java
/**
 * Vista personalizada para renderizar y manejar la interacción del puzzle
 * 
 * Características:
 * - Renderizado dinámico de piezas con imagen de fondo
 * - Animaciones suaves para movimientos de piezas
 * - Detección de toques y gestos para mover piezas
 * - Escalado automático según el tamaño del dispositivo
 * - Efectos visuales modernos con sombras y bordes redondeados
 */
```

**Funcionalidades Visuales:**
- **Rendering Personalizado**: Canvas 2D con recorte de imágenes dinámico
- **Animaciones Fluidas**: ValueAnimator para transiciones suaves
- **Detección de Toques**: Conversión precisa de coordenadas a posición del tablero
- **Escalado Adaptativo**: Ajuste automático al tamaño de pantalla
- **Efectos Visuales**: Sombras, bordes redondeados, y transparencias

#### 💾 Base de Datos (DatabaseHelper.java)
Implementación de SQLite con patrón Singleton para gestión de datos:

```sql
-- Tabla de jugadores
CREATE TABLE players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    created_at INTEGER NOT NULL
);

-- Tabla de registros de juego
CREATE TABLE game_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_name TEXT NOT NULL,
    difficulty INTEGER NOT NULL,
    time_in_millis INTEGER NOT NULL,
    moves INTEGER NOT NULL,
    score INTEGER NOT NULL,
    completed_at INTEGER NOT NULL
);
```

**Operaciones Principales:**
- **CRUD Completo**: Crear, leer, actualizar y eliminar registros
- **Consultas Optimizadas**: Índices automáticos y ordenamiento eficiente
- **Filtros Avanzados**: Por jugador, tiempo, movimientos, y puntuación
- **Integridad de Datos**: Validación y manejo de errores robusto

## 🔧 Instalación y Configuración

### Requisitos del Sistema
- **Android Studio**: Arctic Fox o superior
- **SDK Mínimo**: API 34 (Android 14)
- **SDK Objetivo**: API 36
- **Java**: JDK 11 o superior
- **Gradle**: 8.0 o superior

### Pasos de Instalación

1. **Clonar el Proyecto**
   ```bash
   git clone [URL_DEL_REPOSITORIO]
   cd RompecabezasExamen
   ```

2. **Abrir en Android Studio**
   - Abrir Android Studio
   - Seleccionar "Open an existing project"
   - Navegar y seleccionar la carpeta del proyecto

3. **Sincronizar Dependencias**
   ```bash
   ./gradlew sync
   ```

4. **Compilar el Proyecto**
   ```bash
   ./gradlew build
   ```

5. **Ejecutar en Dispositivo/Emulador**
   ```bash
   ./gradlew installDebug
   ```

### Configuración de Permisos
La aplicación requiere los siguientes permisos (ya configurados en AndroidManifest.xml):

```xml
<!-- Permisos para cámara y galería -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

## 🎯 Uso de la Aplicación

### Pantalla Principal
1. **Registro de Jugador**: Ingresa tu nombre (mínimo 2 caracteres)
2. **Selección de Dificultad**: Elige entre 2x2, 3x3, 4x4, o 5x5
3. **Fuente de Imagen**: Cámara, galería, o imagen predeterminada
4. **Acceso al Ranking**: Ver récords históricos

### Durante el Juego
- **Mover Piezas**: Toca una pieza adyacente al espacio vacío
- **Pausar/Reanudar**: Botón de pausa en la interfaz
- **Ver Miniatura**: Mostrar/ocultar imagen de referencia
- **Mezclar**: Reorganizar piezas aleatoriamente
- **Resolver**: Activar algoritmo A* para resolución automática

### Sistema de Puntuación
```java
// Fórmula de cálculo de puntuación
int baseScore = difficulty * 1000;
int timePenalty = (int) (timeInSeconds * difficulty);
int movesPenalty = moves * difficulty * 10;
int finalScore = Math.max(100, baseScore - timePenalty - movesPenalty);
```

## 🚀 Características Avanzadas

### Algoritmo A* - Detalles Técnicos

El algoritmo A* implementado utiliza la **distancia Manhattan** como función heurística:

```java
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
                int targetRow = (value - 1) / size;
                int targetCol = (value - 1) % size;
                distance += Math.abs(i - targetRow) + Math.abs(j - targetCol);
            }
        }
    }
    return distance;
}
```

**Ventajas del Algoritmo:**
- **Optimización Garantizada**: Encuentra la solución con menor número de movimientos
- **Eficiencia**: Poda de búsqueda reduce significativamente el espacio de estados
- **Escalabilidad**: Maneja eficientemente puzzles hasta 5x5

### Verificación de Solubilidad

Antes de intentar resolver, el algoritmo verifica si el puzzle tiene solución:

```java
/**
 * Un puzzle deslizante es resoluble si:
 * - Para grid impar: número de inversiones es par
 * - Para grid par: 
 *   - Si el espacio vacío está en fila par (desde abajo): inversiones impares
 *   - Si el espacio vacío está en fila impar (desde abajo): inversiones pares
 */
```

### Gestión de Memoria y Rendimiento

- **Threading**: Operaciones pesadas en background threads
- **Bitmap Management**: Reciclaje automático de imágenes
- **Database Pooling**: Conexiones reutilizables de SQLite
- **UI Optimization**: Actualización eficiente de componentes visuales

## 🎨 Diseño y UX

### Paleta de Colores
```xml
<!-- Colores principales inspirados en gradientes modernos -->
<color name="primary_purple">#6C5CE7</color>
<color name="primary_purple_light">#A29BFE</color>
<color name="accent_orange">#FF7675</color>
<color name="accent_blue">#74B9FF</color>
<color name="accent_green">#00B894</color>
```

### Principios de Diseño
- **Consistencia Visual**: Uso coherente de espaciado, tipografía y colores
- **Feedback Inmediato**: Respuesta visual a todas las interacciones
- **Accesibilidad**: Tamaños de toque mínimos y contraste adecuado
- **Progressive Disclosure**: Información revelada progresivamente

## 🧪 Testing y Calidad

### Casos de Prueba Recomendados

#### Funcionalidad Básica
- [ ] Registro de jugador con nombres válidos e inválidos
- [ ] Selección de todas las dificultades
- [ ] Carga de imágenes desde todas las fuentes
- [ ] Movimientos válidos e inválidos de piezas
- [ ] Funcionamiento del cronómetro y pausa

#### Algoritmo A*
- [ ] Resolución de puzzles 2x2 simples
- [ ] Resolución de puzzles 3x3 complejos
- [ ] Verificación de puzzles no resolubles
- [ ] Rendimiento en puzzles 4x4 y 5x5

#### Base de Datos
- [ ] Inserción de récords
- [ ] Filtrado por diferentes criterios
- [ ] Limpieza de datos
- [ ] Manejo de errores de BD

#### Interfaz de Usuario
- [ ] Rotación de pantalla
- [ ] Diferentes tamaños de dispositivos
- [ ] Navegación entre pantallas
- [ ] Estados de error y vacío

## 🔄 Futuras Mejoras

### Funcionalidades Planificadas
- [ ] **Modo Multijugador**: Competencia en tiempo real
- [ ] **Temas Personalizables**: Diferentes estilos visuales
- [ ] **Logros y Badges**: Sistema de reconocimientos
- [ ] **Tutorial Interactivo**: Guía paso a paso para nuevos usuarios
- [ ] **Exportar Récords**: Compartir estadísticas en redes sociales
- [ ] **Modo Sin Conexión**: Sincronización cuando hay internet
- [ ] **IA Mejorada**: Algoritmos de resolución más sofisticados

### Optimizaciones Técnicas
- [ ] **Migración a Kotlin**: Modernización del código
- [ ] **Room Database**: Reemplazo de SQLite crudo
- [ ] **Repository Pattern**: Arquitectura más limpia
- [ ] **Unit Testing**: Cobertura de pruebas automatizadas
- [ ] **CI/CD Pipeline**: Automatización de builds y deploys

## 👥 Contribuciones

### Cómo Contribuir
1. Fork del repositorio
2. Crear branch para feature (`git checkout -b feature/NuevaFuncionalidad`)
3. Commit de cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push al branch (`git push origin feature/NuevaFuncionalidad`)
5. Crear Pull Request

### Estándares de Código
- **Nomenclatura**: camelCase para variables, PascalCase para clases
- **Comentarios**: JavaDoc para métodos públicos
- **Indentación**: 4 espacios, sin tabs
- **Líneas**: Máximo 100 caracteres por línea

## 📄 Licencia

Este proyecto está licenciado bajo la [MIT License](LICENSE) - ver el archivo LICENSE para más detalles.

## 🙏 Agradecimientos

- **Material Design**: Guías de diseño de Google
- **Android Developers**: Documentación y recursos oficiales
- **Stack Overflow**: Comunidad de desarrolladores
- **Open Source Libraries**: Proyectos que inspiraron la implementación

---

## 📞 Contacto

**Desarrollador**: [Tu Nombre]  
**Email**: [tu.email@ejemplo.com]  
**GitHub**: [github.com/tu-usuario]

---

*Desarrollado con ❤️ para Android*
