import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Assignment Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // center on screen

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top navigation bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navBar.setBackground(new Color(45, 45, 48));
        JLabel appTitle = new JLabel("Assignment Tracker");
        appTitle.setForeground(Color.WHITE);
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        appTitle.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        navBar.add(appTitle);

        // Center placeholder content
        JPanel centerPanel = new JPanel(new GridBagLayout());
        JLabel placeholder = new JLabel("Your content goes here");
        placeholder.setFont(new Font("SansSerif", Font.PLAIN, 16));
        placeholder.setForeground(Color.GRAY);
        centerPanel.add(placeholder);

        // Status bar at the bottom
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusBar.add(statusLabel);

        mainPanel.add(navBar, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
}
