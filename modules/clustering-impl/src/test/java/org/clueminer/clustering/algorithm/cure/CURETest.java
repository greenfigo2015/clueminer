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
package org.clueminer.clustering.algorithm.cure;

import org.clueminer.cluster.FakeClustering;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.colors.RandomColorsGenerator;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.utils.Props;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author deric
 */
public class CURETest<E extends Instance, C extends CureCluster<E>> {

    private CURE subject;

    @Before
    public void setUp() {
        subject = new CURE();
    }

    private void printClustering(Clustering<E, C> clustering) {
        for (C c : clustering) {
            System.out.print(c.getName() + " [" + c.size() + "] ");
            System.out.print("{");
            for (int i = 0; i < c.size(); i++) {
                if (i > 0) {
                    System.out.print(", ");
                }
                System.out.print(c.get(i).getIndex());
            }
            System.out.print("}\n");
        }
    }

    @Test
    public void testCluster() {
        subject.setColorGenerator(new RandomColorsGenerator());
        Dataset<? extends Instance> dataset = FakeClustering.schoolData();
        Props params = new Props();
        params.putInt(CURE.K, 2);
        //cluster all data, no subsets
        params.putBoolean(CURE.SAMPLING, false);
        Clustering<E, C> clustering = subject.cluster(dataset, params);

        assertNotNull(clustering);
        assertEquals(2, clustering.size());
        printClustering(clustering);
        assertEquals(17, clustering.instancesCount());
        for (C c : clustering) {
            if (c.size() < 5) {
                assertEquals(2, c.size());
            } else {
                assertEquals(15, c.size());
            }
            assertNotNull(c.getColor());
        }
    }

    @Test
    public void testSubsampling() {
        Dataset<? extends Instance> dataset = FakeClustering.schoolData();
        Props params = new Props();
        params.putInt(CURE.K, 2);
        //cluster all data, no subsets
        params.putBoolean(CURE.SAMPLING, true);
        Clustering<E, C> clustering = subject.cluster(dataset, params);
        assertNotNull(clustering);
        assertEquals(2, clustering.size());
        printClustering(clustering);
        assertEquals(17, clustering.instancesCount());

    }

    @Test
    public void testIris() {
        Dataset<? extends Instance> dataset = FakeClustering.irisDataset();
        Props params = new Props();
        params.putInt(CURE.K, 3);
        Clustering<E, C> clustering = subject.cluster(dataset, params);
        assertEquals(3, clustering.size());
        System.out.println(clustering.toString());
        assertEquals(150, clustering.instancesCount());
        printClustering(clustering);
    }

}
