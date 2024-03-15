import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.ServerSocket;
import java.net.Socket;

public class ServA {
    // Worker class to handle client connections
    static class Worker extends Thread {
        Socket conexion;

        Worker(Socket conexion) {
            this.conexion = conexion;
        }

        public void run() {
            try {
                BufferedReader entrada = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                PrintWriter salida = new PrintWriter(conexion.getOutputStream());

                // Read the request line
                String req = entrada.readLine();
                System.out.println(req);

                // Read request headers
                for (;;) {
                    String encabezado = entrada.readLine();
                    System.out.println(encabezado);
                    if (encabezado.equals("")) break; // Break loop when headers are done
                }

                // Parse the request to extract K_INITIAL and K_FINAL numbers
                String[] partes = req.split(" ")[1].split("/")[1].split(",");
                int kInicial = Integer.parseInt(partes[0]);
                int kFinal = Integer.parseInt(partes[1]);

                // Specify precision to 100 decimal places
                MathContext context = new MathContext(100);

                // Calculate the sum of Ramanujan's series
                BigDecimal resultado = calculatePiWithRamanujanBigDecimal(kInicial, kFinal, context);

                // Build HTTP response with the sum result
                String respuesta = resultado.toString();

                // Send HTTP response
                salida.println("HTTP/1.1 200 OK");
                salida.println("Content-type: text/plain");
                salida.println("Content-length: " + respuesta.length());
                salida.println("Connection: close");
                salida.println();
                salida.println(respuesta);
                salida.flush();
                
            } catch (Exception e) {
                System.err.println(e.getMessage());
            } finally {
                try {
                    conexion.close(); // Close the connection
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // Creating a server socket to listen for incoming connections on port 8080
        ServerSocket servidor = new ServerSocket(8080);

        // Infinite loop to continuously accept incoming connections
        for (;;) {
            Socket conexion = servidor.accept(); // Accepting incoming connection
            new Worker(conexion).start(); // Creating a new Worker thread to handle the connection
        }
    }

    // Function to calculate Pi using Ramanujan's formula
    public static BigDecimal calculatePiWithRamanujanBigDecimal(int lowerLimit, int upperLimit, MathContext context) {
        // Validate lower limit
        if (lowerLimit < 0 || lowerLimit > upperLimit) {
            throw new IllegalArgumentException("Lower limit must be non-negative and less than or equal to upper limit");
        }
          
        BigDecimal sum = BigDecimal.ZERO;
      
        // Calculate the sum of Ramanujan's series
        for (int n = lowerLimit; n <= upperLimit; n++) {
            BigDecimal numerator = factorialBigDecimal(4 * n, context).multiply(new BigDecimal(1103 + 26390 * n), context);
            BigDecimal denominator = factorialBigDecimal(n, context).pow(4, context).multiply(new BigDecimal(396).pow(4 * n, context), context);
            sum = sum.add(numerator.divide(denominator, context));
        }
        
        return sum;
    }
      
    // Helper function to calculate factorial with BigDecimal
    private static BigDecimal factorialBigDecimal(int num, MathContext context) {
        BigDecimal factorial = BigDecimal.ONE;
        for (int i = 2; i <= num; i++) {
            factorial = factorial.multiply(new BigDecimal(i), context);
        }
        return factorial;
    }
}
