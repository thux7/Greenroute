package repository;

import model.ChargingStation;
import java.util.ArrayList;

public class ChargingStationRepository {
    private ArrayList<ChargingStation> chargingStations = new ArrayList<>();

    public void register(ChargingStation cs) { chargingStations.add(cs); }

    public ChargingStation findById(int id) {
        for (ChargingStation cs : chargingStations) if (cs.getId() == id) return cs;
        return null;
    }

    public ArrayList<ChargingStation> findAll() { return new ArrayList<>(chargingStations); }

    public boolean update(ChargingStation updated) {
        for (int i = 0; i < chargingStations.size(); i++) {
            if (chargingStations.get(i).getId() == updated.getId()) {
                chargingStations.set(i, updated);
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
        for (ChargingStation cs : chargingStations) {
            if (cs.getCityId() == cityId) result.add(cs);
        }
        return result;
    }

    public int countByCity(int cityId) {
        int count = 0;
        for (ChargingStation cs : chargingStations) {
            if (cs.getCityId() == cityId) count++;
        }
        return count;
    }
}