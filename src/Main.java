import controller.*;
import repository.*;
import service.GeminiPlannerService;
import service.IAPlannerService;
import view.*;

public class Main {
    public static void main(String[] args) {
        VehicleRepository vehicleRepo = new VehicleRepository();
        CityRepository cityRepo = new CityRepository();
        ChargingStationRepository stationRepo = new ChargingStationRepository();

        VehicleController vehicleController = new VehicleController(vehicleRepo);
        CityController cityController = new CityController(cityRepo);
        ChargingStationController stationController = new ChargingStationController(stationRepo);

        IAPlannerService planner = new GeminiPlannerService();

        RouteController routeController = new RouteController(vehicleController, cityController, stationController, planner);

        VehicleView vehicleView = new VehicleView(vehicleController, planner);
        CityView cityView = new CityView(cityController);
        ChargingStationView stationView = new ChargingStationView(stationController, cityController);
        RouteView routeView = new RouteView(routeController, vehicleController, cityController);

        new MainView(vehicleView, cityView, stationView, routeView);
    }
}