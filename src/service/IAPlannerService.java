package service;

import model.ElectricVehicle;

public interface IAPlannerService {
    ElectricVehicle extractVehicleData(String freeText) throws Exception;
    String planRoute(String vehicleModel, String destination, double currentBattery);
}