package view;

import controller.ChargingStationController;
import controller.CityController;
import model.ChargingStation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ChargingStationView extends JFrame {
    private ChargingStationController controller;
    private CityController cityController;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtLocation, txtCityId, txtConnectors, txtPower, txtPrice, txtSlots;

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

        JPanel form = new JPanel(new GridLayout(7, 2, 5, 5));
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

        JButton btnSave = new JButton("Salvar");
        JButton btnUpdate = new JButton("Atualizar");
        JButton btnDelete = new JButton("Excluir");
        JButton btnRefresh = new JButton("Atualizar Lista");

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

        btnSave.addActionListener(this::save);
        btnUpdate.addActionListener(this::update);
        btnDelete.addActionListener(this::delete);
        btnRefresh.addActionListener(e -> refreshTable());

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (ChargingStation cs : controller.listAll()) {
            tableModel.addRow(new Object[]{cs.getId(), cs.getName(), cs.getCityId(),
                    cs.getAvailableSlots(), cs.getChargingPowerKw()});
        }
    }

    private void save(ActionEvent e) {
        try {
            int cityId = Integer.parseInt(txtCityId.getText());
            controller.registerStation(txtName.getText(), txtLocation.getText(), cityId,
                    txtConnectors.getText(), Double.parseDouble(txtPower.getText()),
                    Double.parseDouble(txtPrice.getText()), Integer.parseInt(txtSlots.getText()));
            JOptionPane.showMessageDialog(this, "Estação salva!");
            refreshTable();
            limpar();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifique os campos numéricos.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void update(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma estação para atualizar.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String novoNome = JOptionPane.showInputDialog(this, "Novo nome:", tableModel.getValueAt(row, 1));
        if (novoNome != null && !novoNome.trim().isEmpty()) {
            try {
                controller.updateStation(id, novoNome.trim());
                JOptionPane.showMessageDialog(this, "Estação atualizada!");
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void delete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma estação para excluir.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Tem certeza?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                controller.deleteStation(id);
                JOptionPane.showMessageDialog(this, "Estação excluída!");
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void limpar() {
        txtName.setText("");
        txtLocation.setText("");
        txtCityId.setText("");
        txtConnectors.setText("");
        txtPower.setText("");
        txtPrice.setText("");
        txtSlots.setText("");
    }
}