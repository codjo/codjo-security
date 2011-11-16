package net.codjo.security.gui.user;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.User;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.util.file.FileUtil;

@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public class ExportCsvForAdsMain {
    DefaultModelManager manager = new DefaultModelManager();
    private static final String EXPORT_FILE_NAME = "exportForAds.csv";


    ExportCsvForAdsMain() throws SQLException, IOException {
        loadModel(new File(
              "C:\\Dev\\TOP\\Working group\\ADS - safe\\_migration\\20110316_bali_prd.security"));
    }


    private static void loadModel(File input) throws SQLException, IOException {
        ModelManager model = XmlCodec.createFromXml(FileUtil.loadContent(input));
        File file = new File(input.getParentFile(), EXPORT_FILE_NAME);
        FileUtil.saveContent(file, CsvCodec.toCsv(model));

        System.out.println("");

        System.out.println("Export dans le fichier \n" + file.getName());
        System.out.println("");
        System.out.println("Dans le répertoire");
        System.out.println(file.getParentFile().getAbsolutePath());
    }


    public static void main(String[] args) throws SQLException, IOException {
        new ExportCsvForAdsMain();
    }


    protected static class CsvCodec {
        static final String NEW_LINE = "\r\n";


        private CsvCodec() {
        }


        public static String toCsv(ModelManager model) {
            Set<Role> rolesName = new HashSet<Role>();
            for (User user : model.getUsers()) {
                List<Role> userRoles = model.getUserRoles(user);
                rolesName.addAll(userRoles);
            }

            StringBuilder csv = new StringBuilder();
            for (Role role : model.getRoles()) {
                if (rolesName.contains(role)) {
                    if (role.getName().contains(" ")) {
                        System.out.println("WARNING role with space : " + role);
                    }
                    csv.append(";").append(role.getName().replaceAll(" ", "_"));
                }
            }
            csv.append(NEW_LINE);

            for (User user : model.getUsers()) {
                List<Role> userRoles = model.getUserRoles(user);
                if (model.getUserRoles(user).isEmpty()) {
                    continue;
                }
                csv.append(user.getName());
                for (Role role : model.getRoles()) {
                    if (rolesName.contains(role)) {
                        csv.append(";");
                        if (userRoles.contains(role)) {
                            csv.append("Y");
                        }
                        else {
                            csv.append("N");
                        }
                    }
                }
                csv.append(NEW_LINE);
            }

            return csv.toString();
        }
    }
}