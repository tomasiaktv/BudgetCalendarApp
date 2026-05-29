package ui;

import model.Transaction;
import service.BudgetService;
import util.AppConstants;
import util.CurrencyFormatter;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class CalendarPanel extends JPanel {

    private final BudgetService budgetService;

    private BudgetDataChangeListener budgetDataChangeListener;

    private YearMonth currentMonth;
    private LocalDate selectedDate;

    private JLabel monthLabel;
    private JLabel selectedDateLabel;
    private JLabel monthSummaryLabel;

    private JPanel calendarGridPanel;
    private JPanel selectedDayDetailsPanel;

    private JTextField descriptionField;
    private JTextField amountField;

    private JComboBox<String> typeComboBox;
    private JComboBox<String> categoryComboBox;

    private JCheckBox showBalanceOnBlankDaysCheckBox;
    private JCheckBox recurringMonthlyCheckBox;

    public CalendarPanel(BudgetService budgetService) {
        this.budgetService = budgetService;
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCalendarWrapper(), BorderLayout.CENTER);
        add(createSidePanel(), BorderLayout.EAST);
        add(createFooterPanel(), BorderLayout.SOUTH);

        refreshView();
    }

    public void setBudgetDataChangeListener(BudgetDataChangeListener budgetDataChangeListener) {
        this.budgetDataChangeListener = budgetDataChangeListener;
    }

    public void refreshView() {
        renderCalendar();
        updateFooter();
        updateSelectedDateLabel();
        updateSelectedDayDetails();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));

        JButton previousButton = new JButton("<");
        JButton todayButton = new JButton("Today");
        JButton nextButton = new JButton(">");

        previousButton.addActionListener(event -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshView();
        });

        todayButton.addActionListener(event -> {
            currentMonth = YearMonth.now();
            selectedDate = LocalDate.now();
            refreshView();
        });

        nextButton.addActionListener(event -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshView();
        });

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonPanel.add(previousButton);
        buttonPanel.add(todayButton);
        buttonPanel.add(nextButton);

        JPanel legendPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JLabel incomeLegendLabel = new JLabel("Green = Income / Positive Balance");
        incomeLegendLabel.setForeground(new Color(46, 125, 50));

        JLabel expenseLegendLabel = new JLabel("Red = Expense / Negative Balance");
        expenseLegendLabel.setForeground(new Color(198, 40, 40));

        showBalanceOnBlankDaysCheckBox = new JCheckBox("Show balance on blank days");
        showBalanceOnBlankDaysCheckBox.addActionListener(event -> refreshView());

        legendPanel.add(incomeLegendLabel);
        legendPanel.add(expenseLegendLabel);
        legendPanel.add(showBalanceOnBlankDaysCheckBox);

        headerPanel.add(buttonPanel, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(legendPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createCalendarWrapper() {
        JPanel wrapperPanel = new JPanel(new BorderLayout(5, 5));

        JPanel daysHeaderPanel = new JPanel(new GridLayout(1, 7));
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (String dayName : dayNames) {
            JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 16));
            daysHeaderPanel.add(dayLabel);
        }

        calendarGridPanel = new JPanel(new GridLayout(6, 7, 5, 5));

        wrapperPanel.add(daysHeaderPanel, BorderLayout.NORTH);
        wrapperPanel.add(calendarGridPanel, BorderLayout.CENTER);

        return wrapperPanel;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel(new BorderLayout(10, 10));
        sidePanel.setPreferredSize(new Dimension(300, 0));

        JPanel formPanel = new JPanel(new GridLayout(11, 1, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Budget Entry"));

        selectedDateLabel = new JLabel("Selected Date:");
        selectedDateLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionField = new JTextField();

        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField();

        JLabel typeLabel = new JLabel("Type:");
        typeComboBox = new JComboBox<>(AppConstants.TRANSACTION_TYPES);

        JLabel categoryLabel = new JLabel("Category:");
        categoryComboBox = new JComboBox<>(AppConstants.TRANSACTION_CATEGORIES);

        recurringMonthlyCheckBox = new JCheckBox("Repeats monthly");

        JButton addButton = new JButton("Add To Selected Day");
        addButton.addActionListener(event -> addTransactionFromCalendar());

        formPanel.add(selectedDateLabel);
        formPanel.add(descriptionLabel);
        formPanel.add(descriptionField);
        formPanel.add(amountLabel);
        formPanel.add(amountField);
        formPanel.add(typeLabel);
        formPanel.add(typeComboBox);
        formPanel.add(categoryLabel);
        formPanel.add(categoryComboBox);
        formPanel.add(recurringMonthlyCheckBox);
        formPanel.add(addButton);

        selectedDayDetailsPanel = new JPanel();
        selectedDayDetailsPanel.setLayout(new BoxLayout(selectedDayDetailsPanel, BoxLayout.Y_AXIS));
        selectedDayDetailsPanel.setBorder(BorderFactory.createTitledBorder("Selected Day Details"));

        JScrollPane detailsScrollPane = new JScrollPane(selectedDayDetailsPanel);

        sidePanel.add(formPanel, BorderLayout.NORTH);
        sidePanel.add(detailsScrollPane, BorderLayout.CENTER);

        return sidePanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());

        monthSummaryLabel = new JLabel("Monthly summary");
        monthSummaryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        monthSummaryLabel.setFont(new Font("Arial", Font.BOLD, 16));

        footerPanel.add(monthSummaryLabel, BorderLayout.CENTER);

        return footerPanel;
    }

    private void renderCalendar() {
        calendarGridPanel.removeAll();

        monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());

        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int daysInMonth = currentMonth.lengthOfMonth();

        int startOffset = getSundayFirstOffset(firstDayOfMonth.getDayOfWeek());

        for (int i = 0; i < 42; i++) {
            if (i < startOffset || i >= startOffset + daysInMonth) {
                calendarGridPanel.add(createEmptyCell());
            } else {
                int dayNumber = i - startOffset + 1;
                LocalDate date = currentMonth.atDay(dayNumber);
                calendarGridPanel.add(createDayCell(date));
            }
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }

    private int getSundayFirstOffset(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue() % 7;
    }

    private JPanel createEmptyCell() {
        JPanel emptyCell = new JPanel();
        emptyCell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return emptyCell;
    }

    private JPanel createDayCell(LocalDate date) {
        JPanel dayCell = new JPanel(new BorderLayout(2, 2));
        dayCell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        dayCell.setBackground(Color.WHITE);
        dayCell.setOpaque(true);

        if (date.equals(selectedDate)) {
            dayCell.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2));
        } else if (date.equals(LocalDate.now())) {
            dayCell.setBorder(BorderFactory.createLineBorder(new Color(46, 125, 50), 2));
        }

        JLabel dayNumberLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
        dayNumberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dayNumberLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel entriesPanel = new JPanel();
        entriesPanel.setLayout(new BoxLayout(entriesPanel, BoxLayout.Y_AXIS));
        entriesPanel.setOpaque(false);

        List<Transaction> transactions = budgetService.getTransactionsForDate(date);

        int maxVisibleEntries = 3;

        for (int i = 0; i < transactions.size() && i < maxVisibleEntries; i++) {
            Transaction transaction = transactions.get(i);
            entriesPanel.add(createTransactionChip(transaction));
        }

        if (transactions.size() > maxVisibleEntries) {
            JLabel moreLabel = new JLabel("...");
            moreLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            entriesPanel.add(moreLabel);
        }

        double runningBalance = calculateMonthToDateBalance(date);

        JLabel dayBalanceLabel = new JLabel(getDayBalanceText(date));
        dayBalanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dayBalanceLabel.setFont(new Font("Arial", Font.BOLD, 11));
        dayBalanceLabel.setForeground(getNetBalanceColor(runningBalance));

        dayCell.add(dayNumberLabel, BorderLayout.NORTH);
        dayCell.add(entriesPanel, BorderLayout.CENTER);
        dayCell.add(dayBalanceLabel, BorderLayout.SOUTH);

        dayCell.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedDate = date;
                refreshView();
            }
        });

        return dayCell;
    }

    private JLabel createTransactionChip(Transaction transaction) {
        String shortDescription = shorten(transaction.getDescription(), 10);
        String amountText = CurrencyFormatter.format(transaction.getAmount());
        String recurringMarker = transaction.isRecurringMonthly() ? " ↻" : "";

        JLabel chipLabel = new JLabel(shortDescription + " " + amountText + recurringMarker);
        chipLabel.setOpaque(true);
        chipLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        chipLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        chipLabel.setToolTipText(
                transaction.getType()
                        + " | "
                        + transaction.getCategory()
                        + " | "
                        + transaction.getDescription()
                        + " | "
                        + amountText
                        + (transaction.isRecurringMonthly() ? " | Repeats monthly" : "")
        );

        if ("Income".equals(transaction.getType())) {
            chipLabel.setBackground(new Color(200, 230, 201));
            chipLabel.setForeground(new Color(27, 94, 32));
        } else {
            chipLabel.setBackground(new Color(255, 205, 210));
            chipLabel.setForeground(new Color(183, 28, 28));
        }

        return chipLabel;
    }

    private void addTransactionFromCalendar() {
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a day first.");
            return;
        }

        String description = descriptionField.getText();
        String amountText = amountField.getText();
        String type = (String) typeComboBox.getSelectedItem();
        String category = (String) categoryComboBox.getSelectedItem();
        boolean recurringMonthly = recurringMonthlyCheckBox.isSelected();

        if (description.isBlank() || amountText.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a description and amount.");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.");
            return;
        }

        Transaction transaction = new Transaction(
                selectedDate,
                description,
                amount,
                type,
                category,
                recurringMonthly
        );

        budgetService.addTransaction(transaction);

        if (budgetDataChangeListener != null) {
            budgetDataChangeListener.onBudgetDataChanged();
        }

        clearTransactionForm();
        refreshView();

        JOptionPane.showMessageDialog(this, "Budget entry added to " + selectedDate + ".");
    }

    private void updateFooter() {
        double income = budgetService.calculateTotalIncomeForMonth(currentMonth);
        double expenses = budgetService.calculateTotalExpensesForMonth(currentMonth);
        double balance = budgetService.calculateBalanceForMonth(currentMonth);

        monthSummaryLabel.setText(String.format(
                "%s summary  |  Income: $%.2f   Expenses: $%.2f   Balance: $%.2f",
                currentMonth,
                income,
                expenses,
                balance
        ));
    }

    private void updateSelectedDateLabel() {
        selectedDateLabel.setText("Selected Date: " + selectedDate);
    }

    private void clearTransactionForm() {
        descriptionField.setText("");
        amountField.setText("");
        typeComboBox.setSelectedIndex(0);
        categoryComboBox.setSelectedIndex(0);
        recurringMonthlyCheckBox.setSelected(false);
    }

    private String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 3) + "...";
    }

    private double calculateDayIncome(LocalDate date) {
        double income = 0;

        List<Transaction> transactions = budgetService.getTransactionsForDate(date);

        for (Transaction transaction : transactions) {
            if ("Income".equals(transaction.getType())) {
                income += transaction.getAmount();
            }
        }

        return income;
    }

    private double calculateDayExpenses(LocalDate date) {
        double expenses = 0;

        List<Transaction> transactions = budgetService.getTransactionsForDate(date);

        for (Transaction transaction : transactions) {
            if ("Expense".equals(transaction.getType())) {
                expenses += transaction.getAmount();
            }
        }

        return expenses;
    }

    private double calculateDayNet(LocalDate date) {
        return calculateDayIncome(date) - calculateDayExpenses(date);
    }

    private String getDayBalanceText(LocalDate date) {
        boolean hasTransactionsOnDate =
                !budgetService.getTransactionsForDate(date).isEmpty();

        if (!hasTransactionsOnDate && !showBalanceOnBlankDaysCheckBox.isSelected()) {
            return "";
        }

        double runningBalance = calculateMonthToDateBalance(date);

        if (!hasTransactionsOnDate && runningBalance == 0) {
            return "";
        }

        return "Balance: " + CurrencyFormatter.format(runningBalance);
    }

    private void updateSelectedDayDetails() {
        selectedDayDetailsPanel.removeAll();

        List<Transaction> transactions = budgetService.getTransactionsForDate(selectedDate);

        JLabel dateHeaderLabel = new JLabel("Date: " + selectedDate);
        dateHeaderLabel.setFont(new Font("Arial", Font.BOLD, 16));
        selectedDayDetailsPanel.add(dateHeaderLabel);

        selectedDayDetailsPanel.add(new JLabel(" "));

        if (transactions.isEmpty()) {
            selectedDayDetailsPanel.add(new JLabel("No budget entries for this day."));
            selectedDayDetailsPanel.revalidate();
            selectedDayDetailsPanel.repaint();
            return;
        }

        JLabel incomeHeaderLabel = new JLabel("Income");
        incomeHeaderLabel.setFont(new Font("Arial", Font.BOLD, 14));
        selectedDayDetailsPanel.add(incomeHeaderLabel);

        boolean hasIncome = false;

        for (Transaction transaction : transactions) {
            if ("Income".equals(transaction.getType())) {
                selectedDayDetailsPanel.add(createDetailLine(transaction));
                hasIncome = true;
            }
        }

        if (!hasIncome) {
            selectedDayDetailsPanel.add(new JLabel("None"));
        }

        selectedDayDetailsPanel.add(new JLabel(" "));

        JLabel expenseHeaderLabel = new JLabel("Expenses");
        expenseHeaderLabel.setFont(new Font("Arial", Font.BOLD, 14));
        selectedDayDetailsPanel.add(expenseHeaderLabel);

        boolean hasExpenses = false;

        for (Transaction transaction : transactions) {
            if ("Expense".equals(transaction.getType())) {
                selectedDayDetailsPanel.add(createDetailLine(transaction));
                hasExpenses = true;
            }
        }

        if (!hasExpenses) {
            selectedDayDetailsPanel.add(new JLabel("None"));
        }

        selectedDayDetailsPanel.add(new JLabel(" "));

        double dayIncome = calculateDayIncome(selectedDate);
        double dayExpenses = calculateDayExpenses(selectedDate);
        double dayNet = calculateDayNet(selectedDate);
        double runningMonthlyNet = calculateMonthToDateBalance(selectedDate);

        JLabel totalIncomeLabel = new JLabel("Day Income: " + CurrencyFormatter.format(dayIncome));
        JLabel totalExpensesLabel = new JLabel("Day Expenses: " + CurrencyFormatter.format(dayExpenses));
        JLabel dayNetLabel = new JLabel("Day Net: " + CurrencyFormatter.format(dayNet));
        JLabel runningMonthlyNetLabel = new JLabel("Month-To-Date Balance: " + CurrencyFormatter.format(runningMonthlyNet));

        totalIncomeLabel.setFont(new Font("Arial", Font.BOLD, 13));
        totalExpensesLabel.setFont(new Font("Arial", Font.BOLD, 13));
        dayNetLabel.setFont(new Font("Arial", Font.BOLD, 14));
        runningMonthlyNetLabel.setFont(new Font("Arial", Font.BOLD, 14));

        totalIncomeLabel.setForeground(new Color(46, 125, 50));
        totalExpensesLabel.setForeground(new Color(198, 40, 40));
        dayNetLabel.setForeground(getNetBalanceColor(dayNet));
        runningMonthlyNetLabel.setForeground(getNetBalanceColor(runningMonthlyNet));

        selectedDayDetailsPanel.add(totalIncomeLabel);
        selectedDayDetailsPanel.add(totalExpensesLabel);
        selectedDayDetailsPanel.add(dayNetLabel);
        selectedDayDetailsPanel.add(runningMonthlyNetLabel);

        selectedDayDetailsPanel.revalidate();
        selectedDayDetailsPanel.repaint();
    }

    private JLabel createDetailLine(Transaction transaction) {
        String text = String.format(
                "%s | %s | $%.2f",
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getAmount()
        );

        JLabel detailLabel = new JLabel(text);
        detailLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2));

        return detailLabel;
    }

    private Color getNetBalanceColor(double netAmount) {
        if (netAmount > 0) {
            return new Color(46, 125, 50); // green
        } else if (netAmount < 0) {
            return new Color(198, 40, 40); // red
        } else {
            return Color.DARK_GRAY;
        }
    }

    private double calculateMonthToDateBalance(LocalDate date) {
        YearMonth dateMonth = YearMonth.from(date);
        double balance = 0;

        for (Transaction transaction : budgetService.getTransactionsForMonth(dateMonth)) {
            boolean transactionIsOnOrBeforeDate =
                    !transaction.getDate().isAfter(date);

            if (transactionIsOnOrBeforeDate) {
                if (AppConstants.TYPE_INCOME.equals(transaction.getType())) {
                    balance += transaction.getAmount();
                } else {
                    balance -= transaction.getAmount();
                }
            }
        }

        return balance;
    }
}