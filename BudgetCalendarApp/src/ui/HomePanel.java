package ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class HomePanel extends JPanel {

    public HomePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        add(createTitlePanel(), BorderLayout.NORTH);
        add(createFeaturePanel(), BorderLayout.CENTER);
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Budget Calendar App");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel subtitleLabel = new JLabel("Track income, expenses, category budgets, and month-to-date balances.");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        return titlePanel;
    }

    private JPanel createFeaturePanel() {
        JPanel featurePanel = new JPanel(new GridLayout(2, 2, 20, 20));

        featurePanel.add(createFeatureCard(
                "Budget Tracker",
                "Add, edit, delete, save, and load monthly budget transactions."
        ));

        featurePanel.add(createFeatureCard(
                "Monthly Calendar",
                "View budget entries inside a calendar with running balances."
        ));

        featurePanel.add(createFeatureCard(
                "Category Budgets",
                "Track spending limits for food, rent, gas, school, entertainment, and more."
        ));

        featurePanel.add(createFeatureCard(
                "CSV Storage",
                "Save and load budget files using clean repository-based CSV persistence."
        ));

        return featurePanel;
    }

    private JPanel createFeatureCard(String title, String description) {
        JPanel cardPanel = new JPanel(new BorderLayout(10, 10));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel descriptionLabel = new JLabel("<html>" + description + "</html>");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        cardPanel.add(titleLabel, BorderLayout.NORTH);
        cardPanel.add(descriptionLabel, BorderLayout.CENTER);

        return cardPanel;
    }
}