package net.codjo.security.common.message;
/**
 *
 */
public class Role implements Comparable<Role> {
    private String name;


    public Role() {
    }


    public Role(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public int compareTo(Role role) {
        return getName().compareTo(role.getName());
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Role role = (Role)object;

        return !(name != null ? !name.equalsIgnoreCase(role.name) : role.name != null);
    }


    @Override
    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }


    @Override
    public String toString() {
        return "Role(" + name + ")";
    }


    public void accept(RoleVisitor visitor) {
        visitor.visit(this);
    }
}
