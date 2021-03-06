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
package org.clueminer.graph.api;

/**
 *
 * @author Tomas Barton
 */
public interface Element {

    /**
     * Unique node ID
     *
     * @return
     */
    long getId();

    /**
     * Node label
     *
     * @return
     */
    Object getLabel();

    /**
     * Gets the attribute for the given key.
     *
     * @param key the column's key
     * @return the attribute value, or null
     */
    Object getAttribute(String key);

    /**
     * Removes the attribute at the given key.
     *
     * @param key the key
     * @return the value being removed, or null
     */
    Object removeAttribute(String key);

    /**
     * Sets the attribute with the given key and value.
     *
     * @param key the column's key
     * @param value the value to set
     */
    void setAttribute(String key, Object value);

    /**
     * Clears all attribute values.
     */
    void clearAttributes();

}
