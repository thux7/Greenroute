package service;

import model.Vehicle;
import model.City;

public interface IAPlannerService {
    Vehicle extractVehicleData(String freeText) throws Exception;
    String planRoute(Vehicle vehicle, City destination);
}