public class ReminderTest {
    
    public static void main(String[] args) {

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void testCheckDueDate() {
        // Test case 1: Assignment due tomorrow
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Notification.checkDueDate("Test Assignment", tomorrow);
        assertEquals("Reminder: Test Assignment is due tomorrow!", JOptionPane.getRootFrame().getTitle());

        // Test case 2: Assignment due today
        LocalDate today = LocalDate.now();
        Notification.checkDueDate("Test Assignment", today);
        assertEquals("Reminder: Test Assignment is due TODAY!", JOptionPane.getRootFrame().getTitle());


        // Test case 3: Assignment not due soon
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        Notification.checkDueDate("Test Assignment", nextWeek);
        assertEquals("", JOptionPane.getRootFrame().getTitle());
        
    }

}

}
