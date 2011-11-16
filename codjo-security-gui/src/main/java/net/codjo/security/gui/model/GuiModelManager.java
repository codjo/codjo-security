package net.codjo.security.gui.model;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.RoleVisitor;
import net.codjo.security.common.message.RoleVisitorAdapter;
import net.codjo.security.common.message.User;
import net.codjo.security.gui.model.ObjectEvent.TYPE;
import org.apache.log4j.Logger;
/**
 *
 */
public class GuiModelManager {
    private final Logger logger = Logger.getLogger(GuiModelManager.class);
    private final ModelManager model;
    private UndoManager undoManager = new UndoManager();
    private Action undoAction = new UndoAction();
    private Action redoAction = new RedoAction();
    private UserUndoableListener userUndoableListener = new UserUndoableListener();
    private UserRoleUndoableListener userRoleUndoableListener = new UserRoleUndoableListener();
    private GuiListenerManager<ObjectEvent<User>> userEventManager = create();
    private GuiListenerManager<ObjectContentEvent<User>> userContentEventManager = createContent();
    private GuiListenerManager<ObjectEvent<RoleComposite>> compositeEventManager = create();
    private GuiListenerManager<ObjectContentEvent<RoleComposite>> compositeContentEventManager
          = createContent();


    public GuiModelManager(ModelManager model) {
        this.model = model;
        userEventManager.addListener(userUndoableListener);
        userContentEventManager.addListener(userRoleUndoableListener);
    }


    public void addUser(final User user) throws ObjectAlreadyExistsException {
        if (model.getUsers().contains(user)) {
            throw new ObjectAlreadyExistsException("L'utilisateur '" + user.getName() + "' existe déjà.");
        }
        model.addUser(user);
        userEventManager.fire(new ObjectEvent<User>(TYPE.ADDED, user));
    }


    public void removeUser(User user) {
        model.removeUser(user);
        userEventManager.fire(new ObjectEvent<User>(TYPE.REMOVED, user));
    }


    public void addRoleToUser(Role role, User user) {
        model.addRoleToUser(role, user);
        userContentEventManager.fire(new ObjectContentEvent<User>(ObjectContentEvent.TYPE.ROLE_ADDED,
                                                                  user,
                                                                  role));
    }


    public void removeRoleToUser(Role role, User user) {
        model.removeRoleToUser(role, user);
        userContentEventManager.fire(new ObjectContentEvent<User>(ObjectContentEvent.TYPE.ROLE_REMOVED,
                                                                  user,
                                                                  role));
    }


    public void addRole(Role role) {
        if (roleExists(role)) {
            throw new ObjectAlreadyExistsException("Le rôle '" + role.getName() + "' existe déjà.");
        }
        model.addRole(role);
        if (role instanceof RoleComposite) {
            compositeEventManager.fire(new ObjectEvent<RoleComposite>(TYPE.ADDED, (RoleComposite)role));
        }
    }


    public void removeRole(Role role) {
        model.removeRole(role);
        if (role instanceof RoleComposite) {
            compositeEventManager.fire(new ObjectEvent<RoleComposite>(TYPE.REMOVED, (RoleComposite)role));
        }
    }


    public void addRoleToComposite(Role role, RoleComposite composite) {
        assertNoCycle(role, composite);

        model.addRoleToComposite(role, composite);
        compositeContentEventManager.fire(new ObjectContentEvent<RoleComposite>(ObjectContentEvent.TYPE.ROLE_ADDED,
                                                                                composite,
                                                                                role));
    }


    public void removeRoleToComposite(Role role, RoleComposite composite) {
        model.removeRoleToComposite(role, composite);
        compositeContentEventManager.fire(new ObjectContentEvent<RoleComposite>(ObjectContentEvent.TYPE.ROLE_REMOVED,
                                                                                composite,
                                                                                role));
    }


    public boolean hasGrants(User user) {
        return model.hasGrants(user);
    }


    public List<RoleComposite> findUsages(final Role role) {
        final List<RoleComposite> foundComposites = new ArrayList<RoleComposite>();

        RoleVisitorAdapter.visitAll(model.getRoles(),
                                    new RoleVisitorAdapter() {
                                        @Override
                                        public void visitComposite(RoleComposite composite) {
                                            if (composite.getRoles().contains(role)) {
                                                foundComposites.add(composite);
                                            }
                                        }
                                    });
        return foundComposites;
    }


    public List<User> findUserUsages(final Role role) {
        final List<User> found = new ArrayList<User>();

        for (final User user : getUsers()) {
            model.visitUserRoles(user, new RoleFinder(role, user, found));
        }
        return found;
    }


    public Set<User> getUsers() {
        return model.getUsers();
    }


    public List<Role> getAllRoles() {
        return model.getRoles();
    }


    public List<Role> getUserRoles(User user) {
        return model.getUserRoles(user);
    }


    public void addUserContentListener(GuiListener<ObjectContentEvent<User>> listener) {
        userContentEventManager.addListener(listener);
    }


    public void removeUserContentListener(GuiListener<ObjectContentEvent<User>> listener) {
        userContentEventManager.removeListener(listener);
    }


    public void addUserListener(GuiListener<ObjectEvent<User>> listener) {
        userEventManager.addListener(listener);
    }


    public void removeUserListener(GuiListener<ObjectEvent<User>> listener) {
        userEventManager.removeListener(listener);
    }


    public void addCompositeListener(GuiListener<ObjectEvent<RoleComposite>> listener) {
        compositeEventManager.addListener(listener);
    }


    public void removeCompositeListener(GuiListener<ObjectEvent<RoleComposite>> listener) {
        compositeEventManager.removeListener(listener);
    }


    public void addCompositeContentListener(GuiListener<ObjectContentEvent<RoleComposite>> listener) {
        compositeContentEventManager.addListener(listener);
    }


    public void removeCompositeContentListener(GuiListener<ObjectContentEvent<RoleComposite>> listener) {
        compositeContentEventManager.removeListener(listener);
    }


    public Action getUndoAction() {
        return undoAction;
    }


    public Action getRedoAction() {
        return redoAction;
    }


    private void updateUndoRedoActionStates() {
        redoAction.setEnabled(undoManager.canRedo());
        undoAction.setEnabled(undoManager.canUndo());
    }


    public ModelManager getModel() {
        return model;
    }


    private static <T> GuiListenerManager<ObjectEvent<T>> create() {
        return new GuiListenerManager<ObjectEvent<T>>();
    }


    private static <T> GuiListenerManager<ObjectContentEvent<T>> createContent() {
        return new GuiListenerManager<ObjectContentEvent<T>>();
    }


    private boolean roleExists(Role role) {
        for (Role current : model.getRoles()) {
            if (current.getName().equalsIgnoreCase(role.getName())) {
                return true;
            }
        }
        return false;
    }


    private void assertNoCycle(final Role role, final RoleComposite composite) {
        int roleIndex = getAllRoles().indexOf(role);
        if (roleIndex == -1) {
            return;
        }
        Role realRole = getAllRoles().get(roleIndex);
        realRole.accept(new RoleVisitorAdapter() {
            @Override
            public void visitComposite(RoleComposite visitedRole) {
                if (visitedRole.equals(composite)) {
                    throw new CycleException(role, composite);
                }
                visitedRole.acceptForSubRoles(this);
            }
        });
    }


    private static class GuiListenerManager<T> {

        private List<GuiListener<T>> listeners = new ArrayList<GuiListener<T>>();


        public void addListener(GuiListener<T> listener) {
            listeners.add(listener);
        }


        public void removeListener(GuiListener<T> listener) {
            listeners.remove(listener);
        }


        public void fire(T event) {
            for (GuiListener<T> listener : new ArrayList<GuiListener<T>>(listeners)) {
                listener.eventTriggered(event);
            }
        }
    }
    private class UndoAction extends AbstractAction {
        private UndoAction() {
            super("undo");
            setEnabled(false);
        }


        public void actionPerformed(ActionEvent event) {
            try {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
            catch (CannotUndoException e) {
                logger.warn(e);
            }
        }
    }
    private class RedoAction extends AbstractAction {
        private RedoAction() {
            super("redo");
            setEnabled(false);
        }


        public void actionPerformed(ActionEvent event) {
            try {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
            catch (CannotRedoException e) {
                logger.warn(e);
            }
        }
    }
    private class UserUndoableEdit extends AbstractUndoableEdit {
        private final User user;
        private final ObjectEvent.TYPE type;


        private UserUndoableEdit(User user, ObjectEvent.TYPE type) {
            this.user = user;
            this.type = type;
        }


        @Override
        public void undo() throws CannotUndoException {
            super.undo();

            userEventManager.removeListener(userUndoableListener);
            try {
                switch (type) {
                    case ADDED:
                        removeUser(user);
                        break;
                    case REMOVED:
                        addUser(user);
                        break;
                }
            }
            finally {
                userEventManager.addListener(userUndoableListener);
                updateUndoRedoActionStates();
            }
        }


        @Override
        public void redo() throws CannotRedoException {
            super.redo();

            userEventManager.removeListener(userUndoableListener);
            try {
                switch (type) {
                    case ADDED:
                        addUser(user);
                        break;
                    case REMOVED:
                        removeUser(user);
                        break;
                }
            }
            finally {
                userEventManager.addListener(userUndoableListener);
                updateUndoRedoActionStates();
            }
        }
    }
    private class UserUndoableListener implements GuiListener<ObjectEvent<User>> {
        public void eventTriggered(ObjectEvent<User> event) {
            undoAction.setEnabled(true);
            undoManager.addEdit(new UserUndoableEdit(event.getObject(), event.getType()));
        }
    }
    private class UserRoleUndoableEdit extends AbstractUndoableEdit {
        private final ObjectContentEvent<User> event;


        private UserRoleUndoableEdit(ObjectContentEvent<User> event) {
            this.event = event;
        }


        @Override
        public void undo() throws CannotUndoException {
            super.undo();

            userContentEventManager.removeListener(userRoleUndoableListener);
            try {
                switch (event.getType()) {
                    case ROLE_ADDED:
                        removeRoleToUser(event.getRole(), event.getObject());
                        break;
                    case ROLE_REMOVED:
                        addRoleToUser(event.getRole(), event.getObject());
                        break;
                }
            }
            finally {
                userContentEventManager.addListener(userRoleUndoableListener);
                updateUndoRedoActionStates();
            }
        }


        @Override
        public void redo() throws CannotRedoException {
            super.redo();

            userContentEventManager.removeListener(userRoleUndoableListener);
            try {
                switch (event.getType()) {
                    case ROLE_ADDED:
                        addRoleToUser(event.getRole(), event.getObject());
                        break;
                    case ROLE_REMOVED:
                        removeRoleToUser(event.getRole(), event.getObject());
                        break;
                }
            }
            finally {
                userContentEventManager.addListener(userRoleUndoableListener);
                updateUndoRedoActionStates();
            }
        }
    }
    private class UserRoleUndoableListener implements GuiListener<ObjectContentEvent<User>> {
        public void eventTriggered(ObjectContentEvent<User> event) {
            undoAction.setEnabled(true);
            undoManager.addEdit(new UserRoleUndoableEdit(event));
        }
    }
    private static class RoleFinder implements RoleVisitor {
        private final Role role;
        private final User user;
        private final List<User> found;


        RoleFinder(Role role, User user, List<User> found) {
            this.role = role;
            this.user = user;
            this.found = found;
        }


        public void visit(Role current) {
            if (current.equals(role)) {
                found.add(user);
            }
        }


        public void visitComposite(RoleComposite composite) {
            if (composite.equals(role)) {
                found.add(user);
                return;
            }
            if (composite.getRoles().contains(role)) {
                found.add(user);
            }
        }
    }
}
