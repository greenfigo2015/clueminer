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
package org.clueminer.chameleon.mo;

import java.util.ArrayList;
import java.util.HashSet;
import org.clueminer.chameleon.GraphCluster;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.fixtures.clustering.FakeDatasets;
import org.clueminer.utils.Props;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author deric
 */
public class PairMergerMSHTest<E extends Instance, C extends GraphCluster<E>, P extends MoPair<E, C>> extends AbstractQueueTest<E, C, P> {

    private FrontHeapQueueMsh queue;
    private PairMergerMSH<E, C, P> subject;

    @Test
    public void testPairsRemoval() {
        Props props = new Props();
        Dataset<E> dataset = (Dataset<E>) FakeDatasets.usArrestData();
        subject = initializeMerger(dataset, new PairMergerMSH<E, C, P>());

        ArrayList<P> pairs = subject.createPairs(subject.getClusters().size(), props);
        HashSet<Integer> blacklist = new HashSet<>();
        subject.queue = new FrontHeapQueueMsh<>(5, blacklist, subject.objectives, props);
        subject.queue.addAll(pairs);

        //for (MoPair<Instance, GraphCluster<Instance>> p : pairs) {
        //    queue.blacklist.insertIntoFront(p.A.getClusterId());
        //}
        FrontHeapQueueMsh queue = (FrontHeapQueueMsh) subject.queue;
        queue.blacklist.add(1);
        queue.blacklist.add(2);
        queue.rebuildQueue();
        assertEquals(0, queue.buffer.size());
    }

    @Test
    public void testIris() {
        Props props = new Props();
        subject = initializeMerger((Dataset<E>) FakeDatasets.irisDataset(), new PairMergerMSH());
        ArrayList<P> pairs = subject.createPairs(subject.getClusters().size(), props);
        HashSet<Integer> blacklist = new HashSet<>();
        subject.queue = new FrontHeapQueueMsh<>(5, blacklist, subject.objectives, props);
        subject.queue.addAll(pairs);

        //merge some items - just enough to overflow queue to buffer
        for (int i = 0; i < 5; i++) {
            subject.singleMerge(subject.queue.poll(), props, 0);
        }
        //make sure we iterate over all items
        int i = 0;
        for (Object p : subject.queue) {
            assertNotNull(p);
            i++;
        }
        assertEquals(i, subject.queue.size());
    }

}
