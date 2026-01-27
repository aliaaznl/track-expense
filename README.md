# TrackExpense - Personal Expense Tracker

A full-stack web application for tracking personal expenses, managing budgets and analyzing spending patterns. Built with Spring Boot backend and vanilla JavaScript frontend.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

### **Live Demo:** [https://expense-tracker-l9tf.onrender.com](https://expense-tracker-l9tf.onrender.com)

## Features

### Dashboard & Analytics
- **Overview Dashboard**: Real-time financial summary with total income and expenses
- **Interactive Charts**: Visual representation of spending patterns using Chart.js

### Transaction Management
- Add, edit and delete income and expense transactions
- Categorize transactions with custom or system categories
- Attach receipts and documents to transactions
- Date-based filtering and search functionality

### Budget Management
- Create and manage budgets for different categories
- Track budget progress with visual indicators
- Set spending limits and monitor remaining budget

### Category Management
- System categories (Housing, Food, Transportation, etc.)
- Create custom categories with custom colors and icons
- Edit and delete user-created categories

### Export & Reports
- Export transactions to PDF or .csv format
- Generate comprehensive expense reports
- Filter exports by date range and categories

### User Management
- User registration and authentication
- JWT-based secure authentication
- Password reset via email
- User profile management
- Account deletion

### Security
- JWT token-based authentication
- Password encryption with Spring Security
- Secure file upload handling
- CORS configuration for API access

## Tech Stack

### Backend
- **Java 21**: Modern Java features
- **Spring Boot 3.3.0**: Application framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database operations
- **MySQL**: Relational database
- **JWT (jjwt)**: Token-based authentication
- **Spring Mail**: Email functionality
- **Lombok**: Boilerplate code reduction

### Frontend
- **HTML5/CSS3**: Modern web standards
- **Vanilla JavaScript**: No framework dependencies
- **Chart.js**: Data visualization
- **jsPDF**: PDF generation
- **Font Awesome**: Icons
- **Google Fonts (DM Sans)**: Typography

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** or higher
- **Maven 3.6+** (or use included Maven Wrapper)
- **MySQL 8.0+** database server
- **Git** for version control

## Getting Started

### 1. Clone the Repository

```bash
git clone [https://github.com/yourusername/expense-tracker.git](https://github.com/yourusername/expense-tracker.git)
cd expense-tracker
