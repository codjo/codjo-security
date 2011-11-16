package net.codjo.security.common.message;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.basic.ThreadSafeSimpleDateFormat;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.Reader;
import java.text.ParseException;
import java.util.Date;
/**
 *
 */
public class XmlCodec {
    public static final int XML_VERSION = 0;


    private XmlCodec() {
    }


    public static ModelManager createFromXml(Reader xmlReader) {
        return (ModelManager)createXstream().fromXML(xmlReader);
    }


    public static ModelManager createFromXml(String xml) {
        return (ModelManager)createXstream().fromXML(xml);
    }


    public static String toXml(ModelManager manager) {
        return XmlCodec.createXstream().toXML(manager);
    }


    public static String toXml(MessageBody body) {
        return XmlCodec.createXstream().toXML(body);
    }


    public static MessageBody createBodyFromXml(String xml) {
        return (MessageBody)createXstream().fromXML(xml);
    }


    private static XStream createXstream() {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("body", MessageBody.class);

        xstream.alias("model", DefaultModelManager.class);
        xstream.alias("user", User.class);
        xstream.alias("role", Role.class);
        xstream.alias("role-composite", RoleComposite.class);

        xstream.useAttributeFor("name", String.class);

        xstream.useAttributeFor("lastLogin", Date.class);
        xstream.useAttributeFor("lastLogout", Date.class);
        // UGLY : Corrige un bug xstream qui lance un NPE si la date est null (en mode attribut)
        xstream.registerConverter(new BugFixConverter());

        xstream.omitField(DefaultModelManager.class, "users");

        xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
        return xstream;
    }


    private static class BugFixConverter implements SingleValueConverter {
        private ThreadSafeSimpleDateFormat dateFormat = new ThreadSafeSimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                                                                                       1, 20);


        public String toString(Object obj) {
            if (obj == null) {
                return null;
            }
            return dateFormat.format((Date)obj);
        }


        public Object fromString(String str) {
            try {
                return dateFormat.parse(str);
            }
            catch (ParseException e) {
                return null;
            }
        }


        public boolean canConvert(Class type) {
            return type == Date.class;
        }
    }
}
