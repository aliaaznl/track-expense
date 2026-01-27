const container = document.getElementById('container');
const signUpBtn = document.getElementById('signUpBtn');
const signInBtn = document.getElementById('signInBtn');

// Animation Logic
signUpBtn.addEventListener('click', () => {
    container.classList.add("right-panel-active");
});

signInBtn.addEventListener('click', () => {
    container.classList.remove("right-panel-active");
});

// API Base URL
const API_BASE_URL = '';

// Custom Dialog Functions
function showAlertDialog(message, title = 'Alert', callback) {
    document.getElementById('alertTitle').textContent = title;
    document.getElementById('alertMessage').textContent = message;
    document.getElementById('alertDialog').style.display = 'flex';
    if (callback) {
        const okBtn = document.querySelector('#alertDialog .dialog-btn-primary');
        okBtn.onclick = () => {
            closeAlertDialog();
            callback();
        };
    }
}

function closeAlertDialog() {
    document.getElementById('alertDialog').style.display = 'none';
}

// Close dialog on backdrop click
setTimeout(() => {
    const alertDialog = document.getElementById('alertDialog');
    if (alertDialog) {
        alertDialog.addEventListener('click', (e) => {
            if (e.target.id === 'alertDialog') {
                closeAlertDialog();
            }
        });
    }
}, 0);

// Forms
const registerForm = document.querySelector('.sign-up-container form');
const loginForm = document.querySelector('.sign-in-container form');

// HANDLE REGISTRATION
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const nameInput = registerForm.querySelector('input[placeholder="Name"]');
    const emailInput = registerForm.querySelector('input[name="email"]');
    const passwordInput = document.getElementById('signup-password');

    const userData = {
        username: nameInput.value,
        email: emailInput.value,
        password: passwordInput.value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            showAlertDialog("Registration Successful! Please Sign In.", "Success");
            container.classList.remove("right-panel-active");
            registerForm.reset();
        } else {
            const errorText = await response.text();
            showAlertDialog("Registration Failed: " + errorText, "Registration Failed");
        }
    } catch (error) {
        console.error("Error:", error);
        showAlertDialog("Cannot connect to Backend. Is the server running?", "Connection Error");
    }
});

// HANDLE LOGIN
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const emailInput = loginForm.querySelector('input[name="email"]');
    const passwordInput = document.getElementById('signin-password');

    // IMPORTANT: Send 'email' not 'username'
    const loginData = {
        email: emailInput.value,
        password: passwordInput.value
    };

    console.log('Attempting login with:', loginData.email);

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(loginData)
        });

        console.log('Login response status:', response.status);

        if (response.ok) {
            const data = await response.json();
            console.log('Token received:', data.token ? 'Yes (' + data.token.length + ' chars)' : 'No');
            
            // Save token
            localStorage.setItem('jwtToken', data.token);
            
            // Verify it was saved
            const savedToken = localStorage.getItem('jwtToken');
            console.log('Token saved to localStorage:', savedToken ? 'Yes' : 'No');
            
            showAlertDialog("Login Successful!", "Success");
            setTimeout(() => {
                window.location.href = "dashboard.html";
            }, 500);
        } else {
            const errorText = await response.text();
            console.error('Login failed:', response.status, errorText);
            showAlertDialog("Login Failed. Please check your email/password.", "Login Failed");
        }
    } catch (error) {
        console.error("Error:", error);
        showAlertDialog("Cannot connect to Backend. Is the server running?", "Connection Error");
    }
});

// If already logged in, redirect to dashboard
const existingToken = localStorage.getItem('jwtToken');
if (existingToken && !token) {
    console.log('Existing token found, redirecting...');
    window.location.href = '/dashboard.html';
}
