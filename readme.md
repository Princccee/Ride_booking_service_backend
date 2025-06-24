# 🚗 Ride Booking Backend API

A backend service for a ride-hailing application built using **Spring Boot**, supporting **user/driver roles**, **ride lifecycle**, **real-time driver notifications**, **JWT authentication**, **payment integration with Razorpay**, and **logging**.

---

## 📁 Project Structure

```
src/main/java/com/ridebooking
├── config/              # Config files (Security, WebSocket, Razorpay, Firebase)
├── controller/          # API endpoints (Auth, Ride, Driver)
├── dto/                 # Data Transfer Objects
├── model/               # Entities and Enums
├── repository/          # Spring Data JPA repositories
├── security/            # JWT authentication classes
├── service/             # Business logic layer
└── RideServiceApplication.java
```

---

## ✅ Features

### 🔐 Authentication & Authorization
- Register/Login for both Users and Drivers
- JWT token generation & validation
- Password hashing using BCrypt
- Role-based access (USER, DRIVER)

### 🚘 Ride Management
- Book ride (by users)
- Accept/Start/Complete ride (by drivers)
- Ride status updates
- Ride cancellation

### 🧭 Location & Notifications
- Driver location tracking
- Notify nearby drivers in real-time using WebSocket

### 💳 Payment Integration
- Razorpay integration for ride fare payments
- Auto-calculated fare (based on distance & time)

### ⭐ Rating System
- Users can rate drivers after ride completion

### 📚 Ride History
- View past rides for both users and drivers

### 🪵 Logging
- All events logged to `logs/server.log`
- Useful for debugging and production monitoring

---

## 🛠️ Tech Stack

| Technology | Purpose                          |
|-----------|----------------------------------|
| Java + Spring Boot | Core application framework     |
| Spring Security | Authentication and Authorization |
| JWT       | Stateless token authentication   |
| WebSocket / FCMS | Real-time notifications          |
| Razorpay  | Payment gateway integration      |
| Hibernate (JPA) | ORM for database interaction     |
| MySQL     | Relational database              |
| Gradle    | Dependency & build tool          |

---

## ⚙️ Setup Instructions

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/Princccee/Ride_booking_service_backend.git
cd Ride_booking_service_backend
```

### 2️⃣ Set Up the Database

Create a MySQL database named `ridebooking` (or update the name in properties).

```sql
CREATE DATABASE ridebooking;
```

### 3️⃣ Configure `application.properties`

Update the following credentials and secrets:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ridebooking
spring.datasource.username=root
spring.datasource.password=yourpassword

jwt.secret=your_super_secure_jwt_secret_key_here
razorpay.key_id=your_razorpay_key
razorpay.key_secret=your_razorpay_secret

logging.file.name=logs/server.log
```

### 4️⃣ Build and Run the Application

```bash
./gradlew build
./gradlew bootRun
```

The server will start at:

```
http://localhost:8080
```

---

## 🔑 Authentication & Authorization

All secured endpoints require a JWT token in the header:

```
Authorization: Bearer <your_jwt_token>
```

---

## 📬 API Endpoints

| Endpoint                      | Method | Role        | Description                       |
| ----------------------------- | ------ | ----------- | --------------------------------- |
| `/auth/register`              | POST   | Public      | Register user or driver           |
| `/auth/login`                 | POST   | Public      | Authenticate and get JWT token    |
| `/api/rides/book`             | POST   | USER        | Book a new ride                   |
| `/api/rides/{id}/accept`      | POST   | DRIVER      | Accept a ride                     |
| `/api/rides/{id}/start`       | POST   | DRIVER      | Start the ride                    |
| `/api/rides/{id}/complete`    | POST   | DRIVER      | Complete the ride and charge fare |
| `/api/rides/history`          | GET    | USER/DRIVER | View ride history                 |
| `/api/driver/location/update` | POST   | DRIVER      | Update driver's current location  |

---

## 🧪 Testing with Postman

1. Register via `/auth/register`
2. Login via `/auth/login` and extract the `token`
3. Use token in Postman under `Authorization` → Bearer Token
4. Test other secured endpoints

---

## 🪵 Logging

Logs are saved to both console and a file:

```
logs/server.log
```

Ensure this is set in `application.properties`:

```properties
logging.file.name=logs/server.log
logging.level.root=INFO
```

---

## ✨ Example Roles

The `User` and `Driver` entities have a `role` field with values:

* `USER`
* `DRIVER`

Spring Security expects: `ROLE_USER` and `ROLE_DRIVER`
So ensure roles are stored properly, or convert them using `CustomUserDetails`.

---

## 🧱 Future Improvements

* Role-based admin dashboard
* Swagger/OpenAPI documentation
* Docker support
* Rate limiting & throttle control
* Email/SMS OTP verification
* Unit and integration tests

---

## 🤝 Contributing

1. Fork this repository
2. Create your feature branch: `git checkout -b feature-name`
3. Commit changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature-name`
5. Open a pull request

---

## 🧑‍💻 Developer Info

**Name:** Prince Kumar
**Email:** [princceekumar07@gmail.com](mailto:prince@example.com)
**LinkedIn:** [https://www.linkedin.com/in/prince-kumar-127903288/](https://linkedin.com/in/yourprofile)

---

[//]: # (## 📄 License)

[//]: # ()
[//]: # (Licensed under the [MIT License]&#40;LICENSE&#41;)

[//]: # (---)

> This backend powers the core logic of a scalable, real-time ride-hailing service.