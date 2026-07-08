package view;

import controller.VehicleController;
import model.Vehicle;
import model.ElectricVehicle;
import model.HybridVehicle;
import service.IAPlannerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class VehicleView extends JFrame {
    private VehicleController controller;
    private IAPlannerService plannerService;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtModel, txtMaxRange, txtBattery, txtConnector, txtFastCharge, txtConsumption, txtFullCharge;
    private JTextField txtFuelCapacity, txtFuelConsumption, txtFuelType;
    private JComboBox<String> cbType;
    private JTextArea txtFreeText;
    private JButton btnExtract, btnSave, btnUpdate, btnDelete, btnRefresh;
    private int editingId = -1;

    public VehicleView(VehicleController controller, IAPlannerService plannerService) {
        this.controller = controller;
        this.plannerService = plannerService;

        setTitle("Gerenciamento de Veículos");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID", "Modelo", "Tipo", "Bateria %", "Autonomia (km)"}, 0);
        table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                if (editingId != -1) {
                    int option = JOptionPane.showConfirmDialog(VehicleView.this,
                            "Há uma edição em andamento. Descartar alterações?",
                            "Edição pendente", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        clearFields();
                    }
                }
            }
        });

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tipo:"), gbc);
        cbType = new JComboBox<>(new String[]{"Elétrico", "Híbrido"});
        cbType.addActionListener(e -> toggleFields());
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbType, gbc);
        row++;

        addLabelAndField(formPanel, "Modelo:", txtModel = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Autonomia máx (km):", txtMaxRange = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Bateria atual (%):", txtBattery = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Consumo (kWh/km):", txtConsumption = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Tempo recarga completa (min):", txtFullCharge = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Tipo conector:", txtConnector = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Tempo recarga rápida (min):", txtFastCharge = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Capacidade tanque (L):", txtFuelCapacity = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Consumo combustível (km/l):", txtFuelConsumption = new JTextField(15), gbc, row++);
        addLabelAndField(formPanel, "Tipo combustível:", txtFuelType = new JTextField(15), gbc, row++);

        toggleFields();

        JLabel lblFree = new JLabel("Cadastro Rápido por IA:");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(lblFree, gbc);
        txtFreeText = new JTextArea(3, 20);
        JScrollPane freeScroll = new JScrollPane(txtFreeText);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(freeScroll, gbc);
        btnExtract = new JButton("Extrair com IA");
        gbc.gridx = 3; gbc.gridy = row;
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

    private void toggleFields() {
        boolean isHybrid = "Híbrido".equals(cbType.getSelectedItem());
        // Campos de conector sempre visíveis
        txtConnector.setVisible(true);
        txtFastCharge.setVisible(true);
        txtFuelCapacity.setVisible(isHybrid);
        txtFuelConsumption.setVisible(isHybrid);
        txtFuelType.setVisible(isHybrid);
        revalidate();
        repaint();
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
            if (model.isEmpty()) throw new IllegalArgumentException("Modelo é obrigatório.");
            double maxRange = parseDouble(txtMaxRange.getText(), "Autonomia máxima");
            double battery = parseDouble(txtBattery.getText(), "Bateria atual");
            double consumption = parseDouble(txtConsumption.getText(), "Consumo");
            int fullCharge = parseInt(txtFullCharge.getText(), "Tempo de recarga completa");
            String connector = txtConnector.getText().trim();
            int fastCharge = parseInt(txtFastCharge.getText(), "Tempo de recarga rápida");

            if ("Elétrico".equals(cbType.getSelectedItem())) {
                if (editingId == -1) {
                    controller.registerElectricVehicle(model, maxRange, battery, connector, fastCharge, consumption, fullCharge);
                } else {
                    controller.updateVehicleFull(editingId, model, maxRange, battery, connector, fastCharge, consumption, fullCharge);
                }
            } else {
                double fuelCapacity = parseDouble(txtFuelCapacity.getText(), "Capacidade do tanque");
                double fuelConsumption = parseDouble(txtFuelConsumption.getText(), "Consumo de combustível");
                String fuelType = txtFuelType.getText().trim();
                if (fuelType.isEmpty()) throw new IllegalArgumentException("Tipo de combustível é obrigatório.");
                if (editingId == -1) {
                    controller.registerHybridVehicle(model, maxRange, battery, consumption, fullCharge,
                            fuelCapacity, fuelConsumption, fuelType, connector, fastCharge);
                } else {
                    controller.updateVehicleFull(editingId, model, maxRange, battery, connector, fastCharge,
                            consumption, fullCharge, fuelCapacity, fuelConsumption, fuelType);
                }
            }
            JOptionPane.showMessageDialog(this, editingId == -1 ? "Veículo cadastrado!" : "Veículo atualizado!");
            editingId = -1;
            cbType.setEnabled(true);
            btnSave.setText("Salvar");
            refreshTable();
            clearFields();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de validação", JOptionPane.ERROR_MESSAGE);
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
            cbType.setEnabled(false);

            txtModel.setText(v.getModel());
            txtMaxRange.setText(String.valueOf(v.getMaximumRange()));
            txtBattery.setText(String.valueOf(v.getCurrentBatteryCharge()));
            txtConsumption.setText(String.valueOf(v.getKwhConsumptionPerKm()));
            txtFullCharge.setText(String.valueOf(v.getFullRechargeTime()));

            if (v instanceof ElectricVehicle) {
                cbType.setSelectedItem("Elétrico");
                ElectricVehicle ev = (ElectricVehicle) v;
                txtConnector.setText(ev.getConnectorType());
                txtFastCharge.setText(String.valueOf(ev.getFastRechargeTime()));
                txtFuelCapacity.setText("");
                txtFuelConsumption.setText("");
                txtFuelType.setText("");
            } else if (v instanceof HybridVehicle) {
                cbType.setSelectedItem("Híbrido");
                HybridVehicle hv = (HybridVehicle) v;
                txtConnector.setText(hv.getConnectorType());
                txtFastCharge.setText(String.valueOf(hv.getFastRechargeTime()));
                txtFuelCapacity.setText(String.valueOf(hv.getFuelTankCapacity()));
                txtFuelConsumption.setText(String.valueOf(hv.getFuelConsumption()));
                txtFuelType.setText(hv.getFuelType());
            }
            toggleFields();
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
                    cbType.setEnabled(true);
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
        btnExtract.setEnabled(false);
        btnExtract.setText("Extraindo...");
        SwingWorker<Vehicle, Void> worker = new SwingWorker<Vehicle, Void>() {
            @Override
            protected Vehicle doInBackground() throws Exception {
                return plannerService.extractVehicleData(text);
            }
            @Override
            protected void done() {
                try {
                    Vehicle v = get();
                    txtModel.setText(v.getModel());
                    txtMaxRange.setText(String.valueOf(v.getMaximumRange()));
                    txtBattery.setText(String.valueOf(v.getCurrentBatteryCharge()));
                    txtConsumption.setText(String.valueOf(v.getKwhConsumptionPerKm()));
                    txtFullCharge.setText(String.valueOf(v.getFullRechargeTime()));

                    if (v instanceof ElectricVehicle) {
                        cbType.setSelectedItem("Elétrico");
                        ElectricVehicle ev = (ElectricVehicle) v;
                        txtConnector.setText(ev.getConnectorType());
                        txtFastCharge.setText(String.valueOf(ev.getFastRechargeTime()));
                        txtFuelCapacity.setText("");
                        txtFuelConsumption.setText("");
                        txtFuelType.setText("");
                    } else if (v instanceof HybridVehicle) {
                        cbType.setSelectedItem("Híbrido");
                        HybridVehicle hv = (HybridVehicle) v;
                        txtConnector.setText(hv.getConnectorType());
                        txtFastCharge.setText(String.valueOf(hv.getFastRechargeTime()));
                        txtFuelCapacity.setText(String.valueOf(hv.getFuelTankCapacity()));
                        txtFuelConsumption.setText(String.valueOf(hv.getFuelConsumption()));
                        txtFuelType.setText(hv.getFuelType());
                    }
                    toggleFields();
                    JOptionPane.showMessageDialog(VehicleView.this, "Dados extraídos com sucesso! Revise e salve.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(VehicleView.this, "Erro na extração: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnExtract.setEnabled(true);
                    btnExtract.setText("Extrair com IA");
                }
            }
        };
        worker.execute();
    }

    private void clearFields() {
        txtModel.setText("");
        txtMaxRange.setText("");
        txtBattery.setText("");
        txtConnector.setText("");
        txtFastCharge.setText("");
        txtConsumption.setText("");
        txtFullCharge.setText("");
        txtFuelCapacity.setText("");
        txtFuelConsumption.setText("");
        txtFuelType.setText("");
        txtFreeText.setText("");
        editingId = -1;
        cbType.setEnabled(true);
        btnSave.setText("Salvar");
        cbType.setSelectedIndex(0);
        toggleFields();
    }

    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Campo '" + fieldName + "' deve ser um número válido.");
        }
    }

    private int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Campo '" + fieldName + "' deve ser um número inteiro válido.");
        }
    }
}