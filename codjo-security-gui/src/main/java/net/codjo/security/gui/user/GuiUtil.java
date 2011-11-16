package net.codjo.security.gui.user;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import net.codjo.security.gui.model.GuiListener;
import net.codjo.security.gui.model.ObjectContentEvent;
import net.codjo.security.gui.model.ObjectEvent;
/**
 *
 */
class GuiUtil {
    private GuiUtil() {
    }


    static <T extends Comparable> void updateModel(JList gui, List<T> list) {
        Collections.sort(list);
        DefaultListModel listModel = getModel(gui);
        listModel.clear();
        for (T role : list) {
            listModel.addElement(role);
        }
    }


    static DefaultListModel getModel(JList list) {
        return (DefaultListModel)list.getModel();
    }


    static <T> GuiListener<ObjectEvent<T>> updateListModel(final JList list) {
        return new GuiListener<ObjectEvent<T>>() {
            public void eventTriggered(ObjectEvent<T> event) {
                if (event.getType() == ObjectEvent.TYPE.REMOVED) {
                    getModel(list).removeElement(event.getObject());
                }
                else if (event.getType() == ObjectEvent.TYPE.ADDED) {
                    getModel(list).addElement(event.getObject());
                }
            }
        };
    }


    static <T> GuiListener<ObjectContentEvent<T>> selectUpdatedObject(final JList list) {
        return new GuiListener<ObjectContentEvent<T>>() {
            public void eventTriggered(ObjectContentEvent<T> event) {
                if (event.getObject() != list.getSelectedValue()) {
                    list.setSelectedValue(event.getObject(), true);
                }
                list.repaint();
            }
        };
    }


    static String format(Date date) {
        if (date == null) {
            return "jamais";
        }
        return new SimpleDateFormat("dd/MM/yy HH:mm").format(date);
    }
}
