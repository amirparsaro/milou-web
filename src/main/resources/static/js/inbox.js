const allLink = document.querySelector('.links p:nth-child(1)');
const unreadLink = document.querySelector('.links p:nth-child(2)');
const sentLink = document.querySelector('.links p:nth-child(3)');
const searchByCodeLink = document.querySelector('.links p:nth-child(4)');
const emailList = document.querySelector('.email-list');
const emailContent = document.querySelector('.email-content');
const composeBtn = document.getElementById('compose');
const logoutBtn = document.getElementById('logout');
const username = document.querySelector('.login h3');

async function markAsRead(message) {
    console.log(message.id);
    const token = localStorage.getItem('token');

    if (!token) {
        console.error("Authorization token not found.");
        return;
    }

    try {
        const response = await fetch(`/api/messages/markAsRead?messageCode=${encodeURIComponent(message.code)}`, {
            method: 'GET',
            headers: {
                'Authorization': token
            }
        });

        const result = await response.text();

        if (response.ok) {
            console.log("Marked as read:", result);
        } else {
            console.log("Failed to mark as read:", result);
        }
    } catch (error) {
        console.log("Error marking as read:", error);
    }
}


function createEmailItem(message) {
    const senderName = message.sender?.name || 'Unknown';
    let recipients = "";
    message.recipients.forEach((recipient, index) => {
        if (index > 0) recipients += ", ";
        recipients += recipient.recipient.name;
    });
    const title = message.title || 'No subject';
    const dateStr = message.date ? new Date(message.date).toLocaleDateString() : 'Unknown date';

    const div = document.createElement('div');
    div.classList.add('email-item');
    div.innerHTML = `<strong>${senderName}</strong><br>${title}`;

    div.addEventListener('click', () => {
        emailContent.innerHTML = `
  <div class="forward-container">
      <label for="forwardInput"><strong>Forward to:</strong></label>
      <input type="email" id="forwardInput" class="forward-email-field" placeholder="Enter email" />
      <button id="forwardSubmit" class="forward-button">Submit</button>
      <p id="forwardStatus" style="color: red; margin-top: 10px;"></p>
    </div>

    <h3>${title}</h3>
    <p><strong>From:</strong> ${senderName}</p>
    <p><strong>To:</strong> ${recipients}</p>
    <p><strong>Message:</strong><br>${message.body || ''}</p>
    <p><small>Sent at: ${dateStr}</small></p>
    <p><small>Code: ${message.code || ''}</small></p>

    <div class="reply-container">
      <label for="replyInput"><strong>Reply:</strong></label><br>
      <textarea id="replyInput" class="reply-text-area" placeholder="Type your reply here..."></textarea><br>
      <button id="replySubmit" class="reply-submit">Send Reply</button>
    </div>

    <p id="actionStatus" style="color: red; margin-top: 10px;"></p>
    <div id="replyContainer" style="margin-top: 20px;"></div>
  `;
        const forwardBtn = document.getElementById('forwardSubmit');
        const forwardInput = document.getElementById('forwardInput');
        const forwardStatus = document.getElementById('forwardStatus');
        const replyBtn = document.getElementById('replySubmit');
        const replyInput = document.getElementById('replyInput');
        const status = document.getElementById('actionStatus');
        const replyContainer = document.getElementById('replyContainer');

        forwardBtn.addEventListener('click', () => {
            const forwardToEmail = forwardInput.value.trim();

            if (!forwardToEmail) {
                forwardStatus.textContent = "Please enter a valid email address.";
                return;
            }

            forwardStatus.textContent = "";

            fetch('api/messages/create/forward?' + new URLSearchParams({
                recipientEmails: forwardToEmail,
                messageCode: message.code
            }), {
                method: 'GET',
                headers: {
                    'Authorization': `${localStorage.getItem('token') || ''}`,
                    'Content-Type': 'application/json'
                }
            })
                .then(response => response.text())
                .then(data => {
                    if (data && data.length === 36) {
                        forwardStatus.textContent = "";
                    } else {
                        forwardStatus.style.color = "red";
                        forwardStatus.textContent = data;
                    }
                })
                .catch(error => {
                    forwardStatus.style.color = "red";
                    forwardStatus.textContent = "Network error: " + error.message;
                });
        });

        replyBtn.addEventListener('click', () => {
            const replyBody = replyInput.value.trim();

            if (!replyBody) {
                status.textContent = "Reply cannot be empty.";
                return;
            }

            status.textContent = "";

            console.log("this works well1");
            fetch('api/messages/create/reply?' + new URLSearchParams({
                messageCode: message.code,
                body: replyBody
            }), {
                method: 'GET',
                headers: {
                    'Authorization': `${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json'
                }
            })
                .then(response => response.text())
                .then(data => {
                    if (data && data.length === 36) {
                        status.style.color = "green";
                        status.textContent = "Reply sent successfully!";
                        replyInput.value = "";
                    } else {
                        status.style.color = "red";
                        status.textContent = data;
                    }
                })
                .catch(error => {
                    status.style.color = "red";
                    status.textContent = "Network error: " + error.message;
                });
        });
        console.log("this works well2");

        markAsRead(message)
    });

    return div;
}

async function fetchMessages(endpoint, params = '') {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login';
        return [];
    }

    try {
        const url = `/api/messages/get/${endpoint}${params}`;
        const res = await fetch(url, {
            headers: {
                'Authorization': token
            }
        });

        if (!res.ok) {
            const errorText = await res.text();
            alert('Error: ' + errorText);
            return [];
        }
        return await res.json();
    } catch (err) {
        alert('Network error.');
        console.error(err);
        return [];
    }
}

function clearEmails() {
    emailList.innerHTML = '';
    emailContent.innerHTML = '<h3>Subject</h3><p>Click on an email to see the content here.</p>';
}

async function loadAllMessages() {
    clearEmails();
    const messages = await fetchMessages('all');
    messages.forEach(m => emailList.appendChild(createEmailItem(m)));
}

async function loadUnreadMessages() {
    clearEmails();
    const messages = await fetchMessages('unread');
    messages.forEach(m => emailList.appendChild(createEmailItem(m)));
}

async function loadSentMessages() {
    clearEmails();
    const messages = await fetchMessages('sent');
    messages.forEach(m => emailList.appendChild(createEmailItem(m)));
}

async function searchByCode() {
    clearEmails();
    const code = prompt('Enter message code:');
    if (!code) return;
    const token = localStorage.getItem('token');
    if (!token) {
        alert('Please log in first.');
        window.location.href = '/login';
        return;
    }

    try {
        const res = await fetch(`/api/messages/get/by-code?code=${encodeURIComponent(code)}`, {
            headers: { 'Authorization': token }
        });

        if (!res.ok) {
            const errorText = await res.text();
            alert('Error: ' + errorText);
            return;
        }
        const message = await res.json();
        emailList.appendChild(createEmailItem(message));
    } catch (e) {
        alert('Network error.');
        console.error(e);
    }
}

allLink.addEventListener('click', loadAllMessages);
unreadLink.addEventListener('click', loadUnreadMessages);
sentLink.addEventListener('click', loadSentMessages);
searchByCodeLink.addEventListener('click', searchByCode);
composeBtn.addEventListener('click', () => {
    window.location.href = '/compose';
});

logoutBtn.addEventListener('click', async () => {
    const token = localStorage.getItem('token');

    if (!token) {
        alert('You are not logged in.');
        window.location.href = '/login';
        return;
    }

    try {
        const res = await fetch('/api/users/log-out', {
            method: 'GET',
            headers: {
                'Authorization': token
            }
        });

        if (!res.ok) {
            const errorText = await res.text();
            alert('Logout failed: ' + errorText);
            return;
        }

        localStorage.removeItem('token');
        window.location.href = '/login';
    } catch (err) {
        alert('Network error during logout.');
        console.error(err);
    }
});

window.addEventListener('DOMContentLoaded', () => {
    if (localStorage.getItem('token')) {
        loadAllMessages();

        try {
            const email = localStorage.getItem('email');
            if (username && email) {
                username.textContent = email;
            }
        } catch (err) {
            username.textContent = 'unable to load username';
        }

    } else {
        window.location.href = '/login';
    }
});