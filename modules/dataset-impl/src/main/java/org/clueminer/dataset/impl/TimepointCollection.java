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
package org.clueminer.dataset.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import org.clueminer.attributes.TimePointAttribute;

/**
 * Collection allowing iteration over data without copying
 *
 * @author deric
 * @param <E>
 */
public class TimepointCollection<E extends Date> implements Collection<E> {

    private final TimePointAttribute[] timePoints;
    private int last = 0;

    public TimepointCollection(TimePointAttribute[] timePoints) {
        this.timePoints = timePoints;
        this.last = timePoints.length;
    }

    public TimepointCollection(TimePointAttribute[] timePoints, int lastAttr) {
        this.timePoints = timePoints;
        this.last = lastAttr;
    }

    @Override
    public int size() {
        return last;
    }

    @Override
    public boolean isEmpty() {
        return timePoints == null || timePoints.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (TimePointAttribute timePoint : timePoints) {
            if (o.equals(timePoint.getDate())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new TimePointIterator();
    }

    class TimePointIterator implements Iterator<E> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public E next() {
            return (E) timePoints[index++].getDate();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove from dataset using the iterator.");

        }
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
