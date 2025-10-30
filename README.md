E-Commerce Application

A Spring Boot-based E-Commerce Application that supports user authentication, product management, and order management. 
This project demonstrates a full-stack backend with JWT-based security, CRUD operations, and a structured REST API design.

Features of the application:
User Authentication & Authorization:
  Registration, login, and JWT token-based authentication
  Role-based access (ROLE_USER, ROLE_ADMIN)
Product Management :
  Add, update, delete, and fetch products
  Admin-only operations for product CRUD
Order Management:
    Users can place orders
  View, update, and delete orders
Security:
  Password encryption using BCrypt
  JWT filter for securing endpoints
Validation:
  Input validation and error handling

Tech Stack->
Backend: Spring Boot, Spring Security, Spring Data JPA
Database: MySQL / H2 (for testing)
Authentication: JWT (JSON Web Tokens)
Build Tool: Maven
Testing: JUnit 5, Mockito

API Endpoints->

Authentication:
POST /api/auth/register — Register a new user
POST /api/auth/login — Login and receive JWT token

User Management:
DELETE /api/auth/delete — Delete self account (requires JWT)
DELETE /api/auth/delete/{id} — Admin deletes any user

Product Management:
GET /api/products — List all products
POST /api/products — Add a new product (Admin only)
PUT /api/products/{id} — Update a product (Admin only)
DELETE /api/products/{id} — Delete a product (Admin only)

Order Management:
GET /api/orders — List user orders
POST /api/orders — Place a new order
PUT /api/orders/{id} — Update order


DELETE /api/orders/{id} — Cancel order

Note: All protected endpoints require Authorization: Bearer <JWT_TOKEN> header.
