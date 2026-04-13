package jdbc;

import java.util.Locale;
import java.util.Scanner;
import java.io.IOException;
import java.sql.*;

/*
* 
* @author MP
* @version 1.0
* @since 2024-11-07
*/
public class Model {

    static String inputData(String str) throws IOException {
        // IMPLEMENTED
        /*
         * Gets input data from user
         * 
         * @param str Description of required input values
         * 
         * @return String containing comma-separated values
         */
        Scanner key = new Scanner(System.in); // Scanner closes System.in if you call close(). Don't do it
        //System.out.println("Enter corresponding values, separated by commas of:");
        System.out.println(str);
        return key.nextLine();
    }

    static void addUser(User userData, Card cardData) {
        // PARCIALLY IMPLEMENTED
        /**
         * Adds a new user with associated card to the database
         * 
         * @param userData User information
         * @param cardData Card information
         * @throws SQLException if database operation fails
         */
        final String INSERT_PERSON = "INSERT INTO person(email, taxnumber, name) VALUES (?,?,?) RETURNING id";
        final String INSERT_CARD = "INSERT INTO card(credit, typeof, client) VALUES (?,?,?)";
        final String INSERT_USER = "INSERT INTO client(person, dtregister) VALUES (?,?)";
        final String CHECK_PERSON = "SELECT id FROM person WHERE email = ?";

        Connection conn = null;
        PreparedStatement pstmtPerson = null;
        PreparedStatement pstmtCard = null;
        PreparedStatement pstmtUser = null;
        PreparedStatement pstmtCheckUser = null;
        ResultSet rs = null;
        ResultSet generatedKeys = null;
        try {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            pstmtPerson = conn.prepareStatement(INSERT_PERSON, Statement.RETURN_GENERATED_KEYS);
            pstmtCard = conn.prepareStatement(INSERT_CARD);
            pstmtUser = conn.prepareStatement(INSERT_USER);
            pstmtCheckUser = conn.prepareStatement(CHECK_PERSON);
            conn.setAutoCommit(false);

            // Insert person
            pstmtPerson.setString(1, userData.getEmail());
            pstmtPerson.setInt(2, userData.getTaxNumber());
            pstmtPerson.setString(3, userData.getName());

            pstmtCheckUser.setString(1, userData.getEmail());
            rs = pstmtCheckUser.executeQuery();
            if (rs.next()) {
                throw new RuntimeException("Creating person failed, person already exists.");
            } else {
                int affectedPersonRows = pstmtPerson.executeUpdate();
                if (affectedPersonRows == 0) {
                    throw new RuntimeException("Creating person failed, no rows affected.");
                }

                int personId = 0;
                try {
                    generatedKeys = pstmtPerson.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        personId = generatedKeys.getInt(1);
                    } else {
                        throw new RuntimeException("Creating person failed, no ID obtained.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // CONTINUE

                // Insert client
                pstmtUser.setInt(1, personId);
                pstmtUser.setTimestamp(2, new Timestamp(System.currentTimeMillis()));

                int affectedUserRows = pstmtUser.executeUpdate();
                if (affectedUserRows == 0) {
                    throw new RuntimeException("Creating client failed, no rows affected.");
                }

                // Insert card
                pstmtCard.setDouble(1, cardData.getCredit());
                pstmtCard.setString(2, cardData.getReference());
                pstmtCard.setInt(3, personId);

                int affectedCardRows = pstmtCard.executeUpdate();
                if (affectedCardRows == 0) {
                    throw new RuntimeException("Creating client failed, no rows affected.");
                }

                conn.commit();
            }
        } catch (SQLException e) {
            System.out.println("Error on insert values");
            // e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Roll back any changes
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new RuntimeException(e.getMessage());
        }
        finally {
            try {
                if (generatedKeys != null)
                    generatedKeys.close();
                if (rs != null)
                    rs.close();
                if (pstmtCheckUser != null)
                    pstmtCheckUser.close();
			    if (pstmtUser != null)
                    pstmtUser.close();
                if (pstmtCard != null)
                    pstmtCard.close();
                if (pstmtPerson != null)
                    pstmtPerson.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
			} catch (Exception e) {
				e.printStackTrace();
		    }
        }
    }

    /**
     * To implement from this point forward. Do not need to change the code above.
     * -------------------------------------------------------------------------------
     * IMPORTANT:
     * --- DO NOT MOVE IN THE CODE ABOVE. JUST HAVE TO IMPLEMENT THE METHODS BELOW
     * ---
     * -------------------------------------------------------------------------------
     **/

    static void listOrders(String[] orders) throws SQLException {
        /**
         * Lists orders based on specified criteria
         * 
         * @param orders Criteria for listing orders
         * @throws SQLException if database operation fails
         */
        final String QUERY = """
                SELECT dtorder, dtreplacement, roccupation
                FROM replacementorder
                WHERE station = ? AND dtorder BETWEEN ? AND ?
                ORDER BY dtorder DESC
                """;
        /* try(
        Connection conn =
        DriverManager.getConnection(UI.getInstance().getConnectionString());
        PreparedStatement pstmt1 = conn.prepareStatement(VALUE_CMD);
        ){
        }*/
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            pstmt = conn.prepareStatement(QUERY);
            pstmt.setInt(1, Integer.parseInt(orders[0]));  // Station ID
            pstmt.setDate(2, Date.valueOf(orders[1]));     // Start date (in 'YYYY-MM-DD' format)
            pstmt.setDate(3, Date.valueOf(orders[2]));     // End date (in 'YYYY-MM-DD' format)
            try {
                rs = pstmt.executeQuery();
                UI.printResults(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            System.out.println("Error while listing orders: " + e.getMessage());
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (pstmt != null)
                    pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    
    public static void listReplacementOrders(int stationId, Timestamp startDate, Timestamp endDate) throws SQLException {
        /**
         * Lists replacement orders for a specific station in a given time period
         * @param stationId Station ID
         * @param startDate Start date for period
         * @param endDate End date for period
         * @throws SQLException if database operation fails
         */
        /* TO BE DONE
        System.out.print("EMPTY")*/
        final String QUERY = "SELECT * FROM replacementorder WHERE station = ? AND dtorder BETWEEN ? AND ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            pstmt = conn.prepareStatement(QUERY);
            pstmt.setInt(1, stationId);
            pstmt.setTimestamp(2, startDate);
            pstmt.setTimestamp(3, endDate);
            rs = pstmt.executeQuery();
            try{
                UI.printResults(rs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (pstmt != null)
                    pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void travel(String[] values){
        /**
         * Processes a travel operation (start or stop)
         * @param values Array containing [operation, name, station, scooter]
         * @throws SQLException if database operation fails
         */
        // TO BE DONE
        String operation = values[0].toUpperCase();
        int clientId = Integer.parseInt(values[1]);
        int stationId = Integer.parseInt(values[2]);
        int scooterId = Integer.parseInt(values[3]);
        try {
            if (operation.equals("START")) {
                startTravel(clientId, scooterId, stationId);
            } else if (operation.equals("STOP")) {
                stopTravel(clientId, scooterId, stationId, values[4], values[5]);
            } else {
                System.out.println("Invalid operation. Use START or STOP.");
            }
        } catch (SQLException e) {
            System.out.println("Error during travel operation: " + e.getMessage());
        }
    }
    
    public static int getClientId(String name) throws SQLException {
        /** Auxiliar method -- if you want
         * Gets client ID by name from database
         * @param name The name of the client
         * @return client ID or -1 if not found
         * @throws SQLException if database operation fails
         */
        final String QUERY = """
                SELECT c.person 
                FROM client c
                JOIN person p ON c.person = p.id
                WHERE p.name = ?
                """;
        Connection conn =  null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try  {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            pstmt = conn.prepareStatement(QUERY);
            pstmt.setString(1, name);
            try {
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("person");
                } else {
                    return -1; // Client not found
                }
            } catch (SQLException e) {
                throw new SQLException(e);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (pstmt != null)
                    pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void startTravel(int clientId, int scooterId, int stationId) throws SQLException {
        /**
         * Starts a new travel
         * @param clientId Client ID
         * @param scooterId Scooter ID
         * @param stationId Station ID
         * @throws SQLException if database operation fails
         */
        /*System.out.print("EMPTY")*/
        final String CHECK_BALANCE = "SELECT credit FROM card WHERE client = ?";
        final String GET_UNLOCK_FEE = "SELECT unlock FROM servicecost";
        final String INSERT_TRAVEL = """
                INSERT INTO travel (dtinitial, client, scooter, stinitial)
                VALUES (?, ?, ?, ?)
                """;
        final String UPDATE_BALANCE = "UPDATE card SET credit = credit - ? WHERE client = ?";
        Connection conn = null;
        PreparedStatement checkBalanceStmt = null;
        PreparedStatement getUnlockFeeStmt = null;
        PreparedStatement insertTravelStmt = null;
        PreparedStatement updateBalanceStmt = null;
        ResultSet rsScooter = null;
        ResultSet rsClient = null;
        ResultSet rsUnlockFee = null;
        ResultSet rsBalance = null;
        try {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            checkBalanceStmt = conn.prepareStatement(CHECK_BALANCE);
            getUnlockFeeStmt = conn.prepareStatement(GET_UNLOCK_FEE);
            insertTravelStmt = conn.prepareStatement(INSERT_TRAVEL);
            updateBalanceStmt = conn.prepareStatement(UPDATE_BALANCE);
            conn.setAutoCommit(false);


            if (Restriction.canRentScooter(scooterId, clientId)) {
                    throw new SQLException("Scooter is not available for travel at the specified station.");
            }

            if (Restriction.canInitiateTravel(clientId))
                throw new SQLException("Client is already traveling.");


            // Fetch unlock fee
            double unlockFee = 0;
            rsUnlockFee = getUnlockFeeStmt.executeQuery();
            if (rsUnlockFee.next()) {
                unlockFee = rsUnlockFee.getDouble("unlock");
            }

            if (Restriction.hasSufficientCredit(scooterId, unlockFee)) {
                throw new SQLException("Insufficient balance to unlock the scooter.");
            }

            // Insert travel record
            insertTravelStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            insertTravelStmt.setInt(2, clientId);
            insertTravelStmt.setInt(3, scooterId);
            insertTravelStmt.setInt(4, stationId);

            int affectedTravelRows = insertTravelStmt.executeUpdate();
            if (affectedTravelRows == 0) {
                throw new RuntimeException("Creating travel failed, no rows affected.");
            }

            // Deduct unlock fee
            updateBalanceStmt.setDouble(1, unlockFee);
            updateBalanceStmt.setInt(2, clientId);
            int affectedCardRows = updateBalanceStmt.executeUpdate();
            if (affectedCardRows == 0) {
                throw new RuntimeException("Deducting fee failed, no rows affected.");
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Roll back any changes
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new SQLException("Failed to start travel: " + e.getMessage(), e);
        } finally {
            try {
                if (rsBalance != null)
                    rsBalance.close();
                if (rsUnlockFee != null)
                    rsUnlockFee.close();
                if (rsClient != null)
                    rsClient.close();
                if (rsScooter != null)
                    rsScooter.close();
                if (updateBalanceStmt != null)
                    updateBalanceStmt.close();
                if (insertTravelStmt != null)
                    insertTravelStmt.close();
                if (getUnlockFeeStmt != null)
                    getUnlockFeeStmt.close();
                if (checkBalanceStmt != null)
                    checkBalanceStmt.close();
                /*if (checkClientStmt != null)
                    checkClientStmt.close();*/
                /*if (checkScooterStmt != null)
                    checkScooterStmt.close();*/
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    
    public static void stopTravel(int clientId, int scooterId, int stationId, String rating, String comment) throws SQLException {
        /**
         * Stops an ongoing travel
         * @param clientId Client ID
         * @param scooterId Scooter ID
         * @param stationId Destination station ID
         * @throws SQLException if database operation fails
         */
        final String ONGOING_TRAVEL = """
                SELECT dtinitial
                FROM travel
                WHERE client = ? AND scooter = ? AND dtfinal IS NULL
                """;
        final String UPDATE_TRAVEL = """
                UPDATE travel
                SET dtfinal = ?, stfinal = ?, evaluation = ?, comment = ?
                WHERE client = ? AND scooter = ? AND dtfinal IS NULL
                """;
        final String TRAVEL_DURATION = """
                SELECT EXTRACT(EPOCH FROM (dtfinal - dtinitial)) / 60 AS duration_minutes
                FROM travel
                WHERE dtinitial = ? AND client = ?;
                """;
        final String SERVICE_COST = """
                Select usable
                from servicecost
                """;
        final String UPDATE_BALANCE = "UPDATE card SET credit = credit - ? WHERE client = ?";
        Connection conn = null;
        PreparedStatement ongoingTravelStmt = null;
        PreparedStatement updateTravelStmt = null;
        PreparedStatement durationTravelStmt = null;
        PreparedStatement serviceCostStmt = null;
        PreparedStatement balanceUpdateStmt = null;
        ResultSet rsInitTravel = null;
        ResultSet rsdurationTravel = null;
        ResultSet rsServiceCost = null;

        try {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            ongoingTravelStmt = conn.prepareStatement(ONGOING_TRAVEL);
            updateTravelStmt = conn.prepareStatement(UPDATE_TRAVEL);
            durationTravelStmt = conn.prepareStatement(TRAVEL_DURATION);
            serviceCostStmt = conn.prepareStatement(SERVICE_COST);
            balanceUpdateStmt = conn.prepareStatement(UPDATE_BALANCE);
            conn.setAutoCommit(false);

            Timestamp initTravelDate = null;
            ongoingTravelStmt.setInt(1, clientId);
            ongoingTravelStmt.setInt(2, scooterId);
            rsInitTravel = ongoingTravelStmt.executeQuery();
            if(rsInitTravel.next())
                initTravelDate = rsInitTravel.getTimestamp("dtinitial");

            if (initTravelDate == null) {
                System.out.println("No ongoing travel found to stop.");
                return;
            }

            // Update travel record
            Integer nRating;
            if(rating != null) nRating = Integer.parseInt(rating); else nRating = null;
            updateTravelStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            updateTravelStmt.setInt(2, stationId);
            if (nRating != null) updateTravelStmt.setInt(3, nRating);
            else updateTravelStmt.setNull(3, Types.INTEGER);
            updateTravelStmt.setString(4, comment);
            updateTravelStmt.setInt(5, clientId);
            updateTravelStmt.setInt(6, scooterId);
            int affectedTravelRows = updateTravelStmt.executeUpdate();
            if (affectedTravelRows == 0) {
                throw new RuntimeException("Ending travel failed, no rows affected.");
            }

            double travelDuration = 0.0;
            durationTravelStmt.setTimestamp(1, initTravelDate);
            durationTravelStmt.setInt(2, clientId);
            rsdurationTravel = durationTravelStmt.executeQuery();
            if (rsdurationTravel.next()) {
                travelDuration = rsdurationTravel.getDouble("duration_minutes");
            }

            double serviceCost = 0.0;
            rsServiceCost = serviceCostStmt.executeQuery();
            if (rsServiceCost.next()) {
                serviceCost = rsServiceCost.getDouble("usable");
            }

            balanceUpdateStmt.setDouble(1, travelDuration * serviceCost);
            balanceUpdateStmt.setInt(2, clientId);
            balanceUpdateStmt.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Roll back any changes
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new SQLException("Failed to stop travel: " + e.getMessage(), e);
        } finally {
            try {
                if (rsServiceCost != null) rsServiceCost.close();
                if (rsdurationTravel != null) rsdurationTravel.close();
                if (rsInitTravel != null) rsInitTravel.close();
                if (balanceUpdateStmt != null) balanceUpdateStmt.close();
                if (serviceCostStmt != null) serviceCostStmt.close();
                if (ongoingTravelStmt != null) ongoingTravelStmt.close();
                if (durationTravelStmt != null) durationTravelStmt.close();
                if (updateTravelStmt != null) updateTravelStmt.close();
                if (ongoingTravelStmt != null) ongoingTravelStmt.close();
                if(conn != null)
                    conn.setAutoCommit(true);
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateDocks(int stationId, String state, int scooterId) throws SQLException{
        /*/*FILL WITH PARAMETERS ) {
        /* TODO
        System.out.println("updateDocks()");*/
        final String UPDATE_DOCK = """
                UPDATE dock
                SET state = ? 
                WHERE station = ? and scooter = ?
                """;
        Connection conn = null;
        PreparedStatement updateDockStmt = null;
        try {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            updateDockStmt = conn.prepareStatement(UPDATE_DOCK);
            conn.setAutoCommit(false);
            updateDockStmt.setString(1, state.toLowerCase());
            updateDockStmt.setInt(2, stationId);
            updateDockStmt.setInt(3, scooterId);

            int rowsUpdated = updateDockStmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("No dock was updated. Verify the station ID and state.");
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Roll back any changes
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new SQLException("Failed to update dock: " + e.getMessage(), e);
        } finally {
            try {
                if (updateDockStmt != null)
                    updateDockStmt.close();
                if (conn != null)
                    conn.setAutoCommit(true);
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void userSatisfaction() throws SQLException{
        /*FILL WITH PARAMETERS ) {
        // TODO
        System.out.println("userSatisfaction()");*/
        final String User_Satisfaction = """
            SELECT 
                s.model AS scooter_model,
                AVG(t.evaluation) AS average_rating,
                COUNT(t.evaluation) AS total_trips,
                100.0 * SUM(CASE WHEN t.evaluation >= 4 THEN 1 ELSE 0 END) / COUNT(t.evaluation) AS satisfaction_percentage
            FROM 
                travel t
            JOIN 
                scooter s ON t.scooter = s.id
            WHERE 
                t.evaluation IS NOT NULL
            GROUP BY 
                s.model
            HAVING 
                COUNT(t.evaluation) > 0
            ORDER BY 
                AVG(t.evaluation) DESC;
            """;
        Connection conn = null;
        PreparedStatement updateTravelStmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            updateTravelStmt = conn.prepareStatement(User_Satisfaction);
            rs = updateTravelStmt.executeQuery();

            UI.printResults(rs);
        } catch (SQLException e) {
            throw new SQLException("Failed to analyse user satisfaction: " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (updateTravelStmt != null)
                    updateTravelStmt.close();
                if(conn != null)
                    conn.setAutoCommit(true);
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void occupationStation() throws SQLException{
        /*FILL WITH PARAMETERS ) {
        // TODO
        System.out.println("occupationStation()");*/
        final String QUERY = """
                SELECT 
                    s.id as station_ID, 
                    COUNT(d.number) as total_docks,
                    SUM(CASE WHEN d.state = 'occupy' THEN 1 ELSE 0 END) AS occupied_docks,
                    100.0 * SUM(CASE WHEN d.state = 'occupy' THEN 1 ELSE 0 END) / COUNT(d.number) AS occupancy_rate
                FROM station s
                JOIN dock d
                ON s.id = d.station
                GROUP BY s.id
                ORDER BY occupancy_rate DESC
                LIMIT 3
                """;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(UI.getInstance().getConnectionString());
            pstmt = conn.prepareStatement(QUERY);
            try {
                rs = pstmt.executeQuery();
                UI.printResults(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to analyse user satisfaction: " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (pstmt != null)
                    pstmt.close();
                if(conn != null)
                    conn.setAutoCommit(true);
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }    
}