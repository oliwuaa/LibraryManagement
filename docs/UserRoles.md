# Roles and Permissions – Library Application

---

## Rola: USER (Czytelnik)

### Katalog i rezerwacje
- Przeglądanie dostępnych książek (`GET /books`, `GET /books/{bookId}`, `GET /books/search`)
- Wyszukiwanie książek po tytule, autorze, ISBN, gatunku (`GET /books/search`)
- Przeglądanie dostępnych kopii książek (`GET /copies/available`)
- Rezerwacja książki (`POST /reservations`)
- Podgląd statusu własnych rezerwacji (`GET /reservations/user/{userId}`)
- Anulowanie własnych rezerwacji (`DELETE /reservations/{reservationId}`)

### Zarządzanie wypożyczeniami
- Podgląd historii własnych wypożyczeń (`GET /users/{userId}/loans`)
- Przedłużanie wypożyczenia (`POST /loans/{loanId}/extend`)
- Możliwość wypożyczenia (`POST /loans`)

### Zarządzanie kontem
- Rejestracja konta (`POST /auth/register`)
- Logowanie (`POST /auth/login`)
- Weryfikacja konta (`GET /auth/verify-email`)
- Edycja własnych danych (`PUT /users/{userId}`)

### Powiadomienia (funkcjonalność systemowa)
- Informacje o zbliżającym się terminie zwrotu

---

## Rola: LIBRARIAN (Bibliotekarz)

### Zarządzanie książkami
- Dodawanie książek (`POST /books`)
- Edycja danych książek (`PUT /books/{bookId}`)
- Usuwanie książek (`DELETE /books/{bookId}`)
- Przypisywanie autorów, ISBN, liczby stron, roku wydania

### Zarządzanie kopiami książek
- Dodawanie kopii książek (`POST /copies`)
- Edycja statusu kopii książki (`PUT /copies/{copyId}`)
- Usuwanie kopii książek (`DELETE /copies/{copyId}`)
- Przegląd wszystkich kopii (`GET /copies`)
- Sprawdzanie dostępności egzemplarzy (`GET /copies/available`)
- 
### Obsługa wypożyczeń i rezerwacji
- Wypożyczanie książek (`POST /loans`)
- Przedłużanie wypożyczeń (`POST /loans/{loanId}/extend`)
- Zwroty książek (`POST /loans/{loanId}/return`)
- Przegląd wypożyczeń wszystkich użytkowników (`GET /loans`)
- Przegląd rezerwacji (`GET /reservations`)

### Komunikacja z użytkownikami
- Powiadamianie o terminie zwrotu

---

##  Rola: ADMIN (Administrator systemu)

> Posiada wszystkie uprawnienia LIBRARIAN oraz dodatkowe funkcje administracyjne.

### Pełny przegląd danych i konfiguracji systemu
- Historia wypożyczeń wszystkich użytkowników (`GET /loans`)
- Przegląd użytkowników (`GET /users`, `GET /users/{id}`, `GET /users/search`)
- Przegląd bibliotek i danych o bibliotekarzach (`GET /libraries`, `GET /users/library/{libraryId}/librarians`)
- Konfiguracja aplikacji: zmienne środowiskowe `.env`, `application.yml`

### Zarządzanie kontami użytkowników
- Dodawanie użytkowników (`POST /users`)
- Edycja danych użytkowników (`PUT /users/{userId}`)
- Usuwanie kont (`DELETE /users/{userId}`)
- Zmiana roli (`POST /users/role/{role}`)

### Panel administracyjny i monitoring
- **Spring Boot Admin** – monitorowanie aplikacji (uptime, health, endpoints)
- **Spring Boot Actuator** – `/actuator/metrics`, `/actuator/health`
- **Logi systemowe:** logback (lokalnie), integracja z ELK (Elasticsearch, Logstash, Kibana)

### Testowanie i CI/CD
- Testy jednostkowe: **JUnit 5**
- Raporty testowe: **Surefire Reports**
- Automatyczne testowanie w CI (GitHub Actions / GitLab CI)

### Integracje
- Integracja z zewnętrznymi API (np. `/external/books`)
- Zarządzanie dostępem do kluczy API (w `.env`, `application.yml`)
- Monitorowanie działania integracji (np. poprzez dedykowane endpointy `/admin/integrations`)

###  Obsługa daty i czasu
- Implementacja przez `java.time.Clock`
