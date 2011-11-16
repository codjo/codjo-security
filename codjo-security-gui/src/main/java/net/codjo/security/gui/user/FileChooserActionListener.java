package net.codjo.security.gui.user;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
/**
 *
 */
public class FileChooserActionListener implements ActionListener {
    private final Component button;
    private final FileCommand fileCommand;
    private final CustomFileFilter fileFilter;
    private FileChooserConfiguration fileChooserConfiguration;


    public FileChooserActionListener(JButton button,
                                     CustomFileFilter fileFilter,
                                     FileCommand fileCommand,
                                     FileChooserConfiguration fileChooserConfiguration) {
        this.button = button;
        this.fileFilter = fileFilter;
        this.fileCommand = fileCommand;
        this.fileChooserConfiguration = fileChooserConfiguration;

        button.addActionListener(this);
    }


    public void actionPerformed(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(fileChooserConfiguration.getCurrentDirectory());
        chooser.addChoosableFileFilter(fileFilter);
        int result = chooser.showDialog(button, "Valider");
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = chooser.getSelectedFile();
                if (!selectedFile.getName().endsWith(fileFilter.getPostfix())) {
                    selectedFile = new File(selectedFile.getParent(),
                                            selectedFile.getName() + fileFilter.getPostfix());
                }
                fileCommand.execute(selectedFile);
            }
            catch (IOException e) {
                JOptionPane
                      .showMessageDialog(button, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
        fileChooserConfiguration.setCurrentDirectory(chooser.getCurrentDirectory());
    }


    public interface FileCommand {
        void execute(File file) throws IOException;
    }

    public static abstract class CustomFileFilter extends FileFilter {
        public abstract String getPostfix();


        @Override
        public boolean accept(File file) {
            return file.isDirectory() || file.getName().endsWith(getPostfix());
        }
    }
}