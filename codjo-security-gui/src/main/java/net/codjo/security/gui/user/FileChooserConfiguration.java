package net.codjo.security.gui.user;
import java.io.File;
/**
 *
 */
public class FileChooserConfiguration {
    private File currentDirectory;


    public File getCurrentDirectory() {
        return currentDirectory;
    }


    public void setCurrentDirectory(File currentDirectory) {
        this.currentDirectory = currentDirectory;
    }
}
