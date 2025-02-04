package by.anpoliakov.utils;

public class Constants {
    public final static String NAME_PROPERTIES_TRANSFORMATION = "Coordinate Transformation Matrix";
    public final static String SCRIPT_NAME_FIX_TOUCH = "auto_start_fix_touch";
    public final static String PATH_AUTOSTART_DIR = "/home/digialq/.config/autostart";
    public final static String SCRIPT_FILE_EXTENSION = ".desktop";
    public final static String PATH_DEF_FOLDER_SERVICES = "/opt";
    public final static String NAME_USER = "digialq";
    public final static String NAME_DIR_LOGS = "logs";
    public final static String[] COORDINATE_RIGHT_ROTATION = new String[]{"0", "1", "0", "-1", "0", "1", "0", "0", "1"};
    public final static String[] COORDINATE_LEFT_ROTATION = new String[]{"0", "-1", "1", "1", "0", "0", "0", "0", "1"};
    public final static String[] COORDINATE_INVERSION = new String[]{"-1", "0", "1", "0", "-1", "1", "0", "0", "1"};
    public final static String[] COORDINATE_DEFAULT = new String[]{"1", "0", "0", "0", "1", "0", "0", "0", "1"};
    public final static byte MATRIX_SIZE = 9;
}
