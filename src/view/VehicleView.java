package view;

import controller.VehicleController;
import model.Vehicle;
import model.ElectricVehicle;
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

    public VehicleView(VehicleController controller, IAPlannerService plannerService) {
        this.controller = controller;
        this.plannerService = plannerService;

        setTitle("Gerenciamento de Veículos");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tabela
        tableModel = new DefaultTableModel(new String[]{"ID", "Modelo", "Tipo", "Bateria %", "Autonomia (km)"}, 0);
        table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        // Painel de formulário
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

        // Área de texto livre para LLM
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

        // Botões
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnSave = new JButton("Salvar");
        btnUpdate = new JButton("Atualizar");
        btnDelete = new JButton("Excluir");
        btnRefresh = new JButton("Atualizar Lista");

        btnPanel.add(btnSave);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        // Layout principal
        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(formPanel, BorderLayout.CENTER);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // Eventos
        btnSave.addActionListener(this::saveVehicle);
        btnUpdate.addActionListener(this::updateVehicle);
        btnDelete.addActionListener(this::deleteVehicle);
        btnRefresh.addActionListener(e -> refreshTable());
        btnExtract.addActionListener(this::extractWithAI);

        refreshTable();
    }

    private void addLabelAndField(JPanel panel, String label, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(field, gbc);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Vehicle> vehicles = controller.listAll();
        for (Vehicle v : vehicles) {
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

    private void saveVehicle(ActionEvent e) {
        try {
            String model = txtModel.getText();
            double maxRange = Double.parseDouble(txtMaxRange.getText());
            double battery = Double.parseDouble(txtBattery.getText());
            String connector = txtConnector.getText();
            int fastCharge = Integer.parseInt(txtFastCharge.getText());
            double consumption = Double.parseDouble(txtConsumption.getText());
            int fullCharge = Integer.parseInt(txtFullCharge.getText());

            controller.registerElectricVehicle(model, maxRange, battery, connector, fastCharge, consumption, fullCharge);
            JOptionPane.showMessageDialog(this, "Veículo salvo com sucesso!");
            refreshTable();
            clearFields();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: Verifique os campos numéricos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateVehicle(ActionEvent e) {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um veículo para atualizar.");
            return;
        }
        int id = (int) tableModel.getValueAt(selected, 0);
        String novoModelo = JOptionPane.showInputDialog(this, "Novo modelo:", tableModel.getValueAt(selected, 1));
        if (novoModelo != null && !novoModelo.trim().isEmpty()) {
            try {
                controller.updateVehicle(id, novoModelo.trim());
                JOptionPane.showMessageDialog(this, "Veículo atualizado!");
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteVehicle(ActionEvent e) {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um veículo para excluir.");
            return;
        }
        int id = (int) tableModel.getValueAt(selected, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteVehicle(id);
                JOptionPane.showMessageDialog(this, "Veículo excluído!");
                refreshTable();
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
            ElectricVehicle ev = plannerService.extractVehicleData(text);
            txtModel.setText(ev.getModel());
            txtMaxRange.setText(String.valueOf(ev.getMaximumRange()));
            txtBattery.setText(String.valueOf(ev.getCurrentBatteryCharge()));
            txtConnector.setText(ev.getConnectorType());
            txtFastCharge.setText(String.valueOf(ev.getFastRechargeTime()));
            txtConsumption.setText(String.valueOf(ev.getKwhConsumptionPerKm()));
            txtFullCharge.setText(String.valueOf(ev.getFullRechargeTime()));
            JOptionPane.showMessageDialog(this, "Dados extraídos com sucesso! Revise e salve.");
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
    }
}