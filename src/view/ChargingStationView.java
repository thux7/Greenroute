package view;

import controller.ChargingStationController;
import controller.CityController;
import model.ChargingStation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChargingStationView extends JFrame {
    private ChargingStationController controller;
    private CityController cityController;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtLocation, txtCityId, txtConnectors, txtPower, txtPrice, txtSlots;
    private JButton btnSave, btnUpdate, btnDelete, btnRefresh;
    private int editingId = -1;

    public ChargingStationView(ChargingStationController controller, CityController cityController) {
        this.controller = controller;
        this.cityController = cityController;
        setTitle("Gerenciamento de Estações");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Cidade ID", "Vagas", "Potência"}, 0);
        table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = new JPanel(new GridLayout(8, 2, 5, 5));
        form.add(new JLabel("Nome:"));
        txtName = new JTextField();
        form.add(txtName);
        form.add(new JLabel("Localização:"));
        txtLocation = new JTextField();
        form.add(txtLocation);
        form.add(new JLabel("ID da Cidade:"));
        txtCityId = new JTextField();
        form.add(txtCityId);
        form.add(new JLabel("Conectores (ex: CCS2, Type2):"));
        txtConnectors = new JTextField();
        form.add(txtConnectors);
        form.add(new JLabel("Potência (kW):"));
        txtPower = new JTextField();
        form.add(txtPower);
        form.add(new JLabel("Preço por kWh:"));
        txtPrice = new JTextField();
        form.add(txtPrice);
        form.add(new JLabel("Vagas:"));
        txtSlots = new JTextField();
        form.add(txtSlots);

        btnSave = new JButton("Salvar");
        btnUpdate = new JButton("Atualizar");
        btnDelete = new JButton("Excluir");
        btnRefresh = new JButton("Atualizar Lista");

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnSave);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        JPanel right = new JPanel(new BorderLayout());
        right.add(form, BorderLayout.CENTER);
        right.add(btnPanel, BorderLayout.SOUTH);
        add(right, BorderLayout.EAST);

        btnSave.addActionListener(this::saveOrUpdate);
        btnUpdate.addActionListener(this::loadForUpdate);
        btnDelete.addActionListener(this::deleteStation);
        btnRefresh.addActionListener(e -> refreshTable());

        refreshTable();
        clearFields();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (ChargingStation cs : controller.listAll()) {
            tableModel.addRow(new Object[]{cs.getId(), cs.getName(), cs.getCityId(),
                    cs.getAvailableSlots(), cs.getChargingPowerKw()});
        }
    }

    private void saveOrUpdate(ActionEvent e) {
        try {
            String name = txtName.getText().trim();
            String location = txtLocation.getText().trim();
            int cityId = Integer.parseInt(txtCityId.getText());
            try {
                cityController.findById(cityId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cidade não encontrada!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String connectors = txtConnectors.getText().trim();
            double power = Double.parseDouble(txtPower.getText());
            double price = Double.parseDouble(txtPrice.getText());
            int slots = Integer.parseInt(txtSlots.getText());

            if (editingId == -1) {
                controller.registerStation(name, location, cityId, connectors, power, price, slots);
                JOptionPane.showMessageDialog(this, "Estação cadastrada!");
            } else {
                controller.updateStationFull(editingId, name, location, cityId, connectors, power, price, slots);
                JOptionPane.showMessageDialog(this, "Estação atualizada!");
                editingId = -1;
                btnSave.setText("Salvar");
            }
            refreshTable();
            clearFields();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifique os campos numéricos.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadForUpdate(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma estação para atualizar.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            ChargingStation cs = controller.findById(id);
            editingId = id;
            txtName.setText(cs.getName());
            txtLocation.setText(cs.getLocation());
            txtCityId.setText(String.valueOf(cs.getCityId()));
            txtConnectors.setText(cs.getAvailableConnectorTypes());
            txtPower.setText(String.valueOf(cs.getChargingPowerKw()));
            txtPrice.setText(String.valueOf(cs.getPricePerKwh()));
            txtSlots.setText(String.valueOf(cs.getAvailableSlots()));
            btnSave.setText("Atualizar");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStation(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma estação para excluir.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Tem certeza?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                controller.deleteStation(id);
                if (editingId == id) {
                    editingId = -1;
                    btnSave.setText("Salvar");
                    clearFields();
                }
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        txtName.setText("");
        txtLocation.setText("");
        txtCityId.setText("");
        txtConnectors.setText("");
        txtPower.setText("");
        txtPrice.setText("");
        txtSlots.setText("");
        editingId = -1;
        btnSave.setText("Salvar");
    }
}