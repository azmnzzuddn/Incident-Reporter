package com.communityreporter.model;

public abstract class Incident {
    protected String id;
    protected String location;
    protected String description;
    protected String date;
    protected IncidentStatus status;

    public Incident(String id, String location, String description, String date, IncidentStatus status) {
        this.id = id;
        this.location = location;
        this.description = description;
        this.date = date;
        this.status = status;
    }

    public abstract String getType();

    // CSV Format matching UI: IncidentID,Type,Severity/Extra,Location,Date,Status,Description
    public String toCSV() {
        // Handle quotes for CSV safety
        String safeLoc = location.contains(",") ? "\"" + location + "\"" : location;
        String safeDesc = description.contains(",") ? "\"" + description + "\"" : description;
        
        // This base method returns common parts, subclasses insert their extra field (Severity/Cost)
        // We defer the full string construction to subclasses or helper to ensure order:
        // ID, Type, Extra, Loc, Date, Status, Desc
        return String.format("%s,%s,%%s,%s,%s,%s,%s", 
            id, getType(), safeLoc, date, status.toString(), safeDesc);
    }
    
    public String getId() { return id; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
}