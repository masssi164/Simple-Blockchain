# ------------------------------------------------
# 1) Build-Stage: TypeScript/Vite-Frontend
# ------------------------------------------------
FROM node:20-alpine AS build

# VITE-Args aus docker-compose
ARG VITE_NODE_URL
ARG VITE_NODE_LIBP2P
ARG VITE_NODE_JWT_SECRET
ARG VITE_NODE_GRPC
ENV VITE_NODE_URL=${VITE_NODE_URL}
ENV VITE_NODE_LIBP2P=${VITE_NODE_LIBP2P}
ENV VITE_NODE_JWT_SECRET=${VITE_NODE_JWT_SECRET}
ENV VITE_NODE_GRPC=${VITE_NODE_GRPC}

WORKDIR /app

# 1. Paket- und Config-Dateien kopieren (inkl. index.html)
COPY package.json package-lock.json tsconfig.json vite.config.ts index.html ./

# 2. Dependencies installieren (inkl. devDependencies)
RUN npm ci

# 3. Quellcode und statische Assets kopieren
COPY src    ./src
COPY public ./public

# 4. Projekt bauen
RUN npm run build


# ------------------------------------------------
# 2) Runtime-Stage: Statische Dateien mit Nginx
# ------------------------------------------------
FROM nginx:1.27-alpine

# Eigene nginx-Konfiguration übernehmen
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Zielverzeichnis für die statischen Dateien
WORKDIR /usr/share/nginx/html

# Nur das gebaute Ergebnis übernehmen
COPY --from=build /app/dist .

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
