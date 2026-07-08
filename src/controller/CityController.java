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

    public void registerCity(String name, String state, double distance) {
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

    public void updateCity(int id, String newName) throws EntidadeNaoEncontradaException {
        City c = findById(id);
        c.setName(newName);
        repository.update(c);
    }

    public void deleteCity(int id) throws EntidadeNaoEncontradaException {
        if (!repository.delete(id))
            throw new EntidadeNaoEncontradaException("Cidade não encontrada.");
    }
}