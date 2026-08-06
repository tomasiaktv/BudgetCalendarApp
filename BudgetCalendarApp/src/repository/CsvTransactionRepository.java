package repository;

import model.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CsvTransactionRepository implements TransactionRepository {

    @Override
    public void save(List<Transaction> transactions, Path filePath) {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,date,description,amount,type,category,recurringMonthly");

            for (Transaction transaction : transactions) {
                lines.add(toCsvLine(transaction));
            }

            Files.write(filePath, lines);

        } catch (IOException exception) {
            System.out.println("Could not save transactions.");
            exception.printStackTrace();
        }
    }

    @Override
    public List<Transaction> load(Path filePath) {
        List<Transaction> transactions = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return transactions;
        }

        try {
            List<String> lines = Files.readAllLines(filePath);

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.isBlank()) {
                    continue;
                }

                List<String> values = parseCsvLine(line);

                if (values.size() != 5 && values.size() != 7) {
                    continue;
                }

                int idx = 0;

                String id = values.size() == 7 ? values.get(idx++) : null;
                LocalDate date = LocalDate.parse(values.get(idx++));
                String description = values.get(idx++);
                double amount = Double.parseDouble(values.get(idx++));
                String type = values.get(idx++);
                String category = values.get(idx++);
                boolean recurringMonthly = values.size() == 7 ? Boolean.parseBoolean(values.get(idx++)) : false;

                Transaction transaction;

                if (id == null) {
                    transaction = new Transaction(
                            date,
                            description,
                            amount,
                            type,
                            category,
                            recurringMonthly
                    );
                } else {
                    transaction = new Transaction(
                            id,
                            date,
                            description,
                            amount,
                            type,
                            category,
                            recurringMonthly
                    );
                }

                transactions.add(transaction);
            }

        } catch (Exception exception) {
            System.out.println("Could not load transactions.");
            exception.printStackTrace();
        }

        return transactions;
    }

    private String toCsvLine(Transaction transaction) {
        return escapeCsv(transaction.getId()) + ","
                + escapeCsv(transaction.getDate().toString()) + ","
                + escapeCsv(transaction.getDescription()) + ","
                + escapeCsv(String.valueOf(transaction.getAmount())) + ","
                + escapeCsv(transaction.getType()) + ","
                + escapeCsv(transaction.getCategory()) + ","
                + escapeCsv(String.valueOf(transaction.isRecurringMonthly()));
    }

    private String escapeCsv(String value) {
        String escapedValue = value.replace("\"", "\"\"");

        if (escapedValue.contains(",") || escapedValue.contains("\"")) {
            return "\"" + escapedValue + "\"";
        }

        return escapedValue;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);

            if (currentChar == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentValue.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (currentChar == ',' && !insideQuotes) {
                values.add(currentValue.toString());
                currentValue.setLength(0);
            } else {
                currentValue.append(currentChar);
            }
        }

        values.add(currentValue.toString());

        return values;
    }
}