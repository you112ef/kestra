#!/bin/bash

# سكريبت اختبار جميع Dockerfiles
echo "==========================================="
echo "اختبار جميع Dockerfiles للتأكد من صحة الصيغة"
echo "==========================================="

# قائمة Dockerfiles للاختبار
dockerfiles=(
    "Dockerfile"
    "Dockerfile.simple-working"
    "Dockerfile.minimal-fix"
    "Dockerfile.alternative"
)

# اختبار كل Dockerfile
for dockerfile in "${dockerfiles[@]}"; do
    echo ""
    echo "اختبار: $dockerfile"
    echo "----------------------------------------"
    
    if [ -f "$dockerfile" ]; then
        # اختبار صيغة Dockerfile
        if docker build --dry-run -f "$dockerfile" . > /dev/null 2>&1; then
            echo "✅ $dockerfile - صيغة صحيحة"
        else
            echo "❌ $dockerfile - خطأ في الصيغة"
            echo "تفاصيل الخطأ:"
            docker build --dry-run -f "$dockerfile" . 2>&1 | head -10
        fi
    else
        echo "⚠️  $dockerfile - الملف غير موجود"
    fi
done

echo ""
echo "==========================================="
echo "انتهى الاختبار"
echo "==========================================="