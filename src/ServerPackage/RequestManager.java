package ServerPackage;
import java.util.Map;
import java.util.HashMap;
final class RequestManager {
    final static int USER_REQUESTED_EXIT = 7;
    final static int USER_REQUESTED_BALANCE = 8;
    final static int USER_REQUESTED_DEPOSIT = 9;
    final static int USER_REQUESTED_WITHDRAW = 10;
    final static int USER_REQUESTED_LOGIN = 11;
    final static int USER_REQUESTED_LOGOUT = 12;
    final static int TOO_MANY_CHARACTERS = 13;
    final static int INVALID_COMMAND = 14;
    final static int MAX_MESSAGE_LENGTH = 250;

    final static int OK_TOO_MANY_CHARACTERS = 32;
    final static int OK_UNABLE_SQL_CONNECT = 33;
    final static int OK_ALREADY_LOGGED_IN = 34;
    final static int OK_NO_USER_LOGGED_IN = 35;
    final static int OK_INCORRECT_CREDENTIALS = 36;
    final static int OK_IMPROPER_NUMBER_OF_ARGUMENTS = 37;
    final static int OK_SUCCESSFUL_LOGIN = 38;
    final static int OK_SUCCESSFUL_LOGOUT = 39;
    final static int OK_NO_BALANCE_FOUND = 40;
    final static int OK_BALANCE_FOUND = 41;
    final static int OK_ARG_NOT_A_NUMBER = 42;
    final static int OK_DEPOSIT_FAIL = 43;
    final static int OK_DEPOSIT_SUCCESS = 44;
    final static int OK_WITHDRAW_FAIL = 45;
    final static int OK_WITHDRAW_SUCCESS = 46;
    final static int FATAL_ERROR = 47;
    final static int OK_INVALID_COMMAND = 48;



    final static Map<Integer, String> messageCodeMap = new HashMap<>() {{
            put(OK_TOO_MANY_CHARACTERS, "OK Too many characters, max is " + MAX_MESSAGE_LENGTH);
            put(OK_UNABLE_SQL_CONNECT, "OK Error: Unable to connect to SQL server");
            put(OK_ALREADY_LOGGED_IN, "OK Error: Already logged in!!!");
            put(OK_NO_USER_LOGGED_IN, "OK Error: No user logged in");
            put(OK_INCORRECT_CREDENTIALS, "OK Error: Incorrect username or password");
            put(OK_IMPROPER_NUMBER_OF_ARGUMENTS, "OK Error: Improper number of arguments provided!!!");
            put(OK_SUCCESSFUL_LOGIN, "OK Successfully logged in as %s!!!");
            put(OK_SUCCESSFUL_LOGOUT, "OK Successfully logged out!!!");
            put(OK_NO_BALANCE_FOUND, "OK Error: Could not find balance");
            put(OK_BALANCE_FOUND, "OK Balance is %s");
            put(OK_ARG_NOT_A_NUMBER, "OK Error: Argument not a number");
            put(OK_DEPOSIT_FAIL, "OK Error: Deposit was not made");
            put(OK_DEPOSIT_SUCCESS, "OK Successfully deposited");
            put(OK_WITHDRAW_FAIL, "OK Error: Withdrawal was not made");
            put(OK_WITHDRAW_SUCCESS, "OK Successfully withdrew");
            put(OK_INVALID_COMMAND, "OK Error: Invalid command");

            put(FATAL_ERROR, "ERROR");


    }};

    static int getUserRequestCode(String message) {
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return TOO_MANY_CHARACTERS;
        }
        if (message.toLowerCase().equals("exit")) {
            return USER_REQUESTED_EXIT;
        }
        if (message.toLowerCase().startsWith("login")) {
            return USER_REQUESTED_LOGIN;
        }
        if (message.toLowerCase().startsWith("logout")) {
            return USER_REQUESTED_LOGOUT;
        }
        if (message.toLowerCase().startsWith("balance")) {
            return USER_REQUESTED_BALANCE;
        }
        if (message.toLowerCase().startsWith("withdraw")) {
            return USER_REQUESTED_WITHDRAW;
        }
        if (message.toLowerCase().startsWith("deposit")) {
            return USER_REQUESTED_DEPOSIT;
        }
        return INVALID_COMMAND;
    }

    static int processUserRequestCode(String userLoggedIn, final int messageType, final String message, StringBuilder returnArg) {

        if (messageType == TOO_MANY_CHARACTERS)
        {return OK_TOO_MANY_CHARACTERS;}

        if (messageType == USER_REQUESTED_LOGIN) {
            return manageLogin(userLoggedIn, message, returnArg);
        }
        if (messageType == USER_REQUESTED_LOGOUT) {
            return manageLogout(userLoggedIn);
        }
        if (messageType == USER_REQUESTED_BALANCE) {
            return getBalance(userLoggedIn, returnArg);
        }
        if (messageType == USER_REQUESTED_DEPOSIT) {
            return handleDeposit(userLoggedIn, message);
        }
        if (messageType == USER_REQUESTED_WITHDRAW) {
            return handleWithdraw(userLoggedIn, message);
        }
        if (messageType == INVALID_COMMAND) {
            return OK_INVALID_COMMAND;
        }

        return FATAL_ERROR;


    }

    private static int manageLogin(final String userLoggedIn, final String loginInfo, StringBuilder returnArg) {
        if (userLoggedIn != null) {return OK_ALREADY_LOGGED_IN;}
        String[] userInfo = loginInfo.split(" ");
        if (userInfo.length != 3) {return OK_IMPROPER_NUMBER_OF_ARGUMENTS;}

        final int matchStatus = SimpleBankSQLConnector.matchUserPass(userInfo[1], userInfo[2]);
        if (matchStatus == SimpleBankSQLConnector.SQL_CONNECT_ERROR) { return OK_UNABLE_SQL_CONNECT; }
        else if (matchStatus == SimpleBankSQLConnector.NO_QUERY_FOUND) { return OK_INCORRECT_CREDENTIALS; }
        else if (matchStatus == SimpleBankSQLConnector.SUCCESS)
        {
            returnArg.append(userInfo[1]);
            return OK_SUCCESSFUL_LOGIN;
        }
        return FATAL_ERROR;


    }

    private static int manageLogout(final String userLoggedIn) {
        if (userLoggedIn == null) {
            return OK_NO_USER_LOGGED_IN;
        }
        else{
            return OK_SUCCESSFUL_LOGOUT;
        }
    }

    private static int getBalance(String userLoggedIn, StringBuilder returnArg) {
        if (userLoggedIn == null) { return OK_NO_USER_LOGGED_IN; }
        StringBuilder amount = new StringBuilder();
        final int sqlStatus = SimpleBankSQLConnector.matchUserAmount(amount, userLoggedIn);
        if (sqlStatus == SimpleBankSQLConnector.SQL_CONNECT_ERROR) { return OK_UNABLE_SQL_CONNECT; }
        else if (sqlStatus == SimpleBankSQLConnector.NO_QUERY_FOUND) { return OK_NO_BALANCE_FOUND; }
        else if (sqlStatus == SimpleBankSQLConnector.SUCCESS) {
            returnArg.append(amount.toString());
            return OK_BALANCE_FOUND;
        }

        return FATAL_ERROR;
    }

    private static int handleDeposit(final String userLoggedIn, final String depositInfo) {
        if (userLoggedIn == null) { return OK_NO_USER_LOGGED_IN; }
        if (depositInfo.split(" ").length != 2) { return OK_IMPROPER_NUMBER_OF_ARGUMENTS;}

        int depositAmount = 0;
        try {depositAmount = Integer.parseInt(depositInfo.split(" ")[1]);}
        catch (NumberFormatException nfe) {return OK_ARG_NOT_A_NUMBER;}

        final int sqlStatus = SimpleBankSQLConnector.updateUserAmount(userLoggedIn, depositAmount, "+");
        if (sqlStatus == SimpleBankSQLConnector.SQL_CONNECT_ERROR) { return OK_UNABLE_SQL_CONNECT; }
        else if (sqlStatus == SimpleBankSQLConnector.NO_UPDATE_MADE) { return OK_DEPOSIT_FAIL; }
        else if (sqlStatus == SimpleBankSQLConnector.SUCCESS) {
            return OK_DEPOSIT_SUCCESS;

        }
        return FATAL_ERROR;
    }

    private static int handleWithdraw(final String userLoggedIn, final String withdrawInfo) {
        if (userLoggedIn == null) {return OK_NO_USER_LOGGED_IN;}
        if (withdrawInfo.split(" ").length != 2) {return OK_IMPROPER_NUMBER_OF_ARGUMENTS;}

        int withdrawAmount = 0;
        try {withdrawAmount = Integer.parseInt(withdrawInfo.split(" ")[1]);}
        catch (NumberFormatException nfe) {return OK_ARG_NOT_A_NUMBER;}

        final int sqlStatus = SimpleBankSQLConnector.updateUserAmount(userLoggedIn, withdrawAmount, "-");
        if (sqlStatus == SimpleBankSQLConnector.SQL_CONNECT_ERROR) {return OK_UNABLE_SQL_CONNECT;}
        else if (sqlStatus == SimpleBankSQLConnector.NO_UPDATE_MADE) {return OK_WITHDRAW_FAIL;}
        else if (sqlStatus == SimpleBankSQLConnector.SUCCESS) {
            return OK_WITHDRAW_SUCCESS;
        }
        return FATAL_ERROR;
    }


    static String getSendBackMessage(final int commandProcessStatus, final String returnArg) {
        if (returnArg == null || returnArg == "") {
            return messageCodeMap.get(commandProcessStatus);
        }
        return String.format(messageCodeMap.get(commandProcessStatus), returnArg);
    }
}

