import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Contador {
    private static AtomicInteger contadorGlobal = new AtomicInteger(0);
    private static Map<String, Integer> contadoresClientes = new HashMap<>(); // contador por cliente
    private static Map<String, Boolean> ultimoClienteMap = new HashMap<>();
    private static List<String> registro = new ArrayList<>();
    private static boolean detenerServidor = false;

    private static Map<String, List<String>> mensajesClientes = new HashMap<>();
    private static Map<String, List<Integer>> valoresPorCliente = new HashMap<>(); // Valores recibidos por cliente

    private static final int LIMITE_CONTADOR = 10;

    // Registrar un cliente (nuevo o reconectado)
    public synchronized String registrarCliente(String idCliente) {
        if (!contadoresClientes.containsKey(idCliente)) {
            contadoresClientes.put(idCliente, 0);
            mensajesClientes.put(idCliente, new ArrayList<>());
            valoresPorCliente.put(idCliente, new ArrayList<>()); // Iniciar lista de valores
            System.out.println("Nuevo cliente registrado: " + idCliente);
        } else {
            System.out.println("Cliente reconectado: " + idCliente + " (mantiene su estado y su historial de valores)");
        }

        // Si ya se había detenido el servidor, notificar al reconectado
        if (detenerServidor) {
            mensajesClientes.get(idCliente).add("El contador ya ha finalizado en " + contadorGlobal.get());
        }

        return idCliente;
    }

    // Incrementar el contador global y actualizar el registro del cliente
    public synchronized int incrementar(String cliente) throws Exception {
        if (detenerServidor) {
            mensajesClientes.get(cliente).add("El servidor ya se ha detenido. No puedes seguir incrementando.");
            throw new Exception("Servidor finalizado. Contador alcanzó el límite.");
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Evitar peticiones consecutivas del mismo cliente
        if (ultimoClienteMap.getOrDefault(cliente, false)) {
            System.out.println("Cliente " + cliente + " no puede hacer dos peticiones seguidas. Petición ignorada.");
            return -1;
        }

        ultimoClienteMap.put(cliente, true);
        for (String c : ultimoClienteMap.keySet()) {
            if (!c.equals(cliente)) {
                ultimoClienteMap.put(c, false);
            }
        }

        // Verificar límite
        if (contadorGlobal.get() >= LIMITE_CONTADOR) {
            detenerServidor = true;
            notificarATodos("El contador global alcanzó el límite de " + LIMITE_CONTADOR + ". Servidor finalizado.");
            throw new Exception("Contador alcanzó el límite. Servidor dejará de aceptar peticiones.");
        }

        int nuevoValor = contadorGlobal.incrementAndGet();
        contadoresClientes.put(cliente, contadoresClientes.getOrDefault(cliente, 0) + 1);
        registro.add(cliente + " -> " + nuevoValor);

        // Registrar el valor recibido por este cliente
        valoresPorCliente.computeIfAbsent(cliente, k -> new ArrayList<>()).add(nuevoValor);

        System.out.println("Cliente " + cliente + " incrementó el contador global a " + nuevoValor);

        // Si se alcanzó el límite justo en este incremento
        if (nuevoValor >= LIMITE_CONTADOR) {
            detenerServidor = true;
            notificarATodos("El contador global alcanzó el límite de " + LIMITE_CONTADOR + ". Servidor finalizado.");
        }

        return nuevoValor;
    }

    // Devolver todos los valores que ha recibido este cliente de manera historica
    public synchronized List<Integer> obtenerValoresCliente(String cliente) {
        return new ArrayList<>(valoresPorCliente.getOrDefault(cliente, new ArrayList<>()));
    }

    // Notificar mensaje a todos los clientes registrados
    private synchronized void notificarATodos(String mensaje) {
        System.out.println("Notificando a todos los clientes: " + mensaje);
        for (String cliente : mensajesClientes.keySet()) {
            mensajesClientes.get(cliente).add(mensaje);
        }
    }

    // Devuelve los mensajes pendientes de un cliente
    public synchronized List<String> obtenerMensajes(String cliente) {
        List<String> mensajes = mensajesClientes.getOrDefault(cliente, new ArrayList<>());
        List<String> copia = new ArrayList<>(mensajes);
        mensajes.clear(); // Limpiar tras entregarlos
        return copia;
    }

    // Devuelve el número de incrementos hechos por un cliente específico
    public synchronized int obtenerContadorCliente(String cliente) {
        return contadoresClientes.getOrDefault(cliente, 0);
    }

    public static boolean debeDetenerServidor() {
        return detenerServidor;
    }

    public static List<String> obtenerRegistro() {
        return new CopyOnWriteArrayList<>(registro);
    }
}
