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

    public void registerElectricVehicle(String model, double maxRange, double battery,
                                        String connector, int fastCharge, double consumption, int fullCharge) {
        ElectricVehicle ev = new ElectricVehicle(nextId++, model, maxRange, battery,
                connector, fastCharge, consumption, fullCharge);
        repository.register(ev);
    }

    public void registerHybridVehicle(String model, double maxRange, double battery,
                                      double consumption, int fullCharge, double fuelCapacity,
                                      double fuelConsumption, String fuelType) {
        HybridVehicle hv = new HybridVehicle(nextId++, model, maxRange, battery,
                consumption, fullCharge, fuelCapacity, fuelConsumption, fuelType);
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
                                  String connector, int fastCharge, double consumption, int fullCharge) throws EntidadeNaoEncontradaException {
        Vehicle v = findById(id);
        if (v instanceof ElectricVehicle) {
            ElectricVehicle ev = (ElectricVehicle) v;
            ev.setModel(model);
            ev.setMaximumRange(maxRange);
            ev.setCurrentBatteryCharge(battery);
            ev.setConnectorType(connector);
            ev.setFastRechargeTime(fastCharge);
            ev.setKwhConsumptionPerKm(consumption);
            ev.setFullRechargeTime(fullCharge);
            repository.update(ev);
        } else {
            throw new EntidadeNaoEncontradaException("Atualização de híbridos ainda não implementada.");
        }
    }

    public void updateVehicle(int id, String newModel) throws EntidadeNaoEncontradaException {
        Vehicle v = findById(id);
        v.setModel(newModel);
        repository.update(v);
    }

    public void deleteVehicle(int id) throws EntidadeNaoEncontradaException {
        if (!repository.delete(id))
            throw new EntidadeNaoEncontradaException("Veículo não encontrado.");
    }
}