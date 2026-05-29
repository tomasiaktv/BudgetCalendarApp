package ui;

import model.Transaction;
import model.CategorySummary;
import repository.CategoryBudgetRepository;
import repository.CsvCategoryBudgetRepository;
import repository.CsvTransactionRepository;
import repository.TransactionRepository;
import service.BudgetService;
import util.AppConstants;
import util.AppPaths;
import util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class BudgetPanel extends JPanel {

    private JTextField dateField;
    private JTextField descriptionField;
    private JTextField amountField;
    private JTextField monthFilterField;

    private JComboBox<String> typeComboBox;
    private JComboBox<String> categoryComboBox;

    private DefaultTableModel tableModel;
    private JTable transactionTable;

    private DefaultTableModel categoryBudgetTableModel;
    private JTable categoryBudgetTable;

    private JCheckBox rolloverCheckBox;
    private JCheckBox recurringMonthlyCheckBox;

    private JLabel startingBalanceLabel;
    private JLabel incomeLabel;
    private JLabel expensesLabel;
    private JLabel balanceLabel;
    private JLabel statusLabel;
    private JLabel saveStatusLabel;
    private JLabel currentFileLabel;

    private BudgetService budgetService;
    private TransactionRepository transactionRepository;
    private CategoryBudgetRepository categoryBudgetRepository;

    private boolean hasUnsavedChanges = false;
    private File currentTransactionFile = null;

    private final File budgetSaveFolder = AppPaths.getBudgetsFolderAsFile();
    private final File categorySettingsFolder = AppPaths.getSettingsFolderAsFile();

    public BudgetPanel(BudgetService budgetService) {
        this.budgetService = budgetService;
        transactionRepository = new CsvTransactionRepository();
        categoryBudgetRepository = new CsvCategoryBudgetRepository();

        if (!budgetSaveFolder.exists()) {
            budgetSaveFolder.mkdirs();
        }

        if (!categorySettingsFolder.exists()) {
            categorySettingsFolder.mkdirs();
        }

        budgetService.replaceCategoryBudgets(
                categoryBudgetRepository.load(AppPaths.DEFAULT_CATEGORY_BUDGET_FILE)
        );

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createTitlePanel(), BorderLayout.NORTH);
        topPanel.add(createFilterPanel(), BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(createInputPanel(), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createSummaryPanel(), BorderLayout.SOUTH);

        updateSummary();
        updateSaveStatusLabel();
        updateCurrentFileLabel();
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        centerPanel.add(createTablePanel());

        JPanel secondItemWrapper = new JPanel(new BorderLayout());
        secondItemWrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Category Budget Table",
                TitledBorder.CENTER,
                TitledBorder.TOP
        ));

        secondItemWrapper.add(createCategoryBudgetPanel());

        centerPanel.add(secondItemWrapper);


        return centerPanel;
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Budget Tracker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        titlePanel.add(titleLabel, BorderLayout.CENTER);

        return titlePanel;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridLayout(1, 6, 10, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));

        JLabel monthFilterLabel = new JLabel("Month yyyy-mm:");
        monthFilterField = new JTextField(LocalDate.now().toString().substring(0, 7));

        rolloverCheckBox = new JCheckBox("Rollover previous balance");
        rolloverCheckBox.addActionListener(event -> updateSummary());

        JButton applyFilterButton = new JButton("Apply Filter");
        applyFilterButton.addActionListener(event -> updateSummary());

        saveStatusLabel = new JLabel("Saved");
        saveStatusLabel.setHorizontalAlignment(JLabel.CENTER);
        currentFileLabel = new JLabel("Current File: Unsaved new budget");
        currentFileLabel.setHorizontalAlignment(JLabel.CENTER);

        filterPanel.add(monthFilterLabel);
        filterPanel.add(monthFilterField);
        filterPanel.add(rolloverCheckBox);
        filterPanel.add(applyFilterButton);
        filterPanel.add(saveStatusLabel);
        filterPanel.add(currentFileLabel);

        return filterPanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(17, 1, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Transaction"));

        JLabel dateLabel = new JLabel("Date yyyy-mm-dd:");
        dateField = new JTextField(LocalDate.now().toString());

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionField = new JTextField();

        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField();

        JLabel typeLabel = new JLabel("Type:");
        typeComboBox = new JComboBox<>(AppConstants.TRANSACTION_TYPES);

        JLabel categoryLabel = new JLabel("Category:");
        categoryComboBox = new JComboBox<>(AppConstants.TRANSACTION_CATEGORIES);

        recurringMonthlyCheckBox = new JCheckBox("Repeats monthly");

        JButton addButton = new JButton("Add Transaction");
        addButton.addActionListener(event -> addTransaction());

        JButton editTransactionButton = new JButton("Edit Selected Transaction");
        editTransactionButton.addActionListener(event -> editSelectedTransaction());

        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(event -> deleteSelectedTransaction());

        JButton editCategoryBudgetButton = new JButton("Edit Category Budget");
        editCategoryBudgetButton.addActionListener(event -> editSelectedCategoryBudget());

        inputPanel.add(dateLabel);
        inputPanel.add(dateField);
        inputPanel.add(descriptionLabel);
        inputPanel.add(descriptionField);
        inputPanel.add(amountLabel);
        inputPanel.add(amountField);
        inputPanel.add(typeLabel);
        inputPanel.add(typeComboBox);
        inputPanel.add(categoryLabel);
        inputPanel.add(categoryComboBox);
        inputPanel.add(recurringMonthlyCheckBox);
        inputPanel.add(addButton);
        inputPanel.add(editTransactionButton);
        inputPanel.add(deleteButton);
        inputPanel.add(editCategoryBudgetButton);
        inputPanel.add(new JLabel());
        inputPanel.add(new JLabel());

        return inputPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"Date", "Description", "Amount", "Type", "Category", "Recurring"};

        tableModel = new DefaultTableModel(columnNames, 0);

        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        return new JScrollPane(transactionTable);
    }

    private JScrollPane createCategoryBudgetPanel() {
        String[] columnNames = {"Category", "Spent", "Limit", "Remaining", "Status"};

        categoryBudgetTableModel = new DefaultTableModel(columnNames, 0);

        categoryBudgetTable = new JTable(categoryBudgetTableModel);
        categoryBudgetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return new JScrollPane(categoryBudgetTable);
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel totalsPanel = new JPanel(new GridLayout(1, 4, 10, 10));

        startingBalanceLabel = new JLabel("Starting Balance: $0.00");
        incomeLabel = new JLabel("Total Income: $0.00");
        expensesLabel = new JLabel("Total Expenses: $0.00");
        balanceLabel = new JLabel("Current Balance: $0.00");

        startingBalanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        incomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        expensesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));

        startingBalanceLabel.setHorizontalAlignment(JLabel.CENTER);
        incomeLabel.setHorizontalAlignment(JLabel.CENTER);
        expensesLabel.setHorizontalAlignment(JLabel.CENTER);
        balanceLabel.setHorizontalAlignment(JLabel.CENTER);

        totalsPanel.add(startingBalanceLabel);
        totalsPanel.add(incomeLabel);
        totalsPanel.add(expensesLabel);
        totalsPanel.add(balanceLabel);

        statusLabel = new JLabel("Monthly Status: No data yet");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);

        summaryPanel.add(totalsPanel);
        summaryPanel.add(statusLabel);

        return summaryPanel;
    }

    private void editSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to edit.");
            return;
        }

        String monthText = monthFilterField.getText();

        YearMonth selectedMonth;

        try {
            selectedMonth = YearMonth.parse(monthText);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Month must use this format: yyyy-mm");
            return;
        }

        List<Transaction> filteredTransactions = budgetService.getTransactionsForMonth(selectedMonth);
        Transaction selectedTransaction = filteredTransactions.get(selectedRow);

        JTextField editDateField = new JTextField(selectedTransaction.getDate().toString());
        JTextField editDescriptionField = new JTextField(selectedTransaction.getDescription());
        JTextField editAmountField = new JTextField(CurrencyFormatter.format(selectedTransaction.getAmount()));

        JCheckBox editRecurringMonthlyCheckBox = new JCheckBox("Repeats monthly");
        editRecurringMonthlyCheckBox.setSelected(selectedTransaction.isRecurringMonthly());

        JComboBox<String> editTypeComboBox = new JComboBox<>(AppConstants.TRANSACTION_TYPES);
        editTypeComboBox.setSelectedItem(selectedTransaction.getType());

        JComboBox<String> editCategoryComboBox = new JComboBox<>(AppConstants.TRANSACTION_CATEGORIES);
        editCategoryComboBox.setSelectedItem(selectedTransaction.getCategory());

        JPanel editPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        editPanel.add(new JLabel("Date yyyy-mm-dd:"));
        editPanel.add(editDateField);

        editPanel.add(new JLabel("Description:"));
        editPanel.add(editDescriptionField);

        editPanel.add(new JLabel("Amount:"));
        editPanel.add(editAmountField);

        editPanel.add(new JLabel("Type:"));
        editPanel.add(editTypeComboBox);

        editPanel.add(new JLabel("Category:"));
        editPanel.add(editCategoryComboBox);

        editPanel.add(new JLabel("Recurring:"));
        editPanel.add(editRecurringMonthlyCheckBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                editPanel,
                "Edit Transaction",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String updatedDateText = editDateField.getText();
        String updatedDescription = editDescriptionField.getText();
        String updatedAmountText = editAmountField.getText();
        String updatedType = (String) editTypeComboBox.getSelectedItem();
        String updatedCategory = (String) editCategoryComboBox.getSelectedItem();
        boolean updatedRecurringMonthly = editRecurringMonthlyCheckBox.isSelected();

        if (updatedDateText.isBlank() || updatedDescription.isBlank() || updatedAmountText.isBlank()) {
            JOptionPane.showMessageDialog(this, "Date, description, and amount cannot be blank.");
            return;
        }

        LocalDate updatedDate;

        try {
            updatedDate = LocalDate.parse(updatedDateText);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Date must use this format: yyyy-mm-dd");
            return;
        }

        double updatedAmount;

        try {
            updatedAmount = Double.parseDouble(updatedAmountText);
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.");
            return;
        }

        Transaction updatedTransaction = new Transaction(
                selectedTransaction.getId(),
                updatedDate,
                updatedDescription,
                updatedAmount,
                updatedType,
                updatedCategory,
                updatedRecurringMonthly
        );

        budgetService.updateTransaction(selectedTransaction, updatedTransaction);

        markUnsavedChanges();

        updateSummary();

        JOptionPane.showMessageDialog(this, "Transaction updated.");
    }

    private void editSelectedCategoryBudget() {
        int selectedRow = categoryBudgetTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category from the category budget table.");
            return;
        }

        String category = (String) categoryBudgetTableModel.getValueAt(selectedRow, 0);
        double currentBudget = budgetService.getCategoryBudget(category);

        String input = JOptionPane.showInputDialog(
                this,
                "Enter new monthly budget for " + category + ":",
                CurrencyFormatter.format(currentBudget)
        );

        if (input == null) {
            return;
        }

        double newBudget;

        try {
            newBudget = Double.parseDouble(input);
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Budget must be a valid number.");
            return;
        }

        if (newBudget < 0) {
            JOptionPane.showMessageDialog(this, "Budget cannot be negative.");
            return;
        }

        budgetService.updateCategoryBudget(category, newBudget);
        categoryBudgetRepository.save(
                budgetService.getCategoryBudgets(),
                AppPaths.DEFAULT_CATEGORY_BUDGET_FILE
        );

        updateSummary();

        JOptionPane.showMessageDialog(this, "Category budget updated.");
    }

    private void addTransaction() {
        String dateText = dateField.getText();
        String description = descriptionField.getText();
        String amountText = amountField.getText();
        String type = (String) typeComboBox.getSelectedItem();
        String category = (String) categoryComboBox.getSelectedItem();
        boolean recurringMonthly = recurringMonthlyCheckBox.isSelected();

        if (dateText.isBlank() || description.isBlank() || amountText.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a date, description, and amount.");
            return;
        }

        LocalDate date;

        try {
            date = LocalDate.parse(dateText);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Date must use this format: yyyy-mm-dd");
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
                date,
                description,
                amount,
                type,
                category,
                recurringMonthly
        );
        budgetService.addTransaction(transaction);

        markUnsavedChanges();

        clearInputFields();
        updateSummary();
    }

    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.");
            return;
        }

        String monthText = monthFilterField.getText();

        YearMonth selectedMonth;

        try {
            selectedMonth = YearMonth.parse(monthText);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Month must use this format: yyyy-mm");
            return;
        }

        List<Transaction> filteredTransactions = budgetService.getTransactionsForMonth(selectedMonth);
        Transaction selectedTransaction = filteredTransactions.get(selectedRow);

        budgetService.deleteTransaction(selectedTransaction);

        markUnsavedChanges();

        updateSummary();
    }

    public void clearAllTransactions() {
        if (!confirmDiscardUnsavedChanges()) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all transactions? This cannot be undone.",
                "Confirm Clear All",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        budgetService.clearAllTransactions();

        setCurrentTransactionFile(null);
        markUnsavedChanges();

        updateSummary();

        JOptionPane.showMessageDialog(this, "All transactions have been cleared.");
    }

    public void saveTransactions() {
        if (currentTransactionFile == null) {
            saveTransactionsAs();
            return;
        }

        transactionRepository.save(
                budgetService.getTransactions(),
                currentTransactionFile.toPath()
        );

        markChangesSaved();

        JOptionPane.showMessageDialog(this, "Transactions saved successfully.");
    }

    public void saveTransactionsAs() {
        JFileChooser fileChooser = new JFileChooser(budgetSaveFolder);
        fileChooser.setDialogTitle("Save Transactions CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int result = fileChooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();

        if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
        }

        if (selectedFile.exists()) {
            int overwriteChoice = JOptionPane.showConfirmDialog(
                    this,
                    "This file already exists. Do you want to overwrite it?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION
            );

            if (overwriteChoice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        transactionRepository.save(
                budgetService.getTransactions(),
                selectedFile.toPath()
        );

        setCurrentTransactionFile(selectedFile);
        markChangesSaved();

        JOptionPane.showMessageDialog(this, "Transactions saved successfully.");
    }

    public void loadTransactionsFromFile() {
        if (!confirmDiscardUnsavedChanges()) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser(budgetSaveFolder);
        fileChooser.setDialogTitle("Load Transactions CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int result = fileChooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();

        List<Transaction> loadedTransactions = transactionRepository.load(selectedFile.toPath());

        budgetService.replaceTransactions(loadedTransactions);

        setCurrentTransactionFile(selectedFile);
        markChangesSaved();

        updateSummary();

        JOptionPane.showMessageDialog(this, "Transactions loaded successfully.");
    }

    public void saveCategoryBudgetsAsDefault() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Save the current category budgets as your default settings?",
                "Save Category Budgets Default",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        categoryBudgetRepository.save(
                budgetService.getCategoryBudgets(),
                AppPaths.DEFAULT_CATEGORY_BUDGET_FILE
        );

        JOptionPane.showMessageDialog(this, "Category budgets saved as default.");
    }

    public void loadCategoryBudgetsFromFile() {
        JFileChooser fileChooser = new JFileChooser(categorySettingsFolder);
        fileChooser.setDialogTitle("Load Category Budgets CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int result = fileChooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();

        budgetService.replaceCategoryBudgets(
                categoryBudgetRepository.load(selectedFile.toPath())
        );

        updateSummary();

        JOptionPane.showMessageDialog(this, "Category budgets loaded successfully.");
    }

    private void clearInputFields() {
        dateField.setText(LocalDate.now().toString());
        descriptionField.setText("");
        amountField.setText("");
        typeComboBox.setSelectedIndex(0);
        categoryComboBox.setSelectedIndex(0);
        recurringMonthlyCheckBox.setSelected(false);
    }

    private void updateSummary() {
        String monthText = monthFilterField.getText();

        if (monthText.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a month using this format: yyyy-mm");
            return;
        }

        YearMonth selectedMonth;

        try {
            selectedMonth = YearMonth.parse(monthText);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Month must use this format: yyyy-mm");
            return;
        }

        double startingBalance = 0;
        double totalIncome = budgetService.calculateTotalIncomeForMonth(selectedMonth);
        double totalExpenses = budgetService.calculateTotalExpensesForMonth(selectedMonth);
        double balance;

        if (rolloverCheckBox.isSelected()) {
            startingBalance = budgetService.calculateStartingBalanceForMonth(selectedMonth);
            balance = budgetService.calculateBalanceForMonthWithRollover(selectedMonth);
        } else {
            balance = budgetService.calculateBalanceForMonth(selectedMonth);
        }

        startingBalanceLabel.setText("Starting Balance: " + CurrencyFormatter.format(startingBalance));
        incomeLabel.setText("Total Income: " + CurrencyFormatter.format(totalIncome));
        expensesLabel.setText("Total Expenses: " + CurrencyFormatter.format(totalExpenses));
        balanceLabel.setText("Current Balance: " + CurrencyFormatter.format(balance));

        updateStatus(balance, rolloverCheckBox.isSelected());

        refreshTableForMonth(selectedMonth);
        refreshCategoryBudgetTable(selectedMonth);
    }

    private void updateStatus(double balance, boolean rolloverEnabled) {
        if (balance > 0) {
            if (rolloverEnabled) {
                statusLabel.setText("Monthly Status: Good habit - you are carrying a positive balance forward.");
            } else {
                statusLabel.setText("Monthly Status: Good month - income is higher than expenses.");
            }
        } else if (balance < 0) {
            statusLabel.setText("Monthly Status: Over budget - expenses are higher than income.");
        } else {
            statusLabel.setText("Monthly Status: Break even - income and expenses are equal.");
        }
    }

    public void newBudget() {
        if (!confirmDiscardUnsavedChanges()) {
            return;
        }

        budgetService.clearAllTransactions();

        setCurrentTransactionFile(null);
        markChangesSaved();

        updateSummary();

        JOptionPane.showMessageDialog(this, "New blank budget created.");
    }


    private void refreshTableForMonth(YearMonth selectedMonth) {
        tableModel.setRowCount(0);

        List<Transaction> filteredTransactions = budgetService.getTransactionsForMonth(selectedMonth);

        for (Transaction transaction : filteredTransactions) {
            tableModel.addRow(new Object[]{
                    transaction.getDate(),
                    transaction.getDescription(),
                    CurrencyFormatter.format(transaction.getAmount()),
                    transaction.getType(),
                    transaction.getCategory(),
                    transaction.isRecurringMonthly() ? "Monthly" : "No"
            });
        }
    }

    private void refreshCategoryBudgetTable(YearMonth selectedMonth) {
        categoryBudgetTableModel.setRowCount(0);

        List<CategorySummary> categorySummaries = budgetService.getCategorySummariesForMonth(selectedMonth);

        for (CategorySummary summary : categorySummaries) {
            String status;

            if (summary.isOverBudget()) {
                status = "Over budget";
            } else {
                status = "Good";
            }

            categoryBudgetTableModel.addRow(new Object[]{
                    summary.getCategory(),
                    CurrencyFormatter.format(summary.getSpentAmount()),
                    CurrencyFormatter.format(summary.getBudgetLimit()),
                    CurrencyFormatter.format(summary.getRemainingAmount()),
                    status
            });
        }
    }

    private void markUnsavedChanges() {
        hasUnsavedChanges = true;
        updateSaveStatusLabel();
    }

    private void markChangesSaved() {
        hasUnsavedChanges = false;
        updateSaveStatusLabel();
    }

    private boolean confirmDiscardUnsavedChanges() {
        if (!hasUnsavedChanges) {
            return true;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes. Continue without saving?",
                "Unsaved Changes",
                JOptionPane.YES_NO_OPTION
        );

        return choice == JOptionPane.YES_OPTION;
    }

    private void updateSaveStatusLabel() {
        if (saveStatusLabel == null) {
            return;
        }

        if (hasUnsavedChanges) {
            saveStatusLabel.setText("Unsaved Changes");
        } else {
            saveStatusLabel.setText("Saved");
        }
    }

    private void setCurrentTransactionFile(File file) {
        currentTransactionFile = file;
        updateCurrentFileLabel();
    }

    private void updateCurrentFileLabel() {
        if (currentFileLabel == null) {
            return;
        }

        if (currentTransactionFile == null) {
            currentFileLabel.setText("Current File: Unsaved new budget");
        } else {
            currentFileLabel.setText("Current File: " + currentTransactionFile.getName());
        }
    }

    public void refreshView() {
        updateSummary();
        updateSaveStatusLabel();
        updateCurrentFileLabel();
    }

    public void handleExternalBudgetChange() {
        markUnsavedChanges();
        updateSummary();
    }

    public boolean canCloseSafely() {
        return confirmDiscardUnsavedChanges();
    }
}