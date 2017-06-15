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

import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.InternalEvaluator;
import org.clueminer.dataset.api.Instance;
import org.clueminer.distance.EuclideanDistance;
import org.clueminer.distance.api.Distance;
import org.clueminer.math.matrix.Maths;
import org.clueminer.utils.Props;
import org.openide.util.lookup.ServiceProvider;

/**
 * Trace of within clusters pooled covariance matrix
 *
 * Trcovw = tr(cov(W_q)))
 *
 * Milligan, Glenn W., and Martha C. Cooper. "An examination of procedures for
 * determining the number of clusters in a data set." Psychometrika 50.2 (1985): 159-179.
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
@ServiceProvider(service = InternalEvaluator.class)
public class Trcovw<E extends Instance, C extends Cluster<E>> extends AbstractEvaluator<E, C> {

    private static final String NAME = "TrcovW";
    private static final long serialVersionUID = 60822019698264781L;

    public Trcovw() {
        dm = new EuclideanDistance();
    }

    public Trcovw(Distance dist) {
        this.dm = dist;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double score(Clustering<E, C> clusters, Props params) {
        // trace(W_q)
        //sc = Wq.trace();
        return Maths.covariance(wqMatrix(clusters)).trace();
    }

    /**
     * Alternate computing method
     *
     * @param clusters
     * @return
     */
    public double score2(Clustering<E, C> clusters) {
        return Maths.covariance(withinGroupScatter(clusters)).trace();
    }

    @Override
    public boolean isBetter(double score1, double score2) {
        return score1 > score2;
    }

    @Override
    public boolean isMaximized() {
        return true;
    }

    @Override
    public double getMin() {
        return 0;
    }

    @Override
    public double getMax() {
        return Double.POSITIVE_INFINITY;
    }

}
