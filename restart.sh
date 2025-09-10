#!/bin/bash

echo "Starting Aurora Connection App with auto-restart..."

# Download dependencies first if not present
if [ ! -d "lib" ]; then
    echo "Downloading dependencies..."
    mkdir -p lib
    wget -q -O lib/aws-advanced-jdbc-wrapper-2.6.3.jar https://repo1.maven.org/maven2/software/amazon/jdbc/aws-advanced-jdbc-wrapper/2.6.3/aws-advanced-jdbc-wrapper-2.6.3.jar
    wget -q -O lib/mariadb-java-client-3.1.4.jar https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/3.1.4/mariadb-java-client-3.1.4.jar
fi

# Compile once at startup
echo "Compiling application..."
mkdir -p build/classes/java/main
javac -cp "lib/*" -d build/classes/java/main src/main/java/software/amazon/aurora/AuroraConnectionApp.java
cp -r src/main/resources/* build/classes/java/main/ 2>/dev/null || true

while true; do
    echo "$(date): Starting application..."
    java -cp "build/classes/java/main:lib/*" software.amazon.aurora.AuroraConnectionApp
    exit_code=$?
    
    if [ $exit_code -eq 0 ]; then
        echo "$(date): Application exited normally, restarting..."
        sleep 1
    else
        echo "$(date): Application crashed with exit code $exit_code"
        read -p "Restart? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            break
        fi
    fi
done

echo "Application stopped."
