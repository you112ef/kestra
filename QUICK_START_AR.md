# دليل البدء السريع - Kestra على Render.com

## 🚀 3 خطوات فقط!

### 1. بناء الصورة
```bash
docker build -f Dockerfile -t kestra-app .
```

### 2. اختبار محلي (اختياري)
```bash
docker run -p 8080:8080 kestra-app
```

### 3. النشر على Render.com
1. اذهب إلى [Render.com](https://render.com)
2. اضغط "New +" → "Web Service"
3. اختر مستودع GitHub
4. استخدم الإعدادات التالية:

## ⚙️ إعدادات Render.com

### Build Settings:
- **Dockerfile Path**: `Dockerfile`
- **Build Command**: (اتركه فارغاً)
- **Start Command**: (اتركه فارغاً)

### Environment Variables:
انسخ من ملف `.env.render` أو أضف يدوياً:

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

## ✅ تم!

بعد النشر ستحصل على:
- تطبيق Kestra يعمل على Render.com
- واجهة ويب كاملة
- API يعمل بشكل صحيح

## 🔧 إذا واجهت مشاكل:

1. **"No open ports detected"**: تأكد من استخدام `Dockerfile`
2. **التطبيق لا يبدأ**: تحقق من السجلات في Render.com
3. **CORS errors**: تأكد من إعداد متغيرات CORS

## 📞 الدعم:
- راجع `RENDER_GUIDE_AR.md` للدليل المفصل
- استخدم `test-render-local.sh` للاختبار المحلي