package main;

public class OID {
    // System Information OIDs
    public static final String SYSTEM_DESCRIPTION = "1.1.1";  // System description
    public static final String SYSTEM_UPTIME = "1.1.2";       // System uptime in seconds
    public static final String SYSTEM_CONTACT = "1.1.3";      // System contact information
    
    // Interface Information OIDs
    public static final String INTERFACE_COUNT = "2.1.1";     // Number of interfaces
    public static final String INTERFACE_STATUS = "2.1.2";    // Interface status (up/down)
    public static final String INTERFACE_SPEED = "2.1.3";     // Interface speed in Mbps
    
    // Performance OIDs
    public static final String CPU_USAGE = "3.1.1";          // CPU usage percentage
    public static final String MEMORY_USAGE = "3.1.2";       // Memory usage percentage
    public static final String DISK_USAGE = "3.1.3";         // Disk usage percentage
    
    // Error OIDs
    public static final String ERROR_COUNT = "4.1.1";        // Number of errors
    public static final String ERROR_TYPE = "4.1.2";         // Type of error
    public static final String ERROR_DESCRIPTION = "4.1.3";  // Error description
    
    // Connection OIDs
    public static final String CONNECTION_STATUS = "5.1.1";   // Connection status
    public static final String CONNECTION_TYPE = "5.1.2";     // Connection type
    public static final String CONNECTION_SPEED = "5.1.3";    // Connection speed
} 