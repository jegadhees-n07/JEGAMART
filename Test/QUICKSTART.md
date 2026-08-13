# Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Prerequisites Check
- Java 17+: `java -version`
- Maven: `mvn -version`

If not installed, download from:
- Java: https://www.oracle.com/java/technologies/javase-downloads.html
- Maven: https://maven.apache.org/download.cgi

---

## ⚡ Running the Application

### Terminal 1: Start Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Wait for: **"Started AuthApplication in X.XXX seconds"**

✅ Backend ready at: `http://localhost:8080`

---

### Terminal 2: Start Frontend

**Option 1 - Python:**
```bash
cd frontend
python -m http.server 3000
```

**Option 2 - Node.js:**
```bash
cd frontend
npx http-server -p 3000
```

✅ Frontend ready at: `http://localhost:3000`

---

## 📝 Test the Application

### 1. Open Browser
Go to: `http://localhost:3000/register.html`

### 2. Register New User
Fill in:
- **Full Name:** John Doe
- **Email:** john@example.com
- **Password:** password123
- **Confirm:** password123

Click **Register** → Auto redirects to login

### 3. Login
- **Email:** john@example.com
- **Password:** password123

Click **Login** → Redirects to Dashboard ✅

### 4. Dashboard
You should see:
- Welcome message
- Your name and email
- Logout button

---

## 📁 Project Files

### Frontend
- `login.html` - Login page
- `register.html` - Registration page
- `dashboard.html` - User dashboard
- `css/style.css` - All styling
- `js/login.js` - Login logic
- `js/register.js` - Registration logic

### Backend
- `pom.xml` - Dependencies
- `AuthApplication.java` - Main app
- `model/User.java` - User entity
- `repository/UserRepository.java` - Database queries
- `service/AuthService.java` - Business logic
- `controller/AuthController.java` - API endpoints
- `util/JwtUtil.java` - Token management
- `config/SecurityConfig.java` - Security settings

---

## 🔌 API Endpoints

### Register
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "fullname": "John Doe",
    "email": "john@example.com",
    "password": "password123"
}
```

### Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "password123"
}
```

### Response (both endpoints)
```json
{
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
        "email": "john@example.com",
        "fullname": "John Doe"
    }
}
```

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| **Backend won't start** | Kill process on port 8080: `lsof -ti:8080 \| xargs kill -9` |
| **Frontend won't load** | Try different port: `python -m http.server 3001` |
| **"User not found" error** | Register first, then login |
| **CORS errors** | Restart backend after checking `SecurityConfig.java` |
| **Database locked** | Ensure only one backend instance is running |

---

## 💾 Database

SQLite database auto-created: `backend/auth.db`

To reset database:
1. Stop backend
2. Delete `auth.db`
3. Start backend (recreates empty database)

---

## 🔐 Production Checklist

Before going live:

- [ ] Change JWT secret in `application.properties`
- [ ] Enable HTTPS
- [ ] Update CORS origins (not `*`)
- [ ] Add email verification
- [ ] Implement password reset
- [ ] Add rate limiting
- [ ] Enable database backups
- [ ] Set up monitoring/logging

---

## 📚 Next Steps

1. **Customize styling** - Edit `frontend/css/style.css`
2. **Add more fields** - Modify `User.java` and forms
3. **Enable email verification** - Add email service
4. **Deploy** - Use Docker, AWS, or Heroku

---

## ❓ Need Help?

Check the full README.md for detailed documentation and API specifications.
