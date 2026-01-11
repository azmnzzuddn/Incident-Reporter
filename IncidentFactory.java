package com.communityreporter.data;

import com.communityreporter.model.Incident;
import com.communityreporter.model.IncidentStatus;
import com.communityreporter.model.MaintenanceIncident;
import com.communityreporter.model.SafetyIncident;

public class IncidentFactory {
    
    public Incident createIncident(String csvLine) {
        // Regex to split by comma but ignore commas inside quotes
        String[] parts = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        
        // CSV Format: IncidentID, Type, Severity/Cost, Location, Date, Status, Description
        if (parts.length < 6) return null; 

        String id = parts[0].trim();
        String type = parts[1].trim();
        String extra = parts[2].trim(); 
        String loc = parts[3].replace("\"", "").trim();
        String date = parts[4].trim();
        IncidentStatus status = IncidentStatus.fromString(parts[5].trim());
        String desc = (parts.length > 6) ? parts[6].replace("\"", "").trim() : "";

        if (isMaintenance(type)) {
            double cost = 0.0;
            try { cost = Double.parseDouble(extra); } catch(NumberFormatException e) {}
            
            MaintenanceIncident inc = new MaintenanceIncident(id, loc, desc, date, status, cost);
            inc.setSpecificType(type); // FIX: Ensure we save "Pothole" instead of "MAINTENANCE"
            return inc;
        } else {
            int severity = 1;
            try { severity = Integer.parseInt(extra); } catch(NumberFormatException e) {}

            SafetyIncident inc = new SafetyIncident(id, loc, desc, date, status, severity);
            inc.setSpecificType(type); // FIX: Ensure we save "Vandalism" instead of "SAFETY"
            return inc;
        }
    }

    private boolean isMaintenance(String type) {
        return type.equalsIgnoreCase("Broken Street Light") || 
               type.equalsIgnoreCase("Pothole") || 
               type.equalsIgnoreCase("Water Leak") ||
               type.equalsIgnoreCase("Sewage Issue") ||
               type.equalsIgnoreCase("Park Maintenance") ||
               type.equalsIgnoreCase("Graffiti") ||
               type.equalsIgnoreCase("MAINTENANCE");
    }
}