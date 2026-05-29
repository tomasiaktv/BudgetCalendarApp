package repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvCategoryBudgetRepository implements CategoryBudgetRepository {

    @Override
    public void save(Map<String, Double> categoryBudgets, Path filePath) {
        try {
            Files.createDirectories(filePath.getParent());

            StringBuilder fileContent = new StringBuilder();
            fileContent.append("category,budgetLimit\n");

            for (String category : categoryBudgets.keySet()) {
                fileContent.append(category)
                        .append(",")
                        .append(categoryBudgets.get(category))
                        .append("\n");
            }

            Files.writeString(filePath, fileContent.toString());

        } catch (IOException exception) {
            System.out.println("Could not save category budgets.");
            exception.printStackTrace();
        }
    }

    @Override
    public Map<String, Double> load(Path filePath) {
        Map<String, Double> categoryBudgets = new HashMap<>();

        if (!Files.exists(filePath)) {
            return categoryBudgets;
        }

        try {
            List<String> lines = Files.readAllLines(filePath);

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length != 2) {
                    continue;
                }

                String category = parts[0];
                double budgetLimit = Double.parseDouble(parts[1]);

                categoryBudgets.put(category, budgetLimit);
            }

        } catch (Exception exception) {
            System.out.println("Could not load category budgets.");
            exception.printStackTrace();
        }

        return categoryBudgets;
    }
}