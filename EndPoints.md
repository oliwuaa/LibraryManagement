# **API - Zarządzanie Bibliotekami: Endpointy**

## **1. Zarządzanie Książkami (BookService)**

### **1.1 Pobieranie książek**
- **GET** `/books`
  - Opis: Pobiera listę wszystkich książek.
  - Parametry: Brak.
  - Dostęp: USER, LIBRARIAN, ADMIN

- **GET** `/books/{bookId}`
  - Opis: Pobiera książkę po ID.
  - Parametry: `bookId` (ID książki).
  - Dostęp: USER, LIBRARIAN, ADMIN

- **GET** `/books/search?title={title}&author={author}&isbn={isbn}`
  - Opis: Pobiera książkę z użyciem parametrów.
  - Parametry: `title` (tytuł książki), `author` (autor), `isbn` (numer ISBN książki).
  - Dostęp: USER, LIBRARIAN, ADMIN

### **1.2 Dodawanie książek**
- **POST** `/books`
  - Opis: Dodaje nową książkę.
  - Parametry: `Book` (obiekt książki).
  - Dostęp: LIBRARIAN, ADMIN

### **1.3 Usuwanie książki**
- **DELETE** `/books/{bookId}`
  - Opis: Usuwa książkę z systemu.
  - Parametry: `bookId` (ID książki).
  - Dostęp: LIBRARIAN, ADMIN

### **1.4 Aktualizowanie książki**
- **PUT** `/books/{bookId}`
  - Opis: Aktualizuje dane książki (tytuł, autor, ISBN).
  - Parametry: `bookId` (ID książki), `Book` (obiekt z nowymi danymi książki).
  - Dostęp: LIBRARIAN, ADMIN

### **1.5 Wyszukiwanie książek**
- **GET** `/books/search`
  - Opis: Wyszukuje książki po tytule, autorze, gatunku, lub ISBN.
  - Parametry: `title`, `author`, `genre`, `isbn`.
  - Dostęp: USER, LIBRARIAN, ADMIN

---

## **2. Zarządzanie Kopiami Książek (CopyService)**

### **2.1 Pobieranie kopii książek**
- **GET** `/copies`
  - Opis: Pobiera listę wszystkich kopii książek.
  - Parametry: Brak.
  - Dostęp: LIBRARIAN, ADMIN

- **GET** `/copies/{copyId}`
  - Opis: Pobiera szczegóły konkretnej kopii książki po ID.
  - Parametry: `copyId` (ID kopii).
  - Dostęp: LIBRARIAN, ADMIN

- **GET** `/copies/available`
  - Opis: Pobiera wszystkie dostępne kopie książek.
  - Parametry: Brak.
  - Dostęp: USER, LIBRARIAN, ADMIN

### **2.2 Dodawanie kopii książek**
- **POST** `/copies`
  - Opis: Dodaje kopię książki do biblioteki.
  - Parametry: `bookId`, `libraryId`.
  - Dostęp: LIBRARIAN, ADMIN

### **2.3 Aktualizowanie statusu kopii książki**
- **PUT** `/copies/{copyId}`
  - Opis: Aktualizuje status kopii (np. z "dostępna" na "wypożyczona").
  - Parametry: `copyId`, `status`.
  - Dostęp: LIBRARIAN, ADMIN

### **2.4 Usuwanie kopii książki**
- **DELETE** `/copies/{copyId}`
  - Opis: Usuwa kopię książki.
  - Parametry: `copyId`.
  - Dostęp: LIBRARIAN, ADMIN

---

## **3. Zarządzanie Bibliotekami (LibraryService)**

### **3.1 Pobieranie bibliotek**
- **GET** `/libraries`
  - Opis: Pobiera listę wszystkich bibliotek.
  - Parametry: Brak.
  - Dostęp: USER, LIBRARIAN, ADMIN

- **GET** `/libraries/{libraryId}`
  - Opis: Pobiera bibliotekę po ID.
  - Parametry: `libraryId`.
  - Dostęp: USER, LIBRARIAN, ADMIN

### **3.2 Dodawanie biblioteki**
- **POST** `/libraries`
  - Opis: Dodaje nową bibliotekę.
  - Parametry: `Library`.
  - Dostęp: ADMIN

### **3.3 Usuwanie biblioteki**
- **DELETE** `/libraries/{libraryId}`
  - Opis: Usuwa bibliotekę.
  - Parametry: `libraryId`.
  - Dostęp: ADMIN

### **3.4 Aktualizowanie danych biblioteki**
- **PUT** `/libraries/{libraryId}`
  - Opis: Aktualizuje dane biblioteki.
  - Parametry: `libraryId`, `Library`.
  - Dostęp: ADMIN

### **3.5 Wyszukiwanie bibliotek**
- **GET** `/libraries/search`
  - Opis: Wyszukuje biblioteki po nazwie lub adresie.
  - Parametry: `name`, `address`.
  - Dostęp: USER, LIBRARIAN, ADMIN

---

## **4. Zarządzanie Użytkownikami (UserService)**

### **4.1 Pobieranie użytkowników**
- **GET** `/users`
  - Opis: Pobiera listę wszystkich użytkowników.
  - Parametry: Brak.
  - Dostęp: ADMIN

- **GET** `/users/{userId}`
  - Opis: Pobiera użytkownika po ID.
  - Parametry: `userId`.
  - Dostęp: ADMIN

- **GET** `/users/role/{role}`
  - Opis: Pobiera użytkowników na podstawie roli.
  - Parametry: `role`.
  - Dostęp: ADMIN

- **GET** `/users/library/{libraryId}/librarians`
  - Opis: Pobiera bibliotekarzy z danej biblioteki.
  - Parametry: `libraryId`.
  - Dostęp: ADMIN

### **4.2 Dodawanie użytkowników**
- **POST** `/users`
  - Opis: Dodaje nowego użytkownika.
  - Parametry: `User`.
  - Dostęp: ADMIN

### **4.3 Aktualizowanie danych użytkownika**
- **PUT** `/users/{userId}`
  - Opis: Aktualizuje dane użytkownika.
  - Parametry: `userId`, `User`.
  - Dostęp: ADMIN, (USER – tylko własne konto)

### **4.4 Usuwanie użytkownika**
- **DELETE** `/users/{userId}`
  - Opis: Usuwa użytkownika z systemu.
  - Parametry: `userId`.
  - Dostęp: ADMIN

### **4.5 Wyszukiwanie użytkowników**
- **GET** `/users/search`
  - Opis: Wyszukuje użytkowników.
  - Parametry: `name`, `email`, `role`.
  - Dostęp: ADMIN

---

## **5. Zarządzanie Wypożyczeniami (LoanService)**

### **5.1 Pobieranie wszystkich wypożyczeń**
- **GET** `/loans`
  - Opis: Pobiera listę wszystkich wypożyczeń.
  - Parametry: Brak.
  - Dostęp: LIBRARIAN, ADMIN

### **5.2 Pobieranie wypożyczeń konkretnego użytkownika**
- **GET** `/users/{userId}/loans`
  - Opis: Pobiera wszystkie wypożyczenia danego użytkownika.
  - Parametry: `userId`.
  - Dostęp: USER (tylko własne), LIBRARIAN, ADMIN

### **5.3 Wypożyczenie książki**
- **POST** `/loans`
  - Opis: Umożliwia wypożyczenie książki przez użytkownika.
  - Parametry: `userId`, `copyId`.
  - Dostęp: LIBRARIAN, ADMIN

### **5.4 Zwracanie książki**
- **POST** `/loans/{loandId}/return`
  - Opis: Umożliwia zwrócenie książki przez użytkownika.
  - Parametry: `userId`, `copyId`, `loanId`.
  - Dostęp: LIBRARIAN, ADMIN

### **5.5 Przedłużenie wypożyczenia**
- **POST** `/loans/{loanId}/extend`
  - Opis: Umożliwia przedłużenie wypożyczenia książki.
  - Parametry: `userId`, `copyId`, `newReturnDate`, `loanId`.
  - Dostęp: USER (tylko własne), LIBRARIAN, ADMIN

---

## **6. Autoryzacja i Logowanie**

### **6.1 Logowanie użytkownika**
- **POST** `/auth/login`
  - Opis: Logowanie użytkownika.
  - Parametry: `email`, `password`.
  - Dostęp: PUBLIC

### **6.2 Rejestracja użytkownika**
- **POST** `/auth/register`
  - Opis: Rejestracja nowego użytkownika.
  - Parametry: `name`, `email`, `password`.
  - Dostęp: PUBLIC

### **6.3 Weryfikacja e-maila**
- **GET** `/auth/verify-email?token={verificationToken}`
  - Opis: Weryfikacja e-maila użytkownika po rejestracji.
  - Parametry: `verificationToken`.
  - Dostęp: PUBLIC

---

## **7. Rezerwacja Książek (ReservationService)**

### **7.1 Rezerwowanie książki**
- **POST** `/reservations`
  - Opis: Rezerwacja książki przez użytkownika.
  - Parametry: `userId`, `bookId`.
  - Dostęp: USER

### **7.2 Pobieranie wszystkich rezerwacji**
- **GET** `/reservations`
  - Opis: Pobiera listę wszystkich rezerwacji książek.
  - Parametry: Brak.
  - Dostęp: LIBRARIAN, ADMIN

### **7.3 Anulowanie rezerwacji**
- **DELETE** `/reservations/{reservationId}`
  - Opis: Anulowanie rezerwacji książki.
  - Parametry: `reservationId`.
  - Dostęp: USER (tylko własne), LIBRARIAN, ADMIN

### **7.4 Pobieranie rezerwacji użytkownika**
- **GET** `/reservations/user/{userId}`
  - Opis: Pobiera listę rezerwacji dla danego użytkownika.
  - Parametry: `userId`.
  - Dostęp: USER (tylko własne), LIBRARIAN, ADMIN

---

## **8. Integracje**

### **8.1 API z danymi książek**
- **GET** `/external/books`
  - Opis: Integracja z zewnętrznym API do pobierania danych o książkach.
  - Parametry: `query`.
  - Dostęp: ADMIN
