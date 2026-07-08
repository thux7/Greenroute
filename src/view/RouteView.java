package view;

import controller.RouteController;
import controller.VehicleController;
import controller.CityController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RouteView extends JFrame {
    private RouteController routeController;
    private JComboBox<Integer> cbVehicles, cbCities;
    private JTextArea txtResult;
    private VehicleController vehicleController;
    private CityController cityController;

    public RouteView(RouteController routeController, VehicleController vc, CityController cc) {
        this.routeController = routeController;
        this.vehicleController = vc;
        this.cityController = cc;
        setTitle("Simulação de Rota");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Veículo:"));
        cbVehicles = new JComboBox<>();
        top.add(cbVehicles);
        top.add(new JLabel("Destino:"));
        cbCities = new JComboBox<>();
        top.add(cbCities);
        JButton btnSimular = new JButton("Simular");
        top.add(btnSimular);

        txtResult = new JTextArea(20, 50);
        txtResult.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtResult);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        refreshCombos();

        btnSimular.addActionListener(this::simular);
    }

    private void refreshCombos() {
        cbVehicles.removeAllItems();
        for (Vehicle v : vehicleController.listAll()) {
            cbVehicles.addItem(v.getId());
        }
        cbCities.removeAllItems();
        for (City c : cityController.listAll()) {
            cbCities.addItem(c.getId());
        }
    }

    private void simular(ActionEvent e) {
        Integer vehicleId = (Integer) cbVehicles.getSelectedItem();
        Integer cityId = (Integer) cbCities.getSelectedItem();
        if (vehicleId == null || cityId == null) return;
        try {
            String resultado = routeController.simulateTrip(vehicleId, cityId);
            txtResult.setText(resultado);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}