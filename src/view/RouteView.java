package view;

import model.Vehicle;
import model.City;
import controller.RouteController;
import controller.VehicleController;
import controller.CityController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RouteView extends JFrame {
    private RouteController routeController;
    private JComboBox<String> cbVehicles, cbCities;
    private JTextArea txtResult;
    private VehicleController vehicleController;
    private CityController cityController;
    private JButton btnSimular;

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
        btnSimular = new JButton("Simular");
        top.add(btnSimular);

        txtResult = new JTextArea(20, 50);
        txtResult.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtResult);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refreshCombos();
            }
        });

        refreshCombos();
        btnSimular.addActionListener(this::simular);
    }

    private void refreshCombos() {
        cbVehicles.removeAllItems();
        for (Vehicle v : vehicleController.listAll()) {
            cbVehicles.addItem(v.getId() + " - " + v.getModel());
        }
        cbCities.removeAllItems();
        for (City c : cityController.listAll()) {
            cbCities.addItem(c.getId() + " - " + c.getName());
        }
    }

    private void simular(ActionEvent e) {
        String selectedVehicle = (String) cbVehicles.getSelectedItem();
        String selectedCity = (String) cbCities.getSelectedItem();
        if (selectedVehicle == null || selectedCity == null) return;
        try {
            int vehicleId = Integer.parseInt(selectedVehicle.split(" - ")[0]);
            int cityId = Integer.parseInt(selectedCity.split(" - ")[0]);

            btnSimular.setEnabled(false);
            btnSimular.setText("Simulando...");
            txtResult.setText("Aguarde, processando...");

            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return routeController.simulateTrip(vehicleId, cityId);
                }
                @Override
                protected void done() {
                    try {
                        String resultado = get();
                        txtResult.setText(resultado);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(RouteView.this, ex.getMessage(), "Erro na rota", JOptionPane.ERROR_MESSAGE);
                        txtResult.setText("Erro: " + ex.getMessage());
                    } finally {
                        btnSimular.setEnabled(true);
                        btnSimular.setText("Simular");
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao selecionar: " + ex.getMessage());
        }
    }
}