package net.codjo.security.common.datagen;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.codjo.security.common.api.Pattern;
import net.codjo.security.common.api.Role;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 *
 */
class RoleReader {
    private List<Role> roles = new ArrayList<Role>();


    RoleReader(InputStream roleStream)
          throws IOException, ParserConfigurationException, SAXException {
        this(new InputStreamReader(roleStream));
    }


    RoleReader(Reader reader)
          throws IOException, ParserConfigurationException, SAXException {
        if (reader == null) {
            throw new IllegalArgumentException("fichier de role null");
        }
        start(toDocument(reader));
        roles = Collections.unmodifiableList(roles);
    }


    public List<Role> getRoles() {
        return roles;
    }


    private static Document toDocument(final Reader reader)
          throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(reader));
    }


    private void start(Document doc)
          throws IOException, ParserConfigurationException, SAXException {
        for (int i = 0; i < doc.getElementsByTagName("role").getLength(); i++) {
            Node roleNode = doc.getElementsByTagName("role").item(i);
            loadRole(roleNode);
        }
    }


    private void loadRole(Node roleNode)
          throws IOException, ParserConfigurationException, SAXException {
        List<Pattern> includes = new ArrayList<Pattern>();
        List<Pattern> excludes = new ArrayList<Pattern>();

        loadIncludeExclude(roleNode, includes, excludes);

        roles.add(new Role(getAttribute(roleNode, "id"), includes, excludes));
    }


    private void loadIncludeExclude(Node roleNode, List<Pattern> includes, List<Pattern> excludes) {
        for (int j = 0; j < roleNode.getChildNodes().getLength(); j++) {
            Node patternNode = roleNode.getChildNodes().item(j);

            if ("include".equals(patternNode.getNodeName())) {
                includes.add(new Pattern(patternNode.getFirstChild().getNodeValue().trim()));
            }
            else if ("exclude".equals(patternNode.getNodeName())) {
                excludes.add(new Pattern(patternNode.getFirstChild().getNodeValue().trim()));
            }
        }
    }


    private static String getAttribute(Node node, String attributeName) {
        if (node.getAttributes() == null
            || node.getAttributes().getNamedItem(attributeName) == null) {
            return null;
        }
        else {
            return node.getAttributes().getNamedItem(attributeName).getNodeValue();
        }
    }
}
