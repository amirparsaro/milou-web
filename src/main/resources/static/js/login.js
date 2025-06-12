const registerBtn = document.getElementById('registerBtn');

registerBtn.addEventListener('click', () => {
    window.location.href = '/register';
})

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');

    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        // Create URL with query parameters
        const params = new URLSearchParams({ email: email, password: password });
        const url = `/api/users/log-in?${params.toString()}`;

        try {
            const response = await fetch(url, {
                method: 'GET'
            });

            const result = await response.text();

            if (response.ok) {
                localStorage.setItem('token', result);
                localStorage.setItem('email', email);
                window.location.href = '/';
            } else {
                alert(result);
            }
        } catch (error) {
            console.error('Login error:', error);
            alert('An error occurred while trying to log in.');
        }
    });
});
