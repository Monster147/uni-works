package jdbc;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

interface DbWorker {
    void doWork();
}

/*
 * 
 * @author MP
 * 
 * @version 1.0
 * 
 * @since 2024-11-07
 */
class UI {
    private enum Option {
        // DO NOT CHANGE ANYTHING!
        Unknown,
        Exit,
        novelUser,
        listReplacementOrder,
        startStopTravel,
        updateDocks,
        userSatisfaction,
        occupationStation,
    }

    private static UI __instance = null;
    private String __connectionString;

    private HashMap<Option, DbWorker> __dbMethods;

    private UI() {
        // DO NOT CHANGE ANYTHING!
        __dbMethods = new HashMap<Option, DbWorker>();
        __dbMethods.put(Option.novelUser, () -> UI.this.novelUser());
        __dbMethods.put(Option.listReplacementOrder, () -> UI.this.listReplacementOrder());
        __dbMethods.put(Option.startStopTravel, () -> UI.this.startStopTravel());
        __dbMethods.put(Option.updateDocks, () -> UI.this.updateDocks());
        __dbMethods.put(Option.userSatisfaction, () -> UI.this.userSatisfaction());
        __dbMethods.put(Option.occupationStation, new DbWorker() {
            public void doWork() {
                UI.this.occupationStation();
            }
        });
    }

    public static UI getInstance() {
        if (__instance == null) {
            __instance = new UI();
        }
        return __instance;
    }

    private Option DisplayMenu() {
        Option option = Option.Unknown;
        try {
            // DO NOT CHANGE ANYTHING!
            System.out.println("Electric Scooter Sharing");
            System.out.println();
            System.out.println("1. Exit");
            System.out.println("2. Novel users");
            System.out.println("3. List of replacements order at a station over a period of time");
            System.out.println("4. Start/Stop a travel");
            System.out.println("5. Update docks' state");
            System.out.println("6. User satisfaction ratings");
            System.out.println("7. List of station");
            System.out.print(">");
            Scanner s = new Scanner(System.in);
            int result = s.nextInt();
            option = Option.values()[result];
        } catch (RuntimeException ex) {
            // nothing to do.
        }
        return option;

    }

    private static void clearConsole() throws Exception {
        for (int y = 0; y < 25; y++) // console is 80 columns and 25 lines
            System.out.println("\n");

    }

    private void Login() throws java.sql.SQLException {
        Connection con = DriverManager.getConnection(getConnectionString());
        if (con != null)
            con.close();
    }

    public void Run() throws Exception {
        Login();
        Option userInput;
        do {
            clearConsole();
            userInput = DisplayMenu();
            clearConsole();
            try {
                __dbMethods.get(userInput).doWork();
                System.in.read();

            } catch (NullPointerException ex) {
                // Nothing to do. The option was not a valid one. Read another.
            }

        } while (userInput != Option.Exit);
    }

    public String getConnectionString() {
        return __connectionString;
    }

    public void setConnectionString(String s) {
        __connectionString = s;
    }

    /**
     * To implement from this point forward. Do not need to change the code above.
     * -------------------------------------------------------------------------------
     * IMPORTANT:
     * --- DO NOT MOVE IN THE CODE ABOVE. JUST HAVE TO IMPLEMENT THE METHODS BELOW
     * ---
     * -------------------------------------------------------------------------------
     * 
     */

    private static final int TAB_SIZE = 24;

    static void printResults(ResultSet dr) throws SQLException {
        ResultSetMetaData smd = dr.getMetaData();
        int columnCount = smd.getColumnCount();

        // Calculate column widths dynamically (including header lengths)
        int[] columnWidths = new int[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnWidths[i - 1] = Math.max(22, smd.getColumnLabel(i).length() + 2);
        }

        // Print horizontal line
        printHorizontalLine(columnWidths);

        // Print column headers
        System.out.print("|");
        for (int i = 1; i <= columnCount; i++) {
            System.out.printf(" %-" + (columnWidths[i - 1] - 2) + "s |", smd.getColumnLabel(i));
        }
        System.out.println();

        // Print horizontal line (header/footer separator)
        printHorizontalLine(columnWidths);

        // Print result rows
        while (dr.next()) {
            System.out.print("|");
            for (int i = 1; i <= columnCount; i++) {
                Object columnValue = dr.getObject(i);
                String displayValue;

                if (columnValue == null) {
                    displayValue = "N/A";
                } else if (columnValue instanceof java.sql.Timestamp) {
                    if (columnValue != null) {
                        displayValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((java.sql.Timestamp) columnValue);
                    } else {
                        displayValue = "N/A";
                    }
                } else if (columnValue instanceof java.sql.Date) {
                    java.sql.Date date = (java.sql.Date) columnValue;
                    displayValue = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
                } else {
                    // Use column label to determine custom formatting
                    String columnLabel = dr.getMetaData().getColumnLabel(i).toLowerCase();
                    if ("satisfaction_percentage".equals(columnLabel) || "occupancy_rate".equals(columnLabel)) {
                        // Format satisfaction_percentage with two decimals and percentage symbol
                        double value = ((Number) columnValue).doubleValue();
                        displayValue = String.format("%.2f%%", value);
                    } else if ("average_rating".equals(columnLabel)) {
                        // Format average_rating with two decimals
                        double value = ((Number) columnValue).doubleValue();
                        displayValue = String.format("%.2f", value);
                    } else {
                        // Default: use the string representation
                        displayValue = columnValue.toString();
                    }
                }

                final int MAX_COLUMN_LENGTH = 25; // Define a max length
                if (displayValue.length() > MAX_COLUMN_LENGTH) {
                    displayValue = displayValue.substring(0, MAX_COLUMN_LENGTH - 3) + "...";
                }

                System.out.printf(" %-" + (columnWidths[i - 1] - 2) + "s |", displayValue);
            }
            System.out.println();
        }

        // Print closing horizontal line
        printHorizontalLine(columnWidths);
    }

    // Helper method to print horizontal lines dynamically
    private static void printHorizontalLine(int[] columnWidths) {
        System.out.print("+");
        for (int width : columnWidths) {
            System.out.print("-".repeat(width) + "+");
        }
        System.out.println();
    }

    private void novelUser() {
        // IMPLEMENTED
        System.out.println("Values must be separated by commas.");
        try {
            String user = Model.inputData("Enter data for a new user (email, tax number, name):\n");
            String card = Model.inputData("Enter data for card acquisition (credit, reference type and, if you want, specify a client):\n");

            // IMPORTANT: The values entered must be separated by a comma with no blank
            // spaces, with the proper order
            User userData = new User(user.split(","));
            Card cardData = new Card(card.split(","));
            Model.addUser(userData, cardData);
            System.out.println("Inserted with success!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void listReplacementOrder() {
        // IMPLEMENTED
        System.out.println("Values must be separated by commas.");
        try {
            // IMPORTANT: The values entered must be separated by a comma with no blank
            // spaces
            String orders = Model.inputData("Enter the station number and the time interval (in the YYYY-MM-DD format):\n");
            String[] parsedData = orders.split(",");

            // Call Model method to list orders
            Model.listOrders(parsedData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startStopTravel() {
        /*// TODO
        System.out.println("startStopTravel()");*/
        System.out.println("Values must be separated by commas.");
        try {
            String input = Model.inputData("""
                    Enter operation (START/STOP), user ID, station ID, and scooterID.
                    For the STOP operation, you can also add a rating and a comment (user ID, station ID, scooterID, rating, comment):
                    """);
            String[] data = input.split(",");
            if (data[0].equalsIgnoreCase("START")) {
                String[] travelData = {"START", data[1], data[2], data[3]}; // Operation, userId, stationId, scooterId
                Model.travel(travelData);
                System.out.print("Started travel successfully!");
            } else if (data[0].equalsIgnoreCase("STOP")) {
                String[] travelData = null;
                if (data.length == 4){
                travelData = new String[]{"STOP", data[1], data[2], data[3], null, null}; // Operation, userId, stationId, scooterId, rating, comment
                } else if (data.length == 5) {
                    travelData = new String[]{"STOP", data[1], data[2], data[3], data[4], null}; // Operation, userId, stationId, scooterId, rating, comment
                } else {
                    String rating = data[4].trim();
                    if (rating.isEmpty()) {
                        throw new IllegalArgumentException("Cannot have a comment if you didn't enter a rating.");
                    }
                    travelData = new String[]{"STOP", data[1], data[2], data[3], data[4], data[5]}; // Operation, userId, stationId, scooterId, rating, comment
                }
                Model.travel(travelData);
                System.out.print("Stopped travel successfully!");
            } else {
                System.out.println("Invalid operation. Please choose START or STOP.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateDocks() {
        /*// TODO
        System.out.println("updateDocks()");*/
        System.out.println("Values must be separated by commas.");
        try {
            String input = Model.inputData("Enter station ID, new state (FREE/OCCUPY/UNDER MAINTENANCE) and scooter ID:\n");
            String[] data = input.split(",");
            int stationId = Integer.parseInt(data[0]);
            String state = data[1].toUpperCase();
            int scooterId = Integer.parseInt(data[2]);
            if (dockState(state)) {
            Model.updateDocks(stationId, state, scooterId); // stationId, state, scooterId
            System.out.println("Dock updated successfully.");
        } else {
            System.out.println("Invalid state. Please choose FREE, OCCUPY or UNDER MAINTENANCE.");
        }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean dockState(String state) {
        return state.equalsIgnoreCase("FREE") || state.equalsIgnoreCase("OCCUPY") || state.equalsIgnoreCase("UNDER MAINTENANCE");
    }

    private void userSatisfaction() {
        /*// TODO
        System.out.println("userSatisfaction()");*/
        try {
            System.out.println("Analyzing user satisfaction...");
            Model.userSatisfaction(); // Call the function from the Model file
        } catch (SQLException e) {
            System.out.println("An error occurred during the analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void occupationStation() {
        /*// TODO
        System.out.println("occupationStation()");*/

        try {
            System.out.println("Analyzing occupation of stations...");
            Model.occupationStation();  // Print the occupation report
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

public class App {

    public static void main(String[] args) throws Exception {
        DatabaseProperties.load();
        String url = String.format("%s?user=%s&password=%s&ssl=false", DatabaseProperties.getUrl(),
                DatabaseProperties.getUser(), DatabaseProperties.getPassword());

        UI.getInstance().setConnectionString(url);
        UI.getInstance().Run();
    }
}