package service;

import model.Vehicle;
import model.City;
import model.ChargingStation;

public interface IAPlannerService {
    Vehicle extractVehicleData(String freeText) throws Exception;
    City extractCityData(String freeText) throws Exception;
    ChargingStation extractStationData(String freeText) throws Exception;
    String planRoute(Vehicle vehicle, City destination);
}