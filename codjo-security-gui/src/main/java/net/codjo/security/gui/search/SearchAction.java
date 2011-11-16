package net.codjo.security.gui.search;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.codjo.gui.toolkit.text.SearchTextField;
/**
 *
 */
public class SearchAction<T> implements DocumentListener, ActionListener {
    private SearchTextField searchField;
    private SearchAdapter<T> searchAdapter;


    public static <T> void activate(JComponent mainContainer,
                                    final SearchTextField searchField,
                                    SearchAdapter<T> searchAdapter) {

        SearchAction<T> listener = new SearchAction<T>(searchField, searchAdapter);

        searchField.getDocument().addDocumentListener(listener);
        searchField.addActionListener(listener);

        InputMap inputMap = mainContainer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("control F"), "search");
        mainContainer.getActionMap().put("search", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                searchField.requestFocus();
                searchField.selectAll();
            }
        });
    }


    private SearchAction(SearchTextField searchField, SearchAdapter<T> searchAdapter) {
        this.searchField = searchField;
        this.searchAdapter = searchAdapter;
    }


    public void insertUpdate(DocumentEvent event) {
        launchSearch();
    }


    public void removeUpdate(DocumentEvent event) {
        launchSearch();
    }


    public void changedUpdate(DocumentEvent event) {
    }


    public void actionPerformed(ActionEvent event) {
        launchSearch();
    }


    public void launchSearch() {
        String text = searchField.getText();
        searchAdapter.setValues(searchAdapter.doSearch(text));
    }


    public static interface SearchAdapter<T> {
        List<T> doSearch(String searchPattern);


        void setValues(List<T> values);
    }
}
