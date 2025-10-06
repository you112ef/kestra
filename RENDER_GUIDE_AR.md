# دليل النشر على Render.com - باللغة العربية

## 🚀 النشر السريع

### الخطوة 1: إعداد المشروع
```bash
# استخدم Dockerfile البسيط
docker build -f Dockerfile.simple-render -t kestra-app .
```

### الخطوة 2: النشر على Render.com
1. اذهب إلى [Render.com](https://render.com)
2. اضغط على "New +" ثم "Web Service"
3. اختر "Build and deploy from a Git repository"
4. اربط مستودع GitHub الخاص بك
5. استخدم الإعدادات التالية:

## ⚙️ إعدادات Render.com

### إعدادات البناء (Build Settings)
- **Dockerfile Path**: `Dockerfile.simple-render`
- **Build Command**: (اتركه فارغاً)
- **Start Command**: (اتركه فارغاً)

### متغيرات البيئة (Environment Variables)
```bash
# إعدادات الخادم
KESTRA_SERVER_PORT=8080
KESTRA_SERVER_HOST=0.0.0.0

# إعدادات قاعدة البيانات
KESTRA_REPOSITORY_TYPE=h2

# إعدادات التخزين
KESTRA_STORAGE_TYPE=local
KESTRA_STORAGE_LOCAL_BASEPATH=/app/storage

# إعدادات CORS
MICRONAUT_SERVER_CORS_ENABLED=true
MICRONAUT_SERVER_CORS_CONFIGURATIONS_ALL_ALLOWEDORIGINS=https://kestra.onrender.com,http://localhost:5173

# إعدادات إضافية
KESTRA_ANONYMOUS_USAGE_REPORT_ENABLED=false
JAVA_OPTS=-Xmx512m -Xms256m
```

## 🔧 حل المشاكل الشائعة

### مشكلة: "No open ports detected"
**الحل**: تأكد من استخدام `Dockerfile.simple-render`

### مشكلة: التطبيق لا يبدأ
**الحل**: تحقق من السجلات (Logs) في Render.com

### مشكلة: CORS errors
**الحل**: تأكد من إعداد متغيرات CORS بشكل صحيح

## 📋 خطوات النشر التفصيلية

### 1. إعداد المستودع
```bash
# تأكد من أن جميع الملفات محفوظة
git add .
git commit -m "إعداد النشر على Render.com"
git push origin develop
```

### 2. إنشاء خدمة جديدة على Render.com
1. سجل الدخول إلى [Render.com](https://render.com)
2. اضغط على "New +"
3. اختر "Web Service"
4. اختر "Build and deploy from a Git repository"
5. اربط مستودع GitHub

### 3. إعداد الخدمة
- **Name**: kestra-production
- **Region**: Oregon (US West)
- **Branch**: develop
- **Dockerfile Path**: Dockerfile.simple-render
- **Plan**: Starter (Free)

### 4. إضافة متغيرات البيئة
أضف جميع متغيرات البيئة المذكورة أعلاه

### 5. النشر
اضغط على "Create Web Service" وانتظر حتى يكتمل النشر

## ✅ التحقق من النجاح

### 1. تحقق من السجلات
- اذهب إلى "Logs" في Render.com
- تأكد من ظهور رسالة "بدء تشغيل Kestra"

### 2. تحقق من الوصول
- اذهب إلى URL المقدم من Render.com
- يجب أن ترى واجهة Kestra

### 3. تحقق من API
```bash
# اختبر API
curl https://your-app-name.onrender.com/api/v1/health
```

## 🎯 نصائح مهمة

### 1. استخدم Dockerfile البسيط
```bash
# استخدم هذا الملف
Dockerfile.simple-render
```

### 2. تأكد من إعداد المنفذ
- Render.com يحدد المنفذ تلقائياً
- استخدم `$PORT` environment variable

### 3. تحقق من السجلات
- راقب السجلات أثناء النشر
- ابحث عن أخطاء في البداية

### 4. اختبر محلياً أولاً
```bash
# اختبر محلياً قبل النشر
docker build -f Dockerfile.simple-render -t kestra-test .
docker run -p 8080:8080 kestra-test
```

## 🚨 استكشاف الأخطاء

### إذا فشل النشر:
1. تحقق من السجلات
2. تأكد من صحة Dockerfile
3. تحقق من متغيرات البيئة
4. جرب Dockerfile مختلف

### إذا لم يعمل التطبيق:
1. تحقق من إعدادات المنفذ
2. تأكد من إعدادات CORS
3. تحقق من إعدادات قاعدة البيانات

## 📞 الدعم

إذا واجهت مشاكل:
1. تحقق من السجلات في Render.com
2. راجع هذا الدليل
3. تأكد من اتباع الخطوات بدقة

## 🎉 النجاح!

بعد النشر الناجح ستحصل على:
- ✅ تطبيق Kestra يعمل على Render.com
- ✅ واجهة ويب كاملة
- ✅ API يعمل بشكل صحيح
- ✅ قاعدة بيانات H2 محلية
- ✅ إعدادات CORS صحيحة