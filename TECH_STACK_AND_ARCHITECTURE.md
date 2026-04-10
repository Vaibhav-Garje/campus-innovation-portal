# 🚀 Campus Innovation Portal — Tech Stack & Workflow Architecture

## Overview

The **Campus Innovation Portal** is a full-stack web application that enables students to submit innovation projects, collaborate in teams, track milestones, and receive faculty approval. It is built using the **Spring Boot MVC** architecture with **Thymeleaf** server-side rendering and a **MySQL** relational database.

---

## 🛠️ Tech Stack

### Backend

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 4.0.3 |
| Web MVC | Spring Web (Spring MVC) | Bundled with Boot |
| Security | Spring Security 6 | Bundled with Boot |
| ORM / Data | Spring Data JPA + Hibernate | Bundled with Boot |
| Build Tool | Apache Maven | Via Maven Wrapper (`mvnw`) |
| Dev Tools | Spring Boot DevTools | Runtime (auto-reload) |

### Frontend (Server-Side Rendered)

| Layer | Technology |
|---|---|
| Template Engine | Thymeleaf 3 |
| Security Integration | `thymeleaf-extras-springsecurity6` |
| Styling | Custom CSS (embedded per template) |
| Scripting | Vanilla JavaScript (inline) |

### Database

| Aspect | Detail |
|---|---|
| RDBMS | MySQL 8+ |
| JDBC Driver | `mysql-connector-j` |
| Schema Management | Hibernate `ddl-auto=update` (auto-migrate) |
| Connection | `jdbc:mysql://localhost:3306/campusportal` |
| Port | 3306 (default MySQL) |

### Utilities / Code Generation

| Library | Purpose |
|---|---|
| Lombok | Boilerplate reduction (via annotation processor) |
| Jakarta Persistence | JPA annotations (`@Entity`, `@Column`, etc.) |

### Server

| Aspect | Detail |
|---|---|
| Embedded Server | Apache Tomcat (embedded via Spring Boot) |
| Application Port | `8081` |
| File Upload Limit | 10 MB per file / 10 MB per request |

---

## 🏗️ Application Architecture

The project follows the classic **MVC (Model-View-Controller)** pattern layered on top of Spring Boot's dependency injection container.

```
┌─────────────────────────────────────────────────────────────────┐
│                         Browser / Client                         │
└───────────────────────────────┬─────────────────────────────────┘
                                │  HTTP Request
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Spring Security Filter Chain                    │
│      (Authentication, Role-Based Authorization, CSRF off)        │
└───────────────────────────────┬─────────────────────────────────┘
                                │  Allowed Request
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Controllers (MVC Layer)                      │
│  AuthController  │  HomeController  │  ProjectController         │
│  TeamController  │  MilestoneController  │  VoteController       │
│  FacultyController                                               │
└───────────────────────────────┬─────────────────────────────────┘
                                │  Business Logic
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Service Layer                               │
│                    UserService                                   │
│     (implements UserDetailsService for Spring Security)          │
└───────────────────────────────┬─────────────────────────────────┘
                                │  DB Operations (JPA)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Repository Layer (JPA)                        │
│  UserRepository  │  ProjectRepository  │  TeamMemberRepository  │
│  MilestoneRepository  │  VoteRepository                         │
└───────────────────────────────┬─────────────────────────────────┘
                                │  SQL
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     MySQL 8+ Database                            │
│     Tables: users, projects, team_members, milestones, votes     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Data Model

### Entity Relationships

```
User
 ├── id, name, email, password, role, department, year, skills
 │
Project  (ManyToOne → User: created_by)
 ├── id, title, description, domain
 ├── status: PENDING | APPROVED | REJECTED
 ├── voteCount, progress (0–100)
 └── createdAt
 │
TeamMember  (ManyToOne → Project, ManyToOne → User)
 ├── status: PENDING | APPROVED | REJECTED
 ├── resumeFile (MEDIUMBLOB), resumeFileName
 └── contactEmail, contactPhone
 │
Milestone  (ManyToOne → Project)
 ├── description
 └── date
 │
Vote  (ManyToOne → Project, ManyToOne → User)
 └── (prevents duplicate votes per user per project)
```

### ER Summary

```
User ──< Project         (one user creates many projects)
User ──< TeamMember      (one user can send many join requests)
Project ──< TeamMember   (one project has many team members)
Project ──< Milestone    (one project has many milestones)
Project ──< Vote         (one project has many votes)
User ──< Vote            (one user can vote on many projects)
```

---

## 🔐 Security Architecture

Implemented via **Spring Security 6** (`SecurityConfig.java`):

| URL Pattern | Access |
|---|---|
| `/`, `/auth/**`, `/css/**`, `/js/**`, `/images/**`, `/projects/list` | **Public** (no login required) |
| `/faculty/**` | Requires role: `FACULTY` |
| `/admin/**` | Requires role: `ADMIN` |
| All other routes | Requires any authenticated user |

### Password Encryption
- **BCryptPasswordEncoder** — passwords are BCrypt-hashed before storage.

### Login Flow
1. User submits credentials at `/auth/login`.
2. `UserService` (implements `UserDetailsService`) loads the user by username from MySQL.
3. Spring Security validates the BCrypt-hashed password.
4. A **custom `AuthenticationSuccessHandler`** redirects based on role:
   - `ADMIN` → `/admin/dashboard`
   - `FACULTY` → `/faculty/dashboard`
   - `STUDENT` → `/dashboard`

### Logout
- POST to `/auth/logout` → redirects to `/auth/login`.

---

## 🌐 URL Routing Map

| Controller | Method | URL | Description |
|---|---|---|---|
| `AuthController` | GET | `/auth/login` | Login page |
| `AuthController` | GET | `/auth/register` | Registration page |
| `AuthController` | POST | `/auth/register` | Register new user |
| `HomeController` | GET | `/` | Landing page |
| `HomeController` | GET | `/dashboard` | Student dashboard |
| `ProjectController` | GET | `/projects/list` | Browse all projects |
| `ProjectController` | GET | `/submit` | Submit project form |
| `ProjectController` | POST | `/submit` | Create new project |
| `VoteController` | POST | `/vote/{projectId}` | Upvote a project |
| `TeamController` | GET | `/team/create` | Create team form |
| `TeamController` | POST | `/team/create` | Create team for a project |
| `TeamController` | GET | `/team/join/{projectId}` | Join request form |
| `TeamController` | POST | `/team/join/{projectId}` | Submit join application + resume |
| `TeamController` | GET | `/team/my-teams` | View my join requests ("My Syncs") |
| `TeamController` | GET | `/team/manage/{projectId}` | Owner manages join requests |
| `TeamController` | POST | `/team/approve/{memberId}` | Owner approves a join |
| `TeamController` | POST | `/team/reject/{memberId}` | Owner rejects a join |
| `TeamController` | GET | `/team/resume/{id}` | Download applicant resume |
| `MilestoneController` | GET | `/milestone/{projectId}` | View milestones (owner/member only) |
| `MilestoneController` | POST | `/milestone/add/{projectId}` | Add milestone (owner only) |
| `MilestoneController` | POST | `/milestone/progress/{projectId}` | Update progress % (owner only) |
| `FacultyController` | GET | `/faculty/dashboard` | Faculty overview of all projects |
| `FacultyController` | GET | `/faculty/projects` | Faculty list for approval |
| `FacultyController` | POST | `/faculty/approve/{id}` | Approve a project |
| `FacultyController` | POST | `/faculty/reject/{id}` | Reject a project |

---

## 🔄 Workflow Architecture

### 1. User Registration & Authentication Workflow

```
[User] → GET /auth/register → Fill form (name, email, password, role, dept, year, skills)
       → POST /auth/register → BCrypt hash password → Save to `users` table
       → Redirect to /auth/login

[User] → POST /auth/login → Spring Security validates → CustomSuccessHandler
       → FACULTY  → /faculty/dashboard
       → ADMIN    → /admin/dashboard
       → STUDENT  → /dashboard
```

---

### 2. Project Submission & Approval Workflow

```
[Student] → GET /submit → Fill form (title, description, domain)
          → POST /submit → Save to `projects` (status = PENDING, voteCount = 0)
          → Redirect to /projects/list

[Any User] → GET /projects/list → Browse + vote on projects

[Faculty] → GET /faculty/dashboard → See PENDING projects
          → POST /faculty/approve/{id} → status = APPROVED
          → POST /faculty/reject/{id}  → status = REJECTED
```

---

### 3. Team Collaboration Workflow

```
[Project Owner] → POST /team/create → Creates TeamMember (status = APPROVED)
                                      (owner becomes first approved member)

[Student]  → GET /projects/list → Finds a project to join
           → GET /team/join/{projectId} → Fill: resume upload, contact details
           → POST /team/join/{projectId} → Creates TeamMember (status = PENDING)
           → Redirected to /projects/list

[Project Owner] → GET /team/manage/{projectId} → Sees PENDING requests + resume download
               → POST /team/approve/{memberId} → status = APPROVED
               → POST /team/reject/{memberId}  → status = REJECTED

[Student]  → GET /team/my-teams ("My Syncs") → View status of all join requests
```

---

### 4. Milestone Tracking Workflow

```
[Milestone Access Gate]
  → Only Project Owner OR Approved Team Member can access /milestone/{projectId}
  → All others → Redirect to /projects/list with "Access Denied" error

[Project Owner]
  → POST /milestone/add/{projectId}       → Adds description + date milestone
  → POST /milestone/progress/{projectId}  → Updates progress % (0–100)

[Approved Member]
  → GET /milestone/{projectId} → View-only: see milestones + progress bar
```

---

### 5. Voting Workflow

```
[Authenticated Student] → POST /vote/{projectId}
  → VoteRepository checks if user already voted (prevents duplicate votes)
  → If not voted: increment project.voteCount + save Vote record
  → If already voted: show error/info message
```

---

## 📁 Project File Structure

```
campus-innovation-portal/
├── pom.xml                          # Maven dependencies
├── src/
│   └── main/
│       ├── java/com/campusportal/campusportal/
│       │   ├── CampusInnovationPortalApplication.java   # Spring Boot entry point
│       │   ├── config/
│       │   │   └── SecurityConfig.java                  # Security rules & login flow
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── HomeController.java
│       │   │   ├── ProjectController.java
│       │   │   ├── TeamController.java
│       │   │   ├── MilestoneController.java
│       │   │   ├── VoteController.java
│       │   │   └── FacultyController.java
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   ├── Project.java
│       │   │   ├── TeamMember.java
│       │   │   ├── Milestone.java
│       │   │   └── Vote.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── ProjectRepository.java
│       │   │   ├── TeamMemberRepository.java
│       │   │   ├── MilestoneRepository.java
│       │   │   └── VoteRepository.java
│       │   └── service/
│       │       └── UserService.java                     # UserDetailsService impl
│       └── resources/
│           ├── application.properties                   # DB config, port, upload settings
│           ├── static/                                  # CSS, JS, images
│           └── templates/
│               ├── index.html           # Landing page
│               ├── dashboard.html       # Student dashboard
│               ├── project-list.html    # Browse projects
│               ├── submit-project.html  # Submit project form
│               ├── milestone.html       # Milestone tracker
│               ├── auth/
│               │   ├── login.html
│               │   └── register.html
│               ├── faculty/
│               │   ├── faculty-dashboard.html
│               │   └── project-approval.html
│               ├── team/
│               │   ├── create.html
│               │   ├── join.html        # Join request form with resume upload
│               │   ├── my-teams.html    # "My Syncs" applicant view
│               │   └── manage.html      # Owner: approve/reject members
│               └── fragments/           # Reusable Thymeleaf fragments
```

---

## ⚙️ Configuration Summary (`application.properties`)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campusportal
spring.datasource.username=root
spring.datasource.password=1234
spring.jpa.hibernate.ddl-auto=update         # Auto-creates/updates tables
spring.jpa.show-sql=true                     # Prints SQL to console
server.port=8081                             # App runs on port 8081
spring.thymeleaf.cache=false                 # Hot-reload templates in dev
spring.servlet.multipart.max-file-size=10MB  # Resume upload limit
spring.servlet.multipart.max-request-size=10MB
```

---

## 👥 Role Hierarchy Summary

| Role | Dashboard | Submit Project | Vote | Join Team | Manage Team | Approve Projects |
|---|---|---|---|---|---|---|
| `STUDENT` | `/dashboard` | ✅ | ✅ | ✅ | ✅ (own projects) | ❌ |
| `FACULTY` | `/faculty/dashboard` | ✅ | ✅ | ✅ | ✅ (own projects) | ✅ |
| `ADMIN` | `/admin/dashboard` | ✅ | ✅ | ✅ | ✅ | ✅ |

---

*Generated: April 2026 — Campus Innovation Portal v0.0.1-SNAPSHOT*
