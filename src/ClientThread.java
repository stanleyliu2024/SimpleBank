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

    final private static int USER_REQUESTED_EXIT = 7;
    final private static int USER_REQUESTED_BALANCE = 8;
    final private static int USER_REQUESTED_DEPOSIT = 9;
    final private static int USER_REQUESTED_WITHDRAW = 10;
    final private static int USER_REQUESTED_LOGIN = 11;
    final private static int USER_REQUESTED_LOGOUT = 12;
    final private static int TOO_MANY_CHARACTERS = 13;
    final private static int INVALID_COMMAND = 14;
    final private static int COMMAND_PROCESSING_ERROR = 15;

    final private static int MAX_MESSAGE_LENGTH = 250;





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
    private int getMessageCode(StringBuilder messageToParse) {
        String message = this.getMessage();

        if (message == null) {
            return BUFFER_READ_ERROR;
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return TOO_MANY_CHARACTERS;
        }
        if (message.toLowerCase().equals("exit")) {
            return USER_REQUESTED_EXIT;
        }
        if (message.toLowerCase().startsWith("login")) {
            messageToParse.append(message.toLowerCase());
            return USER_REQUESTED_LOGIN;
        }
        if (message.toLowerCase().startsWith("logout")) {
            messageToParse.append(message.toLowerCase());
            return USER_REQUESTED_LOGOUT;
        }
        if (message.toLowerCase().startsWith("balance")) {
            messageToParse.append(message.toLowerCase());
            return USER_REQUESTED_BALANCE;
        }
        if (message.toLowerCase().startsWith("withdraw")) {
            messageToParse.append(message.toLowerCase());
            return USER_REQUESTED_WITHDRAW;
        }
        if (message.toLowerCase().startsWith("deposit")) {
            messageToParse.append(message.toLowerCase());
            return USER_REQUESTED_DEPOSIT;
        }
        return INVALID_COMMAND;

    }

    private int processUserMessage(final int messageType, final String message) {
        String sendBack = null;
        if (messageType == TOO_MANY_CHARACTERS) {
            sendBack = "OK Too many characters, max is " + MAX_MESSAGE_LENGTH;
        }
        if (messageType == USER_REQUESTED_LOGIN) {
            sendBack = manageLogin(message);
        }
        if (messageType == USER_REQUESTED_LOGOUT) {
            sendBack = manageLogout();
        }
        if (messageType == USER_REQUESTED_BALANCE) {
            sendBack = this.getBalance();
        }
        if (messageType == USER_REQUESTED_DEPOSIT) {
            sendBack = this.handleDeposit(message);
        }
        if (messageType == USER_REQUESTED_WITHDRAW) {
            sendBack = this.handleWithdraw(message);
        }
        if (messageType == INVALID_COMMAND) {
            sendBack = "OK Invalid command";
        }
        if (sendBack == null) { sendBack = "ERROR"; }
        if (this.sendMessage(sendBack) == BUFFER_WRITE_ERROR) { return BUFFER_WRITE_ERROR; }

        if (sendBack == "ERROR") {return COMMAND_PROCESSING_ERROR;}
        return SUCCESS;

    }



    @Override
    public void run() {

        while (this.threadStatus == THREAD_READY) {
            if( this.waitBuffer() != BUFFER_READY ) { this.threadStatus = THREAD_TERMINATING; break; }

            StringBuilder messageToParse = new StringBuilder();
            int userRequest = this.getMessageCode(messageToParse);
            if (userRequest == BUFFER_READ_ERROR) { this.threadStatus = THREAD_TERMINATING; break; }
            if (userRequest == USER_REQUESTED_EXIT) { this.threadStatus = THREAD_TERMINATING; break; }

            int commandProcessStatus = this.processUserMessage(userRequest, messageToParse.toString());
            if (commandProcessStatus != SUCCESS) {this.threadStatus = THREAD_TERMINATING; break; }


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



    private String manageLogin(final String loginInfo) {
        if (this.userLoggedIn != null) {return "OK Error: Already logged in!!!";}
        String[] userInfo = loginInfo.split(" ");
        if (userInfo.length != 3) {return "OK Error: Improper number of arguments provided!!!";}

        final int matchStatus = SimpleBankSQLConnector.matchUserPass(userInfo[1], userInfo[2]);
        if (matchStatus == SimpleBankSQLConnector.SQL_CONNECT_ERROR) { return "OK Error: Unable to connect to SQL server"; }
        else if (matchStatus == SimpleBankSQLConnector.NO_QUERY_FOUND) { return "OK Error: Incorrect username or password"; }
        else if (matchStatus == SimpleBankSQLConnector.SUCCESS)
        {
            this.userLoggedIn = userInfo[1];
            return "OK Successfully logged in";
        }

        return "ERROR";


    }

    private String manageLogout() {
        if (this.userLoggedIn == null) {
            return "OK Error: No user logged in";
        }
        else {
            this.userLoggedIn = null;
            return "OK Successfully logged out!!!";
        }
    }

    private String getBalance() {
        if (this.userLoggedIn == null) { return "OK Error: No user logged in"; }
        StringBuilder amount = new StringBuilder();
        final int sqlStatus = SimpleBankSQLConnector.matchUserAmount(amount, this.userLoggedIn);
        if (sqlStatus == SimpleBankSQLConnector.SQL_CONNECT_ERROR) { return "OK Error: Unable to connect to SQL server"; }
        else if (sqlStatus == SimpleBankSQLConnector.NO_QUERY_FOUND) { return "OK Error: Could not find balance"; }
        else if (sqlStatus == SimpleBankSQLConnector.SUCCESS) {
            return String.format("OK Balance is %s", amount.toString());

        }

        return "ERROR";


    }

    private String handleDeposit(final String depositInfo) {
        if (this.userLoggedIn == null) { return "OK Error: No user logged in"; }
        if (depositInfo.split(" ").length != 2) { return "OK Error: Improper number of arguments provided!!!";}

        int depositAmount = 0;
        try {depositAmount = Integer.parseInt(depositInfo.split(" ")[1]);}
        catch (NumberFormatException nfe) {return "OK Error: Deposit not a number";}

        final int sqlStatus = SimpleBankSQLConnector.updateUserAmount(this.userLoggedIn, depositAmount, "+");
        if (sqlStatus == SimpleBankSQLConnector.SQL_CONNECT_ERROR) { return "OK Error: Unable to connect to SQL server"; }
        else if (sqlStatus == SimpleBankSQLConnector.NO_UPDATE_MADE) { return "OK Error: Could not deposit"; }
        else if (sqlStatus == SimpleBankSQLConnector.SUCCESS) {
            return String.format("OK Deposit has been made, please check your balance to confirm");

        }
        return "ERROR";
    }

    private String handleWithdraw(final String withdrawInfo) {
        if (this.userLoggedIn == null) {return "OK Error: No user logged in";}
        if (withdrawInfo.split(" ").length != 2) {return "OK Error: Improper number of arguments provided!!!";}

        int withdrawAmount = 0;
        try {withdrawAmount = Integer.parseInt(withdrawInfo.split(" ")[1]);}
        catch (NumberFormatException nfe) {return "OK Error: Withdraw not a number";}

        final int sqlStatus = SimpleBankSQLConnector.updateUserAmount(this.userLoggedIn, withdrawAmount, "-");
        if (sqlStatus == SimpleBankSQLConnector.SQL_CONNECT_ERROR) {return "OK Error: Unable to connect to SQL server";}
        else if (sqlStatus == SimpleBankSQLConnector.NO_UPDATE_MADE) {return "OK Error: Could not withdraw";}
        else if (sqlStatus == SimpleBankSQLConnector.SUCCESS) {
            return String.format("OK Withdrawal has been made, please check your balance to confirm");

        }
        return "ERROR";
    }
}
