package service;

import model.CategorySummary;
import model.Transaction;
import util.AppConstants;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultBudgetService implements BudgetService {

    private final ArrayList<Transaction> transactions;
    private final Map<String, Double> categoryBudgets;

    public DefaultBudgetService() {
        transactions = new ArrayList<>();
        categoryBudgets = new HashMap<>();

        setupDefaultCategoryBudgets();
    }

    private void setupDefaultCategoryBudgets() {
        categoryBudgets.put(AppConstants.CATEGORY_FOOD, 400.00);
        categoryBudgets.put(AppConstants.CATEGORY_RENT, 1200.00);
        categoryBudgets.put(AppConstants.CATEGORY_GAS, 200.00);
        categoryBudgets.put(AppConstants.CATEGORY_SCHOOL, 150.00);
        categoryBudgets.put(AppConstants.CATEGORY_ENTERTAINMENT, 100.00);
        categoryBudgets.put(AppConstants.CATEGORY_SAVINGS, 500.00);
        categoryBudgets.put(AppConstants.CATEGORY_OTHER, 250.00);
    }

    @Override
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    @Override
    public void addTransactions(List<Transaction> loadedTransactions) {
        transactions.addAll(loadedTransactions);
    }

    @Override
    public void replaceTransactions(List<Transaction> newTransactions) {
        transactions.clear();
        transactions.addAll(newTransactions);
    }

    @Override
    public void updateTransaction(Transaction oldTransaction, Transaction updatedTransaction) {
        for (int i = 0; i < transactions.size(); i++) {
            Transaction existingTransaction = transactions.get(i);

            if (existingTransaction.getId().equals(oldTransaction.getId())) {
                transactions.set(i, updatedTransaction);
                return;
            }
        }
    }

    @Override
    public void deleteTransaction(int index) {
        if (index >= 0 && index < transactions.size()) {
            transactions.remove(index);
        }
    }

    @Override
    public void deleteTransaction(Transaction transaction) {
        transactions.removeIf(existingTransaction ->
                existingTransaction.getId().equals(transaction.getId())
        );
    }

    @Override
    public void clearAllTransactions() {
        transactions.clear();
    }

    @Override
    public List<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public List<Transaction> getTransactionsForDate(LocalDate date) {
        List<Transaction> filteredTransactions = new ArrayList<>();

        for (Transaction transaction : transactions) {
            if (transaction.isRecurringMonthly()) {
                Transaction occurrence = createRecurringOccurrenceForMonth(
                        transaction,
                        YearMonth.from(date)
                );

                if (occurrence != null && occurrence.getDate().equals(date)) {
                    filteredTransactions.add(occurrence);
                }
            } else if (transaction.getDate().equals(date)) {
                filteredTransactions.add(transaction);
            }
        }

        return filteredTransactions;
    }

    @Override
    public List<Transaction> getTransactionsForMonth(YearMonth selectedMonth) {
        List<Transaction> filteredTransactions = new ArrayList<>();

        for (Transaction transaction : transactions) {
            if (transaction.isRecurringMonthly()) {
                Transaction occurrence = createRecurringOccurrenceForMonth(transaction, selectedMonth);

                if (occurrence != null) {
                    filteredTransactions.add(occurrence);
                }
            } else {
                YearMonth transactionMonth = YearMonth.from(transaction.getDate());

                if (transactionMonth.equals(selectedMonth)) {
                    filteredTransactions.add(transaction);
                }
            }
        }

        return filteredTransactions;
    }

    @Override
    public double calculateBalance() {
        double balance = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Income")) {
                balance += transaction.getAmount();
            } else {
                balance -= transaction.getAmount();
            }
        }

        return balance;
    }

    @Override
    public double calculateTotalIncome() {
        double totalIncome = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Income")) {
                totalIncome += transaction.getAmount();
            }
        }

        return totalIncome;
    }

    @Override
    public double calculateTotalExpenses() {
        double totalExpenses = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Expense")) {
                totalExpenses += transaction.getAmount();
            }
        }

        return totalExpenses;
    }

    @Override
    public double calculateTotalIncomeForMonth(YearMonth selectedMonth) {
        double totalIncome = 0;

        for (Transaction transaction : getTransactionsForMonth(selectedMonth)) {
            if (AppConstants.TYPE_INCOME.equals(transaction.getType())) {
                totalIncome += transaction.getAmount();
            }
        }

        return totalIncome;
    }

    @Override
    public double calculateTotalExpensesForMonth(YearMonth selectedMonth) {
        double totalExpenses = 0;

        for (Transaction transaction : getTransactionsForMonth(selectedMonth)) {
            if (AppConstants.TYPE_EXPENSE.equals(transaction.getType())) {
                totalExpenses += transaction.getAmount();
            }
        }

        return totalExpenses;
    }

    @Override
    public double calculateBalanceForMonth(YearMonth selectedMonth) {
        double totalIncome = calculateTotalIncomeForMonth(selectedMonth);
        double totalExpenses = calculateTotalExpensesForMonth(selectedMonth);

        return totalIncome - totalExpenses;
    }

    @Override
    public double calculateStartingBalanceForMonth(YearMonth selectedMonth) {
        double startingBalance = 0;

        if (transactions.isEmpty()) {
            return startingBalance;
        }

        YearMonth earliestMonth = YearMonth.from(transactions.get(0).getDate());

        for (Transaction transaction : transactions) {
            YearMonth transactionMonth = YearMonth.from(transaction.getDate());

            if (transactionMonth.isBefore(earliestMonth)) {
                earliestMonth = transactionMonth;
            }
        }

        YearMonth currentMonth = earliestMonth;

        while (currentMonth.isBefore(selectedMonth)) {
            startingBalance += calculateBalanceForMonth(currentMonth);
            currentMonth = currentMonth.plusMonths(1);
        }

        return startingBalance;
    }

    @Override
    public double calculateBalanceForMonthWithRollover(YearMonth selectedMonth) {
        double startingBalance = calculateStartingBalanceForMonth(selectedMonth);
        double monthlyBalance = calculateBalanceForMonth(selectedMonth);

        return startingBalance + monthlyBalance;
    }

    @Override
    public List<CategorySummary> getCategorySummariesForMonth(YearMonth selectedMonth) {
        List<CategorySummary> summaries = new ArrayList<>();

        for (String category : categoryBudgets.keySet()) {
            double spentAmount = calculateExpensesForCategoryAndMonth(category, selectedMonth);
            double budgetLimit = categoryBudgets.get(category);

            CategorySummary summary = new CategorySummary(category, spentAmount, budgetLimit);
            summaries.add(summary);
        }

        return summaries;
    }

    private double calculateExpensesForCategoryAndMonth(String category, YearMonth selectedMonth) {
        double total = 0;

        for (Transaction transaction : getTransactionsForMonth(selectedMonth)) {
            boolean sameCategory = transaction.getCategory().equals(category);
            boolean isExpense = AppConstants.TYPE_EXPENSE.equals(transaction.getType());

            if (sameCategory && isExpense) {
                total += transaction.getAmount();
            }
        }

        return total;
    }

    @Override
    public void updateCategoryBudget(String category, double newBudgetLimit) {
        if (categoryBudgets.containsKey(category)) {
            categoryBudgets.put(category, newBudgetLimit);
        }
    }

    @Override
    public double getCategoryBudget(String category) {
        return categoryBudgets.getOrDefault(category, 0.0);
    }

    @Override
    public Map<String, Double> getCategoryBudgets() {
        return categoryBudgets;
    }

    @Override
    public void replaceCategoryBudgets(Map<String, Double> loadedCategoryBudgets) {
        for (String category : loadedCategoryBudgets.keySet()) {
            if (categoryBudgets.containsKey(category)) {
                categoryBudgets.put(category, loadedCategoryBudgets.get(category));
            }
        }
    }

    private Transaction createRecurringOccurrenceForMonth(Transaction transaction, YearMonth selectedMonth) {
        YearMonth originalMonth = YearMonth.from(transaction.getDate());

        if (selectedMonth.isBefore(originalMonth)) {
            return null;
        }

        int originalDay = transaction.getDate().getDayOfMonth();

        if (originalDay > selectedMonth.lengthOfMonth()) {
            return null;
        }

        LocalDate occurrenceDate = selectedMonth.atDay(originalDay);

        return new Transaction(
                transaction.getId(),
                occurrenceDate,
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                true
        );
    }
}