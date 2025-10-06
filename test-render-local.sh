#!/bin/bash

# سكريبت اختبار النشر المحلي لـ Render.com
echo "==========================================="
echo "اختبار النشر المحلي لـ Kestra على Render.com"
echo "==========================================="

# بناء الصورة
echo "بناء صورة Docker..."
docker build -f Dockerfile.simple-render -t kestra-render-test .

if [ $? -eq 0 ]; then
    echo "✅ تم بناء الصورة بنجاح"
else
    echo "❌ فشل في بناء الصورة"
    exit 1
fi

# تشغيل الحاوية
echo "تشغيل الحاوية..."
docker run -d \
  --name kestra-test \
  -p 8080:8080 \
  -e KESTRA_SERVER_PORT=8080 \
  -e KESTRA_SERVER_HOST=0.0.0.0 \
  -e KESTRA_REPOSITORY_TYPE=h2 \
  -e KESTRA_STORAGE_TYPE=local \
  -e KESTRA_STORAGE_LOCAL_BASEPATH=/app/storage \
  -e MICRONAUT_SERVER_CORS_ENABLED=true \
  -e MICRONAUT_SERVER_CORS_CONFIGURATIONS_ALL_ALLOWEDORIGINS=http://localhost:8080,http://localhost:5173 \
  -e KESTRA_ANONYMOUS_USAGE_REPORT_ENABLED=false \
  -e JAVA_OPTS=-Xmx512m -Xms256m \
  kestra-render-test

if [ $? -eq 0 ]; then
    echo "✅ تم تشغيل الحاوية بنجاح"
else
    echo "❌ فشل في تشغيل الحاوية"
    exit 1
fi

# انتظار بدء التطبيق
echo "انتظار بدء التطبيق..."
sleep 10

# اختبار التطبيق
echo "اختبار التطبيق..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/v1/health > /dev/null 2>&1; then
        echo "✅ التطبيق يعمل بنجاح!"
        echo "🌐 يمكنك الوصول إليه على: http://localhost:8080"
        break
    else
        echo "⏳ انتظار بدء التطبيق... ($i/30)"
        sleep 2
    fi
done

# عرض السجلات
echo "==========================================="
echo "عرض آخر 20 سطر من السجلات:"
echo "==========================================="
docker logs --tail 20 kestra-test

echo "==========================================="
echo "لإيقاف التطبيق، استخدم:"
echo "docker stop kestra-test"
echo "docker rm kestra-test"
echo "==========================================="