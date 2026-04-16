import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DataStore} save/load round-trips and JSON parser edge cases.
 *
 * Each test redirects DataStore to a temp file so the real user data file
 * (~/.assignmenttracker_data.json) is never touched.
 */
public class DataStoreTest {

    @TempDir
    Path tempDir;

    private Path tempFile;

    @BeforeEach
    void redirectDataFile() {
        tempFile = tempDir.resolve("test_data.json");
        DataStore.setDataFile(tempFile);
    }

    @AfterEach
    void restoreDataFile() {
        DataStore.setDataFile(
                Paths.get(System.getProperty("user.home"), ".assignmenttracker_data.json"));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private DefaultListModel<Subject> modelWith(Subject... subjects) {
        DefaultListModel<Subject> model = new DefaultListModel<>();
        for (Subject s : subjects) model.addElement(s);
        return model;
    }

    private Subject subjectWithAssignments(String name, Object[]... rows) {
        Subject s = new Subject(name);
        for (Object[] row : rows) s.getTableModel().addRow(row);
        return s;
    }

    // -----------------------------------------------------------------------
    // Round-trip tests
    // -----------------------------------------------------------------------

    @Test
    void saveAndLoad_emptyModel_producesEmptyModel() {
        DataStore.save(new DefaultListModel<>());

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        assertEquals(0, loaded.size());
    }

    @Test
    void saveAndLoad_singleSubjectNoAssignments() {
        DataStore.save(modelWith(new Subject("Math")));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        assertEquals(1, loaded.size());
        assertEquals("Math", loaded.get(0).getName());
        assertEquals(0, loaded.get(0).getTableModel().getRowCount());
    }

    @Test
    void saveAndLoad_multipleSubjects() {
        DataStore.save(modelWith(
                new Subject("Math"),
                new Subject("English"),
                new Subject("History")
        ));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        assertEquals(3, loaded.size());
        assertEquals("Math",    loaded.get(0).getName());
        assertEquals("English", loaded.get(1).getName());
        assertEquals("History", loaded.get(2).getName());
    }

    @Test
    void saveAndLoad_assignmentsRoundTrip() {
        Subject math = subjectWithAssignments("Math",
                new Object[]{"Homework 1", "04/20/2026", false},
                new Object[]{"Quiz",       "04/22/2026", true}
        );
        DataStore.save(modelWith(math));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        DefaultTableModel tm = loaded.get(0).getTableModel();
        assertEquals(2, tm.getRowCount());

        assertEquals("Homework 1", tm.getValueAt(0, 0));
        assertEquals("04/20/2026", tm.getValueAt(0, 1));
        assertEquals(false,        tm.getValueAt(0, 2));

        assertEquals("Quiz",       tm.getValueAt(1, 0));
        assertEquals("04/22/2026", tm.getValueAt(1, 1));
        assertEquals(true,         tm.getValueAt(1, 2));
    }

    @Test
    void saveAndLoad_doneTrue_preservedCorrectly() {
        Subject s = subjectWithAssignments("CS",
                new Object[]{"Final Project", "05/01/2026", true}
        );
        DataStore.save(modelWith(s));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        assertEquals(true, loaded.get(0).getTableModel().getValueAt(0, 2));
    }

    @Test
    void saveAndLoad_doneFalse_preservedCorrectly() {
        Subject s = subjectWithAssignments("CS",
                new Object[]{"Midterm", "04/25/2026", false}
        );
        DataStore.save(modelWith(s));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        assertEquals(false, loaded.get(0).getTableModel().getValueAt(0, 2));
    }

    @Test
    void saveAndLoad_subjectOrderIsPreserved() {
        DataStore.save(modelWith(
                new Subject("Alpha"),
                new Subject("Beta"),
                new Subject("Gamma")
        ));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        assertEquals("Alpha", loaded.get(0).getName());
        assertEquals("Beta",  loaded.get(1).getName());
        assertEquals("Gamma", loaded.get(2).getName());
    }

    @Test
    void saveAndLoad_assignmentOrderIsPreserved() {
        Subject s = subjectWithAssignments("Biology",
                new Object[]{"Lab Report", "04/18/2026", false},
                new Object[]{"Essay",      "04/19/2026", false},
                new Object[]{"Exam",       "04/30/2026", false}
        );
        DataStore.save(modelWith(s));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        DefaultTableModel tm = loaded.get(0).getTableModel();
        assertEquals("Lab Report", tm.getValueAt(0, 0));
        assertEquals("Essay",      tm.getValueAt(1, 0));
        assertEquals("Exam",       tm.getValueAt(2, 0));
    }

    // -----------------------------------------------------------------------
    // Special characters in names
    // -----------------------------------------------------------------------

    @Test
    void saveAndLoad_subjectNameWithQuotes() {
        DataStore.save(modelWith(new Subject("O'Brien's \"Advanced\" Math")));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        assertEquals("O'Brien's \"Advanced\" Math", loaded.get(0).getName());
    }

    @Test
    void saveAndLoad_subjectNameWithBackslash() {
        DataStore.save(modelWith(new Subject("C:\\Course\\Notes")));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        assertEquals("C:\\Course\\Notes", loaded.get(0).getName());
    }

    @Test
    void saveAndLoad_assignmentNameWithSpecialChars() {
        Subject s = subjectWithAssignments("English",
                new Object[]{"Essay: \"To Kill a Mockingbird\"", "04/21/2026", false}
        );
        DataStore.save(modelWith(s));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        assertEquals("Essay: \"To Kill a Mockingbird\"",
                loaded.get(0).getTableModel().getValueAt(0, 0));
    }

    @Test
    void saveAndLoad_namesWithTabAndNewline() {
        Subject s = subjectWithAssignments("Art",
                new Object[]{"Sketch\twith\nnewline", "04/20/2026", false}
        );
        DataStore.save(modelWith(s));

        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);

        assertEquals("Sketch\twith\nnewline",
                loaded.get(0).getTableModel().getValueAt(0, 0));
    }

    // -----------------------------------------------------------------------
    // Edge cases: missing or empty file
    // -----------------------------------------------------------------------

    @Test
    void load_noFileExists_leavesModelEmpty() {
        // tempFile was never written — it does not exist
        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);
        assertEquals(0, loaded.size());
    }

    @Test
    void load_emptyFile_leavesModelEmpty() throws Exception {
        Files.write(tempFile, new byte[0]);
        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);
        assertEquals(0, loaded.size());
    }

    @Test
    void load_emptyJsonArray_leavesModelEmpty() throws Exception {
        Files.write(tempFile, "[]".getBytes(StandardCharsets.UTF_8));
        DefaultListModel<Subject> loaded = new DefaultListModel<>();
        DataStore.load(loaded);
        assertEquals(0, loaded.size());
    }

    // -----------------------------------------------------------------------
    // Idempotency: save → load → save → load produces the same result
    // -----------------------------------------------------------------------

    @Test
    void doubleRoundTrip_isIdempotent() {
        Subject math = subjectWithAssignments("Math",
                new Object[]{"HW1", "04/20/2026", false},
                new Object[]{"HW2", "04/22/2026", true}
        );
        DataStore.save(modelWith(math));

        DefaultListModel<Subject> first = new DefaultListModel<>();
        DataStore.load(first);
        DataStore.save(first);

        DefaultListModel<Subject> second = new DefaultListModel<>();
        DataStore.load(second);

        assertEquals(first.size(), second.size());
        DefaultTableModel tm1 = first.get(0).getTableModel();
        DefaultTableModel tm2 = second.get(0).getTableModel();
        assertEquals(tm1.getRowCount(), tm2.getRowCount());
        for (int r = 0; r < tm1.getRowCount(); r++) {
            assertEquals(tm1.getValueAt(r, 0), tm2.getValueAt(r, 0));
            assertEquals(tm1.getValueAt(r, 1), tm2.getValueAt(r, 1));
            assertEquals(tm1.getValueAt(r, 2), tm2.getValueAt(r, 2));
        }
    }
}
