package util;

public final class CurrencyFormatter {

    private CurrencyFormatter() {
    }

    public static String format(double amount) {
        if (amount < 0) {
            return String.format("-$%.2f", Math.abs(amount));
        }

        return String.format("$%.2f", amount);
    }

    public static String formatNoSign(double amount){
        if (amount < 0) {
            return String.format("-%.2f", Math.abs(amount));
        }

        return String.format("%.2f", amount);
    }
}