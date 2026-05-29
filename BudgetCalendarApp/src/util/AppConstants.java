package util;

public final class AppConstants {

    public static final String TYPE_INCOME = "Income";
    public static final String TYPE_EXPENSE = "Expense";

    public static final String[] TRANSACTION_TYPES = {
            TYPE_INCOME,
            TYPE_EXPENSE
    };

    public static final String CATEGORY_PAYCHECK = "Paycheck";
    public static final String CATEGORY_FOOD = "Food";
    public static final String CATEGORY_RENT = "Rent";
    public static final String CATEGORY_GAS = "Gas";
    public static final String CATEGORY_SCHOOL = "School";
    public static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    public static final String CATEGORY_SAVINGS = "Savings";
    public static final String CATEGORY_OTHER = "Other";

    public static final String[] TRANSACTION_CATEGORIES = {
            CATEGORY_PAYCHECK,
            CATEGORY_FOOD,
            CATEGORY_RENT,
            CATEGORY_GAS,
            CATEGORY_SCHOOL,
            CATEGORY_ENTERTAINMENT,
            CATEGORY_SAVINGS,
            CATEGORY_OTHER
    };

    private AppConstants() {
    }
}