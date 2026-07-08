package repository;

import model.ChargingStation;

import java.util.ArrayList;

public class ChargingStationRepository {

    private ArrayList<ChargingStation> chargingStations = new ArrayList<>();

    public void register(ChargingStation chargingStation) {
        chargingStations.add(chargingStation);
    }

    public ChargingStation findById(int id) {
        for (int i = 0; i < chargingStations.size(); i++) {
            if (chargingStations.get(i).getId() == id) {
                return chargingStations.get(i);
            }
        }
        return null;
    }

    public ArrayList<ChargingStation> findAll() {
        return new ArrayList<>(chargingStations);
    }

    public boolean update(ChargingStation updatedChargingStation) {
        for (int i = 0; i < chargingStations.size(); i++) {
            if (chargingStations.get(i).getId() == updatedChargingStation.getId()) {
                chargingStations.set(i, updatedChargingStation);
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {
        for (int i = 0; i < chargingStations.size(); i++) {
            if (chargingStations.get(i).getId() == id) {
                chargingStations.remove(i);
                return true;
            }
        }
        return false;
    }

    public ArrayList<ChargingStation> findByCity(int cityId) {
        ArrayList<ChargingStation> result = new ArrayList<>();

        for (int i = 0; i < chargingStations.size(); i++) {
            if (chargingStations.get(i).getCityId() == cityId) {
                result.add(chargingStations.get(i));
            }
        }

        return result;
    }
}