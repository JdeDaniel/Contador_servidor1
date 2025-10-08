//Contador.java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Contador {
    private static final int MAX = 10;                         // Límite para pruebas
    private static final AtomicInteger contador = new AtomicInteger(0);
    private static final ReentrantLock SERIAL = new ReentrantLock(true);

    private static final AtomicInteger secuencialNombre = new AtomicInteger(0);
    private static final Map<String, Cliente> clientes = new ConcurrentHashMap<>();
    private static final List<String> registroGlobal = new CopyOnWriteArrayList<>();

    private static volatile String ultimoId = null;            // Para bloquear doble turno
    private static volatile boolean detenerServidor = false;

    private static class Cliente {
        final String id;
        final String nombre;
        final List<Integer> valores = new CopyOnWriteArrayList<>();
        final AtomicInteger total = new AtomicInteger(0);
        Cliente(String id, String nombre) { this.id=id; this.nombre=nombre; }
    }

    // Registrar o reconectar: el cliente envía su id local
    public String registrarCliente(String idCliente) {
        return clientes.computeIfAbsent(idCliente, id -> {
            String nombre = "Cliente" + secuencialNombre.incrementAndGet();
            System.out.println("Registrado/reconectado: " + nombre + " (id=" + id + ")");
            return new Cliente(id, nombre);
        }).nombre;
    }

    // Incrementar contador. Devuelve -1 si intentó doble turno.
    public int incrementar(String idCliente) throws Exception {
        SERIAL.lock();
        try {
            if (contador.get() >= MAX) {
                detenerServidor = true;
                throw new Exception("Contador alcanzó " + MAX + ". Servidor dejará de aceptar peticiones");
            }

            Cliente c = clientes.get(idCliente);
            if (c == null) throw new Exception("Cliente no registrado");

            if (idCliente.equals(ultimoId)) {
                System.out.println("Doble turno de " + c.nombre + " ignorado.");
                return -1; // Tu cliente ya maneja -1
            }

            // Simular trabajo
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}

            int v = contador.incrementAndGet();
            c.valores.add(v);
            c.total.incrementAndGet();
            ultimoId = idCliente;

            registroGlobal.add(c.nombre + " -> " + v);
            System.out.println(c.nombre + " incrementó a " + v);

            if (v >= MAX) detenerServidor = true;
            return v;
        } finally {
            SERIAL.unlock();
        }
    }

    // Consultas RPC
    public int obtenerContadorCliente(String idCliente) throws Exception {
        Cliente c = clientes.get(idCliente);
        if (c == null) throw new Exception("Cliente no registrado");
        return c.total.get();
    }

    public List<Integer> historialPorCliente(String idCliente) throws Exception {
        Cliente c = clientes.get(idCliente);
        if (c == null) throw new Exception("Cliente no registrado");
        return new ArrayList<>(c.valores);
    }

    public List<String> obtenerRegistroGlobal() {               // opcional por RPC
        return new ArrayList<>(registroGlobal);
    }

    // Para el lazo de apagado del servidor
    public static boolean debeDetenerServidor() { return detenerServidor; }

    // Snapshot para imprimir al final
    public static Map<String, List<Integer>> snapshotHistorialPorCliente() {
        Map<String, List<Integer>> out = new LinkedHashMap<>();
        // Ordenar por nombre para salida estable
        List<Cliente> lista = new ArrayList<>(clientes.values());
        lista.sort(Comparator.comparing(c -> c.nombre));
        for (Cliente c : lista) out.put(c.nombre, new ArrayList<>(c.valores));
        return out;
    }

    public static List<String> snapshotRegistroGlobal() {
        return new ArrayList<>(registroGlobal);
    }
}

/*import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Contador {
    private static AtomicInteger contadorGlobal = new AtomicInteger(0);
    private static Map<String, Integer> contadoresClientes = new HashMap<>(); // contador por cliente
    private static Map<String, Boolean> ultimoClienteMap = new HashMap<>();
    private static List<String> registro = new ArrayList<>();
    private static boolean detenerServidor = false;

    // Guardamos una lista de mensajes para cada cliente
    private static Map<String, List<String>> mensajesClientes = new HashMap<>();

    private static final int LIMITE_CONTADOR = 10;

    // Registrar un cliente (nuevo o reconectado)
    public synchronized String registrarCliente(String idCliente) {
        if (!contadoresClientes.containsKey(idCliente)) {
            contadoresClientes.put(idCliente, 0);
            mensajesClientes.put(idCliente, new ArrayList<>());
            System.out.println("Nuevo cliente registrado: " + idCliente);
        } else {
            System.out.println("Cliente reconectado: " + idCliente + " (mantiene su estado)");
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
        System.out.println("Cliente " + cliente + " incrementó el contador global a " + nuevoValor);

        // Si se alcanzó el límite justo en este incremento
        if (nuevoValor >= LIMITE_CONTADOR) {
            detenerServidor = true;
            notificarATodos("El contador global alcanzó el límite de " + LIMITE_CONTADOR + ". Servidor finalizado.");
        }

        return nuevoValor;
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
    */