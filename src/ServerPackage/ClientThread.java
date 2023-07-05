package ServerPackage;

import java.io.*;
import java.lang.Thread;
import java.net.Socket;


public class ClientThread extends Thread{
    final private Socket socket;
    final private InputStreamReader inputReader;
    final private OutputStreamWriter outputWriter;
    final private BufferedReader bufferReader;
    final private BufferedWriter bufferWriter;
    private int threadStatus;
    private String userLoggedIn;


    final private static int SUCCESS = 0;
    final private static int THREAD_READY = 0;
    final private static int THREAD_TERMINATING = 1;
    final private static int THREAD_STOPPED = 2;
    final private static int BUFFER_READY = 3;
    final private static int EMPTY_BUFFER = 4;
    final private static int BUFFER_READ_ERROR = 5;
    final private static int BUFFER_WRITE_ERROR = 6;





    public ClientThread(Socket clientSocket) throws IOException {
        this.socket = clientSocket;
        this.inputReader = new InputStreamReader(this.socket.getInputStream());
        this.outputWriter = new OutputStreamWriter(this.socket.getOutputStream());
        this.bufferReader = new BufferedReader(this.inputReader);
        this.bufferWriter = new BufferedWriter(this.outputWriter);
        this.threadStatus = THREAD_READY;
        this.userLoggedIn = null;

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
        while (this.threadStatus == THREAD_READY && (bufferReaderStatus = this.getBufferReaderStatus()) == EMPTY_BUFFER) {}
        if (this.threadStatus == THREAD_TERMINATING) { return THREAD_TERMINATING; }
        if (bufferReaderStatus == BUFFER_READ_ERROR) { return BUFFER_READ_ERROR; }
        if (bufferReaderStatus == BUFFER_READY) { return BUFFER_READY; }
        return THREAD_TERMINATING;
   }




    @Override
    public void run() {
        String receivedMessage;
        int userRequestCode;
        StringBuilder returnArg;
        int commandProcessStatus;
        String sendBack;


        while (this.threadStatus == THREAD_READY) {
            if( this.waitBuffer() != BUFFER_READY ) { this.threadStatus = THREAD_TERMINATING; break; }

            if ((receivedMessage = this.getMessage()) == null) {this.threadStatus = THREAD_TERMINATING; break;}
            userRequestCode = RequestManager.getUserRequestCode(receivedMessage);
            if (userRequestCode == RequestManager.USER_REQUESTED_EXIT) { this.threadStatus = THREAD_TERMINATING; break; }

            returnArg = new StringBuilder();
            commandProcessStatus = RequestManager.processUserRequestCode(this.userLoggedIn, userRequestCode, receivedMessage, returnArg);
            if (commandProcessStatus == RequestManager.FATAL_ERROR) {this.threadStatus = THREAD_TERMINATING; break;}
            if (commandProcessStatus == RequestManager.OK_SUCCESSFUL_LOGIN) { this.userLoggedIn = returnArg.toString(); }
            if (commandProcessStatus == RequestManager.OK_SUCCESSFUL_LOGOUT) { this.userLoggedIn = null; }
            sendBack = RequestManager.getSendBackMessage(commandProcessStatus, returnArg.toString());

            if (this.sendMessage(sendBack) == BUFFER_WRITE_ERROR) { this.threadStatus = THREAD_TERMINATING; break; }

        }

        this.cleanUp();
    }

    private void cleanUp() {
        this.sendMessage("EXIT");
        try {this.closeBufferReader(); } catch(IOException ioException) { System.out.println("Unable to close buffer reader"); }
        try {this.closeBufferWriter(); } catch(IOException ioException) {System.out.println("Unable to close buffer writer"); }
        try {this.closeInputReader();} catch(IOException ioException) {System.out.println("Unable to close input reader"); }
        try {this.closeOutputWriter(); } catch(IOException ioException) {System.out.println("Unable to close output writer"); }
        try {this.closeSocket(); } catch(IOException ioException) { System.out.println("Unable to close server client socket");}
        System.out.printf("Client successfully disconnected: %s\n", this);
        this.threadStatus = THREAD_STOPPED;
    }
    public void stopThread() {
        if (this.threadStatus == THREAD_READY){
            this.threadStatus = THREAD_TERMINATING;
        }
    }


    public int getThreadStatus() {
        return this.threadStatus;
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
