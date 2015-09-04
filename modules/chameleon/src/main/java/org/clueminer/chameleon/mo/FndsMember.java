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
package org.clueminer.chameleon.mo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Member of Fast non-dominated sort queue
 *
 * @author deric
 */
public class FndsMember<T> {

    private final T value;
    private final HashSet<FndsMember<T>> iDominate;
    private final HashSet<FndsMember<T>> dominatesMe;
    private int domCnt;
    //currently assigned front ID
    private int front = -1;

    public FndsMember(T value) {
        this.value = value;
        iDominate = new HashSet<>();
        dominatesMe = new HashSet<>();
        domCnt = 0;
    }

    public T getValue() {
        return value;
    }

    public void addIDominate(FndsMember<T> other) {
        iDominate.add(other);
        other.dominatesMe.add(this);
    }

    public void addDominatesMe(FndsMember<T> other) {
        dominatesMe.add(other);
        other.iDominate.add(this);
    }

    /**
     * Rank determines membership to a front
     *
     * @return
     */
    public int rank() {
        return dominatesMe.size();
    }

    public int rankDiff() {
        return dominatesMe.size() - iDominate.size();
    }

    public int domDiff() {
        return dominatesMe.size() - domCnt;
    }

    public void incDomCnt() {
        domCnt++;
    }

    public int getIDominateCnt() {
        return iDominate.size();
    }

    public int getDominatesMeCnt() {
        return dominatesMe.size();
    }

    /**
     * Remove this member from all other lists, thus lowering/increasing rank of
     * others
     */
    public void delete(ArrayList<Set<FndsMember<T>>> fronts) {
        for (FndsMember<T> mem : iDominate) {
            mem.dominatesMe.remove(this);
            //move mem to higher front
            fronts.get(mem.front).remove(mem);

            if (mem.front > 1) {
                mem.front--;
                fronts.get(mem.front).add(mem);
            }
        }

        for (FndsMember<T> mem : dominatesMe) {
            mem.iDominate.remove(this);
            //move mem to lower front
            fronts.get(mem.front).remove(mem);

            if (mem.front < (fronts.size() - 1)) {
                mem.front++;
                fronts.get(mem.front).add(mem);
            } else {
                throw new RuntimeException("front overflow");
            }

        }
    }

    public Iterator<FndsMember<T>> iterIDominate() {
        return iDominate.iterator();
    }

    public Iterator<FndsMember<T>> iterDominatesMe() {
        return dominatesMe.iterator();
    }

    public void setFront(int frontId) {
        this.front = frontId;
    }

    public int getFront() {
        return front;
    }

    /**
     * Algorithm for computing item's assignment to a front.
     *
     * @return index of front (starting from 0 to (n-1))
     */
    public int frontAssign() {
        int rank = 0;
        if (dominatesMe.isEmpty()) {
            return 0; // first front
        }
        int i = 0;
        while (i < dominatesMe.size()) {
            for (FndsMember<T> mem : dominatesMe) {
                if (mem.front == i) {
                    rank += 1;
                }
            }
            if ((dominatesMe.size() - rank) == 0) {
                return i + 1; //front that this member belongs to
            }
            i++;
        }
        return i + 1;
    }

}