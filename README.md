# 📚 College Project Management System

<p align="center">
  <img src="ProjectMgmtSys/src/main/resources/static/img/logo.png" alt="Project Logo" width="100" height="100">
</p>

A **comprehensive and innovative web-based platform** designed to streamline the **management and coordination of academic projects** in engineering colleges.  
It eliminates outdated manual processes with **real-time collaboration**, **centralized data storage**, and **automated notifications** for **students**, **faculty guides**, and **administrators**.

---

## 🚀 Features

### 🔹 Role-Based Access Control
- **Administrator** – Manage accounts, assign guides, oversee projects, and track batch progress.
- **Guide** – Mentor assigned students, review documents, track milestones, and provide feedback.
- **Student** – Submit documents, receive guidance, track deadlines, and monitor progress.

### 🔹 Project Lifecycle Management
- Project allocation & tracking  
- Document submission & storage  
- Milestone monitoring  
- Progress tracking & reports  

### 🔹 Real-Time Notifications
- Email alerts for deadlines, feedback, and updates

### 🔹 User-Friendly Interface
- Responsive design using **Bootstrap**  
- Intuitive navigation and clean layouts  

### 🔹 Data Security & Reliability
- Built with **Spring Boot**, **Thymeleaf**, and **MySQL**  
- Secure authentication and role management  

---

## 🛠️ Tech Stack

| Layer         | Technology Used               |
|--------------|--------------------------------|
| **Backend**  | Spring Boot (Java)             |
| **Frontend** | HTML, CSS, Bootstrap, Thymeleaf|
| **Database** | MySQL                          |
| **Build Tool**| Maven                         |
| **Server**   | Embedded Tomcat                |

---

## 📂 Project Structure

```plaintext
ProjectMgmtSys/
│
├── src/main/java/com/college/projectmgmtsys   # Backend Java code
│   ├── controller                             # Handles HTTP requests
│   ├── model                                  # Entity classes
│   ├── repository                             # Data access layer
│   ├── service                                # Business logic
│   └── security                               # Authentication & authorization
│
├── src/main/resources
│   ├── templates                              # Thymeleaf HTML templates
│   ├── static                                 # CSS, JS, Images
│   └── application.properties                 # Configuration file
│
└── pom.xml                                    # Maven dependencies


