# 🧩 Teamboard Backend

A simple, Kanban-style task & team management backend.

Built with **Spring Boot**, **PostgreSQL (Docker)**, and **Java 23**.

---

## 👨‍🎓 About This Project

Hi! I’m a **second-year Computer Science student**, and this backend is my hands-on project to learn:

- Real software development workflows  
- Backend architecture & clean code practices  
- Working with Dockerized databases  
- Database schema evolution + migrations  
- Authentication & secure systems design  

This repository is where I design, build, break, and improve an actual end-to-end tool — like a real-world dev environment.

---

## 🚀 Tech Stack

**Languages & Frameworks**
- Java 23  
- Spring Boot (Web, Data JPA, Security)

**Infrastructure**
- PostgreSQL (Docker container)  
- Flyway (database migrations)

**Build Tools**
- Maven  

---

## ✨ Planned Features

### 🔐 Authentication
- User accounts  
- Password hashing  
- JWT-based authentication  

### 📋 Boards & Tasks
- Boards  
- Columns  
- Cards (tasks)  
- Full CRUD API for frontend use  

### 🏗️ Architecture
- Layered structure  
  - Controller → Service → Repository  
- Clean separation of concerns  
- Flyway-managed schema versioning  

---

## 🛠️ Getting Started

### **Prerequisites**
- Docker + Docker Desktop  
- Java 23+  
- Maven  

### **Run Locally**

```bash
# Start PostgreSQL via Docker
docker-compose up -d

# Build and run the backend
mvn spring-boot:run
