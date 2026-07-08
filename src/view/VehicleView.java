package view;

import controller.VehicleController;
import model.Vehicle;
import model.ElectricVehicle;
import model.HybridVehicle;
import service.IAPlannerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class VehicleView extends JFrame {
    private VehicleController controller;
    private IAPlannerService plannerService;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtModel, txtMaxRange, txtBattery, txtConnector, txtFastCharge, txtConsumption, txtFullCharge;
    private JTextArea txtFreeText;
    private JButton btnExtract, btnSave, btnUpdate, btnDelete, btnRefresh;
    private int editingId = -1;

    public VehicleView(VehicleController controller, IAPlannerService plannerService) {
        this.controller = controller;
        this.plannerService = plannerService;

        setTitle("Gerenciamento de Veículos");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID", "Modelo", "Tipo", "Bateria %", "Autonomia (km)"}, 0);
        table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        int row = 0;
        addLabelAndField(formPanel, "Modelo:", txtModel = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Autonomia máx (km):", txtMaxRange = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Bateria atual (%):", txtBattery = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Tipo conector:", txtConnector = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Tempo recarga rápida (min):", txtFastCharge = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Consumo (kWh/km):", txtConsumption = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Tempo recarga completa (min):", txtFullCharge = new JTextField(15), gbc, row++);

        JLabel lblFree = new JLabel("Cadastro Rápido por IA:");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(lblFree, gbc);
        txtFreeText = new JTextArea(3, 20);
        JScrollPane freeScroll = new JScrollPane(txtFreeText);
        gbc.gridx = 1; gbc.gridwidth = 1;
        formPanel.add(freeScroll, gbc);
        btnExtract = new JButton("Extrair com IA");
        gbc.gridx = 2; gbc.gridy = row;
        formPanel.add(btnExtract, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnSave = new JButton("Salvar");
        btnUpdate = new JButton("Atualizar");
        btnDelete = new JButton("Excluir");
        btnRefresh = new JButton("Atualizar Lista");

        btnPanel.add(btnSave);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(formPanel, BorderLayout.CENTER);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        btnSave.addActionListener(this::saveOrUpdateVehicle);
        btnUpdate.addActionListener(this::loadForUpdate);
        btnDelete.addActionListener(this::deleteVehicle);
        btnRefresh.addActionListener(e -> refreshTable());
        btnExtract.addActionListener(this::extractWithAI);

        refreshTable();
        clearFields();
    }

    private void addLabelAndField(JPanel panel, String label, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(field, gbc);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Vehicle v : controller.listAll()) {
            String tipo = (v instanceof ElectricVehicle) ? "Elétrico" : "Híbrido";
            tableModel.addRow(new Object[]{
                    v.getId(),
                    v.getModel(),
                    tipo,
                    v.getCurrentBatteryCharge(),
                    v.getMaximumRange()
            });
        }
    }

    private void saveOrUpdateVehicle(ActionEvent e) {
        try {
            String model = txtModel.getText().trim();
            if (model.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Modelo é obrigatório.");
                return;
            }
            double maxRange = Double.parseDouble(txtMaxRange.getText());
            double battery = Double.parseDouble(txtBattery.getText());
            String connector = txtConnector.getText().trim();
            int fastCharge = Integer.parseInt(txtFastCharge.getText());
            double consumption = Double.parseDouble(txtConsumption.getText());
            int fullCharge = Integer.parseInt(txtFullCharge.getText());

            if (editingId == -1) {
                controller.registerElectricVehicle(model, maxRange, battery, connector, fastCharge, consumption, fullCharge);
                JOptionPane.showMessageDialog(this, "Veículo cadastrado com sucesso!");
            } else {
                controller.updateVehicleFull(editingId, model, maxRange, battery, connector, fastCharge, consumption, fullCharge);
                JOptionPane.showMessageDialog(this, "Veículo atualizado com sucesso!");
                editingId = -1; // reseta modo
                btnSave.setText("Salvar"); // opcional
            }
            refreshTable();
            clearFields();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: Verifique os campos numéricos.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadForUpdate(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um veículo para atualizar.");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Vehicle v = controller.findById(id);
            editingId = id;
            txtModel.setText(v.getModel());
            txtMaxRange.setText(String.valueOf(v.getMaximumRange()));
            txtBattery.setText(String.valueOf(v.getCurrentBatteryCharge()));
            if (v instanceof ElectricVehicle) {
                ElectricVehicle ev = (ElectricVehicle) v;
                txtConnector.setText(ev.getConnectorType());
                txtFastCharge.setText(String.valueOf(ev.getFastRechargeTime()));
                txtConsumption.setText(String.valueOf(ev.getKwhConsumptionPerKm()));
                txtFullCharge.setText(String.valueOf(ev.getFullRechargeTime()));
            } else {
                txtConnector.setText("");
                txtFastCharge.setText("0");
                txtConsumption.setText("0");
                txtFullCharge.setText("0");
                JOptionPane.showMessageDialog(this, "Veículo híbrido: campos elétricos não preenchidos.");
            }
            btnSave.setText("Atualizar");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteVehicle(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um veículo para excluir.");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteVehicle(id);
                JOptionPane.showMessageDialog(this, "Veículo excluído!");
                refreshTable();
                if (editingId == id) {
                    editingId = -1;
                    btnSave.setText("Salvar");
                    clearFields();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void extractWithAI(ActionEvent e) {
        String text = txtFreeText.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite um texto para extrair dados.");
            return;
        }
        try {
            Vehicle v = plannerService.extractVehicleData(text);
            if (v instanceof ElectricVehicle) {
                ElectricVehicle ev = (ElectricVehicle) v;
                txtModel.setText(ev.getModel());
                txtMaxRange.setText(String.valueOf(ev.getMaximumRange()));
                txtBattery.setText(String.valueOf(ev.getCurrentBatteryCharge()));
                txtConnector.setText(ev.getConnectorType());
                txtFastCharge.setText(String.valueOf(ev.getFastRechargeTime()));
                txtConsumption.setText(String.valueOf(ev.getKwhConsumptionPerKm()));
                txtFullCharge.setText(String.valueOf(ev.getFullRechargeTime()));
                JOptionPane.showMessageDialog(this, "Dados extraídos com sucesso! Revise e salve.");
            } else {
                JOptionPane.showMessageDialog(this, "Veículo híbrido detectado. Apenas dados comuns preenchidos.");
                txtModel.setText(v.getModel());
                txtMaxRange.setText(String.valueOf(v.getMaximumRange()));
                txtBattery.setText(String.valueOf(v.getCurrentBatteryCharge()));
                txtConnector.setText("");
                txtFastCharge.setText("0");
                txtConsumption.setText("0");
                txtFullCharge.setText("0");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro na extração: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtModel.setText("");
        txtMaxRange.setText("");
        txtBattery.setText("");
        txtConnector.setText("");
        txtFastCharge.setText("");
        txtConsumption.setText("");
        txtFullCharge.setText("");
        txtFreeText.setText("");
        editingId = -1;
        btnSave.setText("Salvar");
    }
}