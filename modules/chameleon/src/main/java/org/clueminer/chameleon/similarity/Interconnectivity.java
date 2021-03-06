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
package org.clueminer.chameleon.similarity;

import org.clueminer.chameleon.Chameleon;
import org.clueminer.chameleon.GraphCluster;
import org.clueminer.chameleon.GraphPropertyStore;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.MergeEvaluation;
import org.clueminer.dataset.api.Instance;
import org.clueminer.utils.Pair;
import org.clueminer.utils.Props;
import org.openide.util.lookup.ServiceProvider;

/**
 * Based on Chameleon's dynamic modeling framework, relative interconnectivity
 * forms half of the objective function.
 *
 * @author deric
 */
@ServiceProvider(service = MergeEvaluation.class)
public class Interconnectivity<E extends Instance> extends AbstractSimilarity<E> implements MergeEvaluation<E> {

    public static final String name = "Interconnectivity";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double score(Cluster<E> a, Cluster<E> b, Props params) {
        checkClusters(a, b);
        GraphCluster<E> x = (GraphCluster<E>) a;
        GraphCluster<E> y = (GraphCluster<E>) b;
        double RIC = getRIC(x, y);
        double priority = params.getDouble(Chameleon.INTERCONNECTIVITY_PRIORITY, 1.0);
        if (priority != 1.0) {
            RIC = Math.pow(RIC, priority);
        }

        return RIC;
    }

    /**
     * Compute relative interconnectivity
     *
     * @param x
     * @param y
     * @return
     */
    public double getRIC(GraphCluster<E> x, GraphCluster<E> y) {
        GraphPropertyStore gps = getGraphPropertyStore(x);
        double eic = gps.getEIC(x.getClusterId(), y.getClusterId());
        return eic / ((x.getIIC() + y.getIIC()) / 2);
    }

    @Override
    public boolean isMaximized() {
        return true;
    }

    @Override
    public void clusterCreated(Pair<? extends Cluster<E>> pair, Cluster<E> newCluster, Props params) {
        //nothing to do
    }

}
