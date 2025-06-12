const loginBtn = document.getElementById('loginBtn');

loginBtn.addEventListener('click', () => {
    window.location.href = '/login';
})

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("registerForm");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const name = document.getElementById("name").value;
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;

        const params = new URLSearchParams({ name, email, password });

        try {
            const response = await fetch(`/api/users/sign-up?${params.toString()}`, {
                method: "GET"
            });

            const result = await response.text();

            if (response.ok) {
                localStorage.setItem("token", result);
                localStorage.setItem('email', email);
                window.location.href = "/";
            } else {
                alert(result);
            }
        } catch (error) {
            console.error("Error during registration:", error);
            alert("An error occurred. Please try again.");
        }
    });
});