package view;

import controller.ChargingStationController;
import controller.CityController;
import model.ChargingStation;
import service.IAPlannerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChargingStationView extends JFrame {
    private ChargingStationController controller;
    private CityController cityController;
    private IAPlannerService plannerService;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtLocation, txtCityId, txtConnectors, txtPower, txtPrice, txtSlots;
    private JTextArea txtFreeText;
    private JButton btnSave, btnUpdate, btnDelete, btnRefresh, btnExtract;
    private int editingId = -1;

    public ChargingStationView(ChargingStationController controller, CityController cityController, IAPlannerService plannerService) {
        this.controller = controller;
        this.cityController = cityController;
        this.plannerService = plannerService;
        setTitle("Gerenciamento de Estações");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Cidade ID", "Vagas", "Potência"}, 0);
        table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        int row = 0;
        addLabelAndField(form, "Nome:", txtName = new JTextField(15), gbc, row++);
        addLabelAndField(form, "Localização:", txtLocation = new JTextField(15), gbc, row++);
        addLabelAndField(form, "ID da Cidade:", txtCityId = new JTextField(15), gbc, row++);
        addLabelAndField(form, "Conectores (ex: CCS2, Type2):", txtConnectors = new JTextField(15), gbc, row++);
        addLabelAndField(form, "Potência (kW):", txtPower = new JTextField(15), gbc, row++);
        addLabelAndField(form, "Preço por kWh:", txtPrice = new JTextField(15), gbc, row++);
        addLabelAndField(form, "Vagas:", txtSlots = new JTextField(15), gbc, row++);

        JLabel lblFree = new JLabel("Cadastro Rápido por IA:");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        form.add(lblFree, gbc);
        txtFreeText = new JTextArea(3, 20);
        JScrollPane freeScroll = new JScrollPane(txtFreeText);
        gbc.gridx = 1; gbc.gridwidth = 2;
        form.add(freeScroll, gbc);
        btnExtract = new JButton("Extrair com IA");
        gbc.gridx = 3; gbc.gridy = row;
        form.add(btnExtract, gbc);

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
            // Validação de cidade já é feita pelo controller
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
        txtFreeText.setText("");
        editingId = -1;
        btnSave.setText("Salvar");
    }

    private void extractWithAI(ActionEvent e) {
        String text = txtFreeText.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite um texto para extrair dados.");
            return;
        }
        btnExtract.setEnabled(false);
        btnExtract.setText("Extraindo...");
        SwingWorker<ChargingStation, Void> worker = new SwingWorker<ChargingStation, Void>() {
            @Override
            protected ChargingStation doInBackground() throws Exception {
                return plannerService.extractStationData(text);
            }
            @Override
            protected void done() {
                try {
                    ChargingStation cs = get();
                    txtName.setText(cs.getName());
                    txtLocation.setText(cs.getLocation());
                    txtConnectors.setText(cs.getAvailableConnectorTypes());
                    txtPower.setText(String.valueOf(cs.getChargingPowerKw()));
                    txtPrice.setText(String.valueOf(cs.getPricePerKwh()));
                    txtSlots.setText(String.valueOf(cs.getAvailableSlots()));
                    txtCityId.setText("");
                    JOptionPane.showMessageDialog(ChargingStationView.this, "Dados extraídos! Preencha o ID da cidade manualmente.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ChargingStationView.this, "Erro na extração: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnExtract.setEnabled(true);
                    btnExtract.setText("Extrair com IA");
                }
            }
        };
        worker.execute();
    }
}