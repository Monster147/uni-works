/*
MIT License

Copyright (c) 2025, Nuno Datia, Matilde Pato, ISEL

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package isel.sisinf.ui;

import java.util.Scanner;
import java.util.HashMap;
import java.util.List;

import isel.sisinf.jpa.ClientRepository;
import isel.sisinf.jpa.DocksRepository;
import isel.sisinf.jpa.ScooterRepository;
import isel.sisinf.jpa.TripRepository;
import isel.sisinf.model.*;
import jakarta.persistence.PersistenceException;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

/**
 * 
 * Didactic material to support
 * to the curricular unit of
 * Introduction to Information Systems
 *
 * The examples may not be complete and/or totally correct.
 * They are made available for teaching and learning purposes and
 * any inaccuracies are the subject of debate.
 */

interface DbWorker {
    void doWork();
}

class UI {
    private enum Option {
        // DO NOT CHANGE ANYTHING!
        Unknown,
        Exit,
        createCostumer,
        listCostumer,
        listDocks,
        startTrip,
        parkScooter,
        about
    }

    private static UI __instance = null;

    private HashMap<Option, DbWorker> __dbMethods;

    private UI() {
        // DO NOT CHANGE ANYTHING!
        __dbMethods = new HashMap<Option, DbWorker>();
        __dbMethods.put(Option.createCostumer, () -> UI.this.createCostumer());
        __dbMethods.put(Option.listCostumer, () -> UI.this.listCostumer());
        __dbMethods.put(Option.listDocks, () -> UI.this.listDocks());
        __dbMethods.put(Option.startTrip, new DbWorker() {
            public void doWork() {
                UI.this.startTrip();
            }
        });
        __dbMethods.put(Option.parkScooter, new DbWorker() {
            public void doWork() {
                UI.this.parkScooter();
            }
        });
        __dbMethods.put(Option.about, new DbWorker() {
            public void doWork() {
                UI.this.about();
            }
        });
    }

    public static UI getInstance() {
        // DO NOT CHANGE ANYTHING!
        if (__instance == null) {
            __instance = new UI();
        }
        return __instance;
    }

    private Option DisplayMenu() {
        Option option = Option.Unknown;
        Scanner s = new Scanner(System.in); // Scanner closes System.in if you call close(). Don't do it
        try {
            // DO NOT CHANGE ANYTHING!
            System.out.println("CITES Manadgement DEMO");
            System.out.println();
            System.out.println("1. Exit");
            System.out.println("2. Create Costumer");
            System.out.println("3. List Existing Costumer");
            System.out.println("4. List Docks");
            System.out.println("5. Start Trip");
            System.out.println("6. Park Scooter");
            System.out.println("7. About");
            System.out.print(">");
            int result = s.nextInt();
            option = Option.values()[result];
        } catch (RuntimeException ex) {
            // nothing to do.
        }

        return option;

    }

    private static void clearConsole() throws Exception {
        // DO NOT CHANGE ANYTHING!
        for (int y = 0; y < 25; y++) // console is 80 columns and 25 lines
            System.out.println("\n");
    }

    public void Run() throws Exception {
        // DO NOT CHANGE ANYTHING!
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

    /**
     * To implement from this point forward.
     * -------------------------------------------------------------------------------------
     * IMPORTANT:
     * --- DO NOT MESS WITH THE CODE ABOVE. YOU JUST HAVE TO IMPLEMENT THE METHODS
     * BELOW ---
     * --- Other Methods and properties can be added to support implementation.
     * ---- Do that also below -----
     * -------------------------------------------------------------------------------------
     * 
     */

    private static final int TAB_SIZE = 24;

    private void createCostumer() {
        Scanner scanner = new Scanner(System.in);
        ClientRepository repository = new ClientRepository();

        try {
            System.out.println("CREATE NEW CUSTOMER");
            System.out.println("-------------------");

            System.out.print("Email: ");
            String email = scanner.nextLine();
            if (!email.contains("@")) {
                System.out.println("Error: Email must contain '@'");
                return;
            }

            System.out.print("Tax Number: ");
            int taxNumber = Integer.parseInt(scanner.nextLine());

            System.out.print("Full Name: ");
            String name = scanner.nextLine();
            if (name.matches(".*\\d.*")) {
                System.out.println("Error: Name must not contain a number");
                return;
            }

            System.out.print("Card Type (resident/tourist): ");
            String cardType = scanner.nextLine().toLowerCase();

            if (!cardType.equals("resident") && !cardType.equals("tourist")) {
                System.out.println("Error: Invalid card type. Must be 'resident' or 'tourist'");
                return;
            }

            System.out.print("Initial Credit: ");
            double credit = Double.parseDouble(scanner.nextLine());

            // Create entities
            Person person = new Person();
            person.setEmail(email);
            person.setTaxNumber(taxNumber);
            person.setName(name);

            repository.createCustomer(person, cardType, credit);

            System.out.println("Customer created successfully!");
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format");
        } catch (PersistenceException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            repository.close();
            System.out.println("\nPress Enter to continue...");
        }
    }

    private void listCostumer() {
        ClientRepository repository = new ClientRepository();
        try {
            List<CustomerPassInfoDTO> customers = repository.getAllCustomersWithCardInfo();

            if (customers.isEmpty()) {
                System.out.println("No customers found.");
                return;
            }

            System.out.println("LIST OF CUSTOMERS");
            System.out.println("-".repeat(131));
            System.out.printf("%-5s %-38s %-38s %-15s %-12s %-10s %-8s%n",
                    "ID", "Name", "Email", "Tax Number", "Reg. Date", "Pass Type", "Credit");
            System.out.println("-".repeat(131));

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (CustomerPassInfoDTO customer : customers) {
                System.out.printf("%-5d %-38s %-38s %-15d %-12s %-10s %-8.2f%n",
                        customer.getId(),
                        customer.getName(),
                        customer.getEmail(),
                        customer.getTaxNumber(),
                        dateFormat.format(customer.getRegistrationDate()),
                        customer.getPassType(),
                        customer.getCredit());
            }

            System.out.println("-".repeat(131));
            System.out.println("Total customers: " + customers.size());
        } catch (Exception e) {
            System.out.println("Error listing customers: " + e.getMessage());
        } finally {
            repository.close();
            System.out.println("\nPress Enter to continue...");
        }
    }

    private void listDocks() {
        DocksRepository repository = new DocksRepository();
        try {
            List<DockOccupancyDTO> docks = repository.getAllDocksWithOccupancy();

            System.out.println("LIST OF DOCKS WITH OCCUPANCY RATES");
            System.out.println("-".repeat(100));
            System.out.printf("%-10s %-10s %-15s %-15s %-20s %-10s %-15s%n",
                    "Dock #", "Station", "Latitude", "Longitude", "State", "Scooter", "Occupancy Rate");
            System.out.println("-".repeat(100));

            for (DockOccupancyDTO dock : docks) {
                System.out.printf("%-10d %-10d %-15.6f %-15.6f %-20s %-10s %-5.2f%%%n",
                        dock.getDockNumber(),
                        dock.getStationId(),
                        dock.getLatitude(),
                        dock.getLongitude(),
                        dock.getDockState(),
                        dock.getScooter(),
                        dock.getOccupancy() * 100);

            }
            System.out.println("-".repeat(100));
            System.out.println("Total docks: " + docks.size());
        } catch (Exception e) {
            System.out.println("Error listing docks: " + e.getMessage());
        } finally {
            repository.close();
            System.out.println("\nPress Enter to continue...");
        }
    }

    private void startTrip() {
        Scanner scanner = new Scanner(System.in);
        TripRepository repository = new TripRepository();

        try {
            System.out.println("START A NEW TRIP");
            System.out.println("--------------------");

            System.out.print("Enter dock number: ");
            int dockId = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter client ID: ");
            int clientId = Integer.parseInt(scanner.nextLine());

            try {
                repository.startNewTrip(dockId, clientId);
                System.out.println("Trip started successfully!");
            } catch (RuntimeException e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("not occupied or does not exist")) {
                    System.out
                            .println("Error: The selected dock does not have a scooter available or it doesnt exist.");
                } else if (msg != null && msg.contains("does not have a valid card")) {
                    System.out.println("Error: The client ID does not exist or the client does not have a valid card.");
                } else if (msg != null && msg.contains("does not have enough credit")) {
                    System.out.println("Error: The client does not have enough credit.");
                } else if (msg != null && msg.contains("already has an ongoing trip")) {
                    System.out.println("Error: The client already has an active trip.");
                } else {
                    System.out.println("Error starting trip: " + e.getMessage());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input: Please enter valid numbers for dock and client IDs");
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        } finally {
            repository.close();
            System.out.println("\nPress Enter to continue...");
        }
    }

    private void parkScooter() {
        ScooterRepository repository = new ScooterRepository();
        TripRepository tripRepository = new TripRepository();
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("PARK SCOOTER");
            System.out.println("------------");

            System.out.print("Enter dock number: ");
            int dockId = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter scooter ID: ");
            int scooterId = Integer.parseInt(scanner.nextLine());
            boolean checkUnfinishedTrip = tripRepository.checkUnfinishedTrip(scooterId);
            String comment = null;

            if (checkUnfinishedTrip) {
                try {
                    System.out.println("You have ended the Trip successfully!");
                    System.out.print("Enter evaluation (1-5, or leave empty): ");
                    String evalInput = scanner.nextLine();
                    Integer evaluation = null;

                    if (evalInput != null && !evalInput.trim().equals("")) {
                        evaluation = Integer.parseInt(evalInput);
                        if (evaluation < 1 || evaluation > 5) {
                            System.out.println("Error: Evaluation must be between 1 and 5.");
                            return;
                        }
                        System.out.print("Enter your comment (cannot be empty): ");
                        comment = scanner.nextLine();
                        if (comment.isEmpty()) {
                            comment = null;
                        }
                    }

                    tripRepository.endTrip(scooterId, comment, evaluation);
                } catch (RuntimeException e) {
                    System.out.println("No ongoing trip to end or error: " + e.getMessage());
                }
            }

            try {
                repository.parksScooter(scooterId, dockId);
                System.out.println("Scooter parked successfully!");
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input format");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            repository.close();
            tripRepository.close();
            System.out.println("\nPress Enter to continue...");
        }
    }

    private void about() {
        // TODO: Add your Group ID & member names
        System.out.println("Group-G01T41D");
        System.out.println("Afonso Jesus - 51561 - A51561@alunos.isel.pt");
        System.out.println("José Saldanha - 51445 - A51445@alunos.isel.pt");
        System.out.println("Ricardo Pinto - 51447 - A51447@alunos.isel.pt");
        System.out.println("DAL version: " + isel.sisinf.jpa.Dal.version());
        System.out.println("Core version: " + isel.sisinf.model.Core.version());
        System.out.println("\nPress Enter to continue...");
    }
}

public class App {
    public static void main(String[] args) throws Exception {
        UI.getInstance().Run();
    }
}