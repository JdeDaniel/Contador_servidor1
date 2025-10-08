import org.apache.xmlrpc.server.PropertyHandlerMapping; // Registra mapeos entre nombres de servicios (strings) y clases Java
import org.apache.xmlrpc.server.XmlRpcServer; // Clase principal que maneja la lógica del servidor RPC basado en XML
import org.apache.xmlrpc.webserver.WebServer; // Servidor web embebido simple que puede escuchar peticiones HTTP en un puerto específico

public class RPCServer {

    public static void main(String[] args) {
        
        try {
            int puerto = 8080;
            System.out.println("Iniciando servidor en el puerto " + puerto + "...");

            WebServer webServer = new WebServer(puerto); // Crear una instancia del servidor web en el puerto especificado
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer(); // Obtener el servidor XML-RPC del servidor web
            
            PropertyHandlerMapping phm = new PropertyHandlerMapping(); // Crear un mapeo de propiedades para registrar los manejadores de servicios
            phm.addHandler("Contador", Contador.class); // Registrar la clase Contador con el nombre "Contador"
            xmlRpcServer.setHandlerMapping(phm); // Asignar el mapeo de propiedades al servidor XML-RPC
            
            webServer.start(); // Iniciar el servidor web para que escuche peticiones
            
            System.out.println("Servidor XML-RPC escuchando en el puerto" + puerto +"...");

            // Monitorear para detener el servidor
            while (true) {
                if (Contador.debeDetenerServidor()) {
                    webServer.shutdown();
                    System.out.println("Servidor detenido automáticamente al llegar a 100.");
                    break;
                }
                Thread.sleep(500);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
}


/* 
public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    private static int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        channel.queuePurge(RPC_QUEUE_NAME);

        channel.basicQos(1);

        System.out.println(" [x] Awaiting RPC requests");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String response = "";
            try {
                String message = new String(delivery.getBody(), "UTF-8");
                int n = Integer.parseInt(message);

                System.out.println(" [.] fib(" + message + ")");
                response += fib(n);
            } catch (RuntimeException e) {
                System.out.println(" [.] " + e);
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {}));
    }
}
*/