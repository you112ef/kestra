# إصلاح مشكلة أمر Kestra

## 🚨 المشكلة: "Exited with status 2"

**السبب**: Kestra لا يتعرف على الأمر `server local`

**الحلول المتاحة:**

## ✅ الحلول السريعة

### الحل 1: استخدم Dockerfile.correct-command (الأفضل)
```bash
# استخدم هذا Dockerfile
docker build -f Dockerfile.correct-command -t kestra-app .
```

**المميزات:**
- ✅ يستخدم `server standalone` (الأمر الصحيح)
- ✅ بسيط ومضمون
- ✅ يعمل على Render.com

### الحل 2: استخدم Dockerfile.no-args
```bash
# استخدم هذا Dockerfile
docker build -f Dockerfile.no-args -t kestra-app .
```

**المميزات:**
- ✅ بدون وسائط - يستخدم الإعدادات الافتراضية
- ✅ أبسط نهج
- ✅ مضمون للعمل

### الحل 3: استخدم Dockerfile.script-approach
```bash
# استخدم هذا Dockerfile
docker build -f Dockerfile.script-approach -t kestra-app .
```

**المميزات:**
- ✅ يجرب أوامر مختلفة تلقائياً
- ✅ مقاوم للأخطاء
- ✅ تشخيص تلقائي

## 🔧 إعدادات Render.com

### Build Settings:
- **Dockerfile Path**: `Dockerfile.correct-command`
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
```

## 🧪 اختبار محلي

```bash
# اختبار سريع
docker build -f Dockerfile.correct-command -t kestra-test .
docker run -p 8080:8080 kestra-test

# أو اختبار الأوامر
./test-commands.sh
```

## 📋 Dockerfiles المتاحة

| Dockerfile | الوصف | الاستخدام |
|------------|--------|-----------|
| `Dockerfile.correct-command` | **الأفضل - يستخدم server standalone** | النشر المضمون |
| `Dockerfile.no-args` | بدون وسائط | النشر البسيط |
| `Dockerfile.script-approach` | يجرب أوامر مختلفة | للمشاكل المعقدة |

## 🎯 خطوات النشر المضمونة

### 1. اختبار محلي
```bash
docker build -f Dockerfile.correct-command -t kestra-test .
docker run -p 8080:8080 kestra-test
```

### 2. النشر على Render.com
1. استخدم `Dockerfile.correct-command`
2. أضف متغيرات البيئة
3. انتظر حتى يكتمل النشر

### 3. التحقق من النجاح
- تحقق من السجلات في Render.com
- اختبر URL المقدم من Render.com

## ✅ النتيجة المتوقعة

بعد استخدام `Dockerfile.correct-command`:
- ✅ لا توجد رسالة "Exited with status 2"
- ✅ Kestra يبدأ ويبقى يعمل
- ✅ التطبيق متاح على Render.com
- ✅ واجهة ويب كاملة

## 🚀 البدء الآن

```bash
# 1. بناء الصورة
docker build -f Dockerfile.correct-command -t kestra-app .

# 2. اختبار محلي
docker run -p 8080:8080 kestra-app

# 3. النشر على Render.com
# استخدم Dockerfile.correct-command
```

**المشكلة محلولة!** 🎉