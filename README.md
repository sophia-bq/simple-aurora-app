# Simple Aurora Connection App

A minimal Java application that connects to Aurora clusters using the AWS Advanced JDBC Wrapper.

## Setup

1. Update connection details in `src/main/resources/aurora.properties`
2. Or modify the constants in `AuroraConnectionApp.java`

## Running the Application

```bash
# Build and run
./gradlew run

# Or build JAR and run
./gradlew build
java -jar build/libs/simple-aurora-app.jar
```

## Commands

- `connect` - Test connection to Aurora cluster
- `test` - Run a simple query
- `bulk <n>` - Run n simple queries in a row
- `quit` - Exit the application

## Restart Capability

The application can be restarted by:
1. Using Ctrl+C and running again
2. External process managers can automatically restart

## Configuration

The app uses AWS JDBC Wrapper with:
- Failover plugin for Aurora failover handling
- Enhanced Failure Monitoring (EFM)
- Configurable socket and connect timeouts
