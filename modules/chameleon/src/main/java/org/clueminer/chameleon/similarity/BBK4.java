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
 * Try using power of interconnectivity multiplied by rho
 *
 * @author deric
 */
@ServiceProvider(service = MergeEvaluation.class)
public class BBK4<E extends Instance> extends AbstractSimilarity<E> implements MergeEvaluation<E> {

    public static final String name = "BBK4";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double score(Cluster<E> a, Cluster<E> b, Props params) {
        checkClusters(a, b);
        GraphCluster<E> x, y;
        x = (GraphCluster<E>) b;
        y = (GraphCluster<E>) a;
        double closenessPriority = params.getDouble(Chameleon.CLOSENESS_PRIORITY, 2.0);
        double interconnectivityPriority = params.getDouble(Chameleon.INTERCONNECTIVITY_PRIORITY, 1.0);
        GraphPropertyStore gps = getGraphPropertyStore(x);
        int i = x.getClusterId();
        int j = y.getClusterId();
        double ec1 = x.getEdgeCount();
        double ec2 = y.getEdgeCount();
        double multip = params.getDouble(Chameleon.INDIVIDUAL_MULTIPLIER, 10);
        double norm = Math.pow(multip, 1.0 / (a.size() + b.size()));

        if (ec1 == 0 || ec2 == 0) {
            return gps.getECL(i, j) * norm;
        }
        double cls = gps.getECL(i, j) / ((x.getACL() * ec1) / (ec1 + ec2) + (y.getACL() * ec2) / (ec1 + ec2));
        double rho = gps.getCnt(i, j) / (Math.min(ec1, ec2) + 1);
        double ics = (Math.min(x.getACL(), y.getACL()) + 1) / Math.max(x.getACL(), y.getACL());

        double val = Math.pow(cls, closenessPriority)
                * Math.pow(ics * rho, interconnectivityPriority)
                * norm;

        return val;
    }

    @Override
    public boolean isMaximized() {
        return true;
    }

    @Override
    public void clusterCreated(Pair<? extends Cluster<E>> pair, Cluster<E> c, Props params) {
        checkClusters(pair.A, pair.B);
        GraphCluster cluster1 = (GraphCluster) pair.A;
        GraphCluster cluster2 = (GraphCluster) pair.B;
        GraphCluster newCluster = (GraphCluster) c;
        int i = Math.max(cluster1.getClusterId(), cluster2.getClusterId());
        int j = Math.min(cluster1.getClusterId(), cluster2.getClusterId());
        GraphPropertyStore gps = getGraphPropertyStore(cluster1);
        double edgeCountSum = cluster1.getEdgeCount() + cluster2.getEdgeCount() + gps.getCnt(i, j);

        double newACL = cluster1.getACL() * (cluster1.getEdgeCount() / edgeCountSum)
                + cluster2.getACL() * (cluster2.getEdgeCount() / edgeCountSum)
                + gps.getECL(i, j) * (gps.getCnt(i, j) / edgeCountSum);

        newCluster.setACL(newACL);
        newCluster.setEdgeCount((int) edgeCountSum);
    }
}
