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
package org.clueminer.attributes;

import org.clueminer.dataset.api.AttributeRole;

/**
 * Attribute roles supported by this implementation.
 *
 * @author Tomas Barton
 */
public enum BasicAttrRole implements AttributeRole {
    /**
     * input data (to be processed by algorithms)
     */
    INPUT,
    /**
     * meta-data are excluded from processing (but can be associated with the result)
     */
    META,
    LABEL,
    /**
     * Known assignment to a class
     */
    CLASS,
    ID;
}
