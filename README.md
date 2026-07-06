# TaskFlow Management System

A full-stack Task Management application developed using **Spring Boot**, **Angular**, and **PostgreSQL**. The system provides secure authentication, role-based access control, task approval workflows, AI-powered assistance, and Docker deployment.

---

## Features

### User & Department Management
- Manage people and departments
- CRUD operations for all entities
- Role-Based Access Control (RBAC)
- Team Manager, Department Head and Member dashboards

### Task Management
- Create, update and delete tasks
- Assign multiple members to a task
- Search and filter tasks
- Sort tasks by deadline
- View overdue and in-progress tasks
- Task approval workflow

### Authentication & Security
- JWT Authentication
- Two Factor Authentication (2FA)
- Refresh Tokens
- Session Expiration
- Password Reset via Email
- Login Attempt Limitation (temporary account lock)
- BCrypt password hashing

### AI Integration
- Generate AI work plans for tasks
- AI-powered task assignee recommendation based on:
  - previous completed tasks
  - workload
  - completion history
  - department
  - task similarity

### Audit Logging
- Record important system actions
- Track:
  - Create
  - Update
  - Delete
  - Task Approval
  - Task Rejection

### Deployment
- Docker
- Docker Compose
- Nginx for Angular frontend

---

## Technology Stack

### Backend
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- JWT
- OpenAI API

### Frontend
- Angular
- Angular Material
- RxJS

### DevOps
- Docker
- Docker Compose
- Nginx

---

## Architecture

The application follows a layered architecture:

```
Angular + Nginx
        │
        ▼
Spring Boot REST API
        │
        ▼
PostgreSQL Database

External Services
• OpenAI API
• Gmail SMTP
```

---

## Authentication Flow

```
Login
   │
   ▼
Verify credentials
   │
   ▼
2FA verification
   │
   ▼
Generate Access Token (1 minute)
Generate Refresh Token (7 days)
   │
   ▼
Access protected resources
```

---

## AI Features

### Work Plan Generation

Generate a concise implementation plan for any task using OpenAI.

### Smart Assignee Recommendation

Department Heads and Team Managers can receive AI recommendations for the best task assignee based on:

- department
- previous experience
- active workload
- completed tasks
- deadline performance

---

## Task Approval Workflow

```
TODO
   │
   ▼
IN_PROGRESS
   │
   ▼
PENDING_APPROVAL
   │
   ├── Approved
   ▼
DONE

   or

Rejected
   │
   ▼
IN_PROGRESS
```

---

## Docker

Start the application using Docker Compose:

```bash
docker compose up --build
```

Services:

- Angular Frontend
- Spring Boot Backend
- PostgreSQL

---

## Project Structure

```
demo_backend/
│
├── controller
├── service
├── repository
├── model
├── config
└── security

demo-app/
│
├── pages
├── services
├── guards
├── interceptors
└── shared
```

---

## Security Features

- JWT Authentication
- Refresh Tokens
- Two Factor Authentication
- Role-Based Authorization
- Login Attempt Limitation
- Password Hashing
- Password Reset
- Session Expiration

---

## Main Roles

### Team Manager

- Manage users
- Manage departments
- Manage tasks
- View audit log
- AI work plan
- AI assignee recommendation

### Department Head

- Manage department members
- Manage department tasks
- Approve or reject completed tasks
- AI work plan
- AI assignee recommendation

### Member

- View assigned tasks
- Update task progress
- Request task approval
- AI work plan

---

## Author

**Tomus Lidia**
