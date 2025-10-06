# Use official Kestra image with proper port binding
FROM kestra/kestra:latest

# Set environment variables for proper startup
ENV KESTRA_SERVER_PORT=8080
ENV KESTRA_SERVER_HOST=0.0.0.0

# Expose the port
EXPOSE 8080

# Start Kestra with explicit port and host binding
CMD ["server", "local", "--server.port=8080", "--server.host=0.0.0.0"]
