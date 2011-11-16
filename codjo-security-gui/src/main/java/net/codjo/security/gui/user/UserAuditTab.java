package net.codjo.security.gui.user;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.codjo.gui.toolkit.table.TableRendererSorter;
import net.codjo.security.common.message.User;
import net.codjo.security.gui.model.GuiListener;
import net.codjo.security.gui.model.GuiModelManager;
import net.codjo.security.gui.model.ObjectEvent;
/**
 *
 */
public class UserAuditTab {
    private JPanel mainPanel;
    private JTable userAuditTable;
    private ConnectionModel connectionModel;


    public UserAuditTab() {
        TableRendererSorter rendererSorter = new TableRendererSorter(userAuditTable);
        rendererSorter.addMouseListenerToHeaderInTable(userAuditTable);

        this.connectionModel = new ConnectionModel();
        rendererSorter.setModel(this.connectionModel);
        userAuditTable.setModel(rendererSorter);

        rendererSorter.changeHeaderRenderer(userAuditTable);
        userAuditTable.setDefaultRenderer(Date.class, new DateCellRenderer());
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    public void initialize(final GuiModelManager guiManager) {
        connectionModel.initialize(guiManager.getUsers());

        guiManager.addUserListener(new GuiListener<ObjectEvent<User>>() {
            public void eventTriggered(ObjectEvent<User> event) {
                connectionModel.initialize(guiManager.getUsers());
            }
        });
    }


    private static class ConnectionModel extends AbstractTableModel {
        private static final String[] HEADER = new String[]{"Nom", "Dernier login", "Dernier logout"};
        private static final Class[] CLAZZ = {String.class, Date.class, Date.class};
        private List<User> users = Collections.emptyList();


        void initialize(Collection<User> userCollection) {
            users = new ArrayList<User>(userCollection.size());
            users.addAll(userCollection);
            Collections.sort(users);
            fireTableDataChanged();
        }


        public int getRowCount() {
            return users.size();
        }


        public int getColumnCount() {
            return HEADER.length;
        }


        @Override
        public String getColumnName(int columnIndex) {
            return HEADER[columnIndex];
        }


        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return CLAZZ[columnIndex];
        }


        public Object getValueAt(int rowIndex, int columnIndex) {
            if (!(rowIndex >= 0 && rowIndex < users.size())) {
                return null;
            }
            User user = users.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return user.getName();
                case 1:
                    return user.getLastLogin();
                case 2:
                    return user.getLastLogout();
                default:
                    return null;
            }
        }
    }
    private static class DateCellRenderer extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object value) {
            super.setValue(GuiUtil.format((Date)value));
        }
    }
}
