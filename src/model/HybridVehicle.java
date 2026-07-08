package model;

public class HybridVehicle extends Vehicle {
    private double fuelTankCapacity;
    private double fuelConsumption;
    private String fuelType;
    private String connectorType;
    private int fastRechargeTime;

    public HybridVehicle(int id, String model, double maximumRange, double currentBatteryCharge,
                         double kwhConsumptionPerKm, int fullRechargeTime, double fuelTankCapacity,
                         double fuelConsumption, String fuelType, String connectorType, int fastRechargeTime) {
        super(id, model, maximumRange, currentBatteryCharge, kwhConsumptionPerKm, fullRechargeTime);
        this.fuelTankCapacity = fuelTankCapacity;
        this.fuelConsumption = fuelConsumption;
        this.fuelType = fuelType;
        this.connectorType = connectorType;
        this.fastRechargeTime = fastRechargeTime;
    }

    public double getFuelTankCapacity() { return fuelTankCapacity; }
    public void setFuelTankCapacity(double fuelTankCapacity) { this.fuelTankCapacity = fuelTankCapacity; }
    public double getFuelConsumption() { return fuelConsumption; }
    public void setFuelConsumption(double fuelConsumption) { this.fuelConsumption = fuelConsumption; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public String getConnectorType() { return connectorType; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }
    public int getFastRechargeTime() { return fastRechargeTime; }
    public void setFastRechargeTime(int fastRechargeTime) { this.fastRechargeTime = fastRechargeTime; }
}