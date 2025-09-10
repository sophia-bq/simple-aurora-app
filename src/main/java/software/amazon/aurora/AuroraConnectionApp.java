package software.amazon.aurora;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

public class AuroraConnectionApp {
    
    // Default configuration constants
    private static final String DEFAULT_ENDPOINT = "your-aurora-cluster.cluster-xxxxx.us-east-1.rds.amazonaws.com";
    private static final String DEFAULT_DATABASE = "your_database";
    private static final String DEFAULT_USERNAME = "your_username";
    private static final String DEFAULT_PASSWORD = "your_password";
    private static final String DEFAULT_WRAPPER_PLUGINS = "failover2,efm2";
    private static final String DEFAULT_SOCKET_TIMEOUT = "0";
    private static final String DEFAULT_CONNECT_TIMEOUT = "10000";

    private static Connection persistentConnection;
    private static Properties config;
    
    public static void main(String[] args) {
        System.out.println("Aurora Connection App Started");
        System.out.println("Commands: 'connect', 'test', 'bulk <n>', 'quit'");
        
        // Load configuration
        loadConfiguration();
        
        // Initialize persistent connection
        initializeConnection();
        
        // Add shutdown hook to close connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (persistentConnection != null) {
                try {
                    persistentConnection.close();
                    System.out.println("Connection closed.");
                } catch (Exception e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        }));
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine().trim().toLowerCase();
            
            if (command.startsWith("bulk ")) {
                try {
                    int n = Integer.parseInt(command.substring(5));
                    runBulkQueries(n);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number. Use: bulk <n>");
                }
            } else {
                switch (command) {
                case "connect":
                    testConnection();
                    break;
                case "test":
                    runSimpleQuery();
                    break;
                case "quit":
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Unknown command. Use: connect, test, bulk <n>, quit");
                }
            }
        }
    }
    
    private static void loadConfiguration() {
        config = new Properties();
        try (InputStream input = AuroraConnectionApp.class.getClassLoader().getResourceAsStream("aurora.properties")) {
            if (input == null) {
                System.out.println("✗ aurora.properties file not found");
                return;
            }
            config.load(input);
            System.out.println("✓ Configuration loaded");
        } catch (Exception e) {
            System.out.println("✗ Failed to load configuration: " + e.getMessage());
        }
    }

    
    private static void initializeConnection() {
        System.out.println("Initializing persistent connection...");
        
        Properties props = new Properties();
        props.setProperty("user", config.getProperty("aurora.username", DEFAULT_USERNAME));
        props.setProperty("password", config.getProperty("aurora.password", DEFAULT_PASSWORD));
        props.setProperty("wrapperPlugins", config.getProperty("wrapper.plugins", DEFAULT_WRAPPER_PLUGINS));
        props.setProperty("socketTimeout", config.getProperty("socket.timeout", DEFAULT_SOCKET_TIMEOUT));
        props.setProperty("connectTimeout", config.getProperty("connect.timeout", DEFAULT_CONNECT_TIMEOUT));
        
        String url = "jdbc:aws-wrapper:mariadb://" + config.getProperty("aurora.endpoint", DEFAULT_ENDPOINT) + ":3306/" + config.getProperty("aurora.database", DEFAULT_DATABASE);
        
        try {
            persistentConnection = DriverManager.getConnection(url, props);
            System.out.println("✓ Persistent connection established!");
        } catch (Exception e) {
            System.out.println("✗ Failed to establish persistent connection: " + e.getMessage());
        }
    }
    
    private static void testConnection() {
        System.out.println("Testing persistent connection...");
        
        if (persistentConnection == null) {
            System.out.println("✗ No persistent connection available");
            return;
        }
        
        try {
            if (persistentConnection.isClosed()) {
                System.out.println("✗ Connection is closed");
                return;
            }
            System.out.println("✓ Persistent connection is active!");
            System.out.println("Connection URL: " + persistentConnection.getMetaData().getURL());
        } catch (Exception e) {
            System.out.println("✗ Connection test failed: " + e.getMessage());
        }
    }
    
    private static void runSimpleQuery() {
        System.out.println("Running simple query...");
        
        if (persistentConnection == null) {
            System.out.println("✗ No persistent connection available");
            return;
        }
        
        try (Statement stmt = persistentConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 as test_value")) {
            
            if (rs.next()) {
                System.out.println("✓ Query result: " + rs.getInt("test_value"));
            }
        } catch (Exception e) {
            System.out.println("✗ Query failed: " + e.getMessage());
        }
    }
    
    private static void runBulkQueries(int n) {
        System.out.println("Running " + n + " queries...");
        
        if (persistentConnection == null) {
            System.out.println("✗ No persistent connection available");
            return;
        }
        
        for (int i = 1; i <= n; i++) {
            try (Statement stmt = persistentConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT " + i + " as query_number")) {
                
                if (rs.next()) {
                    System.out.println("Query " + i + " result: " + rs.getInt("query_number"));
                }
            } catch (Exception e) {
                System.out.println("✗ Query " + i + " failed: " + e.getMessage());
            }
        }
        System.out.println("Completed " + n + " queries.");
    }
}
