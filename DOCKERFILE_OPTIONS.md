# Dockerfile Options Guide

This guide explains the different Dockerfile options available for deploying Kestra.

## 🚀 Quick Start (Recommended)

### Option 1: Simple Working Version
```bash
# Use the main Dockerfile (simplest)
docker build -f Dockerfile -t kestra-app .
docker run -p 8080:8080 kestra-app
```

**File**: `Dockerfile`
**Description**: Simplest working version using official Kestra image
**Best for**: Quick deployment, testing, most platforms

## 🔧 Alternative Options

### Option 2: With Environment Variables
```bash
# Use simple environment variables
docker build -f Dockerfile.simple-env -t kestra-app .
docker run -p 8080:8080 kestra-app
```

**File**: `Dockerfile.simple-env`
**Description**: Uses environment variables for configuration
**Best for**: When you need to customize settings

### Option 3: With Configuration File
```bash
# Use configuration file approach
docker build -f Dockerfile.config-file -t kestra-app .
docker run -p 8080:8080 kestra-app
```

**File**: `Dockerfile.config-file`
**Description**: Uses YAML configuration file
**Best for**: Complex configurations

### Option 4: Clean YAML Approach
```bash
# Use clean YAML configuration
docker build -f Dockerfile.clean -t kestra-app .
docker run -p 8080:8080 kestra-app
```

**File**: `Dockerfile.clean`
**Description**: Creates YAML config file in container
**Best for**: When you need YAML configuration

## 📋 Comparison Table

| Dockerfile | Complexity | Configuration | Use Case | Reliability |
|------------|------------|---------------|----------|-------------|
| `Dockerfile` | ⭐ | Default | Quick start | ⭐⭐⭐⭐⭐ |
| `Dockerfile.simple-env` | ⭐⭐ | Environment vars | Custom settings | ⭐⭐⭐⭐ |
| `Dockerfile.config-file` | ⭐⭐⭐ | YAML file | Complex config | ⭐⭐⭐ |
| `Dockerfile.clean` | ⭐⭐ | YAML in container | YAML config | ⭐⭐⭐ |

## 🎯 Platform-Specific Recommendations

### Render.com
```bash
# Use the simplest approach
docker build -f Dockerfile -t kestra-app .
```

### Local Development
```bash
# Start with simple, add config if needed
docker build -f Dockerfile -t kestra-app .
docker run -p 8080:8080 kestra-app
```

### Production with Custom Config
```bash
# Use environment variables approach
docker build -f Dockerfile.simple-env -t kestra-app .
docker run -p 8080:8080 kestra-app
```

## 🔍 Troubleshooting

### If Simple Version Doesn't Work
1. **Check internet connectivity** - needs to pull official image
2. **Try with environment variables**:
   ```bash
   docker build -f Dockerfile.simple-env -t kestra-app .
   ```

### If You Need Custom Configuration
1. **Use environment variables**:
   ```bash
   docker build -f Dockerfile.simple-env -t kestra-app .
   ```

2. **Or use configuration file**:
   ```bash
   docker build -f Dockerfile.config-file -t kestra-app .
   ```

### If You Need CORS for Frontend
The official Kestra image should have CORS enabled by default, but if you need custom CORS settings:

```bash
# Use environment variables with CORS
docker build -f Dockerfile.simple-env -t kestra-app .
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

## 🎉 Success!

With any of these approaches, you should have:
- ✅ Fast builds (~30 seconds)
- ✅ Reliable deployments
- ✅ All Kestra features
- ✅ Ready for production

**Start with the simple version (`Dockerfile`) and add complexity only if needed!**