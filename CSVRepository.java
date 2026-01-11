package com.communityreporter.data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.communityreporter.model.Incident;

public class CSVRepository {
    private final String FILE_PATH = "src/data/incidentData.csv";
    private IncidentFactory factory = new IncidentFactory();

    public CSVRepository() {
        try {
            Path path = Paths.get("src/data");
            if (!Files.exists(path)) Files.createDirectories(path);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveIncidents(List<Incident> incidents) throws DataException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            // Header matching UI format
            writer.println("IncidentID,Type,Severity,Location,Date,Status,Description");
            for (Incident i : incidents) {
                writer.println(i.toCSV());
            }
        } catch (IOException e) {
            throw new DataException("Error saving data: " + e.getMessage());
        }
    }

    public List<Incident> loadIncidents() throws DataException {
        List<Incident> list = new ArrayList<>();
        if (!Files.exists(Paths.get(FILE_PATH))) return list;

        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!line.trim().isEmpty()) {
                    Incident inc = factory.createIncident(line);
                    if (inc != null) list.add(inc);
                }
            }
        } catch (Exception e) {
            throw new DataException("Error loading data: " + e.getMessage());
        }
        return list;
    }
}