package jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Restriction {
    // Method to check if a client can rent a scooter based on restrictions
    public static boolean canRentScooter(int scooterId, int clientId) throws SQLException {
        String query = "SELECT * FROM dock WHERE scooter = ? AND station = ? AND state = 'free'";
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try  {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            statement = conn.prepareStatement(query);
            statement.setInt(1, scooterId);
            statement.setInt(2, clientId);
            rs = statement.executeQuery();

            // If no rows are returned, the scooter is not available
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (statement != null) statement.close();
            if (conn != null) conn.close();
        }
    }

    public static boolean canInitiateTravel(int clientId) throws SQLException {
        final String CHECK_CLIENT = "SELECT * FROM travel WHERE client = ? AND dtfinal IS NULL";
        Connection conn = null;
        PreparedStatement checkClientStmt = null;
        ResultSet rs = null;
        try{
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            checkClientStmt = conn.prepareStatement(CHECK_CLIENT);

            checkClientStmt.setInt(1, clientId);
            rs = checkClientStmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (checkClientStmt != null) checkClientStmt.close();
            if (conn != null) conn.close();
        }
    }

    // Method to check if a client has enough credit on their card to rent a scooter
    public static boolean hasSufficientCredit(int clientId, double rentalPrice) throws SQLException {
        final String CHECK_BALANCE = "SELECT credit FROM card WHERE client = ?";
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rsCheckBalance = null;
        try  {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            statement = conn.prepareStatement(CHECK_BALANCE);
            statement.setInt(1, clientId);
            rsCheckBalance = statement.executeQuery();

            if (rsCheckBalance.next()) {
                double credit = rsCheckBalance.getDouble("credit");
                return credit >= rentalPrice;
            } else {
                return false; // No card found for the client
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (rsCheckBalance != null) rsCheckBalance.close();
            if (statement != null) statement.close();
            if (conn != null) conn.close();
        }
    }

    /*// Method to check if a client can extend their rental period (e.g., due to maximum limits)
    public boolean canExtendRentalPeriod(int clientId, Timestamp newEndDate) throws SQLException {
        String query = "SELECT dtfinal FROM TRAVEL WHERE client = ? AND dtfinal > ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, clientId);
            statement.setTimestamp(2, newEndDate);
            ResultSet resultSet = statement.executeQuery();

            // If there's an existing travel with an end date greater than the new end date, extension is not possible
            return !resultSet.next();
        }
    }*/

    // Method to apply a top-up to a client's card
    public boolean applyTopUp(int clientId, double amount) throws SQLException {
        String query = "UPDATE CARD SET credit = credit + ? WHERE client = ?";

        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            statement = conn.prepareStatement(query);
            statement.setDouble(1, amount);
            statement.setInt(2, clientId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0; // If at least one row is updated, the top-up is successful
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean checkScooterBattery(int scooterID) throws SQLException {
        final String GET_BATTERY = "SELECT battery FROM scooter WHERE id = ?";
        final String GET_AUTONOMY = """
        SELECT SM.autonomy
        FROM scootermodel SM
        JOIN scooter S ON S.model = SM.number
        WHERE S.id = ?
    """;
        Connection conn = null;
        PreparedStatement batteryStmt = null;
        PreparedStatement autonomyStmt = null;
        ResultSet batteryResult = null;
        ResultSet autonomyResult = null;

        try {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());

            batteryStmt = conn.prepareStatement(GET_BATTERY);
            batteryStmt.setInt(1, scooterID);
            batteryResult = batteryStmt.executeQuery();

            if (!batteryResult.next()) {
                throw new SQLException("Scooter not found with ID: " + scooterID);
            }
            int battery = batteryResult.getInt("battery");

            // Get the autonomy
            autonomyStmt = conn.prepareStatement(GET_AUTONOMY);
            autonomyStmt.setInt(1, scooterID);
            autonomyResult = autonomyStmt.executeQuery();

            if (!autonomyResult.next()) {
                throw new SQLException("Scooter model not found for scooter ID: " + scooterID);
            }
            int autonomy = autonomyResult.getInt("autonomy");

            // Check if battery level is sufficient
            return battery <= autonomy;
        } finally {
            // Close all resources
            try {
                if (batteryResult != null) batteryResult.close();
                if (autonomyResult != null) autonomyResult.close();
                if (batteryStmt != null) batteryStmt.close();
                if (autonomyStmt != null) autonomyStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}