package ClientPackage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    final private Socket socket;
    final private InputStreamReader inputReader;
    final private OutputStreamWriter outputWriter;
    final private BufferedReader bufferReader;
    final private BufferedWriter bufferWriter;
    private int clientStatus;
    final private static int SUCCESS = 0;
    final private static int CLIENT_READY = 0;
    final private static int CLIENT_TERMINATING = 1;
    final private static int CLIENT_STOPPED = 2;
    final private static int BUFFER_READY = 3;
    final private static int EMPTY_BUFFER = 4;
    final private static int BUFFER_READ_ERROR = 5;
    final private static int BUFFER_WRITE_ERROR = 6;
    final private static int SERVER_REQUESTED_EXIT = 7;
    final private static int NOT_OK_SERVER_RESPONSE = 8;


    public Client(String hostName, int port) throws IOException{
        this.socket = new Socket(hostName, port);
        this.inputReader = new InputStreamReader(this.socket.getInputStream());
        this.outputWriter = new OutputStreamWriter(this.socket.getOutputStream());
        this.bufferReader = new BufferedReader(this.inputReader);
        this.bufferWriter = new BufferedWriter(this.outputWriter);
        this.clientStatus = 0;
    }
    private int sendMessage(String messageToSend) {
        try {
            this.bufferWriter.write(messageToSend);
            this.bufferWriter.newLine();
            this.bufferWriter.flush();
        }
        catch (IOException ioException) {
            return BUFFER_WRITE_ERROR;
        }
        return SUCCESS;
    }


    private String getMessage() {
        String message = null;
        try {
            message = this.bufferReader.readLine();
        }
        catch (IOException ioException) {

        }
        return message;
    }

    private int getBufferReaderStatus() {
        try {
            if (this.bufferReader.ready()) {
                return BUFFER_READY;
            }
            else{
                return EMPTY_BUFFER;
            }
        }
        catch (IOException ioException) {
            return BUFFER_READ_ERROR;
        }

    }


    private int waitBuffer() {
        int bufferReaderStatus = EMPTY_BUFFER;
        while (this.clientStatus == CLIENT_READY && (bufferReaderStatus = this.getBufferReaderStatus()) == EMPTY_BUFFER) {}
        if (this.clientStatus == CLIENT_TERMINATING) { return CLIENT_TERMINATING; }
        if (bufferReaderStatus == BUFFER_READ_ERROR) { return BUFFER_READ_ERROR; }
        if (bufferReaderStatus == BUFFER_READY) { return BUFFER_READY; }
        return CLIENT_TERMINATING;
    }


    private int analyzeReply() {
        String reply = this.getMessage();
        if (reply == null) {
            System.out.println("Unable to get reply from server. Disconnecting");
            return BUFFER_READ_ERROR; }
        if (reply.equals("EXIT")) {
            System.out.println("Disconnecting from server.");
            return SERVER_REQUESTED_EXIT; }
        if (reply.startsWith("OK")) {
            System.out.println(reply.substring(2));
            return SUCCESS;
        }
        System.out.println("Received message error");
        return NOT_OK_SERVER_RESPONSE;


    }

    public void printIntro() {
        System.out.println("Welcome to the simple bank client terminal application!");
        System.out.println("Here are the commands and their functions: ");
        System.out.println("     login [username] [password]");
        System.out.println("     logout");
        System.out.println("     balance");
        System.out.println("     deposit [amount]");
        System.out.println("     withdraw [amount]");
        System.out.println("No typos and the spaces must be exact.");
        System.out.println("Arguments provided outside of the ones required will be ignored.");
    }

    public void run() {
        this.printIntro();
        Scanner input = new Scanner(System.in);
        while (this.clientStatus == CLIENT_READY) {
            System.out.print("Enter command: ");
            String messageToSend = input.nextLine();

            if (this.sendMessage(messageToSend) == BUFFER_WRITE_ERROR) {
                this.clientStatus = CLIENT_TERMINATING;
                System.out.println("Error writing to server. Disconnecting");
                break;
            }

            if(this.waitBuffer() != BUFFER_READY )
            {
                this.clientStatus = CLIENT_TERMINATING;
                System.out.println("Error reading from buffer. Disconnecting");
                break;
            }

            if (this.analyzeReply() != SUCCESS)
            {
                this.clientStatus = CLIENT_TERMINATING;
                break;
            }


        }

        this.clientStatus = CLIENT_STOPPED;

    }

    private void cleanUp() {
        try {this.closeBufferReader(); } catch(IOException ioException) { System.out.println("Unable to close buffer reader"); }
        try {this.closeBufferWriter(); } catch(IOException ioException) {System.out.println("Unable to close buffer writer"); }
        try {this.closeInputReader();} catch(IOException ioException) {System.out.println("Unable to close input reader"); }
        try {this.closeOutputWriter(); } catch(IOException ioException) {System.out.println("Unable to close output writer"); }
        try {this.closeSocket(); } catch(IOException ioException) { System.out.println("Unable to close server client socket");}

    }
    private void closeBufferReader() throws IOException {
        if (this.bufferReader != null) {
            this.bufferReader.close();
        }
    }
    private void closeBufferWriter() throws IOException {
        if (this.bufferWriter != null) {
            this.bufferWriter.close();
        }
    }
    private void closeInputReader() throws IOException {
        if (this.inputReader != null) {
            this.inputReader.close();
        }
    }
    private void closeOutputWriter() throws IOException {
        if (this.outputWriter != null) {
            this.outputWriter.close();
        }
    }
    private void closeSocket() throws IOException {
        if (this.socket != null) {
            this.socket.close();
        }
    }
}


