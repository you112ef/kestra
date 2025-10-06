#!/bin/bash

# Docker build script for Kestra
set -e

echo "Building Kestra Docker image..."

# Check if we're in a CI environment
if [ "$CI" = "true" ] || [ "$GITHUB_ACTIONS" = "true" ]; then
    echo "CI environment detected, using robust Dockerfile..."
    DOCKERFILE="Dockerfile.robust"
else
    echo "Local environment detected, checking for pre-built artifacts..."
    
    # Check if build artifacts exist
    if [ -f "build/scriptsShadow/kestra" ] && [ -d "build/libs" ]; then
        echo "Pre-built artifacts found, using prebuilt Dockerfile..."
        DOCKERFILE="Dockerfile.prebuilt"
    else
        echo "No pre-built artifacts found, building first..."
        ./build-for-deploy.sh
        DOCKERFILE="Dockerfile.prebuilt"
    fi
fi

echo "Using Dockerfile: $DOCKERFILE"

# Build the Docker image
docker build -f "$DOCKERFILE" -t kestra-app .

echo "Docker build completed successfully!"
echo "To run the container:"
echo "  docker run -p 8080:8080 kestra-app server local"