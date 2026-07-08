package controller;

import model.ChargingStation;
import repository.ChargingStationRepository;
import exception.EntidadeNaoEncontradaException;

import java.util.ArrayList;

public class ChargingStationController {
    private ChargingStationRepository repository;
    private CityController cityController; // injetado para validação
    private int nextId = 1;

    public ChargingStationController(ChargingStationRepository repository, CityController cityController) {
        this.repository = repository;
        this.cityController = cityController;
    }

    private void validateStation(String name, String location, int cityId,
                                 String connectors, double power, double price, int slots) throws EntidadeNaoEncontradaException {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Nome da estação é obrigatório.");
        if (location == null || location.trim().isEmpty())
            throw new IllegalArgumentException("Localização é obrigatória.");
        if (cityId <= 0)
            throw new IllegalArgumentException("ID da cidade inválido.");
        cityController.findById(cityId); // pode lançar EntidadeNaoEncontradaException
        if (connectors == null || connectors.trim().isEmpty())
            throw new IllegalArgumentException("Pelo menos um tipo de conector deve ser informado.");
        if (power <= 0)
            throw new IllegalArgumentException("Potência de carga deve ser maior que zero.");
        if (price < 0)
            throw new IllegalArgumentException("Preço por kWh não pode ser negativo.");
        if (slots < 0)
            throw new IllegalArgumentException("Número de vagas não pode ser negativo.");
    }

    public void registerStation(String name, String location, int cityId,
                                String connectors, double power, double price, int slots) throws EntidadeNaoEncontradaException {
        validateStation(name, location, cityId, connectors, power, price, slots);
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

    public void updateStationFull(int id, String name, String location, int cityId,
                                  String connectors, double power, double price, int slots) throws EntidadeNaoEncontradaException {
        ChargingStation cs = findById(id);
        validateStation(name, location, cityId, connectors, power, price, slots);
        cs.setName(name);
        cs.setLocation(location);
        cs.setCityId(cityId);
        cs.setAvailableConnectorTypes(connectors);
        cs.setChargingPowerKw(power);
        cs.setPricePerKwh(price);
        cs.setAvailableSlots(slots);
        repository.update(cs);
    }

    public void updateStation(int id, String newName) throws EntidadeNaoEncontradaException {
        ChargingStation cs = findById(id);
        if (newName == null || newName.trim().isEmpty())
            throw new IllegalArgumentException("Nome da estação é obrigatório.");
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