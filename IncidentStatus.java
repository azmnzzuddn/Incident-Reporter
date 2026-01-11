package com.communityreporter.model;

public enum IncidentStatus {
    OPEN, IN_PROGRESS, RESOLVED;

    public static IncidentStatus fromString(String text) {
        if (text.equalsIgnoreCase("Not Resolved")) return OPEN;
        if (text.equalsIgnoreCase("In Progress")) return IN_PROGRESS;
        if (text.equalsIgnoreCase("Resolved")) return RESOLVED;
        return OPEN;
    }
    
    @Override
    public String toString() {
        if (this == OPEN) return "Not Resolved";
        if (this == IN_PROGRESS) return "In Progress";
        if (this == RESOLVED) return "Resolved";
        return super.toString();
    }
}