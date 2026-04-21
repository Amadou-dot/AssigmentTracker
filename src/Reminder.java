import javax.swing.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class Reminder {


    public static void checkDueDate(String assignmentName, LocalDate dueDate) {
        LocalDate today = LocalDate.now();
        long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

        if (daysUntilDue == 1) {
            JOptionPane.showMessageDialog(null,
                "Reminder: " + assignmentName + " is due tomorrow!");
        } else if (daysUntilDue == 0) {
            JOptionPane.showMessageDialog(null,
                "Reminder:" + assignmentName + " is due TODAY!");
        }
    }
}

