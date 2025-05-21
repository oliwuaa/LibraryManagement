# Krok 1: Build aplikacji
FROM node:18 AS build

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build  # Zbuduj aplikację Vite

# Krok 2: Uruchomienie Nginx do serwowania aplikacji
FROM nginx:alpine

# Kopiowanie zbudowanej aplikacji do folderu nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Kopiowanie pliku konfiguracyjnego Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 80
EXPOSE 80

# Uruchomienie Nginx
CMD ["nginx", "-g", "daemon off;"]
