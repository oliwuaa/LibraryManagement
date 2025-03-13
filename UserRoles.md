#  Roles and Permissions – Library Application

## Rola: USER (Czytelnik)

- **Katalog i rezerwacje:**
  - Przeglądanie dostępnych książek
  - Rezerwowanie książek
  - Podgląd statusu rezerwacji

- **Zarządzanie wypożyczeniami:**
  - Wypożyczanie książek (automatycznie lub przez zatwierdzenie przez bibliotekarza)
  - Przedłużanie wypożyczeń (jeśli dostępne)
  - Podgląd historii wypożyczeń

- **Zarządzanie kontem:**
  - Rejestracja konta
  - Edycja danych konta (e-mail, hasło)
  - Dezaktywacja konta

- **Powiadomienia:**
  - Informacje o zbliżającym się terminie zwrotu
  - Powiadomienie o dostępności zarezerwowanych pozycji

## Rola: LIBRARIAN (Bibliotekarz)

- **Zarządzanie książkami:**
  - Dodawanie, edycja, usuwanie książek (encja `Book`)
  - Przypisywanie kategorii, autorów, wydania

- **Obsługa wypożyczeń i rezerwacji:**
  - Wypożyczanie książek użytkownikom
  - Przedłużanie terminów wypożyczenia
  - Zmiana statusu rezerwacji

- **Zarządzanie dostępnością książek:**
  - Sprawdzanie aktualnego statusu książek
  - Zgłaszanie uszkodzonych lub zaginionych egzemplarzy

- **Komunikacja z użytkownikami:**
  - Przypomnienie o terminie zwrotu

## Rola: ADMIN (Administrator systemu)

- **Wszystkie funkcje LIBRARIAN +**

- **Pełny przegląd i zarządzanie danymi:**
  - Historia wypożyczeń użytkowników (`BorrowHistory`)
  - Lista wszystkich użytkowników, ich role, aktywność w systemie
  - Dostęp do globalnej konfiguracji aplikacji (`application.yml`, `.env`)
    
- **Zarządzanie kontami:**
  - Dodaj, usuń i edytuj konto
  - Zmiana roli konta
  - Reset hasła

- **Dostęp do panelu administracyjnego:**
  - Spring Boot Admin – monitoring statusu aplikacji, uptime, health checks

- **Logi systemowe i monitoring:**
  - Logi aplikacji: Spring Boot + Logback
  - Monitoring metryk: Spring Boot Actuator (`/actuator/metrics`, `/actuator/health`)

- **Testy i wgląd w wyniki:**
  - Testy jednostkowe: JUnit 5
  - Raporty: Surefire Reports
  - CI/CD: GitHub Actions lub GitLab CI

- **Testy wydajnościowe:**
  - Gatling – uruchamiane lokalnie, raporty w `/gatling-results`

- **Zarządzanie konfiguracją systemu:**
  - Zmienne środowiskowe `.env`, pliki `application.yml`
  - Obsługa daty i czasu: `java.time.Clock`
  - Konfiguracja stref czasowych w `application.yml`
