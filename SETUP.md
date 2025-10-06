# Kestra Setup Guide

This guide will help you set up and run the Kestra application.

## Prerequisites

- Java 21+
- Node.js 22+ and npm
- Gradle (wrapper included)

## Quick Setup

### 1. Backend Configuration

Create the file `cli/src/main/resources/application-override.yml` with the following content:

```yaml
micronaut:
  server:
    cors:
      enabled: true
      configurations:
        all:
          allowedOrigins:
            - http://localhost:5173
```

### 2. Frontend Configuration

Create the file `ui/.env.development.local` with the following content:

```
VITE_API_BASE_URL=http://localhost:8080
```

### 3. Build the Application

```bash
./gradlew build -x test
```

### 4. Run the Application

#### Local Mode (H2 Database)
```bash
./gradlew runLocal
```

#### Standalone Mode (PostgreSQL)
```bash
./gradlew runStandalone
```

### 5. Access the Application

- Backend API: http://localhost:8080
- Frontend UI: http://localhost:5173 (if running frontend separately)

## Development

### Backend Development
- The backend runs on port 8080 by default
- CORS is configured to allow requests from the frontend development server

### Frontend Development
- Navigate to `ui/` directory
- Run `npm run dev` for development server (port 5173)
- Run `npm run build` for production build

## Notes

- The application uses H2 database in local mode for easy setup
- For production, configure PostgreSQL as described in the main documentation
- Configuration files are gitignored for security reasons