package main;

import java.util.*;

public class Baraja {
    private Map<Integer, String> cartas;
    private Random random;

    public Baraja() {
        cartas = new LinkedHashMap<>();
        random = new Random();
        inicializarBaraja();
    }

    private void inicializarBaraja() {
        String[] palos = {"Corazones", "Diamantes", "Tréboles", "Picas"};
        String[] valores = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

        int id = 1;
        for (String palo : palos) {
            for (String valor : valores) {
                cartas.put(id++, valor + " de " + palo);
            }
        }
    }

    public void barajar() {
        List<Map.Entry<Integer, String>> entradas = new ArrayList<>(cartas.entrySet());
        Collections.shuffle(entradas);

        cartas.clear();
        for (Map.Entry<Integer, String> entrada : entradas) {
            cartas.put(entrada.getKey(), entrada.getValue());
        }
    }

    public synchronized Map<Integer, String> repartir() {
        if (cartas.isEmpty()) {
            throw new IllegalStateException("No hay más cartas en la baraja");
        }

        int indiceAleatorio = random.nextInt(cartas.size());
        Integer idCartaSeleccionada = (Integer) cartas.keySet().toArray()[indiceAleatorio];
        String cartaSeleccionada = cartas.get(idCartaSeleccionada);

        cartas.remove(idCartaSeleccionada);

        Map<Integer, String> cartaRepartida = new HashMap<>();
        cartaRepartida.put(idCartaSeleccionada, cartaSeleccionada);

        return cartaRepartida;
    }

    // Método adicional para imprimir el estado de la baraja (opcional)
    public synchronized void imprimirBaraja() {
        for (Map.Entry<Integer, String> carta : cartas.entrySet()) {
            System.out.println(carta.getKey() + ": " + carta.getValue());
        }
    }
}