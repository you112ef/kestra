#!/bin/bash

# سكريبت تشخيص مشاكل Render.com
echo "==========================================="
echo "تشخيص مشاكل Kestra على Render.com"
echo "==========================================="

# بناء الصورة
echo "بناء صورة Docker..."
docker build -f Dockerfile.simple-working -t kestra-debug .

if [ $? -eq 0 ]; then
    echo "✅ تم بناء الصورة بنجاح"
else
    echo "❌ فشل في بناء الصورة"
    exit 1
fi

# تشغيل الحاوية مع تشخيص
echo "تشغيل الحاوية مع تشخيص..."
docker run --rm -it \
  --name kestra-debug \
  -p 8080:8080 \
  -e KESTRA_SERVER_PORT=8080 \
  -e KESTRA_SERVER_HOST=0.0.0.0 \
  -e KESTRA_REPOSITORY_TYPE=h2 \
  -e KESTRA_STORAGE_TYPE=local \
  -e KESTRA_STORAGE_LOCAL_BASEPATH=/app/storage \
  -e KESTRA_ANONYMOUS_USAGE_REPORT_ENABLED=false \
  -e JAVA_OPTS=-Xmx512m -Xms256m \
  kestra-debug

echo "==========================================="
echo "انتهى التشخيص"
echo "==========================================="