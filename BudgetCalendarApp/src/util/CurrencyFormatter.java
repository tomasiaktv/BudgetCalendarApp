package util;

public final class CurrencyFormatter {

    private CurrencyFormatter() {
    }

    public static String format(double amount) {
        return String.format("$%s%.2f", (amount < 0)?"-":"", Math.abs(amount));
    }

    public static String formatNoSign(double amount){
        return String.format("%s%.2f", (amount < 0)?"-":"", Math.abs(amount));
    }
}