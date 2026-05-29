package service;

import model.CategorySummary;
import model.Transaction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface BudgetService {

    void addTransaction(Transaction transaction);

    void addTransactions(List<Transaction> loadedTransactions);

    void replaceTransactions(List<Transaction> newTransactions);

    void updateTransaction(Transaction oldTransaction, Transaction updatedTransaction);

    void deleteTransaction(int index);

    void deleteTransaction(Transaction transaction);

    void clearAllTransactions();

    List<Transaction> getTransactions();

    List<Transaction> getTransactionsForMonth(YearMonth selectedMonth);

    List<Transaction> getTransactionsForDate(LocalDate date);

    double calculateBalance();

    double calculateTotalIncome();

    double calculateTotalExpenses();

    double calculateTotalIncomeForMonth(YearMonth selectedMonth);

    double calculateTotalExpensesForMonth(YearMonth selectedMonth);

    double calculateBalanceForMonth(YearMonth selectedMonth);

    double calculateStartingBalanceForMonth(YearMonth selectedMonth);

    double calculateBalanceForMonthWithRollover(YearMonth selectedMonth);

    List<CategorySummary> getCategorySummariesForMonth(YearMonth selectedMonth);

    void updateCategoryBudget(String category, double newBudgetLimit);

    double getCategoryBudget(String category);

    Map<String, Double> getCategoryBudgets();

    void replaceCategoryBudgets(Map<String, Double> loadedCategoryBudgets);
}