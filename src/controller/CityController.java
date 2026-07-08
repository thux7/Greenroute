package controller;

import model.City;
import repository.CityRepository;
import exception.EntidadeNaoEncontradaException;

import java.util.ArrayList;

public class CityController {
    private CityRepository repository;
    private int nextId = 1;

    public CityController(CityRepository repository) {
        this.repository = repository;
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
        repository.register(city);
    }

    public ArrayList<City> listAll() {
        return repository.findAll();
    }

    public City findById(int id) throws EntidadeNaoEncontradaException {
        City c = repository.findById(id);
        if (c == null) throw new EntidadeNaoEncontradaException("Cidade não encontrada.");
        return c;
    }

    public void updateCityFull(int id, String name, String state, double distance) throws EntidadeNaoEncontradaException {
        City c = findById(id);
        validateCity(name, state, distance);
        c.setName(name);
        c.setState(state);
        c.setDistanceFromCapital(distance);
        repository.update(c);
    }

    public void updateCity(int id, String newName) throws EntidadeNaoEncontradaException {
        City c = findById(id);
        if (newName == null || newName.trim().isEmpty())
            throw new IllegalArgumentException("Nome da cidade é obrigatório.");
        c.setName(newName);
        repository.update(c);
    }

    public void deleteCity(int id) throws EntidadeNaoEncontradaException {
        if (!repository.delete(id))
            throw new EntidadeNaoEncontradaException("Cidade não encontrada.");
    }
}