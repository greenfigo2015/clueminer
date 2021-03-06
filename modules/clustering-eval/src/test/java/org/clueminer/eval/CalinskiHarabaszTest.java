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
package org.clueminer.eval;

import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ScoreException;
import org.clueminer.distance.EuclideanDistance;
import org.clueminer.fixtures.clustering.FakeClustering;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author deric
 */
public class CalinskiHarabaszTest {

    private static Clustering clusters;
    private static CalinskiHarabasz subject;
    private static final double DELTA = 1e-9;

    @BeforeClass
    public static void setUpClass() {
        clusters = FakeClustering.iris();
        subject = new CalinskiHarabasz(new EuclideanDistance());
    }

    @Test
    public void testGetName() {
        assertEquals("Calinski-Harabasz", subject.getName());
    }

    @Test
    public void testScore_Clustering_Dataset() throws ScoreException {
        long start = System.currentTimeMillis();
        double score = subject.score(clusters);
        System.out.println("Calinski-Harabasz = " + score);
        long end = System.currentTimeMillis();
        System.out.println("measuring took " + (end - start) + " ms");
        assertEquals(false, Double.isNaN(score));
    }

    @Test
    public void testIris() throws ScoreException {
        double s1 = subject.score(FakeClustering.iris());
        double s2 = subject.score(FakeClustering.irisMostlyWrong());
        double s3 = subject.score(FakeClustering.irisWrong5());
        assertEquals(true, subject.isBetter(s1, s2));
        assertEquals(true, subject.isBetter(s1, s3));

        //according to NbClust
        //assertEquals(558.058, s1, DELTA);
    }

    /**
     * Test of isBetter method, of class CalinskiHarabasz.
     */
    @Test
    public void testCompareScore() {
        assertEquals(false, subject.isBetter(2, 20));
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
        double score = subject.score(FakeClustering.int100p4());
        //clusterCrit = 3959.80613603063
        assertEquals(3959.80613603063, score, DELTA);
    }
}
