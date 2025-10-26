



import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.File;
import java.io.FileWriter;

public class InventoryManagementApp {
    // Existing fields unchanged
    private static final String[] COLUMN_NAMES = {
        "SKU", "Item Name", "Quantity", "Cost Price", "Selling Price",
        "Category", "Location", "Min Stock Threshold"
    };
    private int skuCounter = 1;
    private static final String[] CATEGORIES = {
        "Electronics", "Clothing", "Food", "Other"
    };
    private static final String[] LOCATIONS = {
        "Warehouse A", "Warehouse B", "Shelf 1", "Shelf 2", "Other"
    };
    private DefaultTableModel model;
    private JTable inventoryTable;
    private List<SaleRecord> salesLog = new ArrayList<>();
    private TableRowSorter<DefaultTableModel> sorter;

    // SaleRecord class unchanged
    private static class SaleRecord {
        String sku, name, category;
        int quantity;
        double sellingPrice;
        String timestamp;
        SaleRecord(String sku, String name, String category, int quantity, double sellingPrice, String timestamp) {
            this.sku = sku;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.sellingPrice = sellingPrice;
            this.timestamp = timestamp;
        }
    }





    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Apply modern look-and-feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // Global UI customizations
                UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 14));
                UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
                UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
                UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
                UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
                UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
                UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 14));
                UIManager.put("Button.background", new Color(74, 144, 226)); // Blue
                UIManager.put("Button.foreground", Color.WHITE);
                UIManager.put("Panel.background", new Color(245, 245, 245)); // Light gray
            } catch (Exception e) {
                e.printStackTrace();
            }
            new InventoryManagementApp().createAndShowGUI();
		});
        }





    private void createAndShowGUI() {
        JFrame frame = new JFrame("Inventory Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 650);
        frame.setMinimumSize(new Dimension(800, 500));
        frame.getContentPane().setBackground(new Color(245, 245, 245)); // Light gray background

        // Modern tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tabbedPane.setBackground(new Color(230, 230, 230));
        tabbedPane.setForeground(new Color(50, 50, 50));

        // Initialize table model
        model = new DefaultTableModel(COLUMN_NAMES, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        inventoryTable = new JTable(model);
        inventoryTable.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(model);
        inventoryTable.setRowSorter(sorter);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.setFillsViewportHeight(true);
        inventoryTable.setRowHeight(28);
        inventoryTable.setGridColor(new Color(200, 200, 200));
        inventoryTable.getTableHeader().setBackground(new Color(74, 144, 226));
        inventoryTable.getTableHeader().setForeground(Color.WHITE);
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Custom table renderer for low-stock and alternating rows
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                try {
                    int qty = Integer.parseInt(model.getValueAt(modelRow, 2).toString());
                    int min = Integer.parseInt(model.getValueAt(modelRow, 7).toString());
                    if (qty <= min) {
                        c.setBackground(new Color(255, 200, 200)); // Soft red for low stock
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240)); // Alternating rows
                    }
                    if (isSelected) {
                        c.setBackground(new Color(200, 220, 255)); // Selection highlight
                    }
                } catch (Exception ex) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }
                c.setForeground(Color.BLACK);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); // Cell padding
                return c;
            }
        });

        // Entry Tab
        JPanel entryPanel = new JPanel(new GridBagLayout());
        entryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        entryPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Add Item Section
        JPanel addItemPanel = new JPanel(new GridBagLayout());
        addItemPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(74, 144, 226), 2, true), 
            "Add New Item", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), 
            new Color(50, 50, 50)
        ));
        addItemPanel.setBackground(new Color(245, 245, 245));

        JTextField itemNameField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField costField = new JTextField();
        JTextField sellField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(CATEGORIES);
        JComboBox<String> locationBox = new JComboBox<>(LOCATIONS);
        JTextField minStockField = new JTextField();
        JButton addButton = createStyledButton("Add Item");

        itemNameField.setToolTipText("Enter item name");
        quantityField.setToolTipText("Enter quantity (positive integer)");
        costField.setToolTipText("Enter cost price (e.g., 10.50)");
        sellField.setToolTipText("Enter selling price (e.g., 15.00)");
        categoryBox.setToolTipText("Select category");
        locationBox.setToolTipText("Select location");
        minStockField.setToolTipText("Enter minimum stock threshold");
        addButton.setToolTipText("Add item to inventory");

        // Style form fields
        styleTextField(itemNameField);
        styleTextField(quantityField);
        styleTextField(costField);
        styleTextField(sellField);
        styleComboBox(categoryBox);
        styleComboBox(locationBox);
        styleTextField(minStockField);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; addItemPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1; addItemPanel.add(itemNameField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; addItemPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1; addItemPanel.add(quantityField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; addItemPanel.add(new JLabel("Cost Price:"), gbc);
        gbc.gridx = 1; addItemPanel.add(costField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; addItemPanel.add(new JLabel("Selling Price:"), gbc);
        gbc.gridx = 1; addItemPanel.add(sellField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; addItemPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; addItemPanel.add(categoryBox, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; addItemPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1; addItemPanel.add(locationBox, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; addItemPanel.add(new JLabel("Min Stock Threshold:"), gbc);
        gbc.gridx = 1; addItemPanel.add(minStockField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; addItemPanel.add(addButton, gbc);

        // Restock Section
        JPanel restockPanel = new JPanel(new GridBagLayout());
        restockPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(74, 144, 226), 2, true), 
            "Restock Item", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), 
            new Color(50, 50, 50)
        ));
        restockPanel.setBackground(new Color(245, 245, 245));

        JComboBox<String> restockSKUBox = new JComboBox<>();
        JTextField restockQtyField = new JTextField();
        JButton restockButton = createStyledButton("Restock Item");
        styleComboBox(restockSKUBox);
        styleTextField(restockQtyField);
        restockButton.setToolTipText("Add stock to existing item");

        int restockRow = 0;
        gbc.gridx = 0; gbc.gridy = restockRow; restockPanel.add(new JLabel("Restock SKU:"), gbc);
        gbc.gridx = 1; restockPanel.add(restockSKUBox, gbc);
        restockRow++;
        gbc.gridx = 0; gbc.gridy = restockRow; restockPanel.add(new JLabel("Quantity to Add:"), gbc);
        gbc.gridx = 1; restockPanel.add(restockQtyField, gbc);
        restockRow++;
        gbc.gridx = 0; gbc.gridy = restockRow; gbc.gridwidth = 2; restockPanel.add(restockButton, gbc);

        // Combine sections in Entry tab
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0; gbc.gridy = 0; entryPanel.add(addItemPanel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; entryPanel.add(restockPanel, gbc);

        // Inventory Tab
        JPanel listPanel = new JPanel(new BorderLayout(10, 10));
        listPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        listPanel.setBackground(new Color(245, 245, 245));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(245, 245, 245));
        JTextField searchField = new JTextField(20);
        JButton lowStockButton = createStyledButton("Check Low Stock");
        JButton deleteButton = createStyledButton("Delete Selected");
        styleTextField(searchField);
        searchField.setToolTipText("Search by Name, SKU, or Category");
        lowStockButton.setToolTipText("Show items with low stock");
        deleteButton.setToolTipText("Delete selected item");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(lowStockButton);
        searchPanel.add(deleteButton);

        listPanel.add(searchPanel, BorderLayout.NORTH);
        JScrollPane tableScroll = new JScrollPane(inventoryTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        listPanel.add(tableScroll, BorderLayout.CENTER);

        // Exit Tab
        JPanel exitPanel = new JPanel(new GridBagLayout());
        exitPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        exitPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints xgbc = new GridBagConstraints();
        xgbc.insets = new Insets(8, 8, 8, 8);
        xgbc.fill = GridBagConstraints.HORIZONTAL;
        xgbc.weightx = 1.0;

        JPanel sellPanel = new JPanel(new GridBagLayout());
        sellPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(74, 144, 226), 2, true), 
            "Record Sale", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), 
            new Color(50, 50, 50)
        ));
        sellPanel.setBackground(new Color(245, 245, 245));

        JComboBox<String> exitComboBox = new JComboBox<>();
        JTextField exitQtyField = new JTextField();
        JButton exitButton = createStyledButton("Confirm Sale");
        styleComboBox(exitComboBox);
        styleTextField(exitQtyField);
        exitButton.setToolTipText("Sell item (reduce stock)");

        int xrow = 0;
        xgbc.gridx = 0; xgbc.gridy = xrow; sellPanel.add(new JLabel("Select Item:"), xgbc);
        xgbc.gridx = 1; sellPanel.add(exitComboBox, xgbc);
        xrow++;
        xgbc.gridx = 0; xgbc.gridy = xrow; sellPanel.add(new JLabel("Quantity to Sell:"), xgbc);
        xgbc.gridx = 1; sellPanel.add(exitQtyField, xgbc);
        xrow++;
        xgbc.gridx = 0; xgbc.gridy = xrow; xgbc.gridwidth = 2; sellPanel.add(exitButton, xgbc);

        xgbc = new GridBagConstraints();
        xgbc.insets = new Insets(10, 10, 10, 10);
        xgbc.fill = GridBagConstraints.HORIZONTAL;
        xgbc.gridx = 0; xgbc.gridy = 0; exitPanel.add(sellPanel, xgbc);

        // Reports Tab
        JPanel reportsPanel = new JPanel(new BorderLayout(10, 10));
        reportsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        reportsPanel.setBackground(new Color(245, 245, 245));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(new Color(245, 245, 245));
    JButton stockSummaryButton = createStyledButton("Stock Summary");
    JButton salesReportButton = createStyledButton("Sales Report");
    JButton exportCSVButton = createStyledButton("Save to CSV");
    stockSummaryButton.setForeground(Color.BLACK);
    salesReportButton.setForeground(Color.BLACK);
    exportCSVButton.setForeground(Color.BLACK);
        buttonPanel.add(stockSummaryButton);
        buttonPanel.add(salesReportButton);
        buttonPanel.add(exportCSVButton);

        String[] salesColumns = {"Time", "SKU", "Name", "Category", "Qty", "Price"};
        DefaultTableModel salesTableModel = new DefaultTableModel(salesColumns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable salesTable = new JTable(salesTableModel);
        salesTable.setRowHeight(28);
        salesTable.setGridColor(new Color(200, 200, 200));
        salesTable.getTableHeader().setBackground(new Color(74, 144, 226));
        salesTable.getTableHeader().setForeground(Color.WHITE);
        salesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                if (isSelected) c.setBackground(new Color(200, 220, 255));
                c.setForeground(Color.BLACK);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                return c;
            }
        });

        JTextArea reportArea = new JTextArea(15, 60);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reportArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        JScrollPane reportScroll = new JScrollPane(reportArea);
        JScrollPane salesScroll = new JScrollPane(salesTable);
        salesScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));

        reportsPanel.add(buttonPanel, BorderLayout.NORTH);
        reportsPanel.add(reportScroll, BorderLayout.CENTER);

        // Add tabs
        tabbedPane.addTab("Entry", entryPanel);
        tabbedPane.addTab("Inventory", listPanel);
        tabbedPane.addTab("Exit", exitPanel);
        tabbedPane.addTab("Reports", reportsPanel);

        // Event handlers (unchanged from original)
        addButton.addActionListener(e -> {
            String name = itemNameField.getText().trim();
            String qtyStr = quantityField.getText().trim();
            String costStr = costField.getText().trim();
            String sellStr = sellField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            String location = (String) locationBox.getSelectedItem();
            String minStr = minStockField.getText().trim();
            if (name.isEmpty() || qtyStr.isEmpty() || costStr.isEmpty() || sellStr.isEmpty() || minStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int qty, min;
            double cost, sell;
            try {
                qty = Integer.parseInt(qtyStr);
                min = Integer.parseInt(minStr);
                cost = Double.parseDouble(costStr);
                sell = Double.parseDouble(sellStr);
                if (qty < 0 || min < 0 || cost < 0 || sell < 0) throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid numeric values. All must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String sku = String.format("UQ%03d", skuCounter++);
            model.addRow(new Object[]{sku, name, qty, cost, sell, category, location, min});
            itemNameField.setText(""); quantityField.setText(""); costField.setText(""); sellField.setText(""); minStockField.setText("");
            JOptionPane.showMessageDialog(frame, "Item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        model.addTableModelListener(e -> {
            restockSKUBox.removeAllItems();
            for (int i = 0; i < model.getRowCount(); i++) {
                restockSKUBox.addItem(model.getValueAt(i, 0).toString());
            }
        });

        restockButton.addActionListener(e -> {
            String sku = (String) restockSKUBox.getSelectedItem();
            String qtyStr = restockQtyField.getText().trim();
            if (sku == null || qtyStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select SKU and enter quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int qty;
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty <= 0) throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (int i = 0; i < model.getRowCount(); i++) {
                if (sku.equals(model.getValueAt(i, 0))) {
                    int currentQty = Integer.parseInt(model.getValueAt(i, 2).toString());
                    model.setValueAt(currentQty + qty, i, 2);
                    JOptionPane.showMessageDialog(frame, "Stock updated. New quantity: " + (currentQty + qty), "Success", JOptionPane.INFORMATION_MESSAGE);
                    restockQtyField.setText("");
                    break;
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = inventoryTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = inventoryTable.convertRowIndexToModel(selectedRow);
                model.removeRow(modelRow);
                JOptionPane.showMessageDialog(frame, "Item deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an item to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        lowStockButton.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < model.getRowCount(); i++) {
                int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
                int min = Integer.parseInt(model.getValueAt(i, 7).toString());
                if (qty <= min) {
                    sb.append("SKU: ").append(model.getValueAt(i, 0)).append(", Name: ").append(model.getValueAt(i, 1))
                      .append(", Qty: ").append(qty).append(", Min: ").append(min).append("\n");
                }
            }
            if (sb.length() == 0) sb.append("No low-stock items.");
            JOptionPane.showMessageDialog(frame, sb.toString(), "Low Stock Items", JOptionPane.INFORMATION_MESSAGE);
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText().trim();
                if (text.length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1, 5));
                }
            }
        });

        updateNameComboBox(exitComboBox);
        model.addTableModelListener(e -> updateNameComboBox(exitComboBox));

        exitButton.addActionListener(e -> {
            String name = (String) exitComboBox.getSelectedItem();
            String qtyStr = exitQtyField.getText().trim();
            if (name == null || qtyStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select item and enter quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int qty;
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty <= 0) throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (int i = 0; i < model.getRowCount(); i++) {
                if (name.equals(model.getValueAt(i, 1))) {
                    int currentQty = Integer.parseInt(model.getValueAt(i, 2).toString());
                    if (qty > currentQty) {
                        JOptionPane.showMessageDialog(frame, "Sale quantity exceeds stock.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    model.setValueAt(currentQty - qty, i, 2);
                    String sku = model.getValueAt(i, 0).toString();
                    String category = model.getValueAt(i, 5).toString();
                    double sellPrice = Double.parseDouble(model.getValueAt(i, 4).toString());
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    salesLog.add(new SaleRecord(sku, name, category, qty, sellPrice, timestamp));
                    JOptionPane.showMessageDialog(frame, "Sale confirmed. Remaining stock: " + (currentQty - qty), "Success", JOptionPane.INFORMATION_MESSAGE);
                    exitQtyField.setText("");
                    break;
                }
            }
        });

        stockSummaryButton.addActionListener(e -> {
            int totalItems = model.getRowCount();
            double totalValue = 0;
            StringBuilder lowStock = new StringBuilder();
            for (int i = 0; i < model.getRowCount(); i++) {
                int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
                double cost = Double.parseDouble(model.getValueAt(i, 3).toString());
                int min = Integer.parseInt(model.getValueAt(i, 7).toString());
                totalValue += qty * cost;
                if (qty <= min) {
                    lowStock.append("SKU: ").append(model.getValueAt(i, 0)).append(", Name: ").append(model.getValueAt(i, 1))
                            .append(", Qty: ").append(qty).append(", Min: ").append(min).append("\n");
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Total Items: ").append(totalItems).append("\n");
            sb.append("Total Inventory Value: $").append(String.format("%.2f", totalValue)).append("\n");
            sb.append("Low Stock Items:\n").append(lowStock.length() == 0 ? "None" : lowStock.toString());
            reportsPanel.remove(salesScroll);
            reportsPanel.add(reportScroll, BorderLayout.CENTER);
            reportsPanel.revalidate();
            reportsPanel.repaint();
            reportArea.setText(sb.toString());
        });

        salesReportButton.addActionListener(e -> {
            salesTableModel.setRowCount(0);
            for (SaleRecord sr : salesLog) {
                salesTableModel.addRow(new Object[]{sr.timestamp, sr.sku, sr.name, sr.category, sr.quantity, String.format("%.2f", sr.sellingPrice)});
            }
            reportsPanel.remove(reportScroll);
            reportsPanel.add(salesScroll, BorderLayout.CENTER);
            reportsPanel.revalidate();
            reportsPanel.repaint();
        });

        exportCSVButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Inventory as CSV");
            int res = chooser.showSaveDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (FileWriter fw = new FileWriter(file)) {
                    for (int c = 0; c < COLUMN_NAMES.length; c++) {
                        fw.write(COLUMN_NAMES[c]);
                        if (c < COLUMN_NAMES.length - 1) fw.write(",");
                    }
                    fw.write("\n");
                    for (int r = 0; r < model.getRowCount(); r++) {
                        for (int c = 0; c < COLUMN_NAMES.length; c++) {
                            fw.write(model.getValueAt(r, c).toString());
                            if (c < COLUMN_NAMES.length - 1) fw.write(",");
                        }
                        fw.write("\n");
                    }
                    JOptionPane.showMessageDialog(frame, "Exported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error exporting CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add Ctrl+T shortcut to cycle tabs
        InputMap inputMap = tabbedPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = tabbedPane.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), "cycleTabs");
        actionMap.put("cycleTabs", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabbedPane.getSelectedIndex();
                tabbedPane.setSelectedIndex((index + 1) % tabbedPane.getTabCount());
            }
        });

        // Set focus traversal policy for Entry tab
        List<Component> entryOrder = Arrays.asList(
            itemNameField, quantityField, costField, sellField, categoryBox, locationBox, minStockField, addButton,
            restockSKUBox, restockQtyField, restockButton
        );
        entryPanel.setFocusTraversalPolicyProvider(true);
        entryPanel.setFocusTraversalPolicy(new FocusTraversalPolicy() {
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                int idx = entryOrder.indexOf(aComponent);
                return entryOrder.get((idx + 1) % entryOrder.size());
            }
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                int idx = entryOrder.indexOf(aComponent);
                return entryOrder.get((idx - 1 + entryOrder.size()) % entryOrder.size());
            }
            public Component getFirstComponent(Container aContainer) { return entryOrder.get(0); }
            public Component getLastComponent(Container aContainer) { return entryOrder.get(entryOrder.size() - 1); }
            public Component getDefaultComponent(Container aContainer) { return entryOrder.get(0); }
        });

        frame.add(tabbedPane);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }




    // Helper method to create styled buttons with hover effect
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(74, 144, 226)); // Blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 120, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 160, 240)); // Lighter blue on hover
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(74, 144, 226));
            }
        });
        return button;
    }




    // Helper method to style text fields
    private void styleTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.setBackground(Color.WHITE);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(74, 144, 226), 2, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });
    }




    // Helper method to style combo boxes
    private void styleComboBox(JComboBox<?> box) {
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }




    // Unchanged helper method
    private void updateNameComboBox(JComboBox<String> box) {
        box.removeAllItems();
        for (int i = 0; i < model.getRowCount(); i++) {
            box.addItem(model.getValueAt(i, 1).toString());
        }
    }

}