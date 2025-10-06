# Kestra Deployment Guide

This guide explains how to deploy Kestra to various platforms, including fixing the common Docker deployment issue.

## Problem Analysis

The error `/app/kestra: not found` occurs because the Docker container doesn't contain the built Kestra executable. The original Dockerfile expects a pre-built distribution, but we need to build the application first.

## Solution

We've created multiple Dockerfile options and build scripts to fix this issue:

### Option 1: Multi-stage Build (Recommended for CI/CD)

Use `Dockerfile.deploy` for a complete build process:

```bash
# Build the Docker image
docker build -f Dockerfile.deploy -t kestra-app .

# Run the container
docker run -p 8080:8080 kestra-app server local
```

### Option 2: Pre-built Application

Use `Dockerfile.simple` if you've already built the application locally:

```bash
# First, build the application
./build-for-deploy.sh

# Then build the Docker image
docker build -f Dockerfile.simple -t kestra-app .

# Run the container
docker run -p 8080:8080 kestra-app server local
```

## Build Script

The `build-for-deploy.sh` script ensures everything is built correctly:

```bash
chmod +x build-for-deploy.sh
./build-for-deploy.sh
```

This script:
- Cleans and builds the application
- Verifies the executable exists
- Makes it executable
- Shows build artifacts

## Platform-Specific Deployment

### Render.com

1. **Update your Dockerfile**: Use `Dockerfile.deploy` as your main Dockerfile
2. **Build Command**: No additional build command needed (multi-stage build handles it)
3. **Start Command**: `server local` (or `server standalone` for production)

### Docker Compose

Update your `docker-compose.yml` to use the new image:

```yaml
services:
  kestra:
    build:
      context: .
      dockerfile: Dockerfile.deploy
    command: server local
    ports:
      - "8080:8080"
    environment:
      KESTRA_CONFIGURATION: |
        kestra:
          repository:
            type: h2
          storage:
            type: local
            local:
              basePath: "/app/storage"
```

### Kubernetes

Create a deployment with the built image:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kestra
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kestra
  template:
    metadata:
      labels:
        app: kestra
    spec:
      containers:
      - name: kestra
        image: kestra-app:latest
        command: ["server", "local"]
        ports:
        - containerPort: 8080
```

## Configuration

### Local Mode (H2 Database)
- No external database required
- Data stored in `/app/storage`
- Good for development and testing

### Standalone Mode (PostgreSQL)
- Requires PostgreSQL database
- Better for production
- Configure database connection in environment variables

## Environment Variables

```bash
# Database configuration (for standalone mode)
KESTRA_CONFIGURATION='{
  "datasources": {
    "postgres": {
      "url": "jdbc:postgresql://postgres:5432/kestra",
      "driverClassName": "org.postgresql.Driver",
      "username": "kestra",
      "password": "k3str4"
    }
  },
  "kestra": {
    "repository": {"type": "postgres"},
    "storage": {"type": "local", "local": {"basePath": "/app/storage"}},
    "queue": {"type": "postgres"}
  }
}'

# CORS configuration
KESTRA_CORS_ENABLED=true
KESTRA_CORS_ALLOWED_ORIGINS=http://localhost:3000,https://yourdomain.com
```

## Troubleshooting

### Common Issues

1. **`/app/kestra: not found`**
   - Solution: Use `Dockerfile.deploy` or ensure `build-for-deploy.sh` was run

2. **Permission denied**
   - Solution: The Dockerfile sets proper permissions, but ensure the executable is built

3. **Port conflicts**
   - Solution: Map ports correctly (`-p 8080:8080`)

4. **Database connection issues**
   - Solution: Check database configuration and network connectivity

### Debug Commands

```bash
# Check if executable exists in container
docker run --rm kestra-app ls -la /app/

# Test the executable
docker run --rm kestra-app /app/kestra --help

# Check logs
docker logs <container-id>
```

## Production Considerations

1. **Use standalone mode** with PostgreSQL for production
2. **Set up proper secrets management** for database credentials
3. **Configure monitoring and logging**
4. **Set up health checks**
5. **Use a reverse proxy** (nginx, traefik) for SSL termination
6. **Configure backup strategies** for data persistence

## Quick Start

For a quick deployment test:

```bash
# Build and run locally
./build-for-deploy.sh
docker build -f Dockerfile.simple -t kestra-test .
docker run -p 8080:8080 kestra-test server local

# Access the application
open http://localhost:8080
```

This should resolve the deployment issues and get Kestra running successfully!