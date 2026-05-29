package repository;

import java.nio.file.Path;
import java.util.Map;

public interface CategoryBudgetRepository {

    void save(Map<String, Double> categoryBudgets, Path filePath);

    Map<String, Double> load(Path filePath);
}