package org.clueminer.clustering.struct;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dataset.plugin.ArrayDataset;
import org.clueminer.dataset.plugin.SampleDataset;
import org.clueminer.fixtures.CommonFixture;
import org.clueminer.io.ARFFHandler;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author deric
 */
public class ClusterListTest {

    private static ClusterList subject;
    private static CommonFixture tf = new CommonFixture();

    public ClusterListTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        subject = new ClusterList(5);
        subject.createCluster();
        subject.createCluster();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetName() {
    }

    @Test
    public void testEnsureCapacity() {
    }

    @Test
    public void testHasAt() {
        assertEquals(true, subject.hasAt(0));
        assertEquals(true, subject.hasAt(1));
    }

    @Test
    public void testAdd() {
        ClusterList clusters = new ClusterList(3);
        clusters.createCluster();
        clusters.createCluster();
        clusters.createCluster();
        assertEquals(3, clusters.size());
    }

    @Test
    public void testGetCapacity() {
        assertEquals(5, subject.getCapacity());
    }

    @Test
    public void testGetClusterLabel() {
        assertEquals("cluster 1", subject.getClusterLabel(0));
    }

    @Test
    public void testFirst() {
    }

    @Test
    public void testPut_Cluster() {
    }

    @Test
    public void testPut_int_Cluster() {
    }

    @Test
    public void testMerge() {
    }

    @Test
    public void testSize() {
        assertEquals(2, subject.size());
    }

    @Test
    public void testInstancesCount() {
    }

    @Test
    public void testGetCentroid() {
    }

    @Test
    public void testInstancesIterator() {
    }

    @Test
    public void testClusterSizes() {
        int[] sizes = subject.clusterSizes();
        for (int i = 0; i < sizes.length; i++) {
            assertEquals(0, sizes[i]);
        }
    }

    @Test
    public void testAssignedCluster() {
    }

    @Test
    public void testGet() {
    }

    @Test
    public void testIterator() {
    }

    @Test
    public void testIsEmpty() {
        assertEquals(false, subject.isEmpty());

        //empty clusters
        ClusterList clusters = new ClusterList(3);
        assertEquals(true, clusters.isEmpty());
    }

    @Test
    public void testContains() {
    }

    @Test
    public void testToArray_0args() {
    }

    @Test
    public void testToArray_GenericType() {
    }

    @Test
    public void testRemove() {
    }

    @Test
    public void testContainsAll() {
    }

    @Test
    public void testAddAll() {
    }

    @Test
    public void testRemoveAll() {
    }

    @Test
    public void testRetainAll() {
    }

    @Test
    public void testClear() {
    }

    @Test
    public void testCreateCluster_int() {
        ClusterList list = new ClusterList(3);
        //create cluster with given ID
        Cluster c = list.createCluster(0);
        assertEquals(1, c.getClusterId());
        assertEquals(1, list.size());

        //start from non-zero index
        list = new ClusterList(5);
        //create cluster with given ID
        c = list.createCluster(2);
        assertEquals(3, c.getClusterId());
        assertEquals(1, list.size());

    }

    @Test
    public void testCreateCluster_0args() {
        ClusterList list = new ClusterList(3);
        //create cluster with given ID
        Cluster c = list.createCluster();
        assertEquals(1, c.getClusterId());
        assertEquals(1, list.size());
    }

    private Dataset<? extends Instance> loadIris() throws FileNotFoundException, IOException {
        Dataset<? extends Instance> iris = new ArrayDataset(150, 4);
        ARFFHandler arff = new ARFFHandler();
        arff.load(tf.irisArff(), iris, 4);
        return iris;
    }

    @Test
    public void testCreateCluster_int_int() throws FileNotFoundException, IOException {
        ClusterList list = new ClusterList(3);

        Dataset<? extends Instance> iris = loadIris();

        //Dataset<? extends Instance> iris = ;
        //create cluster with given ID
        Cluster c = list.createCluster(0, 5);

        assertEquals(1, c.getClusterId());
        assertEquals(1, list.size());
        assertEquals(5, c.getCapacity());
    }

    @Test
    public void testToString() {
    }

    @Test
    public void testGetLookup() {
    }

    @Test
    public void testLookupAdd() {
    }

    @Test
    public void testLookupRemove() {
    }

    @Test
    public void testGetParams() {
    }

    @Test
    public void testSetParams() {
    }

}