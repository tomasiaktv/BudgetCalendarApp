package ui.menu;

import ui.BudgetPanel;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainMenuBar extends JMenuBar {

    public MainMenuBar(BudgetPanel budgetPanel, Runnable exitAction) {
        add(createFileMenu(budgetPanel, exitAction));
        add(createSettingsMenu(budgetPanel));
    }

    private JMenu createFileMenu(BudgetPanel budgetPanel, Runnable exitAction) {
        JMenu fileMenu = new JMenu("File");

        JMenuItem newBudgetItem = new JMenuItem("New Budget");
        JMenuItem loadBudgetItem = new JMenuItem("Load Budget CSV");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As");
        JMenuItem clearAllItem = new JMenuItem("Clear All Transactions");
        JMenuItem exitItem = new JMenuItem("Exit");

        newBudgetItem.addActionListener(event -> budgetPanel.newBudget());
        loadBudgetItem.addActionListener(event -> budgetPanel.loadTransactionsFromFile());
        saveItem.addActionListener(event -> budgetPanel.saveTransactions());
        saveAsItem.addActionListener(event -> budgetPanel.saveTransactionsAs());
        clearAllItem.addActionListener(event -> budgetPanel.clearAllTransactions());
        exitItem.addActionListener(event -> exitAction.run());

        fileMenu.add(newBudgetItem);
        fileMenu.add(loadBudgetItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(clearAllItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        return fileMenu;
    }

    private JMenu createSettingsMenu(BudgetPanel budgetPanel) {
        JMenu settingsMenu = new JMenu("Settings");

        JMenuItem loadCategoryBudgetsItem = new JMenuItem("Load Category Budgets");
        JMenuItem saveCategoryBudgetsItem = new JMenuItem("Save Category Budgets Default");

        loadCategoryBudgetsItem.addActionListener(event -> budgetPanel.loadCategoryBudgetsFromFile());
        saveCategoryBudgetsItem.addActionListener(event -> budgetPanel.saveCategoryBudgetsAsDefault());

        settingsMenu.add(loadCategoryBudgetsItem);
        settingsMenu.add(saveCategoryBudgetsItem);

        return settingsMenu;
    }
}