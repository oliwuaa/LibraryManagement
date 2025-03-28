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
### 2️⃣ Uruchomienie aplikacji

```bash
set SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```

Aplikacja będzie dostępna pod adresem: [http://localhost:8080](http://localhost:8080)

---

## 🔑 Autoryzacja i role użytkowników

Aplikacja obsługuje uwierzytelnianie użytkowników oraz różne poziomy dostępu:

- **ADMIN** - zarządza użytkownikami, bibliotekami, książkami.
- **LIBRARIAN** - zarządza książkami i egzemplarzami.
- **USER** - może rezerwować i wypożyczać książki.

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

