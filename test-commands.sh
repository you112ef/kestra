#!/bin/bash

# سكريبت اختبار أوامر Kestra المختلفة
echo "==========================================="
echo "اختبار أوامر Kestra المختلفة"
echo "==========================================="

# بناء الصورة
echo "بناء صورة Docker..."
docker build -f Dockerfile.correct-command -t kestra-test .

if [ $? -eq 0 ]; then
    echo "✅ تم بناء الصورة بنجاح"
else
    echo "❌ فشل في بناء الصورة"
    exit 1
fi

# اختبار الأوامر المختلفة
echo ""
echo "اختبار الأوامر المختلفة:"
echo "----------------------------------------"

echo "1. اختبار server standalone:"
docker run --rm kestra-test /app/kestra server standalone --help

echo ""
echo "2. اختبار server local:"
docker run --rm kestra-test /app/kestra server local --help

echo ""
echo "3. اختبار بدون server:"
docker run --rm kestra-test /app/kestra --help

echo ""
echo "4. اختبار بدء تشغيل فعلي:"
echo "بدء تشغيل Kestra لمدة 10 ثوان..."
timeout 10s docker run --rm -p 8080:8080 kestra-test /app/kestra server standalone || echo "انتهت المهلة الزمنية"

echo ""
echo "==========================================="
echo "انتهى الاختبار"
echo "==========================================="