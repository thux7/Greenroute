package model;

public class ElectricVehicle extends Vehicle {

    private String connectorType;
    private int fastRechargeTime;

    public ElectricVehicle(
            int id,
            String model,
            double maximumRange,
            double currentBatteryCharge,
            String connectorType,
            int fastRechargeTime,
            double kwhConsumptionPerKm,
            int fullRechargeTime) {

        super(
                id,
                model,
                maximumRange,
                currentBatteryCharge,
                kwhConsumptionPerKm,
                fullRechargeTime
        );

        this.connectorType = connectorType;
        this.fastRechargeTime = fastRechargeTime;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public int getFastRechargeTime() {
        return fastRechargeTime;
    }

    public void setFastRechargeTime(int fastRechargeTime) {
        this.fastRechargeTime = fastRechargeTime;
    }
}