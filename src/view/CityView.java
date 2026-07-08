package view;

import controller.CityController;
import model.City;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class CityView extends JFrame {
    private CityController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtState, txtDistance;

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
        for (City c : controller.listAll()) {
            tableModel.addRow(new Object[]{c.getId(), c.getName(), c.getState(), c.getDistanceFromCapital()});
        }
    }

    private void save(ActionEvent e) {
        try {
            controller.registerCity(txtName.getText(), txtState.getText(),
                    Double.parseDouble(txtDistance.getText()));
            JOptionPane.showMessageDialog(this, "Cidade salva!");
            refreshTable();
            txtName.setText("");
            txtState.setText("");
            txtDistance.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Distância inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void update(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) tableModel.getValueAt(row, 0);
        String novo = JOptionPane.showInputDialog(this, "Novo nome:", tableModel.getValueAt(row, 1));
        if (novo != null && !novo.trim().isEmpty()) {
            try {
                controller.updateCity(id, novo.trim());
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void delete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Excluir?") == JOptionPane.YES_OPTION) {
            try {
                controller.deleteCity(id);
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}