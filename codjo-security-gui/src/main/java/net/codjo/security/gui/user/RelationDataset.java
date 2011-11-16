package net.codjo.security.gui.user;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
/**
 *
 */
class RelationDataset<T extends Comparable<T>> {
    private final Map<T, Node> dictionnary = new HashMap<T, Node>();
    private final Map<Node, T> reverseDictionnary = new HashMap<Node, T>();


    public void addRelation(T child, T parent) {
        getOrCreate(child).addParent(getOrCreate(parent));
        getOrCreate(parent).addChild(getOrCreate(child));
    }


    public boolean hasParent(T value) {
        return getOrCreate(value).hasParent();
    }


    public boolean hasChild(T value) {
        return getOrCreate(value).hasChild();
    }


    public int distanceToParent(T child, T parent) {
        Integer distanceTo = getOrCreate(child).distanceTo(getOrCreate(parent));
        if (distanceTo == null) {
            return -1;
        }
        return distanceTo;
    }


    public Set<DistanceTo> multipleDistanceToParent(T child, T parent) {
        return getOrCreate(child).multipleDistanceTo(getOrCreate(parent));
    }


    private Node getOrCreate(T value) {
        Node node = dictionnary.get(value);
        if (node == null) {
            node = new Node();
            dictionnary.put(value, node);
            reverseDictionnary.put(node, value);
        }
        return node;
    }


    class Node {
        private Set<Node> parentSet = new HashSet<Node>();
        private Set<Node> childSet = new HashSet<Node>();


        public void addParent(Node parent) {
            parentSet.add(parent);
        }


        public void addChild(Node child) {
            childSet.add(child);
        }


        public boolean hasParent() {
            return !parentSet.isEmpty();
        }


        public boolean hasChild() {
            return !childSet.isEmpty();
        }


        public Integer distanceTo(Node ancestor) {
            Integer distanceTo = null;
            for (Node parent : parentSet) {
                if (parent == ancestor) {
                    return 1;
                }
                Integer parentDistanceTo = parent.distanceTo(ancestor);
                if (parentDistanceTo != null) {
                    if (distanceTo == null) {
                        distanceTo = 1 + parentDistanceTo;
                    }
                    else {
                        distanceTo = Math.min(distanceTo, 1 + parentDistanceTo);
                    }
                }
            }
            return distanceTo;
        }


        public Set<DistanceTo> multipleDistanceTo(Node ancestor) {
            Set<DistanceTo> distanceToSet = new TreeSet<DistanceTo>();
            for (Node parent : parentSet) {
                if (parent == ancestor) {
                    distanceToSet.add(new DistanceTo(1, parent));
                }
                else {
                    Integer distance = parent.distanceTo(ancestor);
                    if (distance != null) {
                        distanceToSet.add(new DistanceTo(1 + distance, parent));
                    }
                }
            }
            return distanceToSet;
        }
    }

    public class DistanceTo implements Comparable<DistanceTo> {
        private final int distance;
        private final T value;


        DistanceTo(int distance, Node parent) {
            this(distance, reverseDictionnary.get(parent));
        }


        DistanceTo(int distance, T value) {
            this.value = value;
            this.distance = distance;
        }


        public int getDistance() {
            return distance;
        }


        public T getValue() {
            return value;
        }


        public int compareTo(DistanceTo distanceTo) {
            if (distance > distanceTo.distance) {
                return 1;
            }
            else if (distance < distanceTo.distance) {
                return -1;
            }
            return value.compareTo(distanceTo.value);
        }
    }
}
