# 🎬 MovieFlix Backend

## 🚀 Overview
The **MovieFlix Backend** is built using **Spring Boot** and provides RESTful APIs for managing movies, authentication, and dashboard statistics.  
It integrates with a relational database using **Spring Data JPA** and supports **JWT-based authentication**.

---

## ⚙️ Tech Stack
- Java 17+
- Spring Boot 3+
- Spring Security + JWT
- Spring Data JPA (Hibernate)
- PostgreSQL
- Redis
- Maven
- Lombok

---

## 📂 Project Structure
movieflix-backend/

│── src/main/java/com/kushagra/movieflix/

│ ├── controller/ # REST Controllers

│ ├── entity/ # JPA Entities

│ ├── repository/ # Database Repositories

│ ├── service/ # Business Logic

│ ├── filter/ # Security Filters (JWT)

│ ├── utils/ # Utility functions for the services

│ ├── config/ # Configuration files (Security Config)

│ └── MovieflixApplication.java

│

│── src/main/resources/

│ ├── application.properties

│

└── pom.xml



---

## 🔑 Features
- ✅ User authentication with **JWT**  
- ✅ Role-based authorization  
- ✅ CRUD operations for Movies  
- ✅ Dashboard stats API (for charts)  
- ✅ Centralized exception handling  
- ✅ **Security headers** enabled for protection against common web vulnerabilities  
- ✅ **RSA + AES hybrid encryption** for secure data transmission  

---

## 🔒 Security Features
### 1️⃣ Secure HTTP Headers
The backend enforces **best-practice security headers**:
- `Strict-Transport-Security` → Forces HTTPS  
- `X-Content-Type-Options` → Prevents MIME-type sniffing  
- `X-Frame-Options` → Prevents clickjacking  
- `X-XSS-Protection` → Mitigates reflected XSS attacks  
- `Content-Security-Policy` → Restricts allowed resources  

These headers are applied globally through a **Spring Security configuration**.

---

### 2️⃣ Hybrid Encryption (RSA + AES)
To ensure **confidentiality and integrity** of sensitive data:
- **AES (Advanced Encryption Standard)** is used for encrypting payload data (fast + efficient).  
- **RSA (Rivest–Shamir–Adleman)** is used for encrypting the AES session key (asymmetric security).  
- Workflow:
  1. Backend generates an AES session key per request.  
  2. AES key is encrypted with RSA and sent to the client.  
  3. Client uses AES key for secure payload encryption/decryption.  

This ensures secure data exchange while minimizing performance overhead.

---


## 🛠️ Setup & Installation
### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL/Postgres
- Redis server

### Steps
```bash
# Clone repo
git clone https://github.com/your-username/movieflix-backend.git
cd movieflix-backend

# Configure DB in application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/movieflix
spring.datasource.username=your_user
spring.datasource.password=your_password

# Run project
mvn spring-boot:run
