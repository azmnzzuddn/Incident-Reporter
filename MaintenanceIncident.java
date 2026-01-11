package com.communityreporter.model;

public class MaintenanceIncident extends Incident {
    private double estimatedCost;
    // Default to MAINTENANCE, but allow overwriting
    private String specificType = "MAINTENANCE";

    public MaintenanceIncident(String id, String location, String description, String date, IncidentStatus status, double estimatedCost) {
        super(id, location, description, date, status);
        this.estimatedCost = estimatedCost;
    }
    
    // FIX: Allow setting specific type
    public void setSpecificType(String type) {
        this.specificType = type;
    }

    // FIX: Return the variable
    @Override
    public String getType() { 
        return specificType; 
    }

    public double getEstimatedCost() { return estimatedCost; }

    @Override
    public String toCSV() {
        return String.format(super.toCSV(), estimatedCost);
    }
}