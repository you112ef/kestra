# Render.com Deployment Guide

This guide specifically addresses Render.com deployment issues and provides solutions for port binding problems.

## 🚨 Common Issues

### Issue: "No open ports detected"
**Cause**: Kestra not binding to the correct port or host
**Solution**: Use proper port binding configuration

## 🚀 Quick Fix (Recommended)

### Option 1: Use Render-Optimized Dockerfile
```bash
# Use the Render-optimized Dockerfile
docker build -f Dockerfile.render -t kestra-app .
```

**File**: `Dockerfile.render`
**Features**:
- Handles Render.com's PORT environment variable
- Explicit port and host binding
- Proper startup script

### Option 2: Use Port-Fixed Dockerfile
```bash
# Use the port-fixed Dockerfile
docker build -f Dockerfile.port-fix -t kestra-app .
```

**File**: `Dockerfile.port-fix`
**Features**:
- Explicit port binding
- Simple configuration
- Works with most platforms

## 🔧 Render.com Specific Solutions

### Solution 1: Environment Variable Support
```bash
# Use Dockerfile that supports Render's PORT variable
docker build -f Dockerfile.render-env -t kestra-app .
```

**Features**:
- Uses `$PORT` environment variable from Render.com
- Automatically adapts to Render's port assignment
- Includes proper host binding

### Solution 2: Updated Main Dockerfile
The main `Dockerfile` has been updated with:
- Explicit port binding (`--server.port=8080`)
- Host binding (`--server.host=0.0.0.0`)
- Environment variables for configuration

## 📋 Dockerfile Comparison

| Dockerfile | Render.com Support | Port Handling | Complexity |
|------------|-------------------|---------------|------------|
| `Dockerfile` | ⭐⭐⭐ | Fixed port 8080 | ⭐⭐ |
| `Dockerfile.render` | ⭐⭐⭐⭐⭐ | Dynamic PORT support | ⭐⭐⭐ |
| `Dockerfile.port-fix` | ⭐⭐⭐⭐ | Fixed port 8080 | ⭐⭐ |
| `Dockerfile.render-env` | ⭐⭐⭐⭐⭐ | Dynamic PORT support | ⭐⭐⭐ |

## 🎯 Render.com Configuration

### Build Settings
- **Dockerfile**: `Dockerfile.render` (recommended)
- **Build Command**: (leave empty)
- **Start Command**: (leave empty)

### Environment Variables
```bash
# Optional: Override default settings
KESTRA_SERVER_PORT=8080
KESTRA_SERVER_HOST=0.0.0.0
KESTRA_REPOSITORY_TYPE=h2
KESTRA_STORAGE_TYPE=local
```

### Port Configuration
- **Port**: Render.com will automatically assign (handled by Dockerfile)
- **Host**: 0.0.0.0 (handled by Dockerfile)

## 🔍 Troubleshooting

### If "No open ports detected" persists:

1. **Check the Dockerfile**:
   ```bash
   # Ensure you're using the right Dockerfile
   docker build -f Dockerfile.render -t kestra-app .
   ```

2. **Test locally first**:
   ```bash
   # Test the build locally
   docker run -p 8080:8080 kestra-app
   # Check if port 8080 is accessible
   curl http://localhost:8080
   ```

3. **Check Render.com logs**:
   - Look for startup messages
   - Check for port binding errors
   - Verify Kestra is starting correctly

### If Kestra doesn't start:

1. **Check the startup command**:
   ```bash
   # The Dockerfile should include explicit port binding
   CMD ["server", "local", "--server.port=8080", "--server.host=0.0.0.0"]
   ```

2. **Verify environment variables**:
   ```bash
   # Check if KESTRA_SERVER_PORT and KESTRA_SERVER_HOST are set
   ```

## ✅ Verification Steps

### Local Testing
```bash
# 1. Build the image
docker build -f Dockerfile.render -t kestra-app .

# 2. Run locally
docker run -p 8080:8080 kestra-app

# 3. Test the port
curl http://localhost:8080
# Should return HTML or JSON response
```

### Render.com Testing
1. **Deploy using `Dockerfile.render`**
2. **Check the logs** for startup messages
3. **Access the URL** provided by Render.com
4. **Verify port binding** in the logs

## 🚀 Deployment Commands

### For Render.com
```bash
# Use the Render-optimized Dockerfile
docker build -f Dockerfile.render -t kestra-app .

# Deploy to Render.com
# (Render will handle the deployment)
```

### For Other Platforms
```bash
# Use the port-fixed Dockerfile
docker build -f Dockerfile.port-fix -t kestra-app .

# Run locally
docker run -p 8080:8080 kestra-app
```

## 🎉 Expected Results

After successful deployment:
- ✅ Port binding detected by Render.com
- ✅ Kestra server starts successfully
- ✅ Web interface accessible at Render.com URL
- ✅ API endpoints working
- ✅ No "No open ports detected" error

## 📝 Additional Notes

- **Render.com automatically assigns ports** - the Dockerfile handles this
- **Host must be 0.0.0.0** - not localhost or 127.0.0.1
- **Port binding is explicit** - not relying on defaults
- **Startup script ensures proper configuration**

The key is ensuring Kestra binds to `0.0.0.0:PORT` where PORT is either 8080 or Render.com's assigned port.