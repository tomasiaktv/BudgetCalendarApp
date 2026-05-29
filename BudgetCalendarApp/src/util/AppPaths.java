package util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppPaths {

    public static final Path DATA_FOLDER = Paths.get("data");
    public static final Path BUDGETS_FOLDER = DATA_FOLDER.resolve("budgets");
    public static final Path SETTINGS_FOLDER = DATA_FOLDER.resolve("settings");

    public static final Path DEFAULT_CATEGORY_BUDGET_FILE =
            SETTINGS_FOLDER.resolve("category_budgets.csv");

    private AppPaths() {
    }

    public static File getBudgetsFolderAsFile() {
        return BUDGETS_FOLDER.toFile();
    }

    public static File getSettingsFolderAsFile() {
        return SETTINGS_FOLDER.toFile();
    }
}