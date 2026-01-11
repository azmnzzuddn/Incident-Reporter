package com.communityreporter.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.communityreporter.data.CSVRepository;
import com.communityreporter.data.DataException;
import com.communityreporter.model.Incident;
import com.communityreporter.model.IncidentStatus;
import com.communityreporter.model.MaintenanceIncident;
import com.communityreporter.model.SafetyIncident;

public class IncidentReporterUI extends JFrame {
    
    // Backend Connections
    private CSVRepository repository = new CSVRepository();
    private List<Incident> incidentList = new ArrayList<>();
    
    // UI Components
    private JComboBox<Integer> severityCombo;
    private JComboBox<String> typeCombo;
    private JTextField locationField, dateField;
    private JTextArea descriptionArea;
    private JTable incidentTable;
    private DefaultTableModel tableModel;
    
    // Dashboard Labels
    private JLabel totalLabel, openLabel, avgSeverityLabel, resolvedLabel;
    
    // Panels that need repainting
    private SeverityChartPanel severityChartPanel;
    private TypeChartPanel typeChartPanel;

    public IncidentReporterUI() {
        setTitle("Community Incident Reporter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        initializeUI();
        refreshData(); 
    }
    
    private void refreshData() {
        try {
            incidentList = repository.loadIncidents();
            
            // 1. Update Table
            updateTable();
            
            // 2. Update Dashboard Stats
            updateDashboard();
            
            // 3. Refresh Charts
            if (severityChartPanel != null) severityChartPanel.repaint();
            if (typeChartPanel != null) typeChartPanel.updateDimensions();
            
        } catch (DataException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void saveData() {
        try {
            repository.saveIncidents(incidentList);
            refreshData(); 
        } catch (DataException e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage());
        }
    }
    
    private void initializeUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        tabbedPane.addTab("📝 Report Incident", createReportPanel());
        tabbedPane.addTab("🔍 View Incidents", createViewPanel());
        tabbedPane.addTab("📊 Analytics", createAnalyticsPanel());
        
        // Custom Chart Panels
        severityChartPanel = new SeverityChartPanel();
        tabbedPane.addTab("📈 Severity Dist.", severityChartPanel);
        
        typeChartPanel = new TypeChartPanel();
        tabbedPane.addTab("📊 Type Dist.", new JScrollPane(typeChartPanel));
        
        add(tabbedPane);
    }
    
    // --- 1. REPORT PANEL (With Severity Chips) ---
    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel title = new JLabel("Report New Incident");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; 
        panel.add(title, gbc);

        // Type
        gbc.gridy++; gbc.gridwidth=1;
        panel.add(new JLabel("Type:"), gbc);
        
        String[] types = {"Pothole", "Broken Street Light", "Graffiti", "Illegal Dumping", "Traffic Hazard", "Water Leak", "Sewage Issue", "Park Maintenance", "Vandalism", "Noise Complaint", "Other"};
        typeCombo = new JComboBox<>(types);
        gbc.gridx=1; panel.add(typeCombo, gbc);
        
        // Severity (CHIP RENDERER)
        gbc.gridx=0; gbc.gridy++;
        panel.add(new JLabel("Severity:"), gbc);
        
        Integer[] levels = {1, 2, 3, 4, 5};
        severityCombo = new JComboBox<>(levels);
        severityCombo.setRenderer(new SeverityChipRenderer()); // <--- Custom Chip Logic
        gbc.gridx=1; panel.add(severityCombo, gbc);
        
        // Location
        gbc.gridx=0; gbc.gridy++;
        panel.add(new JLabel("Location:"), gbc);
        locationField = new JTextField(20);
        gbc.gridx=1; panel.add(locationField, gbc);
        
        // Date
        gbc.gridx=0; gbc.gridy++;
        panel.add(new JLabel("Date (DD/MM/YYYY):"), gbc);
        dateField = new JTextField(new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        gbc.gridx=1; panel.add(dateField, gbc);
        
        // Desc
        gbc.gridx=0; gbc.gridy++;
        panel.add(new JLabel("Description:"), gbc);
        descriptionArea = new JTextArea(4, 20);
        gbc.gridx=1; panel.add(new JScrollPane(descriptionArea), gbc);
        
        // Submit
        JButton submitBtn = new JButton("Submit Report");
        submitBtn.setBackground(new Color(70, 130, 180));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Arial", Font.BOLD, 14));
        submitBtn.addActionListener(e -> submitIncident());
        
        gbc.gridx=1; gbc.gridy++; 
        panel.add(submitBtn, gbc);
        
        return panel;
    }

    private void submitIncident() {
        String type = (String) typeCombo.getSelectedItem();
        int sev = (Integer) severityCombo.getSelectedItem();
        String id = "inc-" + (incidentList.size() + 1);
        
        // Polymorphism Logic
        Incident inc;
        if (isMaintenance(type)) {
            inc = new MaintenanceIncident(id, locationField.getText(), descriptionArea.getText(), dateField.getText(), IncidentStatus.OPEN, 0.0);
            ((MaintenanceIncident)inc).setSpecificType(type);
        } else {
            inc = new SafetyIncident(id, locationField.getText(), descriptionArea.getText(), dateField.getText(), IncidentStatus.OPEN, sev);
            ((SafetyIncident)inc).setSpecificType(type);
        }
        
        incidentList.add(inc);
        saveData();
        JOptionPane.showMessageDialog(this, "Incident Reported Successfully!");
        locationField.setText(""); descriptionArea.setText("");
    }
    
    private boolean isMaintenance(String t) {
        return t.contains("Light") || t.contains("Pothole") || t.contains("Water") || t.contains("Sewage");
    }

    // --- 2. VIEW PANEL ---
    private JPanel createViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"ID", "Type", "Severity", "Location", "Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        incidentTable = new JTable(tableModel);
        incidentTable.setRowHeight(30);
        
        // Apply Chip Renderer to Table too
        incidentTable.getColumnModel().getColumn(2).setCellRenderer(new SeverityTableRenderer());
        
        panel.add(new JScrollPane(incidentTable), BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel();
        JButton resolveBtn = new JButton("Mark Selected Resolved");
        resolveBtn.addActionListener(e -> markResolved());
        btnPanel.add(resolveBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void updateTable() {
        tableModel.setRowCount(0);
        for(Incident i : incidentList) {
            Object sev = (i instanceof SafetyIncident) ? ((SafetyIncident)i).getSeverity() : 0;
            tableModel.addRow(new Object[]{i.getId(), i.getType(), sev, i.getLocation(), i.getDate(), i.getStatus()});
        }
    }
    
    private void markResolved() {
        int row = incidentTable.getSelectedRow();
        if(row == -1) return;
        String id = (String) tableModel.getValueAt(row, 0);
        incidentList.stream().filter(i -> i.getId().equals(id)).findFirst().ifPresent(i -> i.setStatus(IncidentStatus.RESOLVED));
        saveData();
    }

    // --- 3. ANALYTICS DASHBOARD ---
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Analytics Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);
        
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        totalLabel = new JLabel("0");
        openLabel = new JLabel("0");
        avgSeverityLabel = new JLabel("0.0");
        resolvedLabel = new JLabel("0");
        
        cardsPanel.add(createStatCard("Total Incidents", totalLabel, new Color(70, 130, 180)));
        cardsPanel.add(createStatCard("Open Incidents", openLabel, new Color(220, 60, 60)));
        cardsPanel.add(createStatCard("Avg Severity", avgSeverityLabel, new Color(255, 165, 0)));
        cardsPanel.add(createStatCard("Resolved", resolvedLabel, new Color(60, 180, 75)));
        
        panel.add(cardsPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bg);
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Arial", Font.BOLD, 16)); t.setForeground(Color.WHITE);
        
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 40)); valueLabel.setForeground(Color.WHITE);
        
        card.add(t, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }
    
    private void updateDashboard() {
        long total = incidentList.size();
        long open = incidentList.stream().filter(i -> i.getStatus() == IncidentStatus.OPEN).count();
        long resolved = incidentList.stream().filter(i -> i.getStatus() == IncidentStatus.RESOLVED).count();
        
        double avgSev = incidentList.stream()
            .filter(i -> i instanceof SafetyIncident)
            .mapToInt(i -> ((SafetyIncident)i).getSeverity())
            .average().orElse(0.0);
            
        totalLabel.setText(String.valueOf(total));
        openLabel.setText(String.valueOf(open));
        resolvedLabel.setText(String.valueOf(resolved));
        avgSeverityLabel.setText(String.format("%.1f", avgSev));
    }

    // --- 4. CUSTOM RENDERERS (The Chips) ---
    
    private Color getSeverityColor(int s) {
        switch(s) {
            case 1: return new Color(144, 238, 144); // Green (Insignificant)
            case 2: return new Color(173, 216, 230); // Blue (Minor)
            case 3: return new Color(255, 255, 224); // Yellow (Moderate)
            case 4: return new Color(255, 165, 0);   // Orange (Major)
            case 5: return new Color(255, 99, 71);   // Red (Catastrophic)
            default: return Color.WHITE;
        }
    }
    
    private String getSeverityLabel(int s) {
        switch(s) {
            case 1: return "1 - Insignificant";
            case 2: return "2 - Minor";
            case 3: return "3 - Moderate";
            case 4: return "4 - Major";
            case 5: return "5 - Catastrophic";
            default: return String.valueOf(s);
        }
    }

    // Dropdown Renderer
    class SeverityChipRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Integer) {
                int s = (Integer) value;
                label.setText(getSeverityLabel(s));
                label.setBackground(getSeverityColor(s));
                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); // Padding
                if (isSelected) label.setBackground(label.getBackground().darker());
            }
            return label;
        }
    }
    
    // Table Cell Renderer
    class SeverityTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (value instanceof Integer && (Integer)value > 0) {
                int s = (Integer) value;
                setText(getSeverityLabel(s));
                setBackground(getSeverityColor(s));
            } else {
                setText("N/A");
                setBackground(Color.WHITE);
            }
            if (isSelected) setBackground(getBackground().darker());
            return c;
        }
    }

    // --- 5. CHARTS (Ported Logic) ---
    
    class SeverityChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Calculate Counts
            Map<Integer, Integer> counts = new HashMap<>();
            for(int k=1; k<=5; k++) counts.put(k, 0);
            for(Incident i : incidentList) {
                if (i instanceof SafetyIncident) {
                    int s = ((SafetyIncident)i).getSeverity();
                    counts.put(s, counts.getOrDefault(s, 0) + 1);
                }
            }
            
            // Draw Logic
            int w = getWidth(), h = getHeight();
            int max = counts.values().stream().mapToInt(v->v).max().orElse(1);
            int barW = (w - 100) / 5;
            
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            for(int i=1; i<=5; i++) {
                int count = counts.get(i);
                int barH = (int)((double)count / max * (h - 100));
                int x = 50 + (i-1) * barW;
                int y = h - 50 - barH;
                
                g2d.setColor(getSeverityColor(i));
                g2d.fillRect(x + 10, y, barW - 20, barH);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x + 10, y, barW - 20, barH);
                g2d.drawString("Lvl " + i, x + barW/2 - 15, h - 30);
                g2d.drawString(String.valueOf(count), x + barW/2 - 5, y - 5);
            }
            g2d.drawString("Severity Distribution", w/2 - 50, 30);
        }
    }
    
    class TypeChartPanel extends JPanel {
        public TypeChartPanel() { setBackground(Color.WHITE); }
        public void updateDimensions() { setPreferredSize(new Dimension(800, Math.max(400, incidentList.size() * 30))); revalidate(); }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Map<String, Long> counts = incidentList.stream()
                .collect(Collectors.groupingBy(Incident::getType, Collectors.counting()));
            
            int max = counts.values().stream().mapToInt(Long::intValue).max().orElse(1);
            int y = 50;
            int rowH = 30;
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            for(Map.Entry<String, Long> entry : counts.entrySet()) {
                int barW = (int)((double)entry.getValue() / max * (getWidth() - 250));
                
                g2d.setColor(new Color(100, 149, 237));
                g2d.fillRect(200, y, barW, 20);
                g2d.setColor(Color.BLACK);
                g2d.drawString(entry.getKey(), 10, y + 15);
                g2d.drawString(String.valueOf(entry.getValue()), 200 + barW + 10, y + 15);
                
                y += rowH;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IncidentReporterUI().setVisible(true));
    }
}