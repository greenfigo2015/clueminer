/*
 * Copyright (C) 2011-2017 clueminer.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.clueminer.eval.external;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ScoreException;
import org.clueminer.dataset.api.Instance;
import org.clueminer.fixtures.clustering.FakeClustering;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author deric
 */
public class RandIndexTest extends ExternalTest {

    private static Clustering irisCorrect;
    private static Clustering irisWrong;

    public RandIndexTest() throws FileNotFoundException, IOException {
        subject = new RandIndex();
        irisCorrect = FakeClustering.iris();
        irisWrong = FakeClustering.irisWrong2();
    }

    /**
     * Test of score method, of class RandIndex.
     */
    @Test
    public void testScore_Clustering_Dataset() throws ScoreException {
        measure(irisCorrect, 1.0);

        measure(irisWrong, 0.7225950782997763);

        //this clustering shouldn't be better than the previous one, 142 items are in one
        //cluster, so not really the best solution - though the coefficient would prefere this one
        measure(FakeClustering.irisWrong(), 0.36715883668903804);
        measure(FakeClustering.irisWrong4(), 0.9463087248322147);
        measure(FakeClustering.irisWrong5(), 0.7595525727069351);
    }

    /**
     * Test of isBetter method, of class RandIndex.
     */
    @Test
    public void testCompareScore() {
        //one should be the best value
        assertTrue(subject.isBetter(1.0, 0.0));
    }

    @Test
    public void testOneClassPerCluster() throws ScoreException {
        assertEquals(0.0, subject.score(oneClassPerCluster()), delta);
    }

    @Test
    public void testMostlyWrong() throws ScoreException {
        double score = subject.score(FakeClustering.irisMostlyWrong());
        System.out.println("rand (mw): " + score);
        assertEquals(true, score < 0.4);
    }

    /**
     * Based on Details of the Adjusted Rand index and Clustering algorithms
     * Supplement to the paper “An empirical study on Principal Component
     * Analysis for clustering gene expression data” (to appear in
     * Bioinformatics)
     *
     * Ka Yee Yeung, Walter L. Ruzzo, 2001
     *
     */
    @Test
    public void testPcaData() throws ScoreException {
        Clustering<Instance, Cluster<Instance>> clust = pcaData();
        double score = subject.score(clust);
        assertEquals(0.71111111111111, score, delta);
    }

    /**
     * Check against definition (and tests in R package clusterCrit)
     * https://cran.r-project.org/web/packages/clusterCrit/index.html
     *
     * NOTE: There's a small problem with precision of floating point
     * operations. First 7 decimal digits seems to match.
     */
    @Test
    public void testClusterCrit() throws ScoreException {
        measure(ext100p2, ext100p3, 0.512121200561523);
    }
}
