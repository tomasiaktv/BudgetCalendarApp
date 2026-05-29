package model;

public class CategorySummary {

    private String category;
    private double spentAmount;
    private double budgetLimit;

    public CategorySummary(String category, double spentAmount, double budgetLimit) {
        this.category = category;
        this.spentAmount = spentAmount;
        this.budgetLimit = budgetLimit;
    }

    public String getCategory() {
        return category;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public double getRemainingAmount() {
        return budgetLimit - spentAmount;
    }

    public boolean isOverBudget() {
        return spentAmount > budgetLimit;
    }
}