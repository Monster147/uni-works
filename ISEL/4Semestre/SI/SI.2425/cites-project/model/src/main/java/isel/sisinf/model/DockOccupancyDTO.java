package isel.sisinf.model;

public class DockOccupancyDTO {
    private int dockNumber;
    private int stationId;
    private double latitude;
    private double longitude;
    private String dockState;
    private String scooter; // or UUID if you're using a real scooter entity
    private double occupancy; // result of fx_dock_occupancy

    public DockOccupancyDTO(int dockNumber, int stationId, double latitude, double longitude,
                            String dockState, String scooter, double occupancy) {
        this.dockNumber = dockNumber;
        this.stationId = stationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dockState = dockState;
        this.scooter = scooter;
        this.occupancy = occupancy;
    }

    // Getters only (DTOs are best immutable)
    public int getDockNumber() { return dockNumber; }
    public int getStationId() { return stationId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDockState() { return dockState; }
    public String getScooter() { return scooter; }
    public double getOccupancy() { return occupancy; }
}
