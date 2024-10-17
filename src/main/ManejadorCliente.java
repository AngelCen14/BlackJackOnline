package main;

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class ManejadorCliente implements Runnable {
    //private static boolean todosLosClientesTerminados = false;
    private Socket cliente;
    private Baraja baraja;
    private String jugador;
    private GestorPuntuaciones gestorPuntuaciones;
    private int puntos;
    boolean rondaEnProgreso;
    public ManejadorCliente(Socket cliente, Baraja baraja, String jugador, GestorPuntuaciones gestorPuntuaciones) {
        this.cliente = cliente;
        this.baraja = baraja;
        this.jugador = jugador;
        this.gestorPuntuaciones = gestorPuntuaciones;
    }

    @Override
    public void run() {
        try {
            DataOutputStream salida = new DataOutputStream(cliente.getOutputStream());
            DataInputStream entrada = new DataInputStream(cliente.getInputStream());

            rondaEnProgreso = true;
            puntos = 0;

            // Repartir las 2 primeras cartas
            for (int i = 0; i < 2; i++) {
                repartirCarta(salida, entrada);
            }

            while (rondaEnProgreso) {
                // Preguntar al jugador si quiere mas cartas
                salida.writeUTF("¿Quieres coger otra carta (si / no)?");
                salida.flush();

                String respuestaUsuario = entrada.readUTF();
                switch (respuestaUsuario.trim().toLowerCase()) {
                    case "si":
                        // Darle otra carta
                        repartirCarta(salida, entrada);
                        break;
                    case "no":
                        // Asignar la puntuacion del jugador en la ronda actual segun las cartas que tenga
                        finalizarRonda(salida);
                        break;
                    default:
                        salida.writeUTF("ERROR debes escribir (si / no)");
                        salida.flush();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void repartirCarta(DataOutputStream salida, DataInputStream entrada) throws IOException {
        // Coger una carta de la baraja
        try {
            Map<Integer, String> carta = baraja.repartir();
            String descripcionCarta = carta.values().iterator().next();
            salida.writeUTF("Carta repartida: " + descripcionCarta);
            salida.flush();

            // Añadir el valor de la carta a la puntuacion del jugador
            int valor = calcularValorCarta(descripcionCarta, salida, entrada);
            puntos += valor;

            // Si el jugador llega a 21 o se pasa de 21 la ronda finaliza para el y esperara al resto
            if (puntos == 21) {
                salida.writeUTF("Has llegado a 21 :)");
                salida.flush();
                finalizarRonda(salida);
            }
            if (puntos > 21) {
                salida.writeUTF("Te has pasado de 21 :(");
                salida.flush();
                finalizarRonda(salida);
            }
        } catch (IllegalStateException e) {
            salida.writeUTF("No quedan mas cartas en la baraja");
            salida.flush();
        }

    }

    private int calcularValorCarta(String carta, DataOutputStream salida, DataInputStream entrada) throws IOException {
        // Dividir la descripción de la carta en sus componentes
        String[] partes = carta.split(" de ");
        String valorCarta = partes[0];

        // Comprobar si es una figura (J, Q, K) o un As (A)
        switch (valorCarta) {
            case "J":
            case "Q":
            case "K":
                return 10; // Las figuras valen 10
            case "A":
                salida.writeUTF("Has sacado un As. ¿Quieres que valga 1 o 11?");
                while (true) {
                    String respuesta = entrada.readUTF();
                    try {
                        int valorAs = Integer.parseInt(respuesta);
                        if (valorAs == 1 || valorAs == 11) {
                            return valorAs;
                        } else {
                            salida.writeUTF("Por favor, elige 1 o 11.");
                            salida.flush();
                        }
                    } catch (NumberFormatException e) {
                        salida.writeUTF("Entrada inválida. Por favor, elige 1 o 11.");
                        salida.flush();
                    }
                }
            default:
                // Convertir el valor numérico de la carta a un entero
                try {
                    return Integer.parseInt(valorCarta);
                } catch (NumberFormatException e) {
                    throw new IOException("Formato de carta inválido: " + carta);
                }
        }
    }

    private void finalizarRonda(DataOutputStream salida) throws IOException {
        // Añadir la puntuacion
        if (puntos > 21) puntos = 0;
        gestorPuntuaciones.anadirPuntuacion(jugador, puntos);
        // Notificar al jugador
        salida.writeUTF("Has conseguigo "+puntos+" puntos en esta ronda");
        salida.writeUTF("Esperando al resto de jugadores...");
        salida.flush();
        rondaEnProgreso = false; // Terminar el juego para salir del bucle
    }

    // Getters
    public Socket getCliente() {
        return cliente;
    }

    public String getJugador() {
        return jugador;
    }

    public int getPuntos() {
        return puntos;
    }

    // Setters
    public void setBaraja(Baraja baraja) {
        this.baraja = baraja;
    }
}
