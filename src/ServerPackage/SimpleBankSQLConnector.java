package ServerPackage;

import java.sql.*;

final class SimpleBankSQLConnector {
    final static private String connectionInfo = "jdbc:mysql://localhost:3306/SimpleBankSQL";
    final static private String connectionUsername = "ClientThread";
    final static private String connectionPassword = "Password";

    final static public int SQL_CONNECT_ERROR = 1;
    final static public int SUCCESS = 0;
    final static public int NO_QUERY_FOUND = 2;
    final static public int NO_UPDATE_MADE = 3;
    static int matchUserPass(String username, String password) {
        String query = String.format("SELECT username " +
                        "FROM username_password " +
                        "WHERE username = '%s' AND pass = '%s'",
                username, password);

        try (Connection connection = DriverManager.getConnection(connectionInfo, connectionUsername, connectionPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                return SUCCESS;
            } else {
                return NO_QUERY_FOUND;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return SQL_CONNECT_ERROR;
        }


    }

    static int matchUserAmount(StringBuilder amount, String username) {
        String query = String.format("SELECT amount " +
                        "FROM username_amount " +
                        "WHERE username = '%s'",
                username);

        try (Connection connection = DriverManager.getConnection(connectionInfo, connectionUsername, connectionPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                amount.append(resultSet.getString("amount"));
                return SUCCESS;
            } else {
                return NO_QUERY_FOUND;
            }
        } catch (SQLException sqlException) {
            return SQL_CONNECT_ERROR;
        }


    }

    static int updateUserAmount(String username, int amount, String operator) {
        String query = String.format("UPDATE username_amount " +
                "SET amount = amount %s %d " +
                "WHERE username = '%s'", operator, amount, username);

        int rowsAffected = 0;
        try (Connection connection = DriverManager.getConnection(connectionInfo, connectionUsername, connectionPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {
            rowsAffected = statement.executeUpdate(query);

        } catch (SQLException sqlException) {
            return SQL_CONNECT_ERROR;
        }

        if (rowsAffected == 0) { return NO_UPDATE_MADE; }
        return SUCCESS;


    }




}
