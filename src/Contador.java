import java.util.HashMap;
import java.util.Map;

public class Contador {
    private static int contador = 0; // Contador inicializado en 0
    private static Map<Integer, String> clientes = new HashMap<>(); // Mapa para rastrear clientes y sus IDs
    private static int contadorClientes = 0; // Contador para asignar IDs únicos a cada cliente
    private static String ultimoCliente = ""; // Para rastrear el último cliente que hizo una petición
    private static boolean detenerServidor = false; // Bandera para detener el servidor cuando el contador llegue a 100

    // Registrar un nuevo cliente y devolverle su nombre único
    public synchronized String registrarCliente() {
        contadorClientes++;
        String nombreCliente = "Cliente" + contadorClientes;
        clientes.put(contadorClientes, nombreCliente);
        System.out.println("Registrado: " + nombreCliente);
        return nombreCliente;
    }

    public synchronized int incrementar(String cliente) {
        try {
            // Simular tiempo de procesamiento
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verificar si el cliente es el mismo que el último que hizo una petición
        if (cliente.equals(ultimoCliente)) {
            System.out.println("Cliente " + cliente + " no puede hacer dos peticiones seguidas.");
            return contador;
        }

        // Incrementar el contador si es menor que 100
        if (contador < 100) {
            contador++;
            ultimoCliente = cliente;
            System.out.println("Cliente " + cliente + " incrementó el contador a " + contador);
            
        } else { // Si el contador ya es 100, no se incrementa más
            detenerServidor = true;
            System.out.println("Contador alcanzó 100. Servidor dejará de aceptar peticiones.");
        }
        return contador;
    }

    // Método para que el servidor verifique si debe detenerse
    public static boolean debeDetenerServidor() {
        return detenerServidor;
    }

    // Método para detener el servidor cuando el contador llegue a 100
    public static void detenerSiNecesario() {
        if (contador >= 100) {
            detenerServidor = true;
            System.out.println("Contador ha llegado a 100. Deteniendo el servidor...");
        }
    }
}