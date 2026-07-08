package controller;

import model.ChargingStation;
import repository.ChargingStationRepository;
import exception.EntidadeNaoEncontradaException;

import java.util.ArrayList;

public class ChargingStationController {
    private ChargingStationRepository repository;
    private int nextId = 1;

    public ChargingStationController(ChargingStationRepository repository) {
        this.repository = repository;
    }

    public void registerStation(String name, String location, int cityId,
                                String connectors, double power, double price, int slots) {
        ChargingStation cs = new ChargingStation(nextId++, name, location, cityId,
                connectors, power, price, slots);
        repository.register(cs);
    }

    public ArrayList<ChargingStation> listAll() {
        return repository.findAll();
    }

    public ChargingStation findById(int id) throws EntidadeNaoEncontradaException {
        ChargingStation cs = repository.findById(id);
        if (cs == null) throw new EntidadeNaoEncontradaException("Estação não encontrada.");
        return cs;
    }

    public void updateStation(int id, String newName) throws EntidadeNaoEncontradaException {
        ChargingStation cs = findById(id);
        cs.setName(newName);
        repository.update(cs);
    }

    public void deleteStation(int id) throws EntidadeNaoEncontradaException {
        if (!repository.delete(id))
            throw new EntidadeNaoEncontradaException("Estação não encontrada.");
    }

    public ArrayList<ChargingStation> findByCity(int cityId) {
        return repository.findByCity(cityId);
    }
}