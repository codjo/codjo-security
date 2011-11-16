package net.codjo.security.gui.user;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
/**
 *
 */
class RoleCellRenderer extends DefaultListCellRenderer {
    private ImageIcon roleIcon = new ImageIcon(getClass().getResource("role.png"));
    private ImageIcon compositeIcon = new ImageIcon(getClass().getResource("role-composite.png"));


    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setText(((Role)value).getName());
        if (value instanceof RoleComposite) {
            setIcon(compositeIcon);
        }
        else {
            setIcon(roleIcon);
        }
        return this;
    }
}
