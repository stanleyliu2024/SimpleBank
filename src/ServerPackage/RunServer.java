package ServerPackage;

import java.io.*;


public class RunServer {
        public static void main(String args[]) {
            ServerThread serverThread = null;
            try {
                System.out.println("Attempting to set up server");
                serverThread = new ServerThread(51221);
            } catch (IOException ioException) {
                System.out.println("Unable to set up server");
                System.exit(1);
            }
            initializeShutdown(serverThread);
            System.out.println("Server successfully set up");
            serverThread.runServer();

        }

        private static void initializeShutdown(final ServerThread serverThread) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    serverThread.stopClientThreads();
                    System.out.println("Successfully stopped all client threads");
                    try {
                        serverThread.closeServer();
                        System.out.println("Server successfully shut down");
                    }
                    catch (IOException ioException) {
                        System.out.println("Error closing server");
                    }

                }


            });

        }


}





