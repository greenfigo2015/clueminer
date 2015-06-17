/*
 * Copyright (C) 2011-2015 clueminer.org
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
package org.clueminer.evaluation.inline;

import java.util.Comparator;
import java.util.List;
import org.clueminer.clustering.api.ClusterEvaluation;
import org.clueminer.clustering.api.Clustering;

/**
 * Compares two clusterings using multiple objectives
 *
 * @author deric
 */
public class DominanceComparator implements Comparator<Clustering> {

    private final double epsilon = 1e-9;
    private final List<ClusterEvaluation> objectives;

    public DominanceComparator(List<ClusterEvaluation> objectives) {
        this.objectives = objectives;
    }

    /**
     * A comparator with reversed sorting logic
     *
     * @param c1
     * @param c2
     * @return
     */
    @Override
    public int compare(Clustering c1, Clustering c2) {
        boolean solution1Dominates = false;
        boolean solution2Dominates = false;

        int flag;
        double value1, value2;
        double diff;
        for (ClusterEvaluation objective : objectives) {
            value1 = objective.score(c1);
            value2 = objective.score(c2);

            diff = value1 - value2;
            if (Math.abs(diff) <= epsilon) {
                //same with epsilon tolerance
                flag = 0;
            } else {
                if (objective.isMaximized()) {
                    //maximize objective
                    if (diff > 0.0) {
                        //solution 1 dominates
                        flag = -1;
                    } else {
                        flag = 1;
                    }
                } else {
                    //minimize objective
                    if (diff > 0.0) {
                        //solution 1 dominates
                        flag = 1;
                    } else {
                        flag = -1;
                    }
                }
            }

            if (flag == -1) {
                solution1Dominates = true;
            }

            if (flag == 1) {
                solution2Dominates = true;
            }
        }
        return dominance2int(solution1Dominates, solution2Dominates);

    }

    private int dominance2int(boolean solution1Dominates, boolean solution2Dominates) {
        int result;
        if (solution1Dominates == solution2Dominates) {
            // non-dominated solutions
            result = 0;
        } else if (solution1Dominates) {
            // solution1 dominates
            result = -1;
        } else {
            // solution2 dominates
            result = 1;
        }
        return result;
    }
}