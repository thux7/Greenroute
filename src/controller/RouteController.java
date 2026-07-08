package controller;

import model.Vehicle;
import model.City;
import model.ChargingStation;
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

        double currentRange = vehicle.getMaximumRange() * (vehicle.getCurrentBatteryCharge() / 100.0);

        StringBuilder result = new StringBuilder();
        result.append("--- Resumo da Simulação ---\n");
        result.append("Veículo: ").append(vehicle.getModel())
                .append(" | Autonomia atual: ").append(String.format("%.2f", currentRange)).append(" km\n");
        result.append("Destino: ").append(destination.getName())
                .append(" | Distância: ").append(destination.getDistanceFromCapital()).append(" km\n");

        if (currentRange < destination.getDistanceFromCapital()) {
            ArrayList<ChargingStation> stations = stationController.findByCity(cityId);
            String stationList = "";
            if (stations.isEmpty()) {
                stationList = "Nenhuma estação cadastrada nesta cidade.";
            } else {
                StringBuilder sb = new StringBuilder();
                for (ChargingStation s : stations) {
                    sb.append("- ").append(s.getName()).append(" | Local: ").append(s.getLocation())
                            .append(" | Conectores: ").append(s.getAvailableConnectorTypes()).append("\n");
                }
                stationList = sb.toString();
            }
            throw new AutonomiaInsuficienteException(
                    "Autonomia insuficiente para chegar ao destino.\n" +
                            "Estações disponíveis na cidade destino:\n" + stationList
            );
        }

        if (vehicle instanceof model.ElectricVehicle) {
            String connector = ((model.ElectricVehicle) vehicle).getConnectorType();
            ArrayList<ChargingStation> stations = stationController.findByCity(cityId);
            boolean compatible = stations.stream().anyMatch(s ->
                    s.getAvailableConnectorTypes().toLowerCase().contains(connector.toLowerCase())
            );
            if (!compatible) {
                throw new ConectorIncompativelException(
                        "Nenhum eletroposto com conector compatível (" + connector + ") encontrado na cidade destino."
                );
            }
        }

        String roteiro = plannerService.planRoute(vehicle, destination);
        result.append("\n--- Recomendação da IA ---\n").append(roteiro);
        return result.toString();
    }
}