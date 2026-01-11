package com.communityreporter.tests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.communityreporter.data.CSVRepository;
import com.communityreporter.data.DataException;
import com.communityreporter.data.IncidentFactory;
import com.communityreporter.model.Incident;
import com.communityreporter.model.IncidentStatus;
import com.communityreporter.model.MaintenanceIncident;
import com.communityreporter.model.SafetyIncident;

class IncidentSystemTest {

    // --- GROUP 1: MODEL CREATION ---
    
    // Test 1: Safety Incident Creation & Getters
    @Test
    void testSafetyIncident() {
        SafetyIncident i = new SafetyIncident("1", "Park", "Desc", "Date", IncidentStatus.OPEN, 5);
        assertEquals(5, i.getSeverity());
        assertEquals(IncidentStatus.OPEN, i.getStatus());
    }

    // Test 2: Maintenance Incident Creation
    @Test
    void testMaintenanceIncident() {
        MaintenanceIncident i = new MaintenanceIncident("2", "Road", "Desc", "Date", IncidentStatus.OPEN, 100.0);
        assertEquals(100.0, i.getEstimatedCost());
    }

    // Test 3: Specific Type Setting (Polymorphism)
    @Test
    void testSpecificTypeSetting() {
        SafetyIncident i = new SafetyIncident("3", "Loc", "Desc", "Date", IncidentStatus.OPEN, 1);
        i.setSpecificType("Vandalism");
        assertEquals("Vandalism", i.getType());
    }

    // --- GROUP 2: CSV LOGIC ---

    // Test 4: CSV String Generation
    @Test
    void testToCSV() {
        SafetyIncident i = new SafetyIncident("4", "Loc", "Desc", "Date", IncidentStatus.OPEN, 5);
        i.setSpecificType("Safety");
        String csv = i.toCSV();
        // Expect: ID, Type, Severity, Loc, Date, Status, Desc
        assertTrue(csv.contains("4"));
        assertTrue(csv.contains("Safety"));
        assertTrue(csv.contains("5")); 
    }
    
    // Test 5: CSV Quote Handling
    @Test
    void testCSVQuotes() {
        SafetyIncident i = new SafetyIncident("5", "Loc, Comma", "Desc", "Date", IncidentStatus.OPEN, 1);
        String csv = i.toCSV();
        assertTrue(csv.contains("\"Loc, Comma\"")); // Should be quoted
    }

    // --- GROUP 3: FACTORY & PARSING ---

    // Test 6: Factory Creates Correct Class
    @Test
    void testFactoryClassDetection() {
        IncidentFactory f = new IncidentFactory();
        String line = "1,Pothole,0,Loc,Date,Not Resolved,Desc";
        Incident i = f.createIncident(line);
        assertTrue(i instanceof MaintenanceIncident);
        assertEquals("Pothole", i.getType());
    }

    // Test 7: Factory Handles Safety Incident
    @Test
    void testFactorySafety() {
        IncidentFactory f = new IncidentFactory();
        String line = "2,Vandalism,5,Loc,Date,Not Resolved,Desc";
        Incident i = f.createIncident(line);
        assertTrue(i instanceof SafetyIncident);
        assertEquals(5, ((SafetyIncident)i).getSeverity());
    }
    
    // Test 8: Factory Handles Bad Data Gracefully
    @Test
    void testFactoryBadNumber() {
        IncidentFactory f = new IncidentFactory();
        String line = "3,Vandalism,BAD_NUM,Loc,Date,Not Resolved,Desc";
        Incident i = f.createIncident(line);
        assertEquals(1, ((SafetyIncident)i).getSeverity()); // Defaults to 1
    }

    // --- GROUP 4: REPOSITORY & ENUMS ---

    // Test 9: Enum String Conversion
    @Test
    void testEnum() {
        assertEquals(IncidentStatus.OPEN, IncidentStatus.fromString("Not Resolved"));
        assertEquals(IncidentStatus.RESOLVED, IncidentStatus.fromString("Resolved"));
    }

    // Test 10: Repository Load Handles Missing File
    @Test
    void testRepoMissingFile() throws DataException {
        CSVRepository repo = new CSVRepository();
        // Assuming file might not exist in test env, this should return empty list, not crash
        List<Incident> list = repo.loadIncidents();
        assertNotNull(list);
    }
}