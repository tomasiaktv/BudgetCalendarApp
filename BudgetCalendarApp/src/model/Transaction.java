package model;

import java.time.LocalDate;
import java.util.UUID;

public class Transaction {

    private String id;
    private LocalDate date;
    private String description;
    private double amount;
    private String type;
    private String category;
    private boolean recurringMonthly;

    public Transaction(LocalDate date, String description, double amount, String type, String category) {
        this(UUID.randomUUID().toString(), date, description, amount, type, category, false);
    }

    public Transaction(LocalDate date, String description, double amount, String type, String category, boolean recurringMonthly) {
        this(UUID.randomUUID().toString(), date, description, amount, type, category, recurringMonthly);
    }

    public Transaction(String id, LocalDate date, String description, double amount, String type, String category, boolean recurringMonthly) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.recurringMonthly = recurringMonthly;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public boolean isRecurringMonthly() {
        return recurringMonthly;
    }
}