# JD Mart - Modern Full-Stack E-Commerce Web Application

**JD Mart** is a modern, responsive full-stack e-commerce web platform inspired by the core functionalities of platforms like Flipkart and Meesho, built with 100% original branding, modern UI/UX design, and clean enterprise code.

JD Mart supports local development with MySQL or in-memory emulation and is ready for one-click cloud deployment.

---

## 🌟 Key Features

### Customer Storefront
* **Homepage**: Hero promotional carousel, popular category strip, featured products, and trending deals.
* **Product Catalog**: Multi-faceted filtering by Category, Price brackets (₹0-1k, ₹1k-5k, ₹5k-10k, ₹10k+), and Minimum Rating (4★+, 3★+).
* **Search**: Real-time product search by keyword across titles, categories, and descriptions.
* **Product Details**: High-resolution gallery, comprehensive specifications table, stock indicator, quantity selector, Related Products, and instant "Buy Now" flow.
* **Shopping Cart**: Real-time price breakdown, quantity adjustment (+/-), discount calculation, free delivery thresholds, and automatic database persistence.
* **Checkout & Mock Payments**: Shipping address management, order summary, and sandbox payment options:
  * Cash on Delivery (COD)
  * Demo UPI (Google Pay, PhonePe, Paytm virtual ID)
  * Demo Debit/Credit Card (mock sandbox - no sensitive card info stored)
* **Order Tracking (My Orders)**: Unique order IDs (`JDM202609200001`), line item breakdown, delivery address snapshot, and live order status lifecycle (`ORDERED`, `CONFIRMED`, `SHIPPED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`).
* **User Profile**: Account details update, address book management, and secure password change.

### Admin Dashboard & Management
* **Analytics**: Gross sales revenue, total orders, registered users, and active inventory stats.
* **Product Management**: Full CRUD operations: add products, update prices, set discounts, modify specifications, upload image URLs, and adjust live inventory stock.
* **Order Management**: Monitor customer orders in real-time and transition order fulfillment statuses.

### Security & Reliability
* **Stateless JWT Authentication**: Bearer token headers for REST APIs with configurable token expiration.
* **BCrypt Hashing**: Passwords stored using industry-standard salted BCrypt hashing.
* **Role-Based Access Control**: Strict role enforcement (`ROLE_USER`, `ROLE_ADMIN`).
* **Input Validation**: Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Min`, `@Pattern`) with unified `@RestControllerAdvice` error responses.
* **Automatic Database Seeding**: Auto-populates 8 categories, 30+ rich Indian catalog products, 1 admin account, and 2 demo accounts on initial boot.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 21, Spring Boot 3.3.4, Spring MVC, Spring Data JPA, Hibernate, Spring Security 6, JJWT |
| **Database** | MySQL 8.x (Production/Dev), In-Memory H2 (Test suite) |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript (ES6+), Fetch API (Single/Multi-page static architecture) |
| **Build & Tooling** | Apache Maven 3.9+, Maven Wrapper (`mvnw`), Git |

---

## 🚀 Quick Start & Local Setup

### 1. Prerequisites
* **Java 21** or **Java 17** installed (`java -version`)
* **Maven 3.8+** (or use the included `./mvnw` / `mvnw.cmd`)
* **MySQL Server** installed and running on port 3306

### 2. Configure MySQL Database
Log in to your MySQL terminal or workbench:
```sql
CREATE DATABASE jd_mart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Environment Variables
Copy `.env.example` to `.env` or set environment variables:
```bash
# Windows PowerShell
$env:DB_URL="jdbc:mysql://localhost:3306/jd_mart?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_SECRET="JDMartSuperSecretKeyForJWTAuthentication2026SecureKeyWithSufficientEntropy512Bit"
$env:PORT="8080"
```

Or configure directly in `src/main/resources/application.properties`.

### 4. Build & Run Application
From the project root:

```powershell
# Using the Maven Wrapper on Windows:
.\mvnw.cmd clean package -DskipTests
.\mvnw.cmd spring-boot:run

# Or with system Maven:
mvn clean package -DskipTests
mvn spring-boot:run
```

Once started, open your browser:
👉 **[http://localhost:8080](http://localhost:8080)**

### 5. Build & Run with Docker
The repository includes a multi-stage Dockerfile that builds the application with
Java 21 and runs it with the Java 21 runtime image:

```powershell
docker build -t jd-mart .
docker run --rm -p 8080:8080 `
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/jd_mart?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" `
  -e DB_USERNAME="root" `
  -e DB_PASSWORD="your_mysql_password" `
  -e JWT_SECRET="replace-with-a-secure-secret" `
  jd-mart
```

Set the production database and JWT environment variables in the hosting
provider rather than storing them in the image or repository. The application
uses the provider's `PORT` environment variable when one is supplied.

---

## 🔑 Default Seeded Accounts

The application automatically seeds the database on first boot:

| Role | Email | Password | Access |
|---|---|---|---|
| **Administrator** | `admin@jdmart.com` | `Admin@123` | Storefront + Admin Dashboard & CRUD |
| **Demo Customer 1** | `rahul.sharma@example.com` | `User@123` | Storefront, Cart, Checkout, Orders |
| **Demo Customer 2** | `priya.patel@example.com` | `User@123` | Storefront, Cart, Checkout, Orders |

*(Quick-fill buttons are provided on the login page for effortless testing)*

---

## 📡 REST API Reference

### Authentication
* `POST /api/auth/register` - Register a new customer
* `POST /api/auth/login` - Authenticate and receive Bearer JWT
* `GET /api/auth/profile` - Get authenticated user profile

### Catalog
* `GET /api/products` - Paged and filtered products (`keyword`, `categoryId`, `minPrice`, `maxPrice`, `minRating`, `sort`)
* `GET /api/products/{id}` - Single product details & specs
* `GET /api/products/featured` - Featured deals
* `GET /api/products/trending` - Trending products
* `GET /api/products/category/{id}` - Category products
* `GET /api/categories` - All active categories

### Shopping Cart
* `GET /api/cart` - View user's cart
* `POST /api/cart/items` - Add item to cart (increments quantity if already present)
* `PUT /api/cart/items/{id}` - Update quantity
* `DELETE /api/cart/items/{id}` - Remove single item
* `DELETE /api/cart/clear` - Clear all cart items

### Orders & Checkout
* `POST /api/orders` - Place order from current cart
* `GET /api/orders` - Retrieve customer's order history
* `GET /api/orders/{id}` - Get order by ID

### Admin Endpoints (Requires `ROLE_ADMIN`)
* `GET /api/admin/dashboard` - Metrics (Users, Products, Orders, Total Sales)
* `GET /api/admin/products` - List all products
* `POST /api/admin/products` - Create new product
* `PUT /api/admin/products/{id}` - Update existing product
* `DELETE /api/admin/products/{id}` - Soft-delete/deactivate product
* `PATCH /api/admin/products/{id}/stock` - Fast stock quantity update
* `GET /api/admin/orders` - Paged customer orders
* `PUT /api/admin/orders/{id}/status` - Advance order lifecycle status

### Health Check
* `GET /api/health` -> `{"status": "UP", "application": "JD Mart"}`

---

## ☁️ Cloud Deployment Guide

### Option 1: Render / Railway / Heroku
1. Push this project to GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: JD Mart full stack e-commerce app"
   git remote add origin https://github.com/<your-username>/jd-mart.git
   git branch -M main
   git push -u origin main
   ```
2. Create a managed **MySQL Database** on Railway, Render, or Aiven.
3. Create a **New Web Service** and link your GitHub repository.
4. Set Build Command:
   ```bash
   ./mvnw clean package -DskipTests
   ```
5. Set Start Command:
   ```bash
   java -jar target/jd-mart-1.0.0.jar
   ```
6. Configure Production Environment Variables:
   * `DB_URL` = `jdbc:mysql://<host>:<port>/<db_name>?useSSL=true`
   * `DB_USERNAME` = `<cloud_mysql_user>`
   * `DB_PASSWORD` = `<cloud_mysql_password>`
   * `JWT_SECRET` = `<generate-a-64-byte-secure-hex-string>`
   * `PORT` = `8080`
7. Click **Deploy**. Your application will be live at `https://<your-service>.onrender.com` or `https://<your-service>.up.railway.app`.

---

## 🛡️ License
Copyright © 2026 JD Mart. Built with Spring Boot and Modern Web Standards.
