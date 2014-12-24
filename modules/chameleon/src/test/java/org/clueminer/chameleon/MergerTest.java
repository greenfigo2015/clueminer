package org.clueminer.chameleon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.LinkedList;
import org.clueminer.attributes.BasicAttrType;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dataset.plugin.ArrayDataset;
import org.clueminer.distance.EuclideanDistance;
import org.clueminer.distance.api.DistanceMeasure;
import org.clueminer.graph.adjacencyMatrix.AdjMatrixGraph;
import org.clueminer.graph.api.Node;
import org.clueminer.partitioning.impl.KernighanLin;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Tomas Bruna
 */
public class MergerTest {

    private Dataset<? extends Instance> simpleData() {
        Dataset<Instance> data = new ArrayDataset<>(4, 2);
        data.attributeBuilder().create("x", BasicAttrType.NUMERIC);
        data.attributeBuilder().create("y", BasicAttrType.NUMERIC);
        data.builder().create(new double[]{0, 0});
        data.builder().create(new double[]{1, 3});
        data.builder().create(new double[]{2, 2});
        data.builder().create(new double[]{2, 1});
        data.builder().create(new double[]{4, 4});
        return data;
    }

    @Test
    public void simpleDataTest() throws UnsupportedEncodingException, IOException, FileNotFoundException, InterruptedException {
        Dataset<? extends Instance> dataset = simpleData();
        DistanceMeasure dm = new EuclideanDistance();
        KNN knn = new KNN(3);
        int[][] a = knn.getNeighborArray(dataset);

        AdjMatrixGraph g = new AdjMatrixGraph(dataset.size());
        g = (AdjMatrixGraph) knn.getNeighborGraph(dataset, g);

        KernighanLin kl = new KernighanLin(g);
        ArrayList<LinkedList<Node>> result = kl.bisect();
        Merger m = new Merger(g, result);
        m.computeExternalProperties();

        //Assert external interconnectivity
        assertEquals(m.getEIC(1, 0), 1 / (sqrt(2)) + 1 / (sqrt(10)) + 1 / (sqrt(5)) + 1 / (sqrt(8)) + 1 / (sqrt(5)), 0.0001);
        //Assert external closeness
        assertEquals(m.getECL(0, 1), (1 / (sqrt(2)) + 1 / (sqrt(10)) + 1 / (sqrt(5)) + 1 / (sqrt(8)) + 1 / (sqrt(5))) / 5, 0.0001);
    }
}
