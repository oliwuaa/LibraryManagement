# 📚 Library Management System

## 📖 Opis projektu

Library Management System to aplikacja umożliwiająca zarządzanie biblioteką, książkami, egzemplarzami oraz procesem wypożyczania i rezerwacji. Użytkownicy mogą przeglądać dostępne książki, rezerwować je oraz wypożyczać. Bibliotekarze mają możliwość zarządzania zasobami biblioteki.

---

## 🛠 Technologie

Projekt został zbudowany przy użyciu:

- **Java 21**
- **Spring Boot** 
- **Hibernate**
- **H2 / PostgreSQL**
- **Lombok** 
- **Swagger** 
- **Gradle** 

---

## 🏗 Instalacja i uruchomienie

### 1️⃣ Klonowanie repozytorium

```bash
git clone https://github.com/oliwuaa/LibraryManagement
cd LibraryManagement
```
### 2️⃣ Przełączanie na odpowiednią gałąź

Na gałęzi main projekt nie zawiera jeszcze konfiguracji Docker, więc jeśli chcesz pracować z Dockerem, musisz przełączyć się na odpowiednią gałąź - docker-setup-login.

Aby przejść na gałąź docker-setup-login, użyj polecenia:

```bash
git checkout docker-setup-login
```

### 3️⃣ Konfiguracja środowiska

Przed uruchomieniem aplikacji, musisz skonfigurować zmienne środowiskowe, które zawierają dane do logowania oraz klucze aplikacji:
Utwórz plik .env w katalogu głównym projektu i dodaj do niego następujące zmienne:

```bash
DB_USER=your_database_user
DB_PASSWORD=your_database_password
JWT_SECRET=your_super_secure_secret_key_for_jwt
```

### 4️⃣ Uruchomienie aplikacji z Dockerem

Jeśli chcesz uruchomić aplikację z wykorzystaniem Docker i Docker Compose, wykonaj następujące kroki:
Zbuduj i uruchom aplikację z Docker Compose:
W terminalu, w katalogu głównym projektu, uruchom polecenie:

```bash
docker-compose up --build 
```

- Aplikacja będzie dostępna pod adresem: [http://localhost:8080](http://localhost:8080)
- Wszystkie wysyłane maile będą rejestrowane w narzędziu MailHog, które jest dostępne pod adresem:[http://localhost:8025](http://localhost:8025)

---

## 🔑 Autoryzacja i role użytkowników

Aplikacja obsługuje uwierzytelnianie użytkowników oraz różne poziomy dostępu:

- **ADMIN** - zarządza użytkownikami, bibliotekami, książkami.
- **LIBRARIAN** - zarządza książkami i egzemplarzami.
- **USER** - może rezerwować i wypożyczać książki.

Do testowania zostały przygotowane przykładowe profile:
- **Admin**
    - email: admin@example.com
    - password: admin

- **Librarian**
    - email: librarian@example.com
    - password: librarian

- **User**
    - email: user@example.com
    - password: user


---

## 🔗 API Endpoints

### 📜 Dokumentacja API

Po uruchomieniu aplikacji, dokumentacja Swaggera dostępna jest pod adresem:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

#### 📌 Przykładowe endpointy

| Metoda | Endpoint            | Opis                         |
| ------ | ------------------- | ---------------------------- |
| GET    | `/books`            | Pobiera listę książek        |
| POST   | `/books`            | Dodaje nową książkę          |
| GET    | `/copies/available` | Pobiera dostępne egzemplarze |
| POST   | `/loans/borrow`     | Wypożycza egzemplarz         |
| POST   | `/reservations`     | Tworzy rezerwację            |

