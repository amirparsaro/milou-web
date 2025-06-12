# Milou Email

Milou Email is a full-featured web-based email platform that allows users to securely register, log in, send, receive, and manage emails through an interface.

## Features

- User Authentication  
  - Register new accounts  
  - Secure login/logout

- Inbox Management  
  - Email previews listed in a preview panel  
  - Read full email content by clicking on a preview

- Sidebar Navigation  
  - Filter emails by:  
    - All  
    - Unread  
    - Sent  
  - Search for emails by their code title

- Email Viewing & Interaction  
  - View emails in a reading section  
  - Reply to emails  
  - Forward emails to other users

- Compose Emails  
  - `/compose` page to write and send new emails

## Pages

| Route        | Description                              |
|--------------|------------------------------------------|
| `/login`     | Login page for existing users            |
| `/register`  | Sign up page for new users               |
| `/`          | Email dashboard with preview and reading pane |
| `/compose`   | Compose a new email                      |

## Screenshots
`/` (Inbox)
![Inbox Page](screenshots/inbox.png)

`/compose` (Compose)
![Compose Page](screenshots/compose.png)

`/login` (Login)
![login Page](screenshots/login.png)

`/register` (Register)
![register Page](screenshots/register.png)

## Project Structure

```text
Milou-web/
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── milou/
│   │   │           └── spring_boot/
│   │   │               ├── Application.java
│   │   │               ├── SessionFactoryManager.java
│   │   │               ├── controller/
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── MessageController.java
│   │   │               │   ├── UserController.java
│   │   │               │   └── WebController.java
│   │   │               ├── exception/
│   │   │               │   ├── InvalidCredentialsException.java
│   │   │               │   ├── InvalidRegistrationException.java
│   │   │               │   ├── MessageAlreadyExistsException.java
│   │   │               │   ├── MessageNotFoundException.java
│   │   │               │   ├── RecipientAlreadyExistsException.java
│   │   │               │   ├── RecipientNotFoundException.java
│   │   │               │   ├── UserAlreadyExistsException.java
│   │   │               │   └── UserNotFoundException.java
│   │   │               ├── model/
│   │   │               │   ├── Message.java
│   │   │               │   ├── Recipient.java
│   │   │               │   └── User.java
│   │   │               └── service/
│   │   │                   ├── AuthService.java
│   │   │                   ├── MessageService.java
│   │   │                   ├── RecipientService.java
│   │   │                   └── UserService.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   ├── hibernate.cfg.xml
│   │   │   ├── log4j2.properties
│   │   │   ├── static/
│   │   │   │   ├── assets/
│   │   │   │   │   └── milou.png
│   │   │   │   ├── compose.html
│   │   │   │   ├── css/
│   │   │   │   │   ├── compose-new.css
│   │   │   │   │   ├── inbox-new.css
│   │   │   │   │   ├── login-new.css
│   │   │   │   │   └── register-new.css
│   │   │   │   ├── inbox.html
│   │   │   │   ├── js/
│   │   │   │   │   ├── compose.js
│   │   │   │   │   ├── inbox.js
│   │   │   │   │   ├── login.js
│   │   │   │   │   └── register.js
│   │   │   │   ├── login.html
│   │   │   │   └── register.html
│   │   │   ├── template.xml.cfg.hibernate
│   │   │   └── templates/
│   │   └── sql/
│   │       └── tables.sql
│   └── test/
│       └── java/
│           └── com/
│               └── milou/
│                   └── spring_boot/
│                       └── ApplicationTests.java
```
## Tech Stack

- **Frontend:** HTML, CSS, JavaScript  
- **Backend:** Java (Spring Boot)  
- **Database:** MySQL (using Hibernate ORM for Java integration)  

