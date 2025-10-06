#!/bin/bash

# Multi-strategy Docker build script
set -e

echo "Kestra Docker Build - Multi-Strategy Approach"
echo "=============================================="

# Function to try a Dockerfile
try_dockerfile() {
    local dockerfile=$1
    local tag=$2
    local description=$3
    
    echo ""
    echo "Trying: $description"
    echo "Dockerfile: $dockerfile"
    echo "Tag: $tag"
    echo "----------------------------------------"
    
    if docker build -f "$dockerfile" -t "$tag" .; then
        echo "✅ SUCCESS: $description"
        echo "Image built successfully: $tag"
        return 0
    else
        echo "❌ FAILED: $description"
        return 1
    fi
}

# Strategy 1: Pre-built (if artifacts exist)
if [ -f "build/scriptsShadow/kestra" ] && [ -d "build/libs" ]; then
    echo "Pre-built artifacts found, trying pre-built approach..."
    if try_dockerfile "Dockerfile.prebuilt" "kestra-prebuilt" "Pre-built approach"; then
        echo "🎉 Build successful with pre-built approach!"
        exit 0
    fi
fi

# Strategy 2: Minimal approach
echo "Trying minimal approach..."
if try_dockerfile "Dockerfile.minimal" "kestra-minimal" "Minimal approach (using official base)"; then
    echo "🎉 Build successful with minimal approach!"
    exit 0
fi

# Strategy 3: Essential modules only
echo "Trying essential modules approach..."
if try_dockerfile "Dockerfile.essential" "kestra-essential" "Essential modules only"; then
    echo "🎉 Build successful with essential approach!"
    exit 0
fi

# Strategy 4: Shadow JAR approach
echo "Trying shadow JAR approach..."
if try_dockerfile "Dockerfile.shadow" "kestra-shadow" "Shadow JAR approach"; then
    echo "🎉 Build successful with shadow JAR approach!"
    exit 0
fi

# Strategy 5: Build locally first, then use pre-built
echo "Building locally first, then using pre-built approach..."
if ./build-for-deploy.sh; then
    if try_dockerfile "Dockerfile.prebuilt" "kestra-local-prebuilt" "Local build + pre-built"; then
        echo "🎉 Build successful with local build + pre-built approach!"
        exit 0
    fi
fi

# Strategy 6: Robust approach (last resort)
echo "Trying robust approach as last resort..."
if try_dockerfile "Dockerfile.robust" "kestra-robust" "Robust approach"; then
    echo "🎉 Build successful with robust approach!"
    exit 0
fi

echo ""
echo "❌ All build strategies failed!"
echo "Please check the troubleshooting guide: DOCKER_TROUBLESHOOTING.md"
exit 1