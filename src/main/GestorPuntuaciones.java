package main;

import java.util.HashMap;
import java.util.Map;

public class GestorPuntuaciones {
    private final Map<String, Integer> puntuaciones;

    public GestorPuntuaciones() {
        this.puntuaciones = new HashMap<>();
    }

    public synchronized void crearJugador(String jugador) {
        if (!puntuaciones.containsKey(jugador)) {
            puntuaciones.put(jugador, 0);
        }
    }

    public synchronized void anadirPuntuacion(String jugador, int puntos) {
        if (!puntuaciones.containsKey(jugador)) {
            crearJugador(jugador);
        }
        int puntuacionActual = puntuaciones.get(jugador);
        puntuaciones.put(jugador, puntuacionActual + puntos);
    }

    public synchronized int obtenerPuntuacion(String jugador) {
        if (puntuaciones.containsKey(jugador)) {
            return puntuaciones.get(jugador);
        }
        return 0;
    }

    public synchronized Map<String, Integer> obtenerPuntuaciones() {
        return new HashMap<>(puntuaciones);
        // Devuelve una copia para evitar modificaciones externas
    }
}
