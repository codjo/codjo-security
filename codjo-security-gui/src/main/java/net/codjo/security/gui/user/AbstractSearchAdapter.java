package net.codjo.security.gui.user;
import java.util.List;
import javax.swing.JList;
import net.codjo.security.gui.search.SearchAction.SearchAdapter;
/**
 *
 */
public abstract class AbstractSearchAdapter<T extends Comparable> implements SearchAdapter<T> {
    protected final JList listComponent;


    protected AbstractSearchAdapter(JList list) {
        this.listComponent = list;
    }


    public void setValues(List<T> values) {
        @SuppressWarnings({"unchecked"}) T user = (T)listComponent.getSelectedValue();
        GuiUtil.updateModel(listComponent, values);
        listComponent.setSelectedValue(user, true);
    }
}
