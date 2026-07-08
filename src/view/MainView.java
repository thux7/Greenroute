import controller.*;
import repository.*;
import service.GeminiPlannerService;
import service.IAPlannerService;
import view.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainView extends JFrame {
    private VehicleView vehicleView;
    private CityView cityView;
    private ChargingStationView stationView;
    private RouteView routeView;

    public MainView() {
        setTitle("GreenRoute - Sistema de Gestão de Rotas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        VehicleRepository vehicleRepo = new VehicleRepository();
        CityRepository cityRepo = new CityRepository();
        ChargingStationRepository stationRepo = new ChargingStationRepository();

        VehicleController vehicleController = new VehicleController(vehicleRepo);
        CityController cityController = new CityController(cityRepo, stationRepo);
        ChargingStationController stationController = new ChargingStationController(stationRepo, cityController);

        IAPlannerService planner = new GeminiPlannerService();

        RouteController routeController = new RouteController(vehicleController, cityController, stationController, planner);

        vehicleView = new VehicleView(vehicleController, planner);
        cityView = new CityView(cityController, planner);
        stationView = new ChargingStationView(stationController, cityController, planner);
        routeView = new RouteView(routeController, vehicleController, cityController);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnVehicles = new JButton("Gerenciar Veículos");
        JButton btnCities = new JButton("Gerenciar Cidades");
        JButton btnStations = new JButton("Gerenciar Estações");
        JButton btnRoutes = new JButton("Simular Rotas");

        btnVehicles.addActionListener(e -> showView(vehicleView));
        btnCities.addActionListener(e -> showView(cityView));
        btnStations.addActionListener(e -> showView(stationView));
        btnRoutes.addActionListener(e -> showView(routeView));

        panel.add(btnVehicles);
        panel.add(btnCities);
        panel.add(btnStations);
        panel.add(btnRoutes);

        add(panel);
    }

    private void showView(JFrame view) {
        view.setVisible(true);
        view.toFront();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (System.getenv("GEMINI_API_KEY") == null || System.getenv("GEMINI_API_KEY").isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Variável de ambiente GEMINI_API_KEY não configurada.\n" +
                                "A funcionalidade de IA não estará disponível.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            new MainView().setVisible(true);
        });
    }
}