/*
 * Copyright (C) 2011-2016 clueminer.org
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
package org.clueminer.evolution.api;

/**
 *
 * @author Tomas Barton
 * @param <I> individual
 */
public interface Population<I> {

    /**
     *
     * @return size of the population
     */
    int size();

    /**
     * Average fitness in whole population
     *
     * @return
     */
    double getAvgFitness();

    /**
     *
     * @return best solution
     */
    I getBestIndividual();

    /**
     *
     * @return whole population
     */
    I[] getIndividuals();

    /**
     * updates population
     *
     * @param individuals
     */
    void setIndividuals(I[] individuals);

    I getIndividual(int idx);

    void setIndividuals(int index, I individual);

    double getBestFitness();

    /**
     * Sort population by its fitness score
     */
    void sortByFitness();

}
