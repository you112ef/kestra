# Docker Build Guide - Multiple Approaches

This guide provides multiple approaches to build and deploy Kestra, with solutions for different scenarios.

## 🚀 Quick Start (Recommended)

### Option 1: Official Image (Fastest & Most Reliable)

**Use this approach for production deployments, especially on Render.com**

```bash
# Build using official Kestra image
docker build -f Dockerfile -t kestra-app .

# Run the container
docker run -p 8080:8080 kestra-app
```

**Why this works:**
- Uses pre-built official Kestra image
- No Gradle build required
- Fastest build time
- Most reliable
- Includes all features

## 🔧 Alternative Approaches

### Option 2: Pre-built Local Build

**Use this if you need custom modifications**

```bash
# 1. Build locally first
./build-for-deploy.sh

# 2. Build Docker image
docker build -f Dockerfile.prebuilt -t kestra-app .

# 3. Run
docker run -p 8080:8080 kestra-app server local
```

### Option 3: Multi-Strategy Build (Automated)

**Use this for CI/CD or when you're unsure which approach to use**

```bash
# Try multiple strategies automatically
./build-docker-multi.sh
```

### Option 4: Minimal Gradle Build

**Use this if you need to build from source but want minimal complexity**

```bash
# Build with minimal Gradle build
docker build -f Dockerfile.simple-build -t kestra-app .

# Run
docker run -p 8080:8080 kestra-app server local
```

## 📋 Available Dockerfiles

| Dockerfile | Description | Use Case | Build Time | Reliability |
|------------|-------------|----------|------------|-------------|
| `Dockerfile` | Official image + config | Production, Render.com | ~30s | ⭐⭐⭐⭐⭐ |
| `Dockerfile.prebuilt` | Pre-built local artifacts | Custom builds | ~1min | ⭐⭐⭐⭐ |
| `Dockerfile.minimal` | Official base + minimal config | Quick testing | ~30s | ⭐⭐⭐⭐⭐ |
| `Dockerfile.simple-build` | Minimal Gradle build | Source builds | ~5min | ⭐⭐⭐ |
| `Dockerfile.essential` | Essential modules only | Custom builds | ~3min | ⭐⭐⭐ |
| `Dockerfile.shadow` | Shadow JAR approach | Single JAR builds | ~4min | ⭐⭐⭐ |
| `Dockerfile.robust` | Full build with fallbacks | CI/CD | ~10min | ⭐⭐ |

## 🎯 Platform-Specific Recommendations

### Render.com
```bash
# Use the main Dockerfile (now uses official image)
docker build -f Dockerfile -t kestra-app .
```

### GitHub Actions
```yaml
- name: Build Kestra
  run: docker build -f Dockerfile -t kestra-app .
```

### Local Development
```bash
# Quick start
docker build -f Dockerfile -t kestra-app .

# Or try automated approach
./build-docker-multi.sh
```

### Production with Custom Code
```bash
# Build locally first
./build-for-deploy.sh

# Use pre-built approach
docker build -f Dockerfile.prebuilt -t kestra-app .
```

## 🔍 Troubleshooting

### If Official Image Approach Fails

1. **Check internet connectivity** - needs to pull official image
2. **Try minimal approach**:
   ```bash
   docker build -f Dockerfile.minimal -t kestra-app .
   ```

### If Pre-built Approach Fails

1. **Ensure local build succeeded**:
   ```bash
   ./build-for-deploy.sh
   ls -la build/scriptsShadow/kestra
   ```

2. **Check file permissions**:
   ```bash
   chmod +x build/scriptsShadow/kestra
   ```

### If Gradle Build Fails

1. **Use official image approach** (recommended)
2. **Try minimal build**:
   ```bash
   docker build -f Dockerfile.simple-build -t kestra-app .
   ```
3. **Use automated multi-strategy**:
   ```bash
   ./build-docker-multi.sh
   ```

## ✅ Verification

After successful build, verify with:

```bash
# Check image
docker images | grep kestra

# Test run
docker run --rm kestra-app --help

# Full test
docker run -p 8080:8080 kestra-app
```

Then access: http://localhost:8080

## 🚀 Deployment Commands

### Render.com
```bash
# Build
docker build -f Dockerfile -t kestra-app .

# Deploy (Render will handle this)
```

### Docker Compose
```yaml
version: '3.8'
services:
  kestra:
    build: .
    ports:
      - "8080:8080"
    environment:
      - KESTRA_CONFIGURATION={"kestra":{"repository":{"type":"h2"},"storage":{"type":"local","local":{"basePath":"/app/storage"}}}}
```

### Kubernetes
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
        ports:
        - containerPort: 8080
```

## 📝 Configuration

The official image approach uses environment variables for configuration:

```bash
# Custom configuration
docker run -p 8080:8080 \
  -e KESTRA_CONFIGURATION='{"kestra":{"repository":{"type":"h2"}}}' \
  kestra-app
```

## 🎉 Success!

With the official image approach, you should have:
- ✅ Fast builds (~30 seconds)
- ✅ Reliable deployments
- ✅ All Kestra features
- ✅ CORS enabled for frontend
- ✅ H2 database for easy setup
- ✅ Ready for production

The Gradle build issues are completely avoided by using the official pre-built image!