document.addEventListener('DOMContentLoaded', () => {
    console.log('Script loaded');
    console.log('Token:', localStorage.getItem('token'));

    document.getElementById('emailForm').addEventListener('submit', async function (e) {
        e.preventDefault();

        const toInput = document.getElementById('to').value.trim();
        const subject = document.getElementById('subject').value.trim();
        const message = document.getElementById('message').value.trim();

        const recipientEmails = toInput.split(',').map(email => email.trim()).filter(email => email !== '');

        const token = localStorage.getItem('token');

        if (!token) {
            window.location.href = '/login';
            return;
        }

        const urlParams = new URLSearchParams();
        recipientEmails.forEach(email => urlParams.append('recipientEmails', email));
        urlParams.append('title', subject);
        urlParams.append('body', message);

        const url = 'api/messages/create/message?' + urlParams.toString();

        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Authorization': token
                }
            });

            if (response.ok) {
                const messageCode = await response.text();
                alert('Message sent successfully! Code: ' + messageCode);
                this.reset();
            } else {
                const error = await response.text();
                alert('Error: ' + error);
            }
        } catch (error) {
            alert('Network or server error: ' + error.message);
        }
    });
});