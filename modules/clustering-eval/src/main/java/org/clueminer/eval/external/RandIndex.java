package org.clueminer.eval.external;

import com.google.common.collect.Table;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ExternalEvaluator;
import org.clueminer.eval.utils.CountingPairs;
import org.clueminer.eval.utils.Matching;
import org.clueminer.eval.utils.PairMatch;
import org.clueminer.utils.Props;
import org.openide.util.lookup.ServiceProvider;

/**
 * Rand Index as defined in:
 *
 * L. Hubert and P. Arabie. Comparing partitions. Journal of Classification,
 * 2:193–218, 1985.
 *
 * @author Tomas Barton
 */
@ServiceProvider(service = ExternalEvaluator.class)
public class RandIndex extends AbstractCountingPairs {

    private static final long serialVersionUID = -7408696944704938976L;
    private static final String name = "Rand Index";

    @Override
    public String getName() {
        return name;
    }

    /**
     * Should be maximized, lies in interval <0.0 ; 1.0> where 1.0 is the best
     * value
     *
     * @param clusters
     * @param params
     * @return
     */
    @Override
    public double score(Clustering<? extends Cluster> clusters, Props params) {
        PairMatch pm = CountingPairs.matchPairs(clusters);
        return countScore(pm);
    }

    /**
     * In literature usually referred with letters
     * tp = a, fp = b, fn = c, tn = d
     *
     * @param pm
     * @return
     */
    private double countScore(PairMatch pm) {
        return (pm.tp + pm.tn) / (double) (pm.tp + pm.fp + pm.fn + pm.tn);
    }

    @Override
    public double score(Clustering<Cluster> c1, Clustering<Cluster> c2, Props params) {
        PairMatch pm = CountingPairs.matchPairs(c1, c2);
        return countScore(pm);
    }

    @Override
    public double countScore(Table<String, String, Integer> table, Clustering<? extends Cluster> ref, Matching matching) {
        //not used for this index
        throw new UnsupportedOperationException("Not supported.");
    }
}
