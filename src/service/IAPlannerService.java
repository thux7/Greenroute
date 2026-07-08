package service;

import model.Vehicle;

public interface IAPlannerService {
    Vehicle extractVehicleData(String freeText) throws Exception;
    String planRoute(String vehicleModel, String destination, double currentBattery);
}