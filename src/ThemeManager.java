import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Manages light/dark theme colors for the application.
 * Listeners are notified whenever the theme changes so components can repaint.
 */
public class ThemeManager {

    public enum Theme { LIGHT, DARK }

    // -----------------------------------------------------------------------
    // Light theme colors
    // -----------------------------------------------------------------------
    public static final Color LIGHT_CAL_HEADER_BG      = new Color(45, 45, 48);
    public static final Color LIGHT_CAL_HEADER_FG      = Color.WHITE;
    public static final Color LIGHT_CAL_DAY_NAME_BG    = new Color(60, 60, 65);
    public static final Color LIGHT_CAL_DAY_NAME_FG    = new Color(180, 180, 180);
    public static final Color LIGHT_CAL_TODAY_BG       = new Color(255, 223, 100);
    public static final Color LIGHT_CAL_THIS_MONTH_BG  = Color.WHITE;
    public static final Color LIGHT_CAL_OTHER_MONTH_BG = new Color(245, 245, 245);
    public static final Color LIGHT_CAL_OTHER_MONTH_FG = new Color(180, 180, 180);
    public static final Color LIGHT_CAL_GRID_LINE      = new Color(220, 220, 220);
    public static final Color LIGHT_CAL_HOVER_BORDER   = new Color(100, 150, 220);
    public static final Color LIGHT_CAL_DATE_FG        = Color.DARK_GRAY;

    public static final Color LIGHT_SIDEBAR_BG         = new Color(44, 44, 44);
    public static final Color LIGHT_SIDEBAR_FG         = Color.WHITE;
    public static final Color LIGHT_SIDEBAR_SEL_BG     = new Color(70, 70, 70);
    public static final Color LIGHT_SIDEBAR_SEL_FG     = Color.WHITE;

    public static final Color LIGHT_CONTENT_BG         = new Color(245, 245, 245);
    public static final Color LIGHT_CONTENT_FG         = Color.BLACK;
    public static final Color LIGHT_INPUT_BG           = Color.WHITE;
    public static final Color LIGHT_INPUT_FG           = Color.BLACK;

    // -----------------------------------------------------------------------
    // Dark theme colors
    // -----------------------------------------------------------------------
    public static final Color DARK_CAL_HEADER_BG      = new Color(30, 30, 30);
    public static final Color DARK_CAL_HEADER_FG      = new Color(220, 220, 220);
    public static final Color DARK_CAL_DAY_NAME_BG    = new Color(40, 40, 40);
    public static final Color DARK_CAL_DAY_NAME_FG    = new Color(150, 150, 150);
    public static final Color DARK_CAL_TODAY_BG       = new Color(180, 140, 0);
    public static final Color DARK_CAL_THIS_MONTH_BG  = new Color(50, 50, 55);
    public static final Color DARK_CAL_OTHER_MONTH_BG = new Color(38, 38, 42);
    public static final Color DARK_CAL_OTHER_MONTH_FG = new Color(100, 100, 100);
    public static final Color DARK_CAL_GRID_LINE      = new Color(60, 60, 65);
    public static final Color DARK_CAL_HOVER_BORDER   = new Color(80, 130, 200);
    public static final Color DARK_CAL_DATE_FG        = new Color(200, 200, 200);

    public static final Color DARK_SIDEBAR_BG         = new Color(28, 28, 28);
    public static final Color DARK_SIDEBAR_FG         = new Color(220, 220, 220);
    public static final Color DARK_SIDEBAR_SEL_BG     = new Color(60, 60, 60);
    public static final Color DARK_SIDEBAR_SEL_FG     = Color.WHITE;

    public static final Color DARK_CONTENT_BG         = new Color(45, 45, 48);
    public static final Color DARK_CONTENT_FG         = new Color(220, 220, 220);
    public static final Color DARK_INPUT_BG           = new Color(60, 60, 65);
    public static final Color DARK_INPUT_FG           = new Color(220, 220, 220);

    // -----------------------------------------------------------------------
    // Singleton
    // -----------------------------------------------------------------------
    private static final ThemeManager INSTANCE = new ThemeManager();
    private static final String PREF_KEY = "darkMode";

    private Theme current;
    private final List<Runnable> listeners = new ArrayList<>();

    private ThemeManager() {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        current = prefs.getBoolean(PREF_KEY, false) ? Theme.DARK : Theme.LIGHT;
    }

    public static ThemeManager get() {
        return INSTANCE;
    }

    public Theme current() {
        return current;
    }

    public boolean isDark() {
        return current == Theme.DARK;
    }

    /** Toggles between light and dark, persists preference, notifies listeners. */
    public void toggle() {
        current = isDark() ? Theme.LIGHT : Theme.DARK;
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        prefs.putBoolean(PREF_KEY, isDark());
        for (Runnable r : listeners) r.run();
    }

    public void addListener(Runnable r) {
        listeners.add(r);
    }

    // -----------------------------------------------------------------------
    // Convenience accessors — return the right color for the current theme
    // -----------------------------------------------------------------------
    public Color calHeaderBg()      { return isDark() ? DARK_CAL_HEADER_BG      : LIGHT_CAL_HEADER_BG; }
    public Color calHeaderFg()      { return isDark() ? DARK_CAL_HEADER_FG      : LIGHT_CAL_HEADER_FG; }
    public Color calDayNameBg()     { return isDark() ? DARK_CAL_DAY_NAME_BG    : LIGHT_CAL_DAY_NAME_BG; }
    public Color calDayNameFg()     { return isDark() ? DARK_CAL_DAY_NAME_FG    : LIGHT_CAL_DAY_NAME_FG; }
    public Color calTodayBg()       { return isDark() ? DARK_CAL_TODAY_BG       : LIGHT_CAL_TODAY_BG; }
    public Color calThisMonthBg()   { return isDark() ? DARK_CAL_THIS_MONTH_BG  : LIGHT_CAL_THIS_MONTH_BG; }
    public Color calOtherMonthBg()  { return isDark() ? DARK_CAL_OTHER_MONTH_BG : LIGHT_CAL_OTHER_MONTH_BG; }
    public Color calOtherMonthFg()  { return isDark() ? DARK_CAL_OTHER_MONTH_FG : LIGHT_CAL_OTHER_MONTH_FG; }
    public Color calGridLine()      { return isDark() ? DARK_CAL_GRID_LINE      : LIGHT_CAL_GRID_LINE; }
    public Color calHoverBorder()   { return isDark() ? DARK_CAL_HOVER_BORDER   : LIGHT_CAL_HOVER_BORDER; }
    public Color calDateFg()        { return isDark() ? DARK_CAL_DATE_FG        : LIGHT_CAL_DATE_FG; }

    public Color sidebarBg()        { return isDark() ? DARK_SIDEBAR_BG         : LIGHT_SIDEBAR_BG; }
    public Color sidebarFg()        { return isDark() ? DARK_SIDEBAR_FG         : LIGHT_SIDEBAR_FG; }
    public Color sidebarSelBg()     { return isDark() ? DARK_SIDEBAR_SEL_BG     : LIGHT_SIDEBAR_SEL_BG; }
    public Color sidebarSelFg()     { return isDark() ? DARK_SIDEBAR_SEL_FG     : LIGHT_SIDEBAR_SEL_FG; }

    public Color contentBg()        { return isDark() ? DARK_CONTENT_BG         : LIGHT_CONTENT_BG; }
    public Color contentFg()        { return isDark() ? DARK_CONTENT_FG         : LIGHT_CONTENT_FG; }
    public Color inputBg()          { return isDark() ? DARK_INPUT_BG           : LIGHT_INPUT_BG; }
    public Color inputFg()          { return isDark() ? DARK_INPUT_FG           : LIGHT_INPUT_FG; }
}
