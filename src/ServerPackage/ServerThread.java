package ServerPackage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class ServerThread {
    final private ServerSocket server;
    Vector<ClientThread> clientThreads;
    private int status;
    public ServerThread(int port) throws IOException {
        this.server = new ServerSocket(port);
        this.clientThreads = new Vector<ClientThread>();
        this.status = 0;
    }


    public void runServer() {
        Socket clientSocket = null;
        while (this.status == 0) {
            try {clientSocket = server.accept(); }
            catch (IOException ioException) {
                if (this.status == 1) { break; }
            }

            try {
                ClientThread newClient = new ClientThread(clientSocket);
                clientThreads.add(newClient);
                newClient.start();
                System.out.printf("Client successfully connected: %s \n", newClient);
                //System.out.println(clientThreads);
            } catch (IOException ioException){
                System.out.println("Client unsuccessful connection.");
            }
            this.removeStoppedThreads();
        }

    }

    private int removeStoppedThreads() {
        Vector<ClientThread> removeList = new Vector<ClientThread>();
        int removeCount = 0;
        for (ClientThread client : this.clientThreads) {
            if (client.getThreadStatus() == 2) {
                removeList.add(client);
            }
        }
        for (ClientThread client: removeList) {
            this.clientThreads.remove(client);
            System.out.printf("Client successfully removed from list: %s \n", client);
            removeCount++;
        }
        return removeCount;
    }

    public void stopClientThreads() {
        if (this.clientThreads != null) {
            for (ClientThread client : this.clientThreads) {
                client.stopThread();
            }
            for (ClientThread client: this.clientThreads) {
                while (client.getThreadStatus() != 2);
            }
        }
    }

    public void closeServer() throws IOException {
        this.status = 1;
        if (this.server != null) {
            this.server.close();
        }
    }



}




