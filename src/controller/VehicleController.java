package controller;

import model.Vehicle;
import model.ElectricVehicle;
import model.HybridVehicle;
import repository.VehicleRepository;
import exception.EntidadeNaoEncontradaException;

import java.util.ArrayList;

public class VehicleController {
    private VehicleRepository repository;
    private int nextId = 1;

    public VehicleController(VehicleRepository repository) {
        this.repository = repository;
    }

    private void validateCommonFields(String model, double maxRange, double battery,
                                      double consumption, int fullCharge) {
        if (model == null || model.trim().isEmpty())
            throw new IllegalArgumentException("Modelo é obrigatório.");
        if (maxRange <= 0)
            throw new IllegalArgumentException("Autonomia máxima deve ser maior que zero.");
        if (battery < 0 || battery > 100)
            throw new IllegalArgumentException("Bateria atual deve estar entre 0 e 100%.");
        if (consumption <= 0)
            throw new IllegalArgumentException("Consumo (kWh/km) deve ser maior que zero.");
        if (fullCharge <= 0)
            throw new IllegalArgumentException("Tempo de recarga completa deve ser maior que zero.");
    }

    private void validateConnectorFields(String connector, int fastCharge) {
        if (connector == null || connector.trim().isEmpty())
            throw new IllegalArgumentException("Tipo de conector é obrigatório.");
        if (fastCharge <= 0)
            throw new IllegalArgumentException("Tempo de recarga rápida deve ser maior que zero.");
    }

    private void validateHybridFields(double fuelCapacity, double fuelConsumption, String fuelType) {
        if (fuelCapacity <= 0)
            throw new IllegalArgumentException("Capacidade do tanque deve ser maior que zero.");
        if (fuelConsumption <= 0)
            throw new IllegalArgumentException("Consumo de combustível (km/l) deve ser maior que zero.");
        if (fuelType == null || fuelType.trim().isEmpty())
            throw new IllegalArgumentException("Tipo de combustível é obrigatório.");
    }

    public void registerElectricVehicle(String model, double maxRange, double battery,
                                        String connector, int fastCharge, double consumption, int fullCharge) {
        validateCommonFields(model, maxRange, battery, consumption, fullCharge);
        validateConnectorFields(connector, fastCharge);
        ElectricVehicle ev = new ElectricVehicle(nextId++, model, maxRange, battery,
                connector, fastCharge, consumption, fullCharge);
        repository.register(ev);
    }

    public void registerHybridVehicle(String model, double maxRange, double battery,
                                      double consumption, int fullCharge, double fuelCapacity,
                                      double fuelConsumption, String fuelType,
                                      String connector, int fastCharge) {
        validateCommonFields(model, maxRange, battery, consumption, fullCharge);
        validateHybridFields(fuelCapacity, fuelConsumption, fuelType);
        validateConnectorFields(connector, fastCharge);
        HybridVehicle hv = new HybridVehicle(nextId++, model, maxRange, battery,
                consumption, fullCharge, fuelCapacity, fuelConsumption, fuelType,
                connector, fastCharge);
        repository.register(hv);
    }

    public ArrayList<Vehicle> listAll() {
        return repository.findAll();
    }

    public Vehicle findById(int id) throws EntidadeNaoEncontradaException {
        Vehicle v = repository.findById(id);
        if (v == null) throw new EntidadeNaoEncontradaException("Veículo não encontrado.");
        return v;
    }

    public void updateVehicleFull(int id, String model, double maxRange, double battery,
                                  String connector, int fastCharge, double consumption, int fullCharge,
                                  Double fuelCapacity, Double fuelConsumption, String fuelType) throws EntidadeNaoEncontradaException {
        Vehicle v = findById(id);
        validateCommonFields(model, maxRange, battery, consumption, fullCharge);

        if (v instanceof ElectricVehicle) {
            validateConnectorFields(connector, fastCharge);
            ElectricVehicle ev = (ElectricVehicle) v;
            ev.setModel(model);
            ev.setMaximumRange(maxRange);
            ev.setCurrentBatteryCharge(battery);
            ev.setConnectorType(connector);
            ev.setFastRechargeTime(fastCharge);
            ev.setKwhConsumptionPerKm(consumption);
            ev.setFullRechargeTime(fullCharge);
            repository.update(ev);
        } else if (v instanceof HybridVehicle) {
            if (fuelCapacity == null || fuelConsumption == null || fuelType == null)
                throw new IllegalArgumentException("Dados do híbrido incompletos.");
            validateHybridFields(fuelCapacity, fuelConsumption, fuelType);
            validateConnectorFields(connector, fastCharge);
            HybridVehicle hv = (HybridVehicle) v;
            hv.setModel(model);
            hv.setMaximumRange(maxRange);
            hv.setCurrentBatteryCharge(battery);
            hv.setKwhConsumptionPerKm(consumption);
            hv.setFullRechargeTime(fullCharge);
            hv.setFuelTankCapacity(fuelCapacity);
            hv.setFuelConsumption(fuelConsumption);
            hv.setFuelType(fuelType);
            hv.setConnectorType(connector);
            hv.setFastRechargeTime(fastCharge);
            repository.update(hv);
        } else {
            throw new EntidadeNaoEncontradaException("Tipo de veículo inválido.");
        }
    }

    public void updateVehicleFull(int id, String model, double maxRange, double battery,
                                  String connector, int fastCharge, double consumption, int fullCharge) throws EntidadeNaoEncontradaException {
        updateVehicleFull(id, model, maxRange, battery, connector, fastCharge, consumption, fullCharge, null, null, null);
    }

    public void deleteVehicle(int id) throws EntidadeNaoEncontradaException {
        if (!repository.delete(id))
            throw new EntidadeNaoEncontradaException("Veículo não encontrado.");
    }
}