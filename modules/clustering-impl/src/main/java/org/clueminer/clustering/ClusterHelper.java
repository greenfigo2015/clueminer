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
package org.clueminer.clustering;

import java.util.Random;
import static org.clueminer.clustering.api.Algorithm.DISTANCE;
import org.clueminer.distance.EuclideanDistance;
import org.clueminer.distance.api.Distance;
import org.clueminer.distance.api.DistanceFactory;
import org.clueminer.utils.Props;

/**
 *
 * @author deric
 */
public class ClusterHelper {

    public static final String SEED = "seed";

    public static Distance initDistance(Props params) {
        Distance dm;
        // by default use Euclidean distance
        if (!params.containsKey(DISTANCE)) {
            dm = EuclideanDistance.getInstance();
        } else {
            String dist = params.get(DISTANCE);
            dm = DistanceFactory.getInstance().getProvider(dist);
        }

        //fallback
        if (dm == null) {
            dm = EuclideanDistance.getInstance();
        }
        return dm;
    }

    public static Random initSeed(Props params) {
        int seed = params.getInt(SEED, -1);
        Random random = null;
        if (seed != -1) {
            random = new Random(seed);
        } else {
            if (random == null) {
                random = new Random(System.currentTimeMillis());
            }
        }
        return random;
    }

}
