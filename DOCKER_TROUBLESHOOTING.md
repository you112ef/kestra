# Docker Build Troubleshooting Guide

This guide helps resolve common Docker build issues with Kestra.

## Common Issues and Solutions

### 1. Gradle Build Fails in Docker

**Error**: `process "/bin/sh -c ./gradlew build -x test" did not complete successfully: exit code: 1`

**Causes**:
- Insufficient memory in Docker container
- Gradle daemon issues
- Missing dependencies
- Network connectivity issues

**Solutions**:

#### Option A: Use Pre-built Approach (Recommended)
```bash
# Build locally first
./build-for-deploy.sh

# Use pre-built Dockerfile
docker build -f Dockerfile.prebuilt -t kestra-app .
```

#### Option B: Use Robust Dockerfile
```bash
# Use the robust Dockerfile with better error handling
docker build -f Dockerfile.robust -t kestra-app .
```

#### Option C: Increase Docker Resources
```bash
# Increase memory and CPU for Docker
docker build --memory=4g --cpus=2 -t kestra-app .
```

### 2. Memory Issues

**Symptoms**: Build fails with out-of-memory errors

**Solutions**:
```bash
# Set Gradle options for more memory
export GRADLE_OPTS="-Xmx4g -Dorg.gradle.daemon=false"

# Or use the robust Dockerfile which handles this automatically
docker build -f Dockerfile.robust -t kestra-app .
```

### 3. Network Issues

**Symptoms**: Build fails when downloading dependencies

**Solutions**:
```bash
# Use Docker with network access
docker build --network=host -t kestra-app .

# Or configure Gradle to use proxy if needed
docker build --build-arg GRADLE_OPTS="-Dhttp.proxyHost=proxy -Dhttp.proxyPort=8080" -t kestra-app .
```

### 4. Permission Issues

**Symptoms**: Permission denied errors

**Solutions**:
```bash
# Ensure proper permissions
chmod +x gradlew
chmod +x build/scriptsShadow/kestra

# Use the build script which handles permissions
./build-for-deploy.sh
```

## Build Strategies

### Strategy 1: Pre-built (Fastest)
```bash
# 1. Build locally
./build-for-deploy.sh

# 2. Build Docker image
docker build -f Dockerfile.prebuilt -t kestra-app .

# 3. Run
docker run -p 8080:8080 kestra-app server local
```

### Strategy 2: Multi-stage Build (CI/CD)
```bash
# Use the main Dockerfile (now improved)
docker build -t kestra-app .

# Or use the robust version
docker build -f Dockerfile.robust -t kestra-app .
```

### Strategy 3: Development Build
```bash
# Use the simple Dockerfile for development
docker build -f Dockerfile.simple -t kestra-app .
```

## Platform-Specific Solutions

### Render.com
Use the pre-built approach:
1. Build locally or in CI
2. Use `Dockerfile.prebuilt`
3. Ensure build artifacts are included in deployment

### GitHub Actions
```yaml
- name: Build Kestra
  run: ./build-for-deploy.sh

- name: Build Docker
  run: docker build -f Dockerfile.prebuilt -t kestra-app .
```

### Local Development
```bash
# Quick build
./build-docker.sh

# Or manual
./build-for-deploy.sh
docker build -f Dockerfile.prebuilt -t kestra-app .
```

## Debugging Commands

### Check Build Artifacts
```bash
# Verify local build
ls -la build/scriptsShadow/kestra
ls -la build/libs/

# Test executable
./build/scriptsShadow/kestra --help
```

### Debug Docker Build
```bash
# Build with verbose output
docker build --progress=plain -t kestra-app .

# Debug specific stage
docker build --target builder -t kestra-debug .
docker run -it kestra-debug /bin/bash
```

### Check Container Contents
```bash
# Run container and inspect
docker run -it kestra-app /bin/bash
ls -la /app/
/app/kestra --help
```

## Environment Variables

### Gradle Configuration
```bash
# Memory settings
GRADLE_OPTS="-Xmx4g -Dorg.gradle.daemon=false"

# Parallel builds
GRADLE_OPTS="-Dorg.gradle.parallel=true -Dorg.gradle.workers.max=2"

# Debug mode
GRADLE_OPTS="--stacktrace --info"
```

### Docker Configuration
```bash
# Memory limits
docker build --memory=4g -t kestra-app .

# CPU limits
docker build --cpus=2 -t kestra-app .

# Network access
docker build --network=host -t kestra-app .
```

## Quick Fixes

### If Build Fails Immediately
1. Check Docker is running
2. Ensure you have enough disk space
3. Try: `docker system prune` to clean up

### If Gradle Fails
1. Use pre-built approach: `Dockerfile.prebuilt`
2. Increase memory: `--memory=4g`
3. Disable parallel builds: `GRADLE_OPTS="-Dorg.gradle.parallel=false"`

### If Executable Not Found
1. Verify build completed: `ls -la build/scriptsShadow/`
2. Check permissions: `chmod +x build/scriptsShadow/kestra`
3. Use the build script: `./build-for-deploy.sh`

## Success Verification

After successful build, verify with:
```bash
# Check image
docker images | grep kestra

# Test run
docker run --rm kestra-app --help

# Full test
docker run -p 8080:8080 kestra-app server local
```

Then access: http://localhost:8080