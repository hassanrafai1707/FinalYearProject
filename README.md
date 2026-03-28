# QPGen - Question Paper Generation System

A web-based automated question paper generation and review system designed for educational institutions. QPGen streamlines the entire process of creating, reviewing, and approving examination papers, reducing manual effort from hours to minutes.

## 🎯 Project Overview

QPGen is a full-stack web application that automates the question paper creation workflow. Teachers can maintain a structured question bank, generate balanced question papers based on cognitive levels and marks distribution, and submit them for review. Supervisors can review, provide feedback, and approve papers - all through an intuitive web interface.

### Key Features

- **🔐 Role-Based Authentication**: Secure login with JWT tokens for four user roles (Teacher, Supervisor, Student, Admin)
- **📚 Question Bank Management**: Store questions with tags for subject, course outcomes (CO), Bloom's Taxonomy levels (Remembrance/Understanding/Application), and marks (2/4/6)
- **🤖 Automated Paper Generation**: Generate balanced question papers based on configurable parameters - cognitive level distribution, marks allocation, and CO mapping
- **✅ Review & Approval Workflow**: Complete digital workflow where teachers submit papers and supervisors review, comment, and approve
- **📄 PDF Export**: Download approved question papers as professionally formatted PDFs using OpenPDF
- **📧 Email Verification**: Two-factor account verification via email with OTP and confirmation link using JavaMailSender
- **⚡ Redis Caching**: Session management and OTP storage with 10-minute TTL for email verification

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION LAYER                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Thymeleaf   │  │   HTML/CSS   │  │  JavaScript  │  │  Dark Mode   │   │
│  │   Templates  │  │              │  │    (API)     │  │    Toggle    │   │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        APPLICATION LOGIC LAYER                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         CONTROLLERS                                  │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │   │
│  │  │  Admin   │ │ Teacher  │ │Supervisor│ │ Student  │ │   Auth   │ │   │
│  │  │RestCtrl  │ │RestCtrl  │ │RestCtrl  │ │RestCtrl  │ │RestCtrl  │ │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        SECURITY LAYER                               │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│  │  │  JWT Filter  │  │SecurityConfig│  │ Authentication│             │   │
│  │  │              │  │              │  │   Manager    │             │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘             │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                          SERVICES                                    │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐      │   │
│  │  │UserService │ │QuestionSvc│ │QuestionPaper│ │JwtService  │      │   │
│  │  │            │ │           │ │   Service   │ │            │      │   │
│  │  └────────────┘ └────────────┘ └────────────┘ └────────────┘      │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐                     │   │
│  │  │RedisService│ │Conformation│ │MyUserDetail│                     │   │
│  │  │            │ │  Service   │ │  Services  │                     │   │
│  │  └────────────┘ └────────────┘ └────────────┘                     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          DATA STORAGE LAYER                                 │
│  ┌────────────────────────────┐  ┌────────────────────────────┐           │
│  │        PostgreSQL          │  │          Redis             │           │
│  │  ┌──────────────────────┐  │  │  ┌──────────────────────┐  │           │
│  │  │       Users          │  │  │  │  Email Verification  │  │           │
│  │  │     Questions        │  │  │  │       Tokens         │  │           │
│  │  │   QuestionPapers     │  │  │  │   (10 min TTL)       │  │           │
│  │  └──────────────────────┘  │  │  └──────────────────────┘  │           │
│  └────────────────────────────┘  └────────────────────────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 👥 User Roles & Workflow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           USER ROLES & PERMISSIONS                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐ │
│  │   TEACHER   │    │ SUPERVISOR  │    │   STUDENT   │    │    ADMIN    │ │
│  │ (ROLE_      │    │ (ROLE_      │    │ (ROLE_      │    │ (ROLE_      │ │
│  │  TEACHER)   │    │ SUPERVISOR) │    │  STUDENT)   │    │   ADMIN)    │ │
│  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘ │
│       │                  │                  │                  │           │
│       ▼                  ▼                  ▼                  ▼           │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐ │
│  │• Create Ques│    │• View Papers│    │• Browse Ques│    │• Manage All │ │
│  │• Manage Bank│    │• Add Comment│    │• View Papers│    │  Users      │ │
│  │• Generate   │    │• Approve/   │    │• Filter by  │    │• Assign     │ │
│  │  Papers     │    │  Reject     │    │  Subject/CO │    │  Roles      │ │
│  │• Submit for │    │• Track      │    │• View       │    │• Batch Ops  │ │
│  │  Review     │    │  History    │    │  Approved   │    │• Audit Logs │ │
│  │• Download   │    │• Download   │    │  Papers     │    │             │ │
│  │  PDF        │    │  PDF        │    │             │    │             │ │
│  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                        COMPLETE WORKFLOW SEQUENCE                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   TEACHER                    SUPERVISOR                    SYSTEM           │
│      │                            │                           │             │
│      │ 1. Create Questions        │                           │             │
│      ├───────────────────────────►│                           │             │
│      │   (Store in Question Bank) │                           │             │
│      │                            │                           │             │
│      │ 2. Generate Paper          │                           │             │
│      │   (Select Subject, COs,    │                           │             │
│      │    Cognitive Levels, Marks)│                           │             │
│      │                            │                           │             │
│      │ 3. Submit for Review       │                           │             │
│      ├───────────────────────────►│                           │             │
│      │                            │                           │             │
│      │                            │ 4. Review Paper          │             │
│      │                            │   (Add Question-wise     │             │
│      │                            │    Comments)             │             │
│      │                            │                           │             │
│      │                            │ 5. Approve/Reject        │             │
│      │                            ├──────────────────────────►│             │
│      │                            │                           │             │
│      │ 6. Receive Feedback        │                           │             │
│      │◄───────────────────────────┤                           │             │
│      │   (If Rejected, Iterate)   │                           │             │
│      │                            │                           │             │
│      │ 7. Download PDF (If        │                           │             │
│      │    Approved)               │                           │             │
│      │◄───────────────────────────┼───────────────────────────┤             │
│      │                            │                           │             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 💻 Technology Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17+ | Core programming language |
| Spring Boot | 2.7.0 | Application framework |
| Spring Security | - | Authentication & authorization |
| Spring Data JPA | - | ORM and database operations |
| PostgreSQL | 13+ | Primary database |
| Redis | 6+ | Session & token storage |
| JWT (JJWT) | 0.12.6 | Token-based authentication |
| OpenPDF | 1.3.30 | PDF generation |
| Maven | 3.6+ | Build automation |

### Frontend
| Technology | Purpose |
|------------|---------|
| HTML5 | Structure |
| CSS3 | Styling (responsive design, dark mode) |
| JavaScript | Client-side logic, API integration |
| Thymeleaf | Server-side template rendering |

### Development & Deployment
- **IDE**: IntelliJ IDEA / VS Code
- **Version Control**: Git
- **API Testing**: Postman
- **Containerization**: Docker & Docker Compose
- **Database Tools**: pgAdmin, Redis CLI

## 📊 Database Schema

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DATABASE SCHEMA                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐       │
│  │      users      │     │    questions    │     │  question_papers│       │
│  ├─────────────────┤     ├─────────────────┤     ├─────────────────┤       │
│  │ id (PK)         │     │ id (PK)         │     │ id (PK)         │       │
│  │ email           │◄────│ created_by (FK) │     │ exam_title      │       │
│  │ password        │     │ subject_code    │     │ approved (bool) │       │
│  │ name            │     │ subject_name    │     │ comment         │       │
│  │ role            │     │ mapped_co       │     │ generated_by(FK)│──────►│
│  │ is_enable       │     │ cognitive_level │     │ approved_by(FK) │──────►│
│  │ locked          │     │ question_body   │     │ fingerprint     │       │
│  │ expired         │     │ question_marks  │     └─────────────────┘       │
│  │ last_login      │     │ in_use          │              │                 │
│  └─────────────────┘     │ question_title  │              │                 │
│         │                └─────────────────┘              │                 │
│         │                         │                      │                 │
│         │                         │                      │                 │
│         │         ┌───────────────┴──────────────────────┘                 │
│         │         │                                                         │
│         │         ▼                                                         │
│         │    ┌─────────────────────────────────────┐                       │
│         └───►│      question_paper_questions       │                       │
│              │  (Junction Table - Many-to-Many)    │                       │
│              ├─────────────────────────────────────┤                       │
│              │ question_paper_id (FK)              │                       │
│              │ question_id (FK)                    │                       │
│              └─────────────────────────────────────┘                       │
│                                                                             │
│  ┌─────────────────┐                                                        │
│  │   redis_store   │ (In-memory - No SQL Schema)                           │
│  ├─────────────────┤                                                        │
│  │ Key: email      │                                                        │
│  │ Value: JSON {   │                                                        │
│  │   token, otp,   │                                                        │
│  │   user details  │                                                        │
│  │ }               │                                                        │
│  │ TTL: 10 minutes │                                                        │
│  └─────────────────┘                                                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 🔐 Security Implementation

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        SECURITY ARCHITECTURE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. USER REGISTRATION                                                       │
│     ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐          │
│     │ Register │───►│ Password │───►│  Save to │───►│ Send     │          │
│     │          │    │  BCrypt  │    │   DB     │    │ Email    │          │
│     └──────────┘    │  Hash    │    │ (Disabled│    │ with     │          │
│                     └──────────┘    │  Account)│    │ OTP+Link │          │
│                                     └──────────┘    └──────────┘          │
│                                                           │                 │
│  2. EMAIL VERIFICATION                                     ▼                 │
│     ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐          │
│     │ User     │───►│ Verify   │───►│ Check    │───►│ Enable   │          │
│     │ Clicks   │    │ Token &  │    │ Redis    │    │ Account  │          │
│     │ Link     │    │ OTP      │    │ Store    │    │          │          │
│     └──────────┘    └──────────┘    └──────────┘    └──────────┘          │
│                                                                             │
│  3. AUTHENTICATION                                                          │
│     ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐          │
│     │ Login    │───►│ Auth     │───►│ Generate │───►│ Return   │          │
│     │ Request  │    │ Manager  │    │ JWT      │    │ Token    │          │
│     └──────────┘    └──────────┘    └──────────┘    └──────────┘          │
│                                                                             │
│  4. REQUEST AUTHORIZATION                                                   │
│     ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐          │
│     │ Request  │───►│ JWT      │───►│ Validate │───►│ Extract  │          │
│     │ + Token  │    │ Filter   │    │ Signature│    │ Role     │          │
│     └──────────┘    └──────────┘    └──────────┘    └──────────┘          │
│                                                           │                 │
│                                                           ▼                 │
│                                          ┌──────────────────────────┐      │
│                                          │ Role-Based Authorization│      │
│                                          │ @PreAuthorize("hasRole")│      │
│                                          └──────────────────────────┘      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 🚀 Complete API Endpoints

### Authentication Endpoints (3)

| # | Method | Endpoint | Description | Request Body |
|---|--------|----------|-------------|--------------|
| 1 | POST | `/api/v1/auth/register` | Register new user | `{"name": "John Doe", "email": "user@example.com", "password": "123456789"}` |
| 2 | POST | `/api/v1/auth/login` | Login & get JWT | `{"email": "user@example.com", "password": "123456789"}` |
| 3 | POST | `/api/v1/auth/confirm` | Verify email | Query: `?token=xyz&email=user@example.com`<br>Body: `{"otp": 8908}` |

### Admin Endpoints (28)

| # | Method | Endpoint | Description | Parameters/Body |
|---|--------|----------|-------------|-----------------|
| 4 | GET | `/api/v1/admin/user/id/{id}` | Get user by ID | Path: `id` |
| 5 | GET | `/api/v1/admin/user/email/{email}` | Get user by email | Path: `email` |
| 6 | GET | `/api/v1/admin/users/role/{role}` | Get users by role | Path: `role` |
| 7 | GET | `/api/v1/admin/users/role/{role}/paged` | Get users by role (paged) | Path: `role`, Query: `pageNo`, `size` |
| 8 | GET | `/api/v1/admin/users` | Get all users | None |
| 9 | GET | `/api/v1/admin/users/paged` | Get all users (paged) | Query: `pageNo`, `size` |
| 10 | DELETE | `/api/v1/admin/user/email` | Delete user by email | `{"email": "user@example.com", "adminPassword": "admin123"}` |
| 11 | DELETE | `/api/v1/admin/user/id` | Delete user by ID | `{"id": 123, "adminPassword": "admin123"}` |
| 12 | DELETE | `/api/v1/admin/users/ids` | Batch delete by IDs | `{"ids": [123, 456], "adminPassword": "admin123"}` |
| 13 | DELETE | `/api/v1/admin/users/emails` | Batch delete by emails | `{"emails": ["user1@example.com"], "adminPassword": "admin123"}` |
| 14 | PATCH | `/api/v1/admin/suspend/user/id` | Suspend user by ID | `{"id": 123, "adminPassword": "admin123"}` |
| 15 | PATCH | `/api/v1/admin/unsuspend/user/id` | Unsuspend user by ID | `{"id": 123, "adminPassword": "admin123"}` |
| 16 | PATCH | `/api/v1/admin/suspend/user/email` | Suspend user by email | `{"email": "user@example.com", "adminPassword": "admin123"}` |
| 17 | PATCH | `/api/v1/admin/unsuspend/user/email` | Unsuspend user by email | `{"email": "user@example.com", "adminPassword": "admin123"}` |
| 18 | PATCH | `/api/v1/admin/update/user/password/email` | Update password by email | `{"email": "user@example.com", "password": "newPass", "adminPassword": "admin123"}` |
| 19 | PATCH | `/api/v1/admin/update/user/password/id` | Update password by ID | `{"id": 123, "password": "newPass", "adminPassword": "admin123"}` |
| 20 | PATCH | `/api/v1/admin/update/user/role/id` | Update role by ID | `{"role": "ROLE_TEACHER", "id": 123, "password": "admin123"}` |
| 21 | PATCH | `/api/v1/admin/update/user/role/email` | Update role by email | `{"email": "user@example.com", "role": "ROLE_TEACHER", "password": "admin123"}` |
| 22 | PATCH | `/api/v1/admin/update/questionsPapers/generatedBy/email` | Transfer paper ownership by email | `{"replaceEmail": "new@example.com", "originalEmail": "old@example.com", "password": "admin123"}` |
| 23 | PATCH | `/api/v1/admin/update/questionsPapers/generatedBy/id` | Transfer paper ownership by ID | `{"replaceID": 2, "originalID": 1, "password": "admin123"}` |
| 24 | PATCH | `/api/v1/admin/update/questionsPapers/approvedBy/email` | Transfer approval by email | `{"replaceEmail": "new@example.com", "originalEmail": "old@example.com", "password": "admin123"}` |
| 25 | PATCH | `/api/v1/admin/update/questionsPapers/approvedBy/id` | Transfer approval by ID | `{"replaceID": 2, "originalID": 1, "password": "admin123"}` |
| 26 | PATCH | `/api/v1/admin/update/questions/createdBy/email` | Transfer question ownership by email | `{"replaceEmail": "new@example.com", "originalEmail": "old@example.com", "password": "admin123"}` |
| 27 | PATCH | `/api/v1/admin/update/questions/createdBy/id` | Transfer question ownership by ID | `{"replaceID": 2, "originalID": 1, "password": "admin123"}` |
| 28 | PATCH | `/api/v1/admin/update/my/email` | Update own email | `{"newEmail": "admin@example.com"}` |
| 29 | PATCH | `/api/v1/admin/update/my/password` | Update own password | `{"password": "newPass123"}` |
| 30 | POST | `/api/v1/admin/logout` | Logout | None |
| 31 | GET | `/api/v1/admin/test` | Test endpoint | None |

### Student Endpoints (19)

| # | Method | Endpoint | Description | Parameters |
|---|--------|----------|-------------|------------|
| 32 | GET | `/api/v1/student/questions` | Get all questions | None |
| 33 | GET | `/api/v1/student/questions/paged` | Get all questions (paged) | Query: `pageNo`, `size` |
| 34 | GET | `/api/v1/student/question/id` | Get question by ID | Query: `id` |
| 35 | GET | `/api/v1/student/questions/subjectCode` | Filter by subject code | Query: `subjectCode` |
| 36 | GET | `/api/v1/student/questions/subjectCode/pagged` | Filter by subject code (paged) | Query: `subjectCode`, `pageNo`, `size` |
| 37 | GET | `/api/v1/student/questions/subjectCode/mappedCO` | Filter by subject code & CO | Query: `subjectCode`, `mappedCO` |
| 38 | GET | `/api/v1/student/questions/subjectCode/mappedCO/pagged` | Filter by subject code & CO (paged) | Query: `subjectCode`, `mappedCO`, `pageNo`, `size` |
| 39 | GET | `/api/v1/student/questions/subjectCode/mappedCO/cognitiveLevel` | Filter by subject code, CO & cognitive level | Query: `subjectCode`, `mappedCO`, `cognitiveLevel` |
| 40 | GET | `/api/v1/student/questions/subjectCode/mappedCO/cognitiveLevel/pagged` | Filter (paged) | Query: `subjectCode`, `mappedCO`, `cognitiveLevel`, `pageNo`, `size` |
| 41 | GET | `/api/v1/student/questions/subjectName` | Filter by subject name | Query: `subjectName` |
| 42 | GET | `/api/v1/student/questions/subjectName/pagged` | Filter by subject name (paged) | Query: `subjectName`, `pageNo`, `size` |
| 43 | GET | `/api/v1/student/questions/subjectName/mappedCO` | Filter by subject name & CO | Query: `subjectName`, `mappedCO` |
| 44 | GET | `/api/v1/student/questions/subjectName/mappedCO/pagged` | Filter by subject name & CO (paged) | Query: `subjectName`, `mappedCO`, `pageNo`, `size` |
| 45 | GET | `/api/v1/student/questions/subjectName/mappedCO/cognitiveLevel` | Filter by subject name, CO & cognitive level | Query: `subjectName`, `mappedCO`, `cognitiveLevel` |
| 46 | GET | `/api/v1/student/questions/subjectName/mappedCO/cognitiveLevel/pagged` | Filter (paged) | Query: `subjectName`, `mappedCO`, `cognitiveLevel`, `pageNo`, `size` |
| 47 | PATCH | `/api/v1/student/update/user/email` | Update own email | `{"email": "newemail@example.com"}` |
| 48 | PATCH | `/api/v1/student/update/user/password` | Update own password | `{"password": "newPass123"}` |
| 49 | POST | `/api/v1/student/logout` | Logout | None |
| 50 | GET | `/api/v1/student/test` | Test endpoint | None |

### Supervisor Endpoints (41)

| # | Method | Endpoint | Description | Parameters |
|---|--------|----------|-------------|------------|
| 51 | GET | `/api/v1/supervisor/questions` | Get all questions | None |
| 52 | GET | `/api/v1/supervisor/questions/paged` | Get all questions (paged) | Query: `pageNo`, `size` |
| 53 | GET | `/api/v1/supervisor/question/id` | Get question by ID | Query: `id` |
| 54 | GET | `/api/v1/supervisor/questions/subjectCode` | Filter by subject code | Query: `subjectCode` |
| 55 | GET | `/api/v1/supervisor/questions/subjectCode/pagged` | Filter by subject code (paged) | Query: `subjectCode`, `pageNo`, `size` |
| 56 | GET | `/api/v1/supervisor/questions/subjectCode/mappedCO` | Filter by subject code & CO | Query: `subjectCode`, `mappedCO` |
| 57 | GET | `/api/v1/supervisor/questions/subjectCode/mappedCO/pagged` | Filter (paged) | Query: `subjectCode`, `mappedCO`, `pageNo`, `size` |
| 58 | GET | `/api/v1/supervisor/questions/subjectCode/mappedCO/cognitiveLevel` | Filter by subject code, CO & cognitive level | Query: `subjectCode`, `mappedCO`, `cognitiveLevel` |
| 59 | GET | `/api/v1/supervisor/questions/subjectCode/mappedCO/cognitiveLevel/pagged` | Filter (paged) | Query: `subjectCode`, `mappedCO`, `cognitiveLevel`, `pageNo`, `size` |
| 60 | GET | `/api/v1/supervisor/questions/subjectName` | Filter by subject name | Query: `subjectName` |
| 61 | GET | `/api/v1/supervisor/questions/subjectName/pagged` | Filter by subject name (paged) | Query: `subjectName`, `pageNo`, `size` |
| 62 | GET | `/api/v1/supervisor/questions/subjectName/mappedCO` | Filter by subject name & CO | Query: `subjectName`, `mappedCO` |
| 63 | GET | `/api/v1/supervisor/questions/subjectName/mappedCO/pagged` | Filter (paged) | Query: `subjectName`, `mappedCO`, `pageNo`, `size` |
| 64 | GET | `/api/v1/supervisor/questions/subjectName/mappedCO/cognitiveLevel` | Filter by subject name, CO & cognitive level | Query: `subjectName`, `mappedCO`, `cognitiveLevel` |
| 65 | GET | `/api/v1/supervisor/questions/subjectName/mappedCO/cognitiveLevel/pagged` | Filter (paged) | Query: `subjectName`, `mappedCO`, `cognitiveLevel`, `pageNo`, `size` |
| 66 | GET | `/api/v1/supervisor/questions/user/email` | Get questions by creator email | Query: `email` |
| 67 | GET | `/api/v1/supervisor/questions/user/id` | Get questions by creator ID | Query: `id` |
| 68 | GET | `/api/v1/supervisor/questionsPapers` | Get all question papers | None |
| 69 | GET | `/api/v1/supervisor/questionsPapers/paged` | Get all papers (paged) | Query: `pageNo`, `size` |
| 70 | GET | `/api/v1/supervisor/questionsPapers/id` | Get paper by ID | Query: `id` |
| 71 | GET | `/api/v1/supervisor/questionsPapers/examTitle` | Get paper by exam title | Query: `examTitle` |
| 72 | GET | `/api/v1/supervisor/questionsPapers/user/generatedBy/email` | Get papers by generator email | Query: `email` |
| 73 | GET | `/api/v1/supervisor/questionsPapers/user/generatedBy/email/paged` | Get papers by generator email (paged) | Query: `email`, `pageNo`, `size` |
| 74 | GET | `/api/v1/supervisor/questionsPapers/user/generatedBy/id` | Get papers by generator ID | Query: `id` |
| 75 | GET | `/api/v1/supervisor/questionsPapers/user/generatedBy/id/paged` | Get papers by generator ID (paged) | Query: `id`, `pageNo`, `size` |
| 76 | GET | `/api/v1/supervisor/questionsPapers/user/approvedBy/email` | Get papers by approver email | Query: `email` |
| 77 | GET | `/api/v1/supervisor/questionsPapers/user/approvedBy/email/paged` | Get papers by approver email (paged) | Query: `email`, `pageNo`, `size` |
| 78 | GET | `/api/v1/supervisor/questionsPapers/user/approvedBy/id` | Get papers by approver ID | Query: `id` |
| 79 | GET | `/api/v1/supervisor/questionsPapers/user/approvedBy/id/paged` | Get papers by approver ID (paged) | Query: `id`, `pageNo`, `size` |
| 80 | GET | `/api/v1/supervisor/questionsPapers/approved` | Get all approved papers | None |
| 81 | GET | `/api/v1/supervisor/questionsPapers/approved/page` | Get approved papers (paged) | Query: `pageNo`, `size` |
| 82 | GET | `/api/v1/supervisor/questionsPapers/not-approved` | Get all pending/rejected papers | None |
| 83 | GET | `/api/v1/supervisor/questionsPapers/not-approved/paged` | Get pending papers (paged) | Query: `pageNo`, `size` |
| 84 | PATCH | `/api/v1/supervisor/questionsPapers/approv/id` | Approve paper by ID | `{"id": 10, "comment": "Good paper"}` |
| 85 | PATCH | `/api/v1/supervisor/questionsPapers/not-approv/id` | Reject paper by ID | `{"id": 10, "comment": "Needs revision"}` |
| 86 | PATCH | `/api/v1/supervisor/questionsPapers/approv/examTitle` | Approve paper by exam title | `{"examTitle": "Midterm Exam", "comment": "Approved"}` |
| 87 | PATCH | `/api/v1/supervisor/questionsPapers/not-approv/examTitle` | Reject paper by exam title | `{"examTitle": "Midterm Exam", "comment": "Revision needed"}` |
| 88 | PATCH | `/api/v1/supervisor/update/user/email` | Update own email | `{"email": "supervisor@example.com"}` |
| 89 | PATCH | `/api/v1/supervisor/update/user/password` | Update own password | `{"password": "newPass123"}` |
| 90 | POST | `/api/v1/supervisor/logout` | Logout | None |
| 91 | GET | `/api/v1/supervisor/test` | Test endpoint | None |

### Teacher Endpoints (26)

| # | Method | Endpoint | Description | Parameters/Body |
|---|--------|----------|-------------|-----------------|
| 92 | GET | `/api/v1/teacher/questions` | Get all questions | None |
| 93 | GET | `/api/v1/teacher/questions/paged` | Get all questions (paged) | Query: `pageNo`, `size` |
| 94 | GET | `/api/v1/teacher/question/id` | Get question by ID | Query: `id` |
| 95 | GET | `/api/v1/teacher/questions/subjectCode` | Filter by subject code | Query: `subjectCode` |
| 96 | GET | `/api/v1/teacher/questions/subjectCode/pagged` | Filter by subject code (paged) | Query: `subjectCode`, `pageNo`, `size` |
| 97 | GET | `/api/v1/teacher/questions/subjectCode/mappedCO` | Filter by subject code & CO | Query: `subjectCode`, `mappedCO` |
| 98 | GET | `/api/v1/teacher/questions/subjectCode/mappedCO/pagged` | Filter (paged) | Query: `subjectCode`, `mappedCO`, `pageNo`, `size` |
| 99 | GET | `/api/v1/teacher/questions/subjectCode/mappedCO/cognitiveLevel` | Filter by subject code, CO & cognitive level | Query: `subjectCode`, `mappedCO`, `cognitiveLevel` |
| 100 | GET | `/api/v1/teacher/questions/subjectCode/mappedCO/cognitiveLevel/pagged` | Filter (paged) | Query: `subjectCode`, `mappedCO`, `cognitiveLevel`, `pageNo`, `size` |
| 101 | GET | `/api/v1/teacher/questions/subjectName` | Filter by subject name | Query: `subjectName` |
| 102 | GET | `/api/v1/teacher/questions/subjectName/pagged` | Filter by subject name (paged) | Query: `subjectName`, `pageNo`, `size` |
| 103 | GET | `/api/v1/teacher/questions/subjectName/mappedCO` | Filter by subject name & CO | Query: `subjectName`, `mappedCO` |
| 104 | GET | `/api/v1/teacher/questions/subjectName/mappedCO/pagged` | Filter (paged) | Query: `subjectName`, `mappedCO`, `pageNo`, `size` |
| 105 | GET | `/api/v1/teacher/questions/subjectName/mappedCO/cognitiveLevel` | Filter by subject name, CO & cognitive level | Query: `subjectName`, `mappedCO`, `cognitiveLevel` |
| 106 | GET | `/api/v1/teacher/questions/subjectName/mappedCO/cognitiveLevel/pagged` | Filter (paged) | Query: `subjectName`, `mappedCO`, `cognitiveLevel`, `pageNo`, `size` |
| 107 | POST | `/api/v1/teacher/question` | Add new question | `{"subjectCode": "CS101", "subjectName": "Database", "questionBody": "What is SQL?", "mappedCO": "CO1", "cognitiveLevel": "U", "questionMarks": 4}` |
| 108 | POST | `/api/v1/teacher/generate/question-paper/subjectCode` | Generate paper by subject code | `{"subjectCode": "CS101", "mappedCOs": ["CO1","CO2"], "numberOfCognitiveLevel_A": 5, "numberOfCognitiveLevel_R": 3, "numberOfCognitiveLevel_U": 4, "maxNumberOf2Marks": 6, "maxNumberOf4Marks": 12}` |
| 109 | POST | `/api/v1/teacher/generate/question-paper/subjectName` | Generate paper by subject name | `{"subjectName": "Database Systems", "mappedCOs": ["CO1","CO2"], "numberOfCognitiveLevel_A": 5, "numberOfCognitiveLevel_R": 3, "numberOfCognitiveLevel_U": 4, "maxNumberOf2Marks": 6, "maxNumberOf4Marks": 12}` |
| 110 | GET | `/api/v1/teacher/my/questions` | Get own questions | None |
| 111 | GET | `/api/v1/teacher/my/questions/pagged` | Get own questions (paged) | Query: `pageNo`, `size` |
| 112 | POST | `/api/v1/teacher/To-approve/questionPaper` | Submit paper for review | `{"examTitle": "Midterm 2024", "questionDTOList": [{"id": 1}, {"id": 2}]}` |
| 113 | GET | `/api/v1/teacher/my/questionPapers` | Get own papers (paged) | Query: `pageNo`, `size` |
| 114 | GET | `/api/v1/teacher/download/questionsPapers/id` | Download paper PDF | Query: `id` |
| 115 | DELETE | `/api/v1/teacher/question/id` | Delete question | `{"id": 25}` |
| 116 | PATCH | `/api/v1/teacher/update/user/email` | Update own email | `{"email": "teacher@example.com"}` |
| 117 | PATCH | `/api/v1/teacher/update/user/password` | Update own password | `{"password": "newPass123"}` |
| 118 | POST | `/api/v1/teacher/logout` | Logout | None |
| 119 | GET | `/api/v1/teacher/test` | Test endpoint | None |

**Total Endpoints: 119**

## 📁 Project Structure

```
FinalYearProject/
│
├── src/main/java/.../FinalYearProject/
│   │
│   ├── Config/Security/              # Security configuration
│   │   ├── Filter/JwtFilter.java     # JWT authentication filter
│   │   ├── SecurityConfig.java       # Spring Security config
│   │   └── RedisConfig.java          # Redis configuration
│   │
│   ├── Controller/                   # REST Controllers
│   │   ├── AdminRestController.java  # Admin endpoints (534 lines)
│   │   ├── TeacherRestController.java # Teacher endpoints (604 lines)
│   │   ├── SupervisorRestController.java # Supervisor endpoints (853 lines)
│   │   ├── StudentRestController.java   # Student endpoints (460 lines)
│   │   └── AuthRestController.java      # Authentication endpoints
│   │
│   ├── Service/                      # Business Logic
│   │   ├── UserService.java          # User management (462 lines)
│   │   ├── QuestionService.java      # Question operations (666 lines)
│   │   ├── QuestionPaperService.java # Paper operations (768 lines)
│   │   ├── JwtService.java           # JWT generation/validation
│   │   ├── RedisService.java         # Redis operations
│   │   └── ConformationService.java  # Email verification
│   │
│   ├── Domain/                       # Entity Classes
│   │   ├── User.java                 # User entity
│   │   ├── Question.java             # Question entity (193 lines)
│   │   ├── QuestionPaper.java        # QuestionPaper entity (151 lines)
│   │   └── Conformation.java         # Email confirmation entity
│   │
│   ├── Repository/                   # Data Access Layer
│   │   ├── UserRepository.java       # User queries
│   │   ├── QuestionRepository.java   # Question queries (82 lines)
│   │   └── QuestionPaperRepository.java # Paper queries (65 lines)
│   │
│   ├── DTO/                          # Data Transfer Objects
│   │   ├── UserDto/                  # User DTOs
│   │   ├── QuestionDto/              # Question DTOs
│   │   └── QuestionPaperDto/         # Paper DTOs
│   │
│   ├── Util/                         # Utility Classes
│   │   ├── UserUtil.java             # Current user helper
│   │   ├── QuestionUtil.java         # Question validation (76 lines)
│   │   ├── QuestionPaperUtil.java    # Paper fingerprint (52 lines)
│   │   └── ResponseUtility.java      # API response formatter
│   │
│   └── Exceptions/                   # Exception Handling
│       └── GlobalExceptionHandler.java # Global exception handler (129 lines)
│
├── src/main/resources/
│   ├── templates/                    # Thymeleaf HTML templates
│   │   ├── login.html                # Login page
│   │   ├── register.html             # Registration page
│   │   ├── teacher.html              # Teacher dashboard (764 lines)
│   │   ├── supervisor.html           # Supervisor dashboard (821 lines)
│   │   ├── student.html              # Student dashboard (367 lines)
│   │   └── admin.html                # Admin dashboard (628 lines)
│   │
│   ├── static/                       # Static assets
│   │   ├── css/                      # Stylesheets
│   │   │   ├── styles.css            # Main styles (157 lines)
│   │   │   └── style1.css            # Additional styles (940 lines)
│   │   └── js/                       # JavaScript files
│   │       ├── api.js                # API integration (1106 lines)
│   │       ├── teacher-dashboard.js  # Teacher JS (1249 lines)
│   │       ├── supervisor-dashboard.js # Supervisor JS (762 lines)
│   │       └── student-dashboard.js  # Student JS (537 lines)
│   │
│   └── application.properties        # Configuration
│
├── Docker/                           # Docker configuration
│   ├── Dockerfile                    # Docker image definition
│   └── compose.yaml                  # Docker Compose config
│
├── pom.xml                           # Maven dependencies
└── README.md                         # Documentation
```

**Code Statistics**: ~18,500 lines of code across 130+ files

## ⚙️ Installation & Setup

### Prerequisites
- Java JDK 17+
- Maven 3.6+
- PostgreSQL 13+
- Redis 6+
- Git

### Quick Start

```bash
# 1. Clone repository
git clone https://github.com/hassanrafai1707/FinalYearProject.git
cd FinalYearProject

# 2. Create PostgreSQL database
createdb finalyearproject

# 3. Configure application.properties with your credentials

# 4. Build the application
mvn clean install

# 5. Run the application
mvn spring-boot:run

# Application runs at: http://localhost:8080
```

### Docker Deployment

```bash
# Build and run with Docker Compose
docker-compose up -d
```

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Specific service tests
mvn test -Dtest=QuestionServiceTest
mvn test -Dtest=UserServiceTest
```

## ⚠️ Current Limitations

1. **Data Requirements**: Requires substantial question data (20-30+ questions per subject) for effective paper generation
2. **Internet Dependency**: Web-based system requires stable internet connection
3. **Single Institution**: Currently designed for single institution use (department-based isolation)
4. **Manual Question Entry**: Questions must be entered manually or via bulk import

## 🔮 Future Enhancements

- **AI-Powered Question Generation**: NLP-based question creation from course content
- **Automatic Answer Keys**: Generate answer keys automatically for approved papers
- **Plagiarism Detection**: Detect repeated questions across different papers
- **Multi-Institution Support**: Cloud-based multi-tenant architecture
- **LMS Integration**: Connect with Moodle, Canvas, etc.
- **Analytics Dashboard**: Track question usage patterns and difficulty analytics
- **Mobile Application**: Native mobile app for on-the-go access

## 👨‍💻 Developer

**Hassan Abdul Rahim Patel**  
*Diploma in Computer Engineering (Semester 6)*  
Vidyalankar Polytechnic, Mumbai  
Email: hassanrafai1707@gmail.com

**Project Guide**: Prof. Madhavi M

## 📄 Acknowledgments

- Vidyalankar Polytechnic, Department of Computer Engineering
- Prof. Madhavi M for guidance and mentorship
- All faculty members and peers who contributed feedback

---

*Built with ❤️ for streamlined examination management*