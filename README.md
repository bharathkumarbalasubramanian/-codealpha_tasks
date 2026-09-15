# TASK-1-Student-Grade-Tracker

A comprehensive Student Grade Tracker project built as part of the CodeAlpha Java Development Internship. Includes both a full Java Swing Desktop Application and an Interactive Web Frontend.

---

## 🌟 Project Overview

The **Student Grade Tracker** allows educators and students to dynamically record, analyze, and visualize academic performance. It automatically calculates key metrics such as **Class Average**, **Highest Score**, **Lowest Score**, and **Total Records**.

### Features

- 🖥️ **Java Swing Desktop Application**:
  - Clean GUI built with `JFrame`, `JTextField`, `JButton`, `JTextArea`, and dynamic `JLabel` statistics cards.
  - Backend data management powered by `ArrayList<Double>`.
  - Input validation with `JOptionPane` popups for non-numeric or out-of-range inputs.

- 🌐 **Web Frontend Interface**:
  - Modern, responsive dashboard created using HTML5 and vanilla CSS.
  - Interactive table report with performance grade badges (`Grade A+`, `Grade A`, `Grade B`, `Grade C`, `Grade F`).
  - Flexbox & Grid layouts styled with a soft blue theme.

---

## 📂 Project Structure

```text
├── StudentGradeTracker.java  # Java Swing GUI & backend logic (ArrayList<Double>)
├── StudentGradeTracker.class # Compiled Java bytecode
├── index.html                # HTML5 Web UI layout
├── style.css                 # Custom CSS styling (Soft blue design system)
├── app.js                    # Client-side JavaScript logic
└── README.md                 # Project documentation
```

---

## 🛠️ Requirements & How to Run

### 1. Running the Java Swing Desktop Application

**Prerequisites**: JDK 8 or higher (Java 17+ / Java 26 recommended).

```bash
# Compile the Java application
javac StudentGradeTracker.java

# Run the Swing GUI application
java StudentGradeTracker
```

---

### 2. Opening the Web Frontend

Simply open `index.html` in any standard web browser (Chrome, Firefox, Edge, Safari):

```bash
# Double-click index.html or open via terminal
start index.html
```

---

## 📝 License

Developed for CodeAlpha Internship - Task 1.
