import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * A panel that shows a text editor for the notes of the currently selected assignment.
 *
 * Wire it up in Main.java like this:
 *
 *   NotesPanel notesPanel = new NotesPanel(subjectModel, assignmentTable);
 *
 * Then add it somewhere in your layout, e.g. below the assignment table.
 * Call notesPanel.setSubject(subject) whenever the selected subject changes.
 * The panel saves notes back to the table model and persists them via DataStore.
 */
public class NotesPanel extends JPanel {

    private final DefaultListModel<Subject> subjectModel;
    private final JTable assignmentTable;

    private Subject currentSubject;
    private int currentRow = -1;

    private final JTextArea notesArea;
    private final JButton saveButton;
    private final JLabel headerLabel;

    public NotesPanel(DefaultListModel<Subject> subjectModel, JTable assignmentTable) {
        this.subjectModel    = subjectModel;
        this.assignmentTable = assignmentTable;

        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createTitledBorder("Notes"));

        headerLabel = new JLabel("Select an assignment to view or edit its notes.");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.ITALIC, 11f));
        headerLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        add(headerLabel, BorderLayout.NORTH);

        notesArea = new JTextArea(5, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setEnabled(false);
        add(new JScrollPane(notesArea), BorderLayout.CENTER);

        saveButton = new JButton("Save Notes");
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveNotes());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.add(saveButton);
        add(south, BorderLayout.SOUTH);

        assignmentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) onTableSelectionChanged();
            }
        });
    }

    /**
     * Call this from Main whenever the user selects a different subject in the list.
     * @param subject the newly selected subject, or null if nothing is selected
     */
    public void setSubject(Subject subject) {
        currentSubject = subject;
        currentRow     = -1;
        clearEditor();
    }

    private void onTableSelectionChanged() {
        int row = assignmentTable.getSelectedRow();
        if (currentSubject == null || row < 0) {
            currentRow = -1;
            clearEditor();
            return;
        }

        currentRow = assignmentTable.convertRowIndexToModel(row);
        DefaultTableModel tm = currentSubject.getTableModel();
        String assignmentName = (String) tm.getValueAt(currentRow, 0);
        String notes = (String) tm.getValueAt(currentRow, 3);

        headerLabel.setText("Notes for: " + assignmentName);
        headerLabel.setForeground(UIManager.getColor("Label.foreground"));
        notesArea.setText(notes != null ? notes : "");
        notesArea.setEnabled(true);
        notesArea.setCaretPosition(0);
        saveButton.setEnabled(true);
    }

    private void saveNotes() {
        if (currentSubject == null || currentRow < 0) return;

        String notes = notesArea.getText();
        currentSubject.getTableModel().setValueAt(notes, currentRow, 3);
        DataStore.save(subjectModel);
    }

    private void clearEditor() {
        headerLabel.setText("Select an assignment to view or edit its notes.");
        headerLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        notesArea.setText("");
        notesArea.setEnabled(false);
        saveButton.setEnabled(false);
    }
}
