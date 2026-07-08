package repository;

import model.Vehicle;

import java.util.ArrayList;

public class VehicleRepository {

    private ArrayList<Vehicle> vehicles = new ArrayList<>();

    public void register(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public Vehicle findById(int id) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getId() == id) {
                return vehicles.get(i);
            }
        }
        return null;
    }

    public ArrayList<Vehicle> findAll() {
        return new ArrayList<>(vehicles);
    }

    public boolean update(Vehicle updatedVehicle) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getId() == updatedVehicle.getId()) {
                vehicles.set(i, updatedVehicle);
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getId() == id) {
                vehicles.remove(i);
                return true;
            }
        }
        return false;
    }
}