# Kestra - دليل النشر على Render.com

## 🚀 النشر السريع

### للبدء فوراً:
```bash
# 1. بناء الصورة
docker build -f Dockerfile.simple-render -t kestra-app .

# 2. تشغيل محلياً
docker run -p 8080:8080 kestra-app

# 3. أو استخدام Docker Compose
docker-compose -f docker-compose.render.yml up
```

## 📋 الملفات المطلوبة

### Dockerfiles المتاحة:
- `Dockerfile.simple-render` - **الأفضل لـ Render.com**
- `Dockerfile.render-production` - إعدادات إنتاج كاملة
- `Dockerfile.render` - مع دعم متغير PORT

### ملفات الإعدادات:
- `render.yaml` - إعدادات Render.com
- `application-production.yml` - إعدادات الإنتاج
- `docker-compose.render.yml` - للاختبار المحلي

## ⚙️ إعدادات Render.com

### 1. إنشاء خدمة جديدة
1. اذهب إلى [Render.com](https://render.com)
2. اضغط "New +" → "Web Service"
3. اختر مستودع GitHub

### 2. إعدادات البناء
- **Dockerfile Path**: `Dockerfile.simple-render`
- **Build Command**: (اتركه فارغاً)
- **Start Command**: (اتركه فارغاً)

### 3. متغيرات البيئة
```bash
KESTRA_SERVER_PORT=8080
KESTRA_SERVER_HOST=0.0.0.0
KESTRA_REPOSITORY_TYPE=h2
KESTRA_STORAGE_TYPE=local
KESTRA_STORAGE_LOCAL_BASEPATH=/app/storage
MICRONAUT_SERVER_CORS_ENABLED=true
MICRONAUT_SERVER_CORS_CONFIGURATIONS_ALL_ALLOWEDORIGINS=https://kestra.onrender.com,http://localhost:5173
KESTRA_ANONYMOUS_USAGE_REPORT_ENABLED=false
JAVA_OPTS=-Xmx512m -Xms256m
```

## 🔧 الاختبار المحلي

### اختبار سريع:
```bash
./test-render-local.sh
```

### اختبار يدوي:
```bash
# بناء وتشغيل
docker build -f Dockerfile.simple-render -t kestra-test .
docker run -p 8080:8080 kestra-test

# اختبار API
curl http://localhost:8080/api/v1/health
```

## 🚨 حل المشاكل

### مشكلة: "No open ports detected"
**الحل**: استخدم `Dockerfile.simple-render`

### مشكلة: التطبيق لا يبدأ
**الحل**: تحقق من السجلات في Render.com

### مشكلة: CORS errors
**الحل**: تأكد من إعداد متغيرات CORS

## 📚 الدلائل المتاحة

- `RENDER_GUIDE_AR.md` - دليل شامل باللغة العربية
- `DOCKERFILE_OPTIONS.md` - خيارات Dockerfile المختلفة
- `RENDER_DEPLOYMENT.md` - دليل النشر على Render.com

## ✅ التحقق من النجاح

بعد النشر الناجح:
1. ✅ التطبيق يعمل على Render.com
2. ✅ واجهة ويب متاحة
3. ✅ API يعمل بشكل صحيح
4. ✅ قاعدة بيانات H2 محلية
5. ✅ إعدادات CORS صحيحة

## 🎯 نصائح مهمة

1. **استخدم Dockerfile البسيط** - `Dockerfile.simple-render`
2. **اختبر محلياً أولاً** - استخدم `test-render-local.sh`
3. **راقب السجلات** - تحقق من أخطاء البداية
4. **تأكد من متغيرات البيئة** - خاصة CORS و PORT

## 📞 الدعم

إذا واجهت مشاكل:
1. تحقق من السجلات في Render.com
2. راجع `RENDER_GUIDE_AR.md`
3. اختبر محلياً باستخدام `test-render-local.sh`
4. تأكد من استخدام `Dockerfile.simple-render`

## 🎉 النجاح!

بعد اتباع هذا الدليل ستحصل على:
- تطبيق Kestra يعمل بشكل كامل على Render.com
- واجهة ويب كاملة مع جميع الميزات
- API يعمل بشكل صحيح
- إعدادات إنتاج محسنة
- دعم CORS للواجهة الأمامية