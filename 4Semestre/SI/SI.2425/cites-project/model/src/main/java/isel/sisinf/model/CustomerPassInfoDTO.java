package isel.sisinf.model;


import java.sql.Timestamp;

public class CustomerPassInfoDTO {
    private int id;
    private String name;
    private String email;
    private int taxNumber;
    private Timestamp registrationDate;
    private String passType;
    private double credit;

    public CustomerPassInfoDTO(int id, String name, String email, int taxNumber,
                               Timestamp registrationDate, String passType, double credit) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.taxNumber = taxNumber;
        this.registrationDate = registrationDate;
        this.passType = passType;
        this.credit = credit;
    }

    // Getters (no setters if it's meant to be immutable)
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getTaxNumber() { return taxNumber; }
    public Timestamp getRegistrationDate() { return registrationDate; }
    public String getPassType() { return passType; }
    public double getCredit() { return credit; }
}