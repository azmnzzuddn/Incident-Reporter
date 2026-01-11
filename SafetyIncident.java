package com.communityreporter.model;

public class SafetyIncident extends Incident {
    private int severityLevel;
    // Default to SAFETY, but allow overwriting
    private String specificType = "SAFETY";

    public SafetyIncident(String id, String location, String description, String date, IncidentStatus status, int severityLevel) {
        super(id, location, description, date, status);
        this.severityLevel = severityLevel;
    }

    // FIX: Allow setting the specific type (e.g., "Vandalism")
    public void setSpecificType(String type) {
        this.specificType = type;
    }

    // FIX: Return the variable, NOT the hardcoded string
    @Override
    public String getType() { 
        return specificType; 
    }

    public int getSeverity() { return severityLevel; }

    @Override
    public String toCSV() {
        // We use the parent toCSV, which calls our fixed getType()
        return String.format(super.toCSV(), severityLevel);
    }
}