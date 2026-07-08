package model;

public abstract class Vehicle {

    protected int id;
    protected String model;
    protected double maximumRange;
    protected double currentBatteryCharge;
    protected double kwhConsumptionPerKm;
    protected int fullRechargeTime;

    public Vehicle(
            int id,
            String model,
            double maximumRange,
            double currentBatteryCharge,
            double kwhConsumptionPerKm,
            int fullRechargeTime) {

        this.id = id;
        this.model = model;
        this.maximumRange = maximumRange;
        this.currentBatteryCharge = currentBatteryCharge;
        this.kwhConsumptionPerKm = kwhConsumptionPerKm;
        this.fullRechargeTime = fullRechargeTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getMaximumRange() {
        return maximumRange;
    }

    public void setMaximumRange(double maximumRange) {
        this.maximumRange = maximumRange;
    }

    public double getCurrentBatteryCharge() {
        return currentBatteryCharge;
    }

    public void setCurrentBatteryCharge(double currentBatteryCharge) {
        this.currentBatteryCharge = currentBatteryCharge;
    }

    public double getKwhConsumptionPerKm() {
        return kwhConsumptionPerKm;
    }

    public void setKwhConsumptionPerKm(double kwhConsumptionPerKm) {
        this.kwhConsumptionPerKm = kwhConsumptionPerKm;
    }

    public int getFullRechargeTime() {
        return fullRechargeTime;
    }

    public void setFullRechargeTime(int fullRechargeTime) {
        this.fullRechargeTime = fullRechargeTime;
    }
}