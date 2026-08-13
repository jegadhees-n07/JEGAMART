# Login & Registration System

A complete authentication system with HTML/CSS frontend, Java Spring Boot backend, and SQLite database.

## Project Structure

```
Test/
├── frontend/
│   ├── login.html           # Login page
│   ├── register.html        # Registration page
│   ├── dashboard.html       # Dashboard page (after login)
│   ├── css/
│   │   └── style.css        # Styling for all pages
│   └── js/
│       ├── login.js         # Login functionality
│       └── register.js      # Registration functionality
└── backend/
    ├── pom.xml              # Maven dependencies
    ├── src/
    │   └── main/
    │       ├── java/com/auth/
    │       │   ├── AuthApplication.java      # Main Spring Boot app
    │       │   ├── model/
    │       │   │   └── User.java             # User entity
    │       │   ├── repository/
    │       │   │   └── UserRepository.java   # Database access
    │       │   ├── service/
    │       │   │   └── AuthService.java      # Business logic
    │       │   ├── controller/
    │       │   │   └── AuthController.java   # REST API endpoints
    │       │   ├── dto/
    │       │   │   ├── LoginRequest.java
    │       │   │   ├── RegisterRequest.java
    │       │   │   └── AuthResponse.java
    │       │   └── util/
    │       │       └── JwtUtil.java          # JWT token management
    │       └── resources/
    │           └── application.properties    # Configuration
    └── auth.db              # SQLite database (auto-created)
```

## Prerequisites

1. **Java 17 or higher** - [Download](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
2. **Maven** - [Download](https://maven.apache.org/download.cgi)
3. **Node.js** (optional, for serving frontend) - [Download](https://nodejs.org/)
4. **A modern web browser** (Chrome, Firefox, Edge, Safari)

## Installation & Setup

### 1. Backend Setup

#### Step 1: Navigate to backend directory
```bash
cd backend
```

#### Step 2: Build the project with Maven
```bash
mvn clean install
```

#### Step 3: Run the Spring Boot application
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

You should see output like:
```
Started AuthApplication in X.XXX seconds
```

### 2. Frontend Setup

#### Option A: Using Python (Simple HTTP Server)
```bash
cd frontend

# Python 3
python -m http.server 3000

# Or Python 2
python -m SimpleHTTPServer 3000
```

#### Option B: Using Node.js (http-server)
```bash
cd frontend

# Install http-server globally (if not already installed)
npm install -g http-server

# Start the server
http-server -p 3000
```

#### Option C: Using VS Code Live Server Extension
1. Install "Live Server" extension in VS Code
2. Right-click on `login.html` and select "Open with Live Server"

The frontend will be available at `http://localhost:3000`

## Features

### Authentication Flow

1. **Registration**
   - User enters Full Name, Email, Password
   - Password validation (minimum 6 characters)
   - Password confirmation check
   - Email uniqueness validation
   - JWT token generated upon successful registration

2. **Login**
   - User enters Email and Password
   - Credentials validated against database
   - JWT token generated upon successful login
   - Token stored in browser's localStorage
   - User redirected to dashboard

3. **Dashboard**
   - Shows logged-in user's information
   - Logout functionality
   - Token-based authentication

## API Endpoints

### POST /api/auth/register
Register a new user

**Request:**
```json
{
    "fullname": "John Doe",
    "email": "john@example.com",
    "password": "password123"
}
```

**Response (Success - 201):**
```json
{
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
        "email": "john@example.com",
        "fullname": "John Doe"
    }
}
```

**Response (Error - 400):**
```json
{
    "message": "Email already registered"
}
```

### POST /api/auth/login
Login an existing user

**Request:**
```json
{
    "email": "john@example.com",
    "password": "password123"
}
```

**Response (Success - 200):**
```json
{
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
        "email": "john@example.com",
        "fullname": "John Doe"
    }
}
```

**Response (Error - 401):**
```json
{
    "message": "Invalid password"
}
```

### GET /api/auth/validate
Validate JWT token

**Headers:**
```
Authorization: Bearer <token>
```

**Response (Valid - 200):**
```
Token is valid
```

**Response (Invalid - 401):**
```
Invalid token
```

## Database

SQLite database is automatically created at `backend/auth.db`

**Users Table:**
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fullname VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Configuration

Edit `backend/src/main/resources/application.properties` to customize:

```properties
# Server Port
server.port=8080

# JWT Secret (CHANGE IN PRODUCTION!)
jwt.secret=your-secret-key-change-in-production

# JWT Expiration (in milliseconds, default: 24 hours)
jwt.expiration=86400000

# Database
spring.datasource.url=jdbc:sqlite:auth.db
```

## Troubleshooting

### 1. Port 8080 is already in use
Edit `application.properties`:
```properties
server.port=8090
```

### 2. CORS errors in browser console
The backend already has CORS enabled with `@CrossOrigin(origins = "*")`
If needed, update the controller's CrossOrigin annotation:
```java
@CrossOrigin(origins = "http://localhost:3000")
```

### 3. "Connection refused" error
- Ensure backend is running on port 8080
- Check firewall settings
- Verify URLs in `login.js` and `register.js` point to correct backend URL

### 4. Database file not created
- Run `mvn spring-boot:run` first to create the database
- Check write permissions in the backend directory

## Security Notes

⚠️ **IMPORTANT FOR PRODUCTION:**

1. Change the JWT secret in `application.properties`:
   ```properties
   jwt.secret=USE_A_VERY_LONG_RANDOM_STRING_WITH_SPECIAL_CHARACTERS
   ```

2. Enable HTTPS in production

3. Use environment variables for sensitive data:
   ```bash
   export JWT_SECRET=your-secret-key
   ```

4. Implement rate limiting for login/register endpoints

5. Add email verification for registration

6. Implement password reset functionality

7. Use secure cookies instead of localStorage for tokens

## Testing

### Test Registration
1. Open `http://localhost:3000/register.html`
2. Enter details:
   - Full Name: John Doe
   - Email: john@example.com
   - Password: password123
   - Confirm: password123
3. Click Register
4. Should redirect to login page

### Test Login
1. Open `http://localhost:3000/login.html`
2. Enter:
   - Email: john@example.com
   - Password: password123
3. Click Login
4. Should redirect to dashboard

### Test Invalid Login
1. Try wrong email or password
2. Should see error message

## Future Enhancements

- [ ] Email verification
- [ ] Password reset functionality
- [ ] Two-factor authentication
- [ ] Social login (Google, GitHub)
- [ ] User profile update
- [ ] Account deletion
- [ ] Session management
- [ ] Refresh token implementation
- [ ] User roles and permissions
- [ ] Audit logging

## License

This project is provided as-is for educational purposes.

## Support

For issues or questions, please check:
1. Console errors (F12 in browser)
2. Backend logs
3. Ensure all services are running on correct ports
