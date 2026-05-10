import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PC 3 - SERVIDOR DESTINO
 * Ejecutar SEGUNDO en la PC donde se escribirá el archivo destino.
 * Espera que PC 1 le envíe el contenido y lo guarda en disco.
 */
public class ServidorDestino {

    private static final int PUERTO = 5002;
    private static String rutaArchivo = "destino.txt";

    public static void main(String[] args) throws Exception {

        // Si se pasa la ruta del archivo destino como argumento
        if (args.length > 0) {
            rutaArchivo = args[0];
        }

        System.out.println("===========================================");
        System.out.println("   PC 3 - SERVIDOR DESTINO");
        System.out.println("===========================================");
        System.out.println("Archivo destino  : " + new File(rutaArchivo).getAbsolutePath());
        System.out.println("Puerto           : " + PUERTO);
        System.out.println("-------------------------------------------");

        // Mostrar IP local
        try {
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("Mi IP (informar a PC 1): " + ip.getHostAddress());
        } catch (Exception e) {
            System.out.println("Mi IP: ejecuta 'ipconfig' (Windows) o 'ip a' (Linux)");
        }

        System.out.println("-------------------------------------------");
        System.out.println("Esperando datos de PC 1...");
        System.out.println();

        // Modo continuo: acepta múltiples transferencias
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (true) {
                Socket cliente = serverSocket.accept();
                System.out.println("[CONEXION] PC 1 conectada desde: " + cliente.getInetAddress().getHostAddress());

                new Thread(() -> recibirArchivo(cliente)).start();
            }
        }
    }

    private static void recibirArchivo(Socket cliente) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                PrintWriter out = new PrintWriter(cliente.getOutputStream(), true)
        ) {
            // Leer handshake inicial
            String handshake = in.readLine();
            if (handshake == null || !handshake.startsWith("INICIO:")) {
                System.out.println("[ERROR] Protocolo incorrecto.");
                out.println("ERROR:Protocolo incorrecto");
                return;
            }

            long totalLineas = Long.parseLong(handshake.split(":")[1]);
            System.out.println("[INFO] Se recibirán " + totalLineas + " registros.");
            out.println("OK:LISTO");

            // Crear/sobrescribir el archivo destino
            try (PrintWriter fileWriter = new PrintWriter(new FileWriter(rutaArchivo, false))) {

                // Escribir encabezado con timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                fileWriter.println("# Archivo transferido el: " + timestamp);
                fileWriter.println("# Total de registros: " + totalLineas);
                fileWriter.println("# -----------------------------------");

                String linea;
                int contador = 0;
                while ((linea = in.readLine()) != null) {
                    if (linea.equals("FIN")) break;
                    fileWriter.println(linea);
                    contador++;
                    System.out.println("  [RECIBIENDO] " + contador + "/" + totalLineas + " -> " + linea);
                }

                System.out.println("[OK] Archivo guardado: " + new File(rutaArchivo).getAbsolutePath());
                System.out.println("     Total registros recibidos: " + contador);
                out.println("OK:TRANSFERENCIA_COMPLETA:" + contador);

            } catch (IOException e) {
                System.out.println("[ERROR] Al escribir archivo: " + e.getMessage());
                out.println("ERROR:" + e.getMessage());
            }

            System.out.println("-------------------------------------------");
            System.out.println("Esperando proxima transferencia...");
            System.out.println();

        } catch (IOException e) {
            System.out.println("[ERROR] Conexion: " + e.getMessage());
        } finally {
            try { cliente.close(); } catch (IOException ignored) {}
        }
    }
}