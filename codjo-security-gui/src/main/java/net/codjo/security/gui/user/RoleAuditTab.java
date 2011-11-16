package net.codjo.security.gui.user;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.gui.model.GuiListener;
import net.codjo.security.gui.model.GuiModelManager;
import net.codjo.security.gui.model.ObjectEvent;
/**
 *
 */
public class RoleAuditTab {
    private JPanel mainPanel;
    private JList roleList;


    public RoleAuditTab() {
        roleList.setCellRenderer(new RoleCellRenderer());
        roleList.setModel(new DefaultListModel());
        roleList.getActionMap().put("copy", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(selectionToString()), null);
            }
        });
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    public void initialize(final GuiModelManager guiManager) {
        GuiUtil.updateModel(roleList, new ArrayList<Comparable>(guiManager.getAllRoles()));
        guiManager.addCompositeListener(new GuiListener<ObjectEvent<RoleComposite>>() {
            public void eventTriggered(ObjectEvent<RoleComposite> event) {
                GuiUtil.updateModel(roleList, new ArrayList<Comparable>(guiManager.getAllRoles()));
            }
        });
    }


    public String selectionToString() {
        StringBuilder buffer = new StringBuilder();
        for (Object obj : roleList.getSelectedValues()) {
            if (buffer.length() != 0) {
                buffer.append("\n");
            }
            buffer.append(((Role)obj).getName());
        }
        return buffer.toString();
    }
}
