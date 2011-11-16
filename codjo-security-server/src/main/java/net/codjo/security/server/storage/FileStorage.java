package net.codjo.security.server.storage;
import java.io.File;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.util.file.FileUtil;
/**
 *
 */
class FileStorage extends AbstractStorage {
    private final File file;


    FileStorage(File file) {
        this.file = file;
    }


    public ModelManager loadModel() throws Exception {
        synchronized (file) {
            if (file.exists()) {
                String fileContent = FileUtil.loadContent(file);
                if (fileContent.length() != 0) {

                    Pattern pattern = Pattern.compile("<xml version=\"(\\d)\">(.*)</xml>",
                                                      Pattern.MULTILINE | Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(fileContent);

                    if (!matcher.matches()) {
                        throw new Exception("le contenu du fichier est mal formé");
                    }

                    if (matcher.groupCount() == 2) {
                        Integer xmlModelVersion = Integer.valueOf(matcher.group(1));
                        if (xmlModelVersion != XmlCodec.XML_VERSION) {
                            throw new BadConfigurationException(BAD_DATA + "La version du modèle '"
                                                                + xmlModelVersion + "' n'est pas gérée.");
                        }
                        return fromXml(matcher.group(2));
                    }
                }
            }
            return new DefaultModelManager();
        }
    }


    public Timestamp saveModel(ModelManager manager) throws Exception {
        synchronized (file) {
            String fileContent = String.format("<xml version=\"%d\">%s</xml>",
                                               XmlCodec.XML_VERSION,
                                               toXml(manager));
            FileUtil.saveContent(file, fileContent);
            return getModelTimestamp();
        }
    }


    public Timestamp getModelTimestamp() throws Exception {
        if (file.exists()) {
            return new Timestamp(file.lastModified());
        }
        return NO_TIMESTAMP;
    }
}
