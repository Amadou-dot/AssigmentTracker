import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Main class with main method invoked on app start.
 * @version 1.0.4
 * @author Dr. Jody Paul
 * @author Ayslynn Wardall, Kenneth Pyron, Amadou Seck
 */
public class Main {
    /** Private constructor to prevent instantiation of entry point class. */
    private Main() { }

    /**
     * Invoked on start.
     * @param args ignored
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    /** Escapes the characters that matter for Swing's HTML tooltip renderer. */
    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /** Draws a small calendar icon at the given size using Java2D. */
    private static ImageIcon makeCalendarIcon(int size) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = size / 5;
        // Body
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 2, size, size - 2, arc, arc);
        // Header bar
        g.setColor(new Color(80, 120, 200));
        g.fillRoundRect(0, 2, size, size / 3, arc, arc);
        g.fillRect(0, size / 3 - arc / 2, size, arc / 2 + 1); // square off bottom of header
        // Border
        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(0, 2, size - 1, size - 3, arc, arc);
        // Ring pegs at top
        g.setColor(new Color(60, 60, 60));
        int pegY = 0;
        int pegW = Math.max(2, size / 8);
        int pegH = size / 4;
        g.fillRoundRect(size / 4 - pegW / 2, pegY, pegW, pegH, 2, 2);
        g.fillRoundRect(3 * size / 4 - pegW / 2, pegY, pegW, pegH, 2, 2);
        // Grid dots (3x2)
        g.setColor(new Color(80, 120, 200));
        int dotSize = Math.max(1, size / 10);
        int gridTop = size / 3 + size / 8;
        int rowH = (size - gridTop - 2) / 2;
        int colW = size / 3;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int dx = colW * col + colW / 2 - dotSize / 2;
                int dy = gridTop + row * rowH + rowH / 2 - dotSize / 2;
                g.fillRect(dx, dy, dotSize + 1, dotSize + 1);
            }
        }
        g.dispose();
        return new ImageIcon(img);
    }

    private static void createAndShowGUI() {
        ThemeManager theme = ThemeManager.get();

        JFrame frame = new JFrame("Assignment Tracker");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(750, 550);
        frame.setLocationRelativeTo(null);

        // --- Data models ---
        DefaultListModel<Subject> subjectListModel = new DefaultListModel<>();
        DataStore.load(subjectListModel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DataStore.save(subjectListModel);
                frame.dispose();
            }
        });
        // 4 columns to match Subject's table model (see Subject.java).
        // The 4th column ("Notes") is hidden from the view below and edited
        // through the Notes button.
        DefaultTableModel emptyTableModel = new DefaultTableModel(
                new String[]{"Assignment", "Due Date", "Done", "Notes"}, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 2 ? Boolean.class : String.class; // col 2 = "Done" checkbox
            }
        };

        // --- Center: assignment table ---
        // Override getToolTipText so hovering a row shows its note.
        // The Notes column is hidden from the view but still in the model at
        // index 3, which is where we read the tooltip text from.
        JTable table = new JTable(emptyTableModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row < 0) return null;
                Object note = getModel().getValueAt(row, 3);
                if (note == null) return null;
                String text = note.toString();
                if (text.isEmpty()) return null; // no tooltip when there's no note
                // Wrap in HTML so long notes wrap; escape user text first.
                return "<html><p style='width:240px'>" + escapeHtml(text) + "</p></html>";
            }
        };
        // JTable doesn't listen for tooltips unless a tooltip is set or the
        // component is registered. Explicit register keeps the intent obvious.
        ToolTipManager.sharedInstance().registerComponent(table);

        // Hide the "Notes" column from the user. The data stays in the model
        // (so saving/loading still sees it); only the view drops the column.
        // Must be called after every setModel() because swapping the model
        // rebuilds the JTable's column view from scratch.
        Runnable hideNotesColumn = () -> {
            if (table.getColumnCount() > 3) {
                table.removeColumn(table.getColumnModel().getColumn(3));
            }
        };
        hideNotesColumn.run();
        table.setRowHeight(24);
        table.setBackground(theme.contentBg());
        table.setForeground(theme.contentFg());
        table.getTableHeader().setBackground(theme.contentBg());
        table.getTableHeader().setForeground(theme.contentFg());

        // --- South: input panel (disabled until a class is selected) ---
        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        // dateField shows MM/dd/yyyy to the user but is read-only; picker button sets it
        JTextField nameField = new JTextField(20);
        JTextField dateField = new JTextField(10);
        dateField.setEditable(false);
        dateField.setToolTipText("Click 📅 to pick a date");
        JButton datePickerButton = new JButton(makeCalendarIcon(16));
        datePickerButton.setToolTipText("Pick a date");
        datePickerButton.setFocusPainted(false);
        // Holds the date in MM/dd/yyyy format; empty string when unset
        final LocalDate[] pickedDate = {null};
        datePickerButton.addActionListener(e -> {
            LocalDate initial = pickedDate[0] != null ? pickedDate[0] : LocalDate.now();
            LocalDate chosen = DatePickerDialog.show(datePickerButton, initial);
            if (chosen != null) {
                pickedDate[0] = chosen;
                dateField.setText(chosen.format(displayFmt));
            }
        });
        JButton addButton = new JButton("Add");
        JButton removeButton = new JButton("Remove Selected");
        // Notes button opens a dialog to edit the selected assignment's note.
        // Disabled until a subject AND a row are both selected.
        JButton notesButton = new JButton("Notes…");
        notesButton.setToolTipText("Edit notes for the selected assignment");
        nameField.setEnabled(false);
        dateField.setEnabled(false);
        datePickerButton.setEnabled(false);
        addButton.setEnabled(false);
        removeButton.setEnabled(false);
        notesButton.setEnabled(false);

        JLabel darkModeLabel = new JLabel(theme.isDark() ? "Light Mode" : "Dark Mode");
        JToggleButton darkModeToggle = new JToggleButton(theme.isDark() ? "☀" : "☾");
        darkModeToggle.setSelected(theme.isDark());
        darkModeToggle.setFocusPainted(false);
        darkModeToggle.setToolTipText("Toggle dark/light mode");

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Assignment:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Due Date:"));
        inputPanel.add(dateField);
        inputPanel.add(datePickerButton);
        inputPanel.add(addButton);
        inputPanel.add(removeButton);
        inputPanel.add(notesButton);

        // Spacer to push toggle to the right
        inputPanel.add(Box.createHorizontalStrut(20));
        inputPanel.add(darkModeLabel);
        inputPanel.add(darkModeToggle);

        // --- West: sidebar ---
        JList<Subject> subjectList = new JList<>(subjectListModel);
        subjectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subjectList.setBackground(theme.sidebarBg());
        subjectList.setForeground(theme.sidebarFg());
        subjectList.setSelectionBackground(theme.sidebarSelBg());
        subjectList.setSelectionForeground(theme.sidebarSelFg());
        subjectList.setFont(subjectList.getFont().deriveFont(13f));

        JButton newClassButton = new JButton("New Class");
        JButton renameButton = new JButton("Rename");
        JButton deleteButton = new JButton("Delete");
        renameButton.setEnabled(false);
        deleteButton.setEnabled(false);

        JPanel sidebarButtonPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        sidebarButtonPanel.add(newClassButton);
        sidebarButtonPanel.add(renameButton);
        sidebarButtonPanel.add(deleteButton);
        sidebarButtonPanel.setBackground(theme.sidebarBg());

        JScrollPane sidebarScroll = new JScrollPane(subjectList);
        sidebarScroll.getViewport().setBackground(theme.sidebarBg());

        JPanel sidebarPanel = new JPanel(new BorderLayout(0, 4));
        sidebarPanel.setPreferredSize(new Dimension(150, 0));
        sidebarPanel.setBackground(theme.sidebarBg());
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        sidebarPanel.add(sidebarScroll, BorderLayout.CENTER);
        sidebarPanel.add(sidebarButtonPanel, BorderLayout.SOUTH);

        // --- Calendar panel ---
        // Built up here (before the action listeners below) so those listeners
        // can capture it and call refresh() when assignments change.
        CalendarPanel calendarPanel = new CalendarPanel(subjectListModel);

        // Theme listener registered later (after tableScroll/tabs are in scope)

        // Apply initial theme to input panel and frame background
        inputPanel.setBackground(theme.contentBg());
        nameField.setBackground(theme.inputBg());
        nameField.setForeground(theme.inputFg());
        dateField.setBackground(theme.inputBg());
        dateField.setForeground(theme.inputFg());
        frame.getContentPane().setBackground(theme.contentBg());
        for (Component c : inputPanel.getComponents()) {
            if (c instanceof JLabel) {
                ((JLabel) c).setForeground(theme.contentFg());
            }
        }

        // --- Dark mode toggle action ---
        darkModeToggle.addActionListener(e -> theme.toggle());

        // --- Selection listener: swap table model, toggle controls ---
        subjectList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Subject selected = subjectList.getSelectedValue();
            boolean subjectSelected = selected != null;
            table.setModel(subjectSelected ? selected.getTableModel() : emptyTableModel);
            hideNotesColumn.run(); // setModel rebuilds the view — re-hide Notes
            nameField.setEnabled(subjectSelected);
            dateField.setEnabled(subjectSelected);
            datePickerButton.setEnabled(subjectSelected);
            addButton.setEnabled(subjectSelected);
            removeButton.setEnabled(subjectSelected);
            renameButton.setEnabled(subjectSelected);
            deleteButton.setEnabled(subjectSelected);
            // Swapping the model clears the table selection, so the Notes
            // button has no row to act on until the user picks one.
            notesButton.setEnabled(false);
        });

        // Notes button needs BOTH a subject and a selected row to be enabled.
        // The table's selection model is stable across setModel() calls, so we
        // only need to wire this once.
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            notesButton.setEnabled(subjectList.getSelectedValue() != null
                    && table.getSelectedRow() >= 0);
        });

        // --- Assignment actions ---
        // Each listener calls getSelectedValue() at the moment it fires, rather than
        // capturing the selection up front. This ensures the selection is always current.
        addButton.addActionListener(e -> {
            Subject selected = subjectList.getSelectedValue();
            if (selected == null) return;
            String name = nameField.getText().trim();
            String date = dateField.getText().trim(); // already MM/dd/yyyy or empty
            if (!name.isEmpty()) {
                // New assignments start with an empty note ("" — not null,
                // so the JSON writer and the notes dialog don't have to
                // special-case nulls).
                selected.getTableModel().addRow(new Object[]{name, date, false, ""});
                nameField.setText("");
                dateField.setText("");
                pickedDate[0] = null;
                nameField.requestFocus();
                calendarPanel.refresh(); // keep due-date markers in sync
            }
        });

        // --- Remove assignment ---
        removeButton.addActionListener(e -> {
            Subject selected = subjectList.getSelectedValue();
            if (selected == null) return;
            int row = table.getSelectedRow();
            if (row >= 0) {
                selected.getTableModel().removeRow(row);
                calendarPanel.refresh(); // keep due-date markers in sync
            }
        });

        // --- Edit notes for the selected row ---
        // Opens a small dialog with a multi-line text area pre-filled with
        // the current note. On OK, writes the text back into the hidden
        // Notes column (index 3) of the subject's table model.
        notesButton.addActionListener(e -> {
            Subject selected = subjectList.getSelectedValue();
            if (selected == null) return;
            int row = table.getSelectedRow();
            if (row < 0) return;

            DefaultTableModel tm = selected.getTableModel();
            String assignmentName = (String) tm.getValueAt(row, 0);
            String currentNote = (String) tm.getValueAt(row, 3);
            if (currentNote == null) currentNote = ""; // defensive for older data

            JTextArea area = new JTextArea(currentNote, 4, 32);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            JScrollPane sp = new JScrollPane(area);

            int result = JOptionPane.showConfirmDialog(frame, sp,
                    "Notes — " + assignmentName,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                tm.setValueAt(area.getText(), row, 3);
            }
        });

        // --- New Class ---
        newClassButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Class name:", "New Class",
                    JOptionPane.PLAIN_MESSAGE);
            if (input == null) return;
            String name = input.trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Class name cannot be empty.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (int i = 0; i < subjectListModel.size(); i++) {
                if (subjectListModel.get(i).getName().equalsIgnoreCase(name)) {
                    JOptionPane.showMessageDialog(frame, "A class with that name already exists.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            Subject subject = new Subject(name);
            subjectListModel.addElement(subject);
            subjectList.setSelectedValue(subject, true);
        });

        // --- Rename ---
        renameButton.addActionListener(e -> {
            Subject selected = subjectList.getSelectedValue();
            if (selected == null) return;
            String input = (String) JOptionPane.showInputDialog(frame, "New name:",
                    "Rename Class", JOptionPane.PLAIN_MESSAGE, null, null, selected.getName());
            if (input == null) return;
            String name = input.trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Class name cannot be empty.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (int i = 0; i < subjectListModel.size(); i++) {
                Subject s = subjectListModel.get(i);
                if (s != selected && s.getName().equalsIgnoreCase(name)) {
                    JOptionPane.showMessageDialog(frame, "A class with that name already exists.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            int index = subjectList.getSelectedIndex();
            selected.setName(name);
            subjectListModel.set(index, selected);
            calendarPanel.refresh(); // tooltip labels include the subject name
        });

        // --- Delete ---
        deleteButton.addActionListener(e -> {
            Subject selected = subjectList.getSelectedValue();
            if (selected == null) return;
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Delete '" + selected.getName() + "' and all its assignments? This cannot be undone.",
                    "Delete Class", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            int index = subjectList.getSelectedIndex();
            subjectListModel.remove(index);
            if (!subjectListModel.isEmpty()) {
                subjectList.setSelectedIndex(Math.max(0, index - 1));
            }
            calendarPanel.refresh(); // deleted subject's assignments come off the calendar
            // When the list is empty, removing the last element clears the JList
            // selection automatically, which fires the ListSelectionListener above.
            // That listener resets the table and disables all controls.
        });

        // --- Assemble frame ---
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.getViewport().setBackground(theme.contentBg());
        tableScroll.setBackground(theme.contentBg());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(theme.contentBg());
        tabs.setForeground(theme.contentFg());
        tabs.addTab("Assignments", tableScroll);
        tabs.addTab("Calendar", calendarPanel);

        // --- Theme change listener: recolor all non-calendar components ---
        theme.addListener(() -> {
            // Table
            table.setBackground(theme.contentBg());
            table.setForeground(theme.contentFg());
            table.getTableHeader().setBackground(theme.contentBg());
            table.getTableHeader().setForeground(theme.contentFg());
            tableScroll.getViewport().setBackground(theme.contentBg());
            tableScroll.setBackground(theme.contentBg());

            // Tabs
            tabs.setBackground(theme.contentBg());
            tabs.setForeground(theme.contentFg());

            // Sidebar
            subjectList.setBackground(theme.sidebarBg());
            subjectList.setForeground(theme.sidebarFg());
            subjectList.setSelectionBackground(theme.sidebarSelBg());
            subjectList.setSelectionForeground(theme.sidebarSelFg());
            sidebarButtonPanel.setBackground(theme.sidebarBg());
            sidebarScroll.getViewport().setBackground(theme.sidebarBg());
            sidebarPanel.setBackground(theme.sidebarBg());

            // Input panel
            inputPanel.setBackground(theme.contentBg());
            nameField.setBackground(theme.inputBg());
            nameField.setForeground(theme.inputFg());
            dateField.setBackground(theme.inputBg());
            dateField.setForeground(theme.inputFg());

            // Toggle button label
            darkModeToggle.setText(theme.isDark() ? "☀" : "☾");
            darkModeLabel.setText(theme.isDark() ? "Light Mode" : "Dark Mode");

            // Labels in input panel
            for (Component c : inputPanel.getComponents()) {
                if (c instanceof JLabel) {
                    ((JLabel) c).setForeground(theme.contentFg());
                }
            }

            frame.getContentPane().setBackground(theme.contentBg());
            frame.repaint();
        });

        // Redraw the calendar whenever the user switches to its tab so any
        // assignments added/removed on the Assignments tab show up.
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == calendarPanel) {
                calendarPanel.refresh();
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(sidebarPanel, BorderLayout.WEST);
        frame.add(tabs, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }
}
