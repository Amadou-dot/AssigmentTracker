import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A small modal calendar popup that lets the user click a date to select it.
 * Returns the chosen date as a LocalDate, or null if the dialog was cancelled.
 */
public class DatePickerDialog extends JDialog {

    private LocalDate selected;
    private YearMonth displayedMonth;

    private JLabel monthYearLabel;
    private JPanel gridPanel;

    /**
     * Shows the dialog and returns the picked date, or null if cancelled.
     *
     * @param owner   the parent component (used for positioning)
     * @param initial the date to highlight on open, or null for today
     */
    public static LocalDate show(Component owner, LocalDate initial) {
        Window parentWindow = SwingUtilities.getWindowAncestor(owner);
        DatePickerDialog dlg = (parentWindow instanceof Frame)
                ? new DatePickerDialog((Frame) parentWindow, initial)
                : new DatePickerDialog((Dialog) parentWindow, initial);
        dlg.setVisible(true);
        return dlg.selected;
    }

    private DatePickerDialog(Frame owner, LocalDate initial) {
        super(owner, "Pick a Date", true);
        init(initial);
    }

    private DatePickerDialog(Dialog owner, LocalDate initial) {
        super(owner, "Pick a Date", true);
        init(initial);
    }

    private void init(LocalDate initial) {
        selected = null;
        displayedMonth = YearMonth.from(initial != null ? initial : LocalDate.now());

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        gridPanel = new JPanel();
        add(gridPanel, BorderLayout.CENTER);
        rebuildGrid(initial);

        setResizable(false);
        pack();
        setLocationRelativeTo(getOwner());
    }

    // -----------------------------------------------------------------------
    // Header (month/year + prev/next)
    // -----------------------------------------------------------------------

    private JPanel buildHeader() {
        ThemeManager tm = ThemeManager.get();
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(tm.calHeaderBg());
        header.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JButton prev = makeNavBtn("\u25C0");
        JButton next = makeNavBtn("\u25B6");

        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setForeground(tm.calHeaderFg());
        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        prev.addActionListener(e -> {
            displayedMonth = displayedMonth.minusMonths(1);
            rebuildGrid(null);
        });
        next.addActionListener(e -> {
            displayedMonth = displayedMonth.plusMonths(1);
            rebuildGrid(null);
        });

        header.add(prev,            BorderLayout.WEST);
        header.add(monthYearLabel,  BorderLayout.CENTER);
        header.add(next,            BorderLayout.EAST);
        return header;
    }

    private JButton makeNavBtn(String symbol) {
        ThemeManager tm = ThemeManager.get();
        JButton btn = new JButton(symbol);
        btn.setForeground(tm.calHeaderFg());
        btn.setBackground(tm.calHeaderBg());
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        return btn;
    }

    // -----------------------------------------------------------------------
    // Grid
    // -----------------------------------------------------------------------

    private void rebuildGrid(LocalDate highlighted) {
        ThemeManager tm = ThemeManager.get();
        String monthName = displayedMonth.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.getDefault());
        monthYearLabel.setText(monthName + " " + displayedMonth.getYear());

        remove(gridPanel);
        gridPanel = new JPanel(new GridLayout(0, 7, 1, 1));
        gridPanel.setBackground(tm.calGridLine());
        gridPanel.setBorder(new LineBorder(tm.calGridLine()));

        // Day-name row
        String[] dayNames = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String n : dayNames) {
            JLabel lbl = new JLabel(n, SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(tm.calDayNameBg());
            lbl.setForeground(tm.calDayNameFg());
            lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            gridPanel.add(lbl);
        }

        LocalDate today        = LocalDate.now();
        LocalDate firstOfMonth = displayedMonth.atDay(1);
        int startOffset        = firstOfMonth.getDayOfWeek().getValue() % 7;
        LocalDate cellDate     = firstOfMonth.minusDays(startOffset);

        for (int i = 0; i < 42; i++) {
            gridPanel.add(buildCell(cellDate, today, highlighted));
            cellDate = cellDate.plusDays(1);
        }

        add(gridPanel, BorderLayout.CENTER);
        revalidate();
        pack();
    }

    private JPanel buildCell(LocalDate date, LocalDate today, LocalDate highlighted) {
        ThemeManager tm = ThemeManager.get();
        boolean isThisMonth  = YearMonth.from(date).equals(displayedMonth);
        boolean isToday      = date.equals(today);
        boolean isHighlighted = date.equals(highlighted);

        JPanel cell = new JPanel(new BorderLayout());
        cell.setPreferredSize(new Dimension(36, 30));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color bg;
        if (isHighlighted) {
            bg = tm.calHoverBorder();
        } else if (isToday) {
            bg = tm.calTodayBg();
        } else if (isThisMonth) {
            bg = tm.calThisMonthBg();
        } else {
            bg = tm.calOtherMonthBg();
        }
        cell.setBackground(bg);
        cell.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));

        JLabel lbl = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", isToday ? Font.BOLD : Font.PLAIN, 12));
        Color fg = isHighlighted ? Color.WHITE
                 : isThisMonth   ? tm.calDateFg()
                 :                 tm.calOtherMonthFg();
        lbl.setForeground(fg);
        cell.add(lbl, BorderLayout.CENTER);

        final LocalDate picked = date;
        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = picked;
                dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                cell.setBorder(new LineBorder(tm.calHoverBorder(), 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                cell.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
            }
        });

        return cell;
    }
}
