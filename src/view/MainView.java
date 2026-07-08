package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainView extends JFrame {
    private VehicleView vehicleView;
    private CityView cityView;
    private ChargingStationView stationView;
    private RouteView routeView;

    public MainView(VehicleView vehicleView, CityView cityView,
                    ChargingStationView stationView, RouteView routeView) {
        this.vehicleView = vehicleView;
        this.cityView = cityView;
        this.stationView = stationView;
        this.routeView = routeView;

        setTitle("GreenRoute - Sistema de Logística");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnVehicles = new JButton("Gerenciar Veículos");
        JButton btnCities = new JButton("Gerenciar Cidades");
        JButton btnStations = new JButton("Gerenciar Estações de Recarga");
        JButton btnRoute = new JButton("Simular Rota Intermunicipal");
        JButton btnExit = new JButton("Sair");

        btnVehicles.addActionListener(e -> vehicleView.setVisible(true));
        btnCities.addActionListener(e -> cityView.setVisible(true));
        btnStations.addActionListener(e -> stationView.setVisible(true));
        btnRoute.addActionListener(e -> {
            routeView.setVisible(true);
        });
        btnExit.addActionListener(e -> System.exit(0));

        panel.add(btnVehicles);
        panel.add(btnCities);
        panel.add(btnStations);
        panel.add(btnRoute);
        panel.add(btnExit);

        add(panel);
        setVisible(true);
    }
}