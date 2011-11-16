package net.codjo.security.gui.user;
import net.codjo.security.gui.user.FileChooserActionListener.CustomFileFilter;
/**
 *
 */
class CsvFileFilter extends CustomFileFilter {
    private static final String CSV_FILE_POSTFIX = ".csv";


    @Override
    public String getPostfix() {
        return CSV_FILE_POSTFIX;
    }


    @Override
    public String getDescription() {
        return String.format("CSV (séparateur: point-virgule) (*%s)", getPostfix());
    }
}
