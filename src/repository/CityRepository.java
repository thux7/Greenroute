package repository;

import model.City;

import java.util.ArrayList;

public class CityRepository {

    private ArrayList<City> cities = new ArrayList<>();

    public void register(City city) {
        cities.add(city);
    }

    public City findById(int id) {
        for (int i = 0; i < cities.size(); i++) {
            if (cities.get(i).getId() == id) {
                return cities.get(i);
            }
        }
        return null;
    }

    public ArrayList<City> findAll() {
        return new ArrayList<>(cities);
    }

    public boolean update(City updatedCity) {
        for (int i = 0; i < cities.size(); i++) {
            if (cities.get(i).getId() == updatedCity.getId()) {
                cities.set(i, updatedCity);
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