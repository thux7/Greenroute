package view;

import controller.CityController;
import model.City;
import service.IAPlannerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CityView extends JFrame {
    private CityController controller;
    private IAPlannerService plannerService;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtState, txtDistance;
    private JTextArea txtFreeText;
    private JButton btnSave, btnUpdate, btnDelete, btnRefresh, btnExtract;
    private int editingId = -1;

    public CityView(CityController controller, IAPlannerService plannerService) {
        this.controller = controller;
        this.plannerService = plannerService;
        setTitle("Gerenciamento de Cidades");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Estado", "Dist. Capital (km)"}, 0);
        table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        int row = 0;
        addLabelAndField(form, "Nome:", txtName = new JTextField(15), gbc, row++);
        addLabelAndField(form, "Estado:", txtState = new JTextField(15), gbc, row++);
        addLabelAndField(form, "Distância da capital:", txtDistance = new JTextField(15), gbc, row++);

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
        btnDelete.addActionListener(this::deleteCity);
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
        SwingWorker<City, Void> worker = new SwingWorker<City, Void>() {
            @Override
            protected City doInBackground() throws Exception {
                return plannerService.extractCityData(text);
            }
            @Override
            protected void done() {
                try {
                    City c = get();
                    txtName.setText(c.getName());
                    txtState.setText(c.getState());
                    txtDistance.setText(String.valueOf(c.getDistanceFromCapital()));
                    JOptionPane.showMessageDialog(CityView.this, "Dados extraídos com sucesso! Revise e salve.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CityView.this, "Erro na extração: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnExtract.setEnabled(true);
                    btnExtract.setText("Extrair com IA");
                }
            }
        };
        worker.execute();
    }
}