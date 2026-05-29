# BudgetCalendarApp
A Java Swing desktop application for tracking income, expenses, category budgets, and month-to-date balances through both a table-based budget view and a monthly calendar view.

## Features

- Add, edit, and delete budget transactions
- Track income and expenses by date, category, and type
- Save, Save As, and Load budget CSV files
- Manual file workflow with unsaved-changes protection
- Month filtering for budget transactions
- Rollover balance option for month-to-month tracking
- Editable category budget limits
- Persistent category budget settings
- Monthly calendar view with color-coded income and expenses
- Calendar day cells show budget entries and running balance
- Add income or expenses directly from the calendar
- Selected day details panel with day totals and month-to-date balance

## Technologies Used

- Java
- Java Swing
- Object-Oriented Programming
- Interfaces
- Repository pattern
- CSV file persistence
- IntelliJ IDEA

## Architecture

The application is organized into separate layers:

- `model` contains data classes such as `Transaction` and `CategorySummary`
- `service` contains business logic through `BudgetService` and `DefaultBudgetService`
- `repository` handles CSV file loading and saving
- `ui` contains Swing panels and the main application window
- `util` contains shared constants, paths, and formatting helpers

## Why This Project Matters

This project demonstrates practical Java desktop development skills, including GUI design, event handling, file persistence, interface-based architecture, and separation of concerns.

## Future Improvements

- Add charts for category spending
- Add SQLite or Azure SQL storage
- Add budget reports and export options
- Add user preferences for themes and display settings
