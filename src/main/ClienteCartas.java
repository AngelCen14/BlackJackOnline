package main;

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClienteCartas {
    private static final String HOST = "localhost";
    private static final int PUERTO = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PUERTO);
             DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
             DataInputStream entrada = new DataInputStream(socket.getInputStream());
             BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in))) {

            String mensajeDelServidor;
            boolean juegoTerminado = false;
            while (!juegoTerminado) {
                mensajeDelServidor = entrada.readUTF();
                System.out.println("Servidor: " + mensajeDelServidor);

                // Si el servidor te pide si quieres coger o no otra carta
                if (mensajeDelServidor.toLowerCase().contains("otra carta")) {
                    String respuestaUsuario = lectorConsola.readLine();
                    salida.writeUTF(respuestaUsuario);
                    salida.flush();
                }

                // Si el servidor pide una decisión para el As
                if (mensajeDelServidor.contains("1 o 11")) {
                    System.out.print("Tu elección (1 o 11): ");
                    String respuestaUsuario = lectorConsola.readLine();
                    salida.writeUTF(respuestaUsuario);
                    salida.flush();
                }

                // Si el jugador ha terminado tiene que esperar al resto de jugadores
                if (mensajeDelServidor.toLowerCase().contains("esperando")) {
                    while (true) {
                        String mensajeServidor = entrada.readUTF();
                        if (mensajeServidor.toLowerCase().contains("ganado la ronda") || mensajeServidor.toLowerCase().contains("empatada")) {
                            System.out.println("Servidor: "+mensajeServidor);
                            break;
                        }
                    }
                }

                if (mensajeDelServidor.toLowerCase().contains("fin de la partida")) {
                    juegoTerminado = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
