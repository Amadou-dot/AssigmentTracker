import javax.swing.table.DefaultTableModel;

/**
 * Represents a class or subject that holds a list of assignments.
 */
public class Subject {
    private String name;
    private final DefaultTableModel tableModel;

    /**
     * Creates a new Subject with the given display name.
     *
     * @param name the display name of this class/subject
     */
    public Subject(String name) {
        this.name = name;
        // Columns: Assignment, Due Date, Done, Notes.
        // The Notes column is hidden from the user-facing JTable (see Main)
        // and edited through a separate dialog. It is kept in the table model
        // so each row carries its own note without a parallel data structure.
        this.tableModel = new DefaultTableModel(
                new String[]{"Assignment", "Due Date", "Done", "Notes"}, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 2 ? Boolean.class : String.class; // col 2 = "Done" checkbox
            }
        };
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DefaultTableModel getTableModel() { return tableModel; }

    @Override
    public String toString() { return name; }
}
