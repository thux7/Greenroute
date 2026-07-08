package controller;

import model.Vehicle;
import model.City;
import model.ChargingStation;
import exception.AutonomiaInsuficienteException;
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

        if (currentRange >= destination.getDistanceFromCapital()) {
            result.append("Status: Viagem possível!\n");
        } else {
            result.append("Status: ATENÇÃO! Autonomia insuficiente.\n");
            result.append("Estações de recarga sugeridas:\n");
            ArrayList<ChargingStation> stations = stationController.findByCity(cityId);
            if (stations.isEmpty()) {
                result.append("Nenhuma estação cadastrada nesta cidade.\n");
            } else {
                for (ChargingStation s : stations) {
                    result.append("- ").append(s.getName()).append(" | Local: ").append(s.getLocation())
                            .append(" | Conectores: ").append(s.getAvailableConnectorTypes()).append("\n");
                }
            }
            // Usar LLM para roteirização inteligente
            String roteiro = plannerService.planRoute(vehicle.getModel(), destination.getName(), vehicle.getCurrentBatteryCharge());
            result.append("\n--- Recomendação da IA ---\n").append(roteiro);
        }
        return result.toString();
    }
}