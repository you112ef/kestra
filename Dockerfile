# Dockerfile محسن ومقاوم للأخطاء لـ Render.com
FROM kestra/kestra:latest

# إنشاء مجلدات مطلوبة
RUN mkdir -p /app/storage /tmp/kestra-wd/tmp

# إعداد متغيرات البيئة الأساسية
ENV KESTRA_SERVER_PORT=8080
ENV KESTRA_SERVER_HOST=0.0.0.0
ENV KESTRA_REPOSITORY_TYPE=h2
ENV KESTRA_STORAGE_TYPE=local
ENV KESTRA_STORAGE_LOCAL_BASEPATH=/app/storage
ENV KESTRA_ANONYMOUS_USAGE_REPORT_ENABLED=false
ENV KESTRA_TASKS_TMPDIR_PATH=/tmp/kestra-wd/tmp
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# فتح المنفذ
EXPOSE 8080

# بدء Kestra مع الأمر الصحيح
CMD ["/app/kestra", "server", "standalone"]
