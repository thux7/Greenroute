package model;

public class ChargingStation {

    private int id;
    private String name;
    private String location;
    private int cityId;
    private String availableConnectorTypes;
    private double chargingPowerKw;
    private double pricePerKwh;
    private int availableSlots;

    public ChargingStation(int id, String name, String location, int cityId,
                           String availableConnectorTypes, double chargingPowerKw,
                           double pricePerKwh, int availableSlots) {

        this.id = id;
        this.name = name;
        this.location = location;
        this.cityId = cityId;
        this.availableConnectorTypes = availableConnectorTypes;
        this.chargingPowerKw = chargingPowerKw;
        this.pricePerKwh = pricePerKwh;
        this.availableSlots = availableSlots;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getAvailableConnectorTypes() {
        return availableConnectorTypes;
    }

    public void setAvailableConnectorTypes(String availableConnectorTypes) {
        this.availableConnectorTypes = availableConnectorTypes;
    }

    public double getChargingPowerKw() {
        return chargingPowerKw;
    }

    public void setChargingPowerKw(double chargingPowerKw) {
        this.chargingPowerKw = chargingPowerKw;
    }

    public double getPricePerKwh() {
        return pricePerKwh;
    }

    public void setPricePerKwh(double pricePerKwh) {
        this.pricePerKwh = pricePerKwh;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }
}