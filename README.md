# 🎬 CinePass – Movie Ticket Booking Web Application

A full-stack, enterprise-grade **Movie Ticket Booking Website** developed with **Java**, **Spring Boot**, **MySQL**, **HTML5**, **CSS3**, and **JavaScript**.

---

## 🌟 Feature Implementations (All 10 Requirements)

## 🔄 Standard User Flow
The application strictly enforces the following end-to-end journey:
1. **Register** (`/login.html` → "Register New Account" tab)
   - User enters Name, Email, Phone, and Password.
   - Upon successful registration, the user is **redirected to the Login page** (they are not logged in automatically).
2. **Login** (`/login.html` → "Sign In" tab)
   - Login fields start completely empty with no autofill.
   - User inputs their registered email and password.
   - Upon successful authentication, an automated **Login Success Notification** email is sent to their registered email address.
   - The user is then redirected to the **Home Page** (`/home.html`).
3. **Home Page & Select Movie** (`/home.html`)
   - Protected route (unauthenticated visitors are blocked and redirected to Login).
   - View recommended movies and now-showing movies.
   - Inspect movie details (Rating, Duration, Genre, Language, Cast, Synopsis).
4. **Book Seats**
   - Calendar date picker at top (Today, Tomorrow, next 5 days).
   - Select partner theatre and showtime.
   - Interactive seating layout with two distinct categories: **Premium** (Rows A-D) and **Elite** (Rows E-H).
   - Accept the cinema Terms & Conditions.
   - Review registered contact details (email and mobile) on the order summary.
5. **Make Payment**
   - Choose payment method (Google Pay, PhonePe, or dynamic UPI QR Code).
   - Complete payment simulation with secure gateway handshake.
6. **Receive E-Ticket by Email**
   - Official E-Ticket confirmation email delivered to the user's registered email address including:
     - **Theatre name**
     - **Theatre location**
     - **Movie name**
     - **Booking date and show time**
     - **Number of seats booked**
     - **Selected seat numbers**
   - Digital printable e-ticket presented on screen.

### 2. Home Page and Recommended Movies (Post-Authentication)
- Accessible only after login.
- **Hero Showcase**: Cinematic featured banner with high-resolution backdrops, rating badge (e.g., ⭐ 8.8/10), runtime, genre, and quick "Book Tickets" call to action.
- **Recommended Movies**: Dedicated section displaying top-rated movies handpicked for the user with ratings and direct booking options.
- **Search & Filter**: Real-time search bar and genre filtering chips (Action, Sci-Fi, Drama, Adventure, Comedy, Animation).

### 3. Movie Details Modal & View
- Clicking on any movie displays its complete details:
  - High-definition poster and banner
  - Rating, Duration, Language, and Genre tags
  - Director, Star Cast, and Release Date
  - Full storyline synopsis
  - Prominent **"Book Ticket"** button.

### 4. Theatre and Show Selection
- **Calendar Date Picker at the top**: Interactive date selector strip displaying consecutive days (Today, Tomorrow, +5 upcoming days) with weekdays and date numbers.
- **Theatres & Timings**: Lists partner multiplexes (PVR Cinemas, INOX, Cinepolis) with addresses and acoustic amenities (e.g., 4K RGB Laser, Dolby Atmos 7.1, Recliners).
- **Showtimes**: Morning, afternoon, evening, and night showtime slots. Clicking any slot smoothly navigates to the seat layout.

### 5. Seat Categories and Interactive Seating Layout
- **Cinema Screen Visualization**: Curved screen graphic with ambient illumination ("All eyes this way please").
- **Two Distinct Seat Categories with Pricing**:
  - ⭐ **Premium** (Rows A to D) – Plush comfort, prime viewing angle (e.g., ₹280.00).
  - 💎 **Elite** (Rows E to H) – Standard cinema seating (e.g., ₹180.00).
- **Seating Layout**: Complete row lettering (A through H) and seat numbers (1 to 12) structured with center and side aisles.
- **Dynamic Calculation**: Selecting/deselecting seats updates the seat count, list, and total price in real time.

### 6. Terms and Conditions
- Modal pop-up displayed before payment containing admission rules, age rating certification rules ('A' certificate 18+ verification), outside food & beverage restrictions, and arrival timing guidelines.
- Features distinct **"Cancel"** and **"Accept & Continue"** action buttons.

### 7. User Contact Details Confirmation
- Displays the user's **Registered Email Address** and **Registered Mobile Number** before payment for verification.
- Complete ticket breakdown: Movie, Theatre, Screen, Date, Time, Selected Seats, Subtotal, Convenience Fee, and Total Amount Payable.

### 8. Payment Options
- Multiple payment modes supported:
  - **Google Pay (GPay)** (Quick UPI payment)
  - **PhonePe** (UPI payment)
  - **QR Code Payment** (Dynamic SVG QR Code with live 5-minute countdown timer for scanning with any UPI app).
- Realistic gateway handshake animation ("Processing payment securely...").
- Transitions to an animated **"Payment Successful"** screen with Transaction ID and receipt.

### 9. Seat Availability Management & Concurrency Control
- **Concurrency Protection**: Strict database-level unique constraint on `(show_id, seat_number)` in `booking_seats`.
- Atomic transactional booking (`@Transactional(isolation = Isolation.SERIALIZABLE)`):
  - Pre-booking availability verification prevents race conditions if multiple users attempt booking identical seats simultaneously.
  - Returns friendly collision error (`409 Conflict`) if another user booked the seat a second prior.
- Live endpoint `/api/shows/{id}/booked-seats` marks booked seats as unavailable/sold out in real time.

### 10. Email Ticket Confirmation & Digital E-Ticket
- **Email Delivery**: Rich HTML e-ticket dispatched to the user's registered email with all 7 required fields:
  1. Movie Name
  2. Theatre Name
  3. Theatre Location / Address
  4. Show Date & Show Time
  5. Selected Seat Numbers (with category tag)
  6. Number of Seats
  7. Total Ticket Price Paid
- **On-Screen E-Ticket**: Printable ticket card with digital QR/barcode, booking reference code, transaction ID, and a "Print / Save Ticket" button.
- **My Bookings**: Dedicated portal to view past bookings and re-download e-tickets.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 23 / 17+, Spring Boot 4.1.1, Spring Data JPA, Hibernate, JavaMailSender, BCrypt |
| **Database** | MySQL 8.0 (`movie_booking_db`) |
| **Frontend** | HTML5, CSS3 (Modern Cinema Dark theme), Vanilla JavaScript (ES6+ async/await) |
| **Build Tool** | Apache Maven 3.10 (via included `mvnw.cmd`) |

---

## 🚀 How to Run the Application

### 1. Database Configuration
Ensure MySQL Server 8.0 is running on port 3306. The database settings in `src/main/resources/application.properties` are configured as:
```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/movie_booking_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
```

*(Note: Set the `DB_PASSWORD` environment variable for your MySQL instance).*

### 2. Start the Server
Run the Maven wrapper from the project root:
```powershell
.\mvnw.cmd spring-boot:run
```

The application will start on:
👉 **`http://localhost:8080/`**

### 3. Default Demo Account
You can register a new account anytime or use the pre-configured credentials:
- **Email:** `dhanaraj082001@gmail.com`
- **Password:** `Password@123`

---

## 📡 REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Authenticate user & send login notification email |
| `GET` | `/api/auth/current-user` | Retrieve currently logged-in user profile |
| `POST` | `/api/auth/logout` | End user session |
| `GET` | `/api/movies` | List all movies (supports `?search=` filter) |
| `GET` | `/api/movies/recommended` | List recommended movies |
| `GET` | `/api/movies/{id}` | Get detailed movie info |
| `GET` | `/api/shows?movieId={id}&date={date}` | Get available theatres & showtimes |
| `GET` | `/api/shows/{id}/booked-seats` | Get list of booked seat numbers |
| `POST` | `/api/bookings` | Create booking, process payment & send e-ticket email |
| `GET` | `/api/bookings/my` | Retrieve booking history for current user |
