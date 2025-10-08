import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Contador {
    private static AtomicInteger contador = new AtomicInteger(0); // Contador inicializado en 0
    private static Map<Integer, String> clientes = new HashMap<>(); // Mapa para rastrear clientes y sus IDs
    private static AtomicInteger contadorClientes = new AtomicInteger(0); // Contador para asignar IDs únicos a cada cliente
    private static Map<String, Boolean> ultimoCliemteMap = new HashMap<>(); // Para rastrear el último cliente que hizo una petición
    private static boolean detenerServidor = false; // Bandera para detener el servidor cuando el contador llegue a 100

    // Lista para registrar los logs de cada llamada: cliente -> valor del contador
    private static List<String> registro = new ArrayList<>();


    // Registrar un nuevo cliente y devolverle su nombre único
    public synchronized String registrarCliente() {
        int id = contadorClientes.incrementAndGet();
        String nombreCliente = "Cliente" + id;
        clientes.put(id, nombreCliente);
        System.out.println("Registrado: " + nombreCliente);
        return nombreCliente;
    }

    public synchronized int incrementar(String cliente) throws Exception{
        try {
            // Simular tiempo de procesamiento
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verificar si el cliente es el mismo que el último que hizo una petición
        if (ultimoCliemteMap.getOrDefault(cliente, false)) {
            System.out.println("Cliente " + cliente + " no puede hacer dos peticiones seguidas. Petición ignorada.");
            // Lanzar excepción para que el cliente sepa que no se procesó
            throw new Exception("Petición no procesada: no se permiten dos llamadas consecutivas del mismo cliente.");
        }

        ultimoCliemteMap.put(cliente, true); // Marcar este cliente como el último que hizo una petición
        // Resetear el estado de los otros clientes
        for (String c : ultimoCliemteMap.keySet()) {
            if (!c.equals(cliente)) {
                ultimoCliemteMap.put(c, false);
            }
        }
        
        if (contador.get() >= 100) { // Si el contador ya llegó a 100, no incrementamos más
            detenerServidor = true;
            throw new Exception("Contador alcanzó 100. Servidor dejará de aceptar peticiones.");
        }

        int nuevoValor = contador.incrementAndGet(); // Incrementar el contador de forma atómica
        registro.add(cliente + " -> " + nuevoValor); // Registrar la llamada
        System.out.println("Cliente " + cliente + " incrementó el contador a " + nuevoValor);
        return nuevoValor;
    
    }

    // Método para que el servidor verifique si debe detenerse
    public static boolean debeDetenerServidor() {
        return detenerServidor;
    }

    // Obtener el registro completo
    public static List<String> obtenerRegistro() {
        return new CopyOnWriteArrayList<>(registro); // Devolver copia para evitar modificación externa
    }

    /*
    // Método para detener el servidor cuando el contador llegue a 100
    public static void detenerSiNecesario() {
        if (contador >= 100) {
            detenerServidor = true;
            System.out.println("Contador ha llegado a 100. Deteniendo el servidor...");
        }
    }
    */
}