package repository;

import model.City;
import java.util.ArrayList;

public class CityRepository {
    private ArrayList<City> cities = new ArrayList<>();

    public void register(City city) { cities.add(city); }

    public City findById(int id) {
        for (City c : cities) if (c.getId() == id) return c;
        return null;
    }

    public City findByName(String name) {
        for (City c : cities) if (c.getName().equalsIgnoreCase(name)) return c;
        return null;
    }

    public ArrayList<City> findAll() { return new ArrayList<>(cities); }

    public boolean update(City updated) {
        for (int i = 0; i < cities.size(); i++) {
            if (cities.get(i).getId() == updated.getId()) {
                cities.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {
        for (int i = 0; i < cities.size(); i++) {
            if (cities.get(i).getId() == id) {
                cities.remove(i);
                return true;
            }
        }
        return false;
    }
}