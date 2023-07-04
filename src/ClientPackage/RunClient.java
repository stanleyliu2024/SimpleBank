package ClientPackage;
import java.io.IOException;

public class RunClient {

    public static void main(String args[]) {
        Client client = null;
        try {
            System.out.println("Attempting to setup client connection");
            client = new Client("localhost", 51221);
            System.out.println("Client connection successfully set up!");
        }
        catch (IOException ioException) {
            System.out.println("Client connection failed to be set up");
            System.exit(1);
        }
        client.run();
    }
}

