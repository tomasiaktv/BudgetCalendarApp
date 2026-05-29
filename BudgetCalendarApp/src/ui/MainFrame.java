package ui;

import service.BudgetService;
import service.DefaultBudgetService;
import ui.menu.MainMenuBar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private JPanel contentPanel;

    private final BudgetService budgetService;
    private final BudgetPanel budgetPanel;
    private final CalendarPanel calendarPanel;

    public MainFrame() {
        setTitle("Budget Calendar App");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        budgetService = new DefaultBudgetService();
        budgetPanel = new BudgetPanel(budgetService);
        calendarPanel = new CalendarPanel(budgetService);

        calendarPanel.setBudgetDataChangeListener(() -> budgetPanel.handleExternalBudgetChange());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                attemptExit();
            }
        });

        setJMenuBar(new MainMenuBar(budgetPanel, this::attemptExit));

        createLayout();
    }

    private void createLayout() {
        JPanel navigationPanel = createNavigationPanel();

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        showPanel(new HomePanel());

        add(navigationPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createNavigationPanel() {
        JPanel navigationPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton homeButton = new JButton("Home");
        JButton budgetButton = new JButton("Budget");
        JButton calendarButton = new JButton("Calendar");
        JButton exitButton = new JButton("Exit");

        homeButton.addActionListener(event -> showPanel(new HomePanel()));

        budgetButton.addActionListener(event -> {
            budgetPanel.refreshView();
            showPanel(budgetPanel);
        });

        calendarButton.addActionListener(event -> {
            calendarPanel.refreshView();
            showPanel(calendarPanel);
        });

        exitButton.addActionListener(event -> System.exit(0));

        navigationPanel.add(homeButton);
        navigationPanel.add(budgetButton);
        navigationPanel.add(calendarButton);
        navigationPanel.add(exitButton);

        return navigationPanel;
    }

    private void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void attemptExit() {
        if (budgetPanel.canCloseSafely()) {
            dispose();
            System.exit(0);
        }
    }
}