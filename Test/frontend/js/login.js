document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        const data = await response.json();

        if (response.ok) {
            // Store token in localStorage
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify(data.user));
            
            // Redirect to dashboard or home page
            window.location.href = 'dashboard.html';
        } else {
            showError(data.message || 'Login failed');
        }
    } catch (error) {
        showError('Connection error: ' + error.message);
    }
});

function showError(message) {
    // Create and display error message
    const errorDiv = document.createElement('div');
    errorDiv.style.cssText = `
        background-color: #f8d7da;
        color: #721c24;
        padding: 12px;
        border-radius: 5px;
        margin-bottom: 20px;
        border: 1px solid #f5c6cb;
    `;
    errorDiv.textContent = message;
    
    const form = document.getElementById('loginForm');
    form.parentElement.insertBefore(errorDiv, form);
    
    setTimeout(() => errorDiv.remove(), 5000);
}
