package controller;

import model.Vehicle;
import model.City;
import model.ChargingStation;
import model.ElectricVehicle;
import model.HybridVehicle;
import exception.AutonomiaInsuficienteException;
import exception.ConectorIncompativelException;
import service.IAPlannerService;

import java.util.ArrayList;

public class RouteController {
    private VehicleController vehicleController;
    private CityController cityController;
    private ChargingStationController stationController;
    private IAPlannerService plannerService;

    public RouteController(VehicleController vehicleController,
                           CityController cityController,
                           ChargingStationController stationController,
                           IAPlannerService plannerService) {
        this.vehicleController = vehicleController;
        this.cityController = cityController;
        this.stationController = stationController;
        this.plannerService = plannerService;
    }

    public String simulateTrip(int vehicleId, int cityId) throws Exception {
        Vehicle vehicle = vehicleController.findById(vehicleId);
        City destination = cityController.findById(cityId);

        double currentRange;
        if (vehicle instanceof HybridVehicle) {
            HybridVehicle hv = (HybridVehicle) vehicle;
            double electricRange = hv.getMaximumRange() * (hv.getCurrentBatteryCharge() / 100.0);
            double combustionRange = hv.getFuelTankCapacity() * hv.getFuelConsumption();
            currentRange = electricRange + combustionRange;
        } else {
            currentRange = vehicle.getMaximumRange() * (vehicle.getCurrentBatteryCharge() / 100.0);
        }

        StringBuilder result = new StringBuilder();
        result.append("--- Resumo da Simulação ---\n");
        result.append("Veículo: ").append(vehicle.getModel())
                .append(" | Autonomia atual: ").append(String.format("%.2f", currentRange)).append(" km\n");
        result.append("Destino: ").append(destination.getName())
                .append(" | Distância: ").append(destination.getDistanceFromCapital()).append(" km\n");

        if (currentRange >= destination.getDistanceFromCapital()) {
            String roteiro = plannerService.planRoute(vehicle, destination);
            result.append("\n--- Recomendação da IA ---\n").append(roteiro);
            return result.toString();
        }

        ArrayList<ChargingStation> stations = stationController.findByCity(cityId);
        if (stations.isEmpty()) {
            throw new ConectorIncompativelException(
                    "Autonomia insuficiente e não há eletropostos cadastrados na cidade destino."
            );
        }

        String connector = null;
        if (vehicle instanceof ElectricVehicle) {
            connector = ((ElectricVehicle) vehicle).getConnectorType();
        } else if (vehicle instanceof HybridVehicle) {
            connector = ((HybridVehicle) vehicle).getConnectorType();
        }

        if (connector != null) {
            ArrayList<ChargingStation> compatibleStations = new ArrayList<>();
            for (ChargingStation s : stations) {
                if (s.getAvailableConnectorTypes().toLowerCase().contains(connector.toLowerCase())) {
                    compatibleStations.add(s);
                }
            }
            if (compatibleStations.isEmpty()) {
                throw new ConectorIncompativelException(
                        "Autonomia insuficiente e nenhum posto com conector " + connector +
                                " disponível na cidade destino."
                );
            }
            stations = compatibleStations;
        }

        StringBuilder stationList = new StringBuilder();
        for (ChargingStation s : stations) {
            stationList.append("- ").append(s.getName())
                    .append(" | Local: ").append(s.getLocation())
                    .append(" | Conectores: ").append(s.getAvailableConnectorTypes()).append("\n");
        }
        throw new AutonomiaInsuficienteException(
                "Autonomia insuficiente para chegar ao destino.\n" +
                        "Estações disponíveis na cidade destino (com conector compatível):\n" + stationList
        );
    }
}