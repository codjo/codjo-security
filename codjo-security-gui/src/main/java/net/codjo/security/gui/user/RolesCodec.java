package net.codjo.security.gui.user;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.RoleVisitorAdapter;
/**
 *
 */
class RolesCodec {

    public String toCsv(ModelManager modelManager) {
        List<Role> allRoleList = modelManager.getRoles();
        RelationDataset<Role> relationDataset = createRelationDataset(allRoleList);

        List<Role> headerRoleList = extractHeaderRoleList(allRoleList);
        List<Role> rowRoleList = extractRowRoleList(allRoleList, relationDataset);

        StringBuilder stringBuilder = new StringBuilder();
        appendHeader(stringBuilder, headerRoleList);
        appendRows(stringBuilder, headerRoleList, rowRoleList, relationDataset);

        return stringBuilder.toString();
    }


    private RelationDataset<Role> createRelationDataset(List<Role> roleList) {
        final RelationDataset<Role> relationDataset = new RelationDataset<Role>();
        for (final Role role : roleList) {
            role.accept(new RoleVisitorAdapter() {
                @Override
                public void visitComposite(RoleComposite roleComposite) {
                    for (Role child : roleComposite.getRoles()) {
                        relationDataset.addRelation(child, role);
                    }
                }
            });
        }
        return relationDataset;
    }


    private List<Role> extractHeaderRoleList(List<Role> roleList) {
        final List<Role> result = new ArrayList<Role>();
        for (Role role : roleList) {
            role.accept(new RoleVisitorAdapter() {
                @Override
                public void visitComposite(RoleComposite role) {
                    result.add(role);
                }
            });
        }
        Collections.sort(result);
        return result;
    }


    private List<Role> extractRowRoleList(List<Role> roleList, final RelationDataset<Role> relationDataset) {
        final List<Role> result = new ArrayList<Role>();
        for (Role role : roleList) {
            role.accept(new RoleVisitorAdapter() {
                @Override
                public void visit(Role role) {
                    result.add(role);
                }


                @Override
                public void visitComposite(RoleComposite role) {
                    if (relationDataset.hasParent(role)) {
                        result.add(role);
                    }
                }
            });
        }
        Collections.sort(result);
        return result;
    }


    private void appendHeader(StringBuilder stringBuilder, List<Role> headerRoleList) {
        for (Role role : headerRoleList) {
            stringBuilder.append(";").append(role.getName());
        }
        stringBuilder.append("\r\n");
    }


    private void appendRows(StringBuilder stringBuilder,
                            List<Role> headerRoleList,
                            List<Role> rowRoleList,
                            RelationDataset<Role> relationDataset) {
        for (Role rowRole : rowRoleList) {
            stringBuilder.append(rowRole.getName());
            for (Role headerRole : headerRoleList) {
                stringBuilder.append(";");
                Set<RelationDataset<Role>.DistanceTo> distance = relationDataset.multipleDistanceToParent(
                      rowRole,
                      headerRole);
                stringBuilder.append(distanceToString(distance));
            }
            stringBuilder.append("\r\n");
        }
    }


    private String distanceToString(Set<RelationDataset<Role>.DistanceTo> distances) {
        StringBuilder stringBuilder = new StringBuilder();
        for (RelationDataset<Role>.DistanceTo distanceTo : distances) {
            stringBuilder.append(", ");
            if (distanceTo.getDistance() == 1) {
                stringBuilder.append(1);
            }
            else {
                stringBuilder.append(String.format("%d (%s)",
                                                   distanceTo.getDistance(),
                                                   distanceTo.getValue().getName()));
            }
        }
        stringBuilder.delete(0, 2);
        return stringBuilder.toString();
    }
}
