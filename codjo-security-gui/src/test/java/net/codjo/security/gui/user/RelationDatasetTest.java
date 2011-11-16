package net.codjo.security.gui.user;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
/**
 *
 */
public class RelationDatasetTest {
    private RelationDataset<String> relationDataset = new RelationDataset<String>();


    @Test
    public void test_hasParent() throws Exception {
        relationDataset.addRelation("child", "father");
        relationDataset.addRelation("junior", "child");

        assertTrue(relationDataset.hasParent("child"));
        assertTrue(relationDataset.hasParent("junior"));
        assertFalse(relationDataset.hasParent("father"));
    }


    @Test
    public void test_distanceToParent() throws Exception {
        relationDataset.addRelation("child", "father");
        relationDataset.addRelation("junior", "child");
        relationDataset.addRelation("emma", "junior");
        relationDataset.addRelation("emma", "father");

        assertEquals(1, relationDataset.distanceToParent("child", "father"));
        assertEquals(-1, relationDataset.distanceToParent("child", "unknown"));
        assertEquals(1, relationDataset.distanceToParent("junior", "child"));
        assertEquals(2, relationDataset.distanceToParent("junior", "father"));
        assertEquals(1, relationDataset.distanceToParent("emma", "father"));
        assertEquals(2, relationDataset.distanceToParent("emma", "child"));
        assertEquals(1, relationDataset.distanceToParent("emma", "junior"));
    }


    @Test
    public void test_multipleDistanceToParent() throws Exception {
        relationDataset.addRelation("child", "father");
        relationDataset.addRelation("junior", "child");
        relationDataset.addRelation("emma", "junior");
        relationDataset.addRelation("emma", "father");
        relationDataset.addRelation("junior", "father");
        relationDataset.addRelation("goldfish", "emma");

        assertDistances(relationDataset.multipleDistanceToParent("child", "father"),
                        createDistanceTo(1, "father"));
        assertDistances(relationDataset.multipleDistanceToParent("child", "unknown"));
        assertDistances(relationDataset.multipleDistanceToParent("junior", "child"),
                        createDistanceTo(1, "child"));
        assertDistances(relationDataset.multipleDistanceToParent("junior", "father"),
                        createDistanceTo(1, "father"),
                        createDistanceTo(2, "child"));
        assertDistances(relationDataset.multipleDistanceToParent("emma", "father"),
                        createDistanceTo(1, "father"),
                        createDistanceTo(2, "junior"));
        assertDistances(relationDataset.multipleDistanceToParent("emma", "child"),
                        createDistanceTo(2, "junior"));
        assertDistances(relationDataset.multipleDistanceToParent("emma", "junior"),
                        createDistanceTo(1, "junior"));
        assertDistances(relationDataset.multipleDistanceToParent("goldfish", "child"),
                        createDistanceTo(3, "emma"));
    }


    private RelationDataset<String>.DistanceTo createDistanceTo(int distance, String value) {
        return relationDataset.new DistanceTo(distance, value);
    }


    private void assertDistances(Collection<RelationDataset<String>.DistanceTo> distanceToCollection,
                                 RelationDataset<String>.DistanceTo... expected) {
        Iterator<RelationDataset<String>.DistanceTo> iterator = distanceToCollection.iterator();
        for (RelationDataset<String>.DistanceTo expectedDistanceTo : expected) {
            assertTrue("Il manque des distances !!!", iterator.hasNext());
            RelationDataset<String>.DistanceTo actualDistanceTo = iterator.next();
            assertEquals(expectedDistanceTo.getDistance(), actualDistanceTo.getDistance());
            assertEquals(expectedDistanceTo.getValue(), actualDistanceTo.getValue());
        }
        assertFalse("Il y a trop de distances !!!", iterator.hasNext());
    }
}
