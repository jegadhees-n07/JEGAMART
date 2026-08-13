document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const fullname = document.getElementById('fullname').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    // Validate password match
    if (password !== confirmPassword) {
        showError('Passwords do not match');
        return;
    }

    // Validate password strength
    if (password.length < 6) {
        showError('Password must be at least 6 characters');
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                fullname: fullname,
                email: email,
                password: password
            })
        });

        const data = await response.json();

        if (response.ok) {
            showSuccess('Registration successful! Redirecting to login...');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
        } else {
            showError(data.message || 'Registration failed');
        }
    } catch (error) {
        showError('Connection error: ' + error.message);
    }
});

function showError(message) {
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
    
    const form = document.getElementById('registerForm');
    form.parentElement.insertBefore(errorDiv, form);
    
    setTimeout(() => errorDiv.remove(), 5000);
}

function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.style.cssText = `
        background-color: #d4edda;
        color: #155724;
        padding: 12px;
        border-radius: 5px;
        margin-bottom: 20px;
        border: 1px solid #c3e6cb;
    `;
    successDiv.textContent = message;
    
    const form = document.getElementById('registerForm');
    form.parentElement.insertBefore(successDiv, form);
}
