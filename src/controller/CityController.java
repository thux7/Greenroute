package controller;

import model.City;
import repository.CityRepository;
import repository.ChargingStationRepository;
import exception.EntidadeNaoEncontradaException;

import java.util.ArrayList;

public class CityController {
    private CityRepository cityRepository;
    private ChargingStationRepository stationRepository;
    private int nextId = 1;

    public CityController(CityRepository cityRepository, ChargingStationRepository stationRepository) {
        this.cityRepository = cityRepository;
        this.stationRepository = stationRepository;
    }

    private void validateCity(String name, String state, double distance) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Nome da cidade é obrigatório.");
        if (state == null || state.trim().isEmpty())
            throw new IllegalArgumentException("Estado é obrigatório.");
        if (distance <= 0)
            throw new IllegalArgumentException("Distância da capital deve ser maior que zero.");
    }

    public void registerCity(String name, String state, double distance) {
        validateCity(name, state, distance);
        City city = new City(nextId++, name, state, distance);
        cityRepository.register(city);
    }

    public ArrayList<City> listAll() {
        return cityRepository.findAll();
    }

    public City findById(int id) throws EntidadeNaoEncontradaException {
        City c = cityRepository.findById(id);
        if (c == null) throw new EntidadeNaoEncontradaException("Cidade não encontrada.");
        return c;
    }

    public City findByName(String name) throws EntidadeNaoEncontradaException {
        City c = cityRepository.findByName(name);
        if (c == null) throw new EntidadeNaoEncontradaException("Cidade não encontrada com nome: " + name);
        return c;
    }

    public void updateCityFull(int id, String name, String state, double distance) throws EntidadeNaoEncontradaException {
        City c = findById(id);
        validateCity(name, state, distance);
        c.setName(name);
        c.setState(state);
        c.setDistanceFromCapital(distance);
        cityRepository.update(c);
    }

    public void updateCity(int id, String newName) throws EntidadeNaoEncontradaException {
        City c = findById(id);
        if (newName == null || newName.trim().isEmpty())
            throw new IllegalArgumentException("Nome da cidade é obrigatório.");
        c.setName(newName);
        cityRepository.update(c);
    }

    public void deleteCity(int id) throws EntidadeNaoEncontradaException, IllegalStateException {
        int count = stationRepository.countByCity(id);
        if (count > 0) {
            throw new IllegalStateException("Não é possível excluir a cidade, pois existem " + count + " estação(ões) associada(s).");
        }
        if (!cityRepository.delete(id))
            throw new EntidadeNaoEncontradaException("Cidade não encontrada.");
    }
}