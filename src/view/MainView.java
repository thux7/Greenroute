import controller.*;
import repository.*;
import service.GeminiPlannerService;
import service.IAPlannerService;
import view.*;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                VehicleRepository vehicleRepo = new VehicleRepository();
                CityRepository cityRepo = new CityRepository();
                ChargingStationRepository stationRepo = new ChargingStationRepository();

                VehicleController vehicleController = new VehicleController(vehicleRepo);
                CityController cityController = new CityController(cityRepo, stationRepo);
                ChargingStationController stationController = new ChargingStationController(stationRepo, cityController);

                IAPlannerService planner = new GeminiPlannerService();

                RouteController routeController = new RouteController(vehicleController, cityController, stationController, planner);

                VehicleView vehicleView = new VehicleView(vehicleController, planner);
                CityView cityView = new CityView(cityController, planner);
                ChargingStationView stationView = new ChargingStationView(stationController, cityController, planner);
                RouteView routeView = new RouteView(routeController, vehicleController, cityController);

                new MainView(vehicleView, cityView, stationView, routeView);
            }
        });
    }
}