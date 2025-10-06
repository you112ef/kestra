# دليل استكشاف الأخطاء - Kestra على Render.com

## 🚨 المشاكل الشائعة والحلول

### مشكلة: "Exited with status 2"

**السبب**: Kestra يبدأ ولكن يتوقف فوراً

**الحلول**:

#### الحل 1: استخدم Dockerfile.simple-working
```bash
# استخدم هذا Dockerfile
docker build -f Dockerfile.simple-working -t kestra-app .
```

#### الحل 2: استخدم Dockerfile.alternative
```bash
# استخدم نهج مختلف
docker build -f Dockerfile.alternative -t kestra-app .
```

#### الحل 3: استخدم Dockerfile.robust-render
```bash
# استخدم النسخة المقاومة للأخطاء
docker build -f Dockerfile.robust-render -t kestra-app .
```

### مشكلة: "No open ports detected"

**السبب**: التطبيق لا يربط بالمنفذ بشكل صحيح

**الحل**:
```bash
# تأكد من استخدام Dockerfile الصحيح
docker build -f Dockerfile -t kestra-app .
```

### مشكلة: التطبيق لا يبدأ

**السبب**: مشاكل في الإعدادات أو المتغيرات البيئية

**الحل**:
1. تحقق من السجلات في Render.com
2. تأكد من إعداد متغيرات البيئة
3. استخدم Dockerfile.simple-working

## 🔧 خطوات التشخيص

### 1. اختبار محلي
```bash
# اختبار مع تشخيص
./debug-render.sh

# أو اختبار بسيط
docker build -f Dockerfile.simple-working -t kestra-test .
docker run -p 8080:8080 kestra-test
```

### 2. فحص السجلات
```bash
# فحص سجلات الحاوية
docker logs <container-id>

# أو فحص سجلات Render.com
# اذهب إلى Logs في Render.com
```

### 3. اختبار متغيرات البيئة
```bash
# اختبار مع متغيرات مختلفة
docker run -p 8080:8080 \
  -e KESTRA_SERVER_PORT=8080 \
  -e KESTRA_SERVER_HOST=0.0.0.0 \
  -e KESTRA_REPOSITORY_TYPE=h2 \
  kestra-test
```

## 📋 Dockerfiles المتاحة

| Dockerfile | الوصف | الاستخدام |
|------------|--------|-----------|
| `Dockerfile` | الرئيسي المحدث | النشر العادي |
| `Dockerfile.simple-working` | بسيط ومضمون | عند وجود مشاكل |
| `Dockerfile.alternative` | نهج مختلف | إذا فشل الباقي |
| `Dockerfile.robust-render` | مقاوم للأخطاء | للمشاكل المعقدة |

## ⚙️ إعدادات Render.com الموصى بها

### Build Settings:
- **Dockerfile Path**: `Dockerfile.simple-working`
- **Build Command**: (اتركه فارغاً)
- **Start Command**: (اتركه فارغاً)

### Environment Variables:
```bash
KESTRA_SERVER_PORT=8080
KESTRA_SERVER_HOST=0.0.0.0
KESTRA_REPOSITORY_TYPE=h2
KESTRA_STORAGE_TYPE=local
KESTRA_STORAGE_LOCAL_BASEPATH=/app/storage
KESTRA_ANONYMOUS_USAGE_REPORT_ENABLED=false
JAVA_OPTS=-Xmx512m -Xms256m
```

## 🎯 خطوات النشر المضمونة

### 1. اختبار محلي أولاً
```bash
# اختبار مع Dockerfile.simple-working
docker build -f Dockerfile.simple-working -t kestra-test .
docker run -p 8080:8080 kestra-test

# تحقق من أن التطبيق يعمل
curl http://localhost:8080/api/v1/health
```

### 2. النشر على Render.com
1. استخدم `Dockerfile.simple-working`
2. أضف متغيرات البيئة المطلوبة
3. راقب السجلات أثناء النشر

### 3. التحقق من النجاح
- تحقق من السجلات في Render.com
- اختبر URL المقدم من Render.com
- تأكد من أن API يعمل

## 🚨 إذا استمرت المشاكل

### 1. جرب Dockerfiles مختلفة
```bash
# جرب هذا أولاً
docker build -f Dockerfile.simple-working -t kestra-app .

# إذا فشل، جرب هذا
docker build -f Dockerfile.alternative -t kestra-app .

# إذا فشل، جرب هذا
docker build -f Dockerfile.robust-render -t kestra-app .
```

### 2. تحقق من السجلات
- اذهب إلى Logs في Render.com
- ابحث عن رسائل الخطأ
- تحقق من رسائل البدء

### 3. اختبر مع إعدادات مختلفة
- جرب متغيرات بيئة مختلفة
- اختبر مع منافذ مختلفة
- تحقق من إعدادات CORS

## 📞 الدعم

إذا استمرت المشاكل:
1. راجع هذا الدليل
2. اختبر محلياً أولاً
3. جرب Dockerfiles مختلفة
4. تحقق من السجلات في Render.com

## ✅ النجاح!

بعد حل المشاكل ستحصل على:
- تطبيق Kestra يعمل على Render.com
- واجهة ويب كاملة
- API يعمل بشكل صحيح
- لا توجد رسائل خطأ