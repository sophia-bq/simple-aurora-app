#!/bin/bash

echo "Starting Aurora Connection App with auto-restart..."

while true; do
    echo "$(date): Starting application..."
    ./gradlew run
    exit_code=$?
    
    if [ $exit_code -eq 0 ]; then
        echo "$(date): Application exited normally, restarting..."
        sleep 2
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
