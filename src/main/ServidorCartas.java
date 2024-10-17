package main;

import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.*;

public class ServidorCartas {
    private static final int PUERTO = 12345;
    private static int maximoClientes;
    private static int rondas;
    private static Baraja baraja = new Baraja();
    private static List<ManejadorCliente> manejadores = new ArrayList<>();
    //Con esto lo usaremos para guardar las puntuaciones
    private static GestorPuntuaciones gestorPuntuaciones = new GestorPuntuaciones();

    private static void enviarMensajeGlobal(String mensaje) throws IOException {
        System.out.println(mensaje);
        for (ManejadorCliente manejador : manejadores) {
            DataOutputStream dataOutputStream = new DataOutputStream(manejador.getCliente().getOutputStream());
            dataOutputStream.writeUTF(mensaje);
            dataOutputStream.flush();
        }
    }

    private static void enviarMensajesBienvenida() throws IOException {
        for (ManejadorCliente manejador : manejadores) {
            DataOutputStream dataOutputStream = new DataOutputStream(manejador.getCliente().getOutputStream());
            dataOutputStream.writeUTF("--- BIENVENIDO A LA PARTIDA DE BLACKJACK ---");
            dataOutputStream.writeUTF("Tu nombre de jugador es: " + manejador.getJugador());
            dataOutputStream.flush();
        }
    }

    private static void reiniciarBaraja() {
        /*
         * Es necesacrio hacerlo de esta forma porque si no
         * la baraja de cada manejador seria una instancia
         * distinta del objeto
         */
        baraja = new Baraja();
        baraja.barajar();

        for (ManejadorCliente manejador : manejadores) {
            manejador.setBaraja(baraja);
        }
    }

    public static String getGanadorRonda() {
        String ganador = null;
        int puntosGanador = 0, cantidadEmpatados = 0;

        for (ManejadorCliente manejador : manejadores) {
            String cliente = manejador.getJugador();
            int puntosActuales = manejador.getPuntos();

            if (puntosActuales > puntosGanador) {
                ganador = cliente;
                puntosGanador = puntosActuales;
                cantidadEmpatados = 0;
            } else if (puntosActuales == puntosGanador) {
                cantidadEmpatados++;
            }
        }

        // Empate o ningún ganador
        if (cantidadEmpatados > 0 || ganador == null) {
            return "Ronda empatada";
        }

        return ganador+" ha ganado la ronda con "+puntosGanador+" puntos";
    }

    private static void printTablaPuntos() {
        System.out.println("--- TABLA PUNTUACIONES ---");
        Map<String, Integer> puntos = gestorPuntuaciones.obtenerPuntuaciones();
        for (Map.Entry<String, Integer> resultado : puntos.entrySet()) {
            System.out.println(resultado.getKey() + ": " + resultado.getValue());
        }
    }

    private static void enviarTablaPuntos(ManejadorCliente manejador) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(manejador.getCliente().getOutputStream());
        String cabecera = "--- TABLA PUNTUACIONES ---";
        dataOutputStream.writeUTF(cabecera);
        Map<String, Integer> puntos = gestorPuntuaciones.obtenerPuntuaciones();
        for (Map.Entry<String, Integer> resultado : puntos.entrySet()) {
            String puntuacion = resultado.getKey() + ": " + resultado.getValue();
            dataOutputStream.writeUTF(puntuacion);
        }
        dataOutputStream.flush();
    }

    private static void cerrarTodosLosClientes() throws IOException {
        for (ManejadorCliente manejador : manejadores) {
            manejador.getCliente().close();
        }
    }

    public static void main(String[] args) {
        try {
            // Asignar los parametros de entrada el programa
            maximoClientes = Integer.parseInt(args[0]);
            rondas = Integer.parseInt(args[1]);

            // Iniciar el servidor
            ServerSocket servidor = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado. Esperando clientes...");

            //Lista de hilos
            List<Thread> hilosClientes = new ArrayList<>();
            // Esperamos a que todos este conectados
            // Le pasamos el nombre del jugador
            for (int i = 0; i < maximoClientes; i++) {
                Socket cliente = servidor.accept();
                String nombreJugador = "Jugador" + (i + 1);
                gestorPuntuaciones.crearJugador(nombreJugador);
                ManejadorCliente manejador = new ManejadorCliente(cliente, baraja, nombreJugador, gestorPuntuaciones);
                manejadores.add(manejador);
            }

            enviarMensajesBienvenida();

            for (int i = 0; i < rondas; i++) {
                System.out.println();
                enviarMensajeGlobal("--- RONDA " + (i + 1) + " ---");

                // Reiniciar la baraja al iniciar cada ronda
                reiniciarBaraja();

                // Iniciar cada manejador en un hilo
                for (ManejadorCliente manejador : manejadores) {
                    Thread hilo = new Thread(manejador);
                    hilo.start();
                    hilosClientes.add(hilo);
                }
                System.out.println("[HILOS ACTIVOS]:");
                hilosClientes.forEach(hilo -> System.out.println("\t"+hilo));

                // Esperar a que acaben todos los hilos
                for (Thread hilo : hilosClientes) {
                    try {
                        hilo.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Interrumpido mientras esperaba a que los clientes terminaran.");
                    }
                }
                hilosClientes.clear(); // Vaciar la lista para quitar los hilos terminados

                enviarMensajeGlobal(getGanadorRonda());
                printTablaPuntos();
            }

            for (ManejadorCliente manejador : manejadores) {
                enviarTablaPuntos(manejador);
            }
            enviarMensajeGlobal("--- FIN DE LA PARTIDA ---");

            cerrarTodosLosClientes(); // Cerrar todas las conexiones
            servidor.close(); // Cerrar el servidor
        } catch (IndexOutOfBoundsException e) {
            System.err.println("[ERROR] Debes pasar al menos 2 argumentos al servidor");
            System.err.println("\t- Argumento 1 = numero maximo de clientes");
            System.err.println("\t- Argumento 2 = numero de rondas de la partida");
        } catch (NumberFormatException e) {
            System.err.println("[ERROR] Alguno de los argumentos pasados no es valido");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}