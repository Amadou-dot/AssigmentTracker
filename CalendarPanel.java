import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.*;

public class CalendarPanel extends JPanel {

    private static final Color HEADER_BG      = new Color(45, 45, 48);
    private static final Color HEADER_FG      = Color.WHITE;
    private static final Color DAY_NAME_BG    = new Color(60, 60, 65);
    private static final Color DAY_NAME_FG    = new Color(180, 180, 180);
    private static final Color TODAY_BG       = new Color(255, 223, 100);
    private static final Color THIS_MONTH_BG  = Color.WHITE;
    private static final Color OTHER_MONTH_BG = new Color(245, 245, 245);
    private static final Color OTHER_MONTH_FG = new Color(180, 180, 180);
    private static final Color GRID_LINE      = new Color(220, 220, 220);
    private static final Color HOVER_BORDER   = new Color(100, 150, 220);

    private YearMonth displayedMonth;

    private JLabel monthYearLabel;
    private JPanel gridPanel;

    public CalendarPanel() {
        this.displayedMonth = YearMonth.now();
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        gridPanel = new JPanel();
        add(gridPanel, BorderLayout.CENTER);
        rebuildGrid();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JButton prevButton = makeNavButton("◀");
        JButton nextButton = makeNavButton("▶");

        prevButton.addActionListener(e -> {
            displayedMonth = displayedMonth.minusMonths(1);
            rebuildGrid();
        });
        nextButton.addActionListener(e -> {
            displayedMonth = displayedMonth.plusMonths(1);
            rebuildGrid();
        });

        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setForeground(HEADER_FG);
        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        header.add(prevButton,     BorderLayout.WEST);
        header.add(monthYearLabel, BorderLayout.CENTER);
        header.add(nextButton,     BorderLayout.EAST);
        return header;
    }

    private JButton makeNavButton(String symbol) {
        JButton btn = new JButton(symbol);
        btn.setForeground(HEADER_FG);
        btn.setBackground(HEADER_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        return btn;
    }

    private void rebuildGrid() {
        String monthName = displayedMonth.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.getDefault());
        monthYearLabel.setText(monthName + " " + displayedMonth.getYear());

        remove(gridPanel);
        gridPanel = new JPanel(new GridLayout(0, 7, 1, 1));
        gridPanel.setBackground(GRID_LINE);
        gridPanel.setBorder(BorderFactory.createLineBorder(GRID_LINE));

        addDayNameRow();
        addDateCells();

        add(gridPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void addDayNameRow() {
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String name : dayNames) {
            JLabel label = new JLabel(name, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(DAY_NAME_BG);
            label.setForeground(DAY_NAME_FG);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            label.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            gridPanel.add(label);
        }
    }

    private void addDateCells() {
        LocalDate today        = LocalDate.now();
        LocalDate firstOfMonth = displayedMonth.atDay(1);

        int startOffset = firstOfMonth.getDayOfWeek().getValue() % 7;

        LocalDate cellDate = firstOfMonth.minusDays(startOffset);

        for (int i = 0; i < 42; i++) {
            boolean isThisMonth = YearMonth.from(cellDate).equals(displayedMonth);
            boolean isToday     = cellDate.equals(today);
            gridPanel.add(buildDateCell(cellDate, isThisMonth, isToday));
            cellDate = cellDate.plusDays(1);
        }
    }

    private JPanel buildDateCell(LocalDate date, boolean isThisMonth, boolean isToday) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (isToday) {
            cell.setBackground(TODAY_BG);
        } else if (isThisMonth) {
            cell.setBackground(THIS_MONTH_BG);
        } else {
            cell.setBackground(OTHER_MONTH_BG);
        }

        JLabel dateLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
        dateLabel.setFont(new Font("SansSerif", isToday ? Font.BOLD : Font.PLAIN, 13));
        dateLabel.setForeground(isThisMonth ? Color.DARK_GRAY : OTHER_MONTH_FG);
        cell.add(dateLabel, BorderLayout.NORTH);

        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cell.setBorder(BorderFactory.createLineBorder(HOVER_BORDER, 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                cell.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            }
        });

        return cell;
    }
}