#!/bin/bash

# Build script for Kestra deployment
set -e

echo "Building Kestra application for deployment..."

# Clean and build the application
echo "Running Gradle build..."
./gradlew clean build -x test

# Ensure the executable exists and is executable
if [ ! -f "build/scriptsShadow/kestra" ]; then
    echo "Error: Kestra executable not found at build/scriptsShadow/kestra"
    exit 1
fi

chmod +x build/scriptsShadow/kestra

echo "Build completed successfully!"
echo "Kestra executable: build/scriptsShadow/kestra"
echo "JAR files: build/libs/"

# Show file sizes
echo "Build artifacts:"
ls -la build/scriptsShadow/kestra
ls -la build/libs/