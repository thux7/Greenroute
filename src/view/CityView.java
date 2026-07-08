package view;

import controller.CityController;
import model.City;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CityView extends JFrame {
    private CityController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtState, txtDistance;
    private JButton btnSave, btnUpdate, btnDelete, btnRefresh;  // <-- ATRIBUTOS
    private int editingId = -1;

    public CityView(CityController controller) {
        this.controller = controller;
        setTitle("Gerenciamento de Cidades");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Estado", "Dist. Capital (km)"}, 0);
        table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("Nome:"));
        txtName = new JTextField();
        form.add(txtName);
        form.add(new JLabel("Estado:"));
        txtState = new JTextField();
        form.add(txtState);
        form.add(new JLabel("Distância da capital:"));
        txtDistance = new JTextField();
        form.add(txtDistance);

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
        btnDelete.addActionListener(this::deleteCity);
        btnRefresh.addActionListener(e -> refreshTable());

        refreshTable();
        clearFields();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (City c : controller.listAll()) {
            tableModel.addRow(new Object[]{c.getId(), c.getName(), c.getState(), c.getDistanceFromCapital()});
        }
    }

    private void saveOrUpdate(ActionEvent e) {
        try {
            String name = txtName.getText().trim();
            String state = txtState.getText().trim();
            double distance = Double.parseDouble(txtDistance.getText());
            if (editingId == -1) {
                controller.registerCity(name, state, distance);
                JOptionPane.showMessageDialog(this, "Cidade cadastrada!");
            } else {
                controller.updateCityFull(editingId, name, state, distance);
                JOptionPane.showMessageDialog(this, "Cidade atualizada!");
                editingId = -1;
                btnSave.setText("Salvar");
            }
            refreshTable();
            clearFields();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Distância inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadForUpdate(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma cidade.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            City c = controller.findById(id);
            editingId = id;
            txtName.setText(c.getName());
            txtState.setText(c.getState());
            txtDistance.setText(String.valueOf(c.getDistanceFromCapital()));
            btnSave.setText("Atualizar");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCity(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Excluir?") == JOptionPane.YES_OPTION) {
            try {
                controller.deleteCity(id);
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
        txtState.setText("");
        txtDistance.setText("");
        editingId = -1;
        btnSave.setText("Salvar");
    }
}