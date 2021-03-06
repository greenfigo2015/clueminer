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
package org.clueminer.evolution.mo;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.clueminer.clustering.ClusteringExecutorCached;
import org.clueminer.clustering.api.AlgParams;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.ClusterEvaluation;
import org.clueminer.clustering.api.Executor;
import org.clueminer.dataset.api.Instance;
import org.clueminer.events.ListenerList;
import org.clueminer.evolution.api.Evolution;
import org.clueminer.evolution.api.EvolutionListener;
import org.clueminer.evolution.api.EvolutionMO;
import org.clueminer.evolution.api.Individual;
import org.clueminer.evolution.hac.SimpleIndividual;
import org.clueminer.evolution.multim.MultiMuteEvolution;
import org.clueminer.oo.api.OpListener;
import org.clueminer.oo.api.OpSolution;
import org.clueminer.utils.PropType;
import org.clueminer.utils.Props;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.IntegerSBXCrossover;
import org.uma.jmetal.operator.impl.mutation.IntegerPolynomialMutation;
import org.uma.jmetal.operator.impl.selection.NaryTournamentSelection;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.DominanceComparator;

/**
 *
 * @author Tomas Barton
 * @param <I>
 * @param <E>
 * @param <C>
 */
@ServiceProvider(service = Evolution.class)
public class MoEvolution<I extends Individual<I, E, C>, E extends Instance, C extends Cluster<E>>
        extends MultiMuteEvolution<I, E, C> implements Runnable, EvolutionMO<I, E, C>, Lookup.Provider {

    private static final String name = "MOE";
    private static final Logger LOG = LoggerFactory.getLogger(MoEvolution.class);
    protected List<ClusterEvaluation<E, C>> objectives;
    private int numSolutions = 5;
    private boolean kLimit;
    protected final transient ListenerList<OpListener> moListeners = new ListenerList<>();

    public MoEvolution() {
        init(new ClusteringExecutorCached<>());
    }

    public MoEvolution(Executor executor) {
        init(executor);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void prepareHook() {
        this.objectives = Lists.newLinkedList();
    }

    @Override
    public void addObjective(ClusterEvaluation eval) {
        objectives.add(eval);
    }

    @Override
    public void removeObjective(ClusterEvaluation eval) {
        objectives.remove(eval);
    }

    @Override
    public ClusterEvaluation getObjective(int idx) {
        return objectives.get(idx);
    }

    @Override
    public List<ClusterEvaluation<E, C>> getObjectives() {
        return objectives;
    }

    @Override
    public int getNumObjectives() {
        return objectives.size();
    }

    @Override
    public void run() {
        MoProblem problem = new MoProblem(this);
        Props def = new Props();
        def.put(PropType.PERFORMANCE, AlgParams.KEEP_PROXIMITY, true);
        problem.setDefaultProps(def);
        Algorithm moAlg;
        CrossoverOperator crossover;
        MutationOperator mutation;
        SelectionOperator selection;
        if (getNumObjectives() < 2) {
            throw new RuntimeException("provide at least 2 objectives. currently we have just" + getNumObjectives());
        }
        LOG.info("starting evolution {}", getName());
        LOG.info("variables: {}", problem.getNumberOfVariables());
        LOG.info("objectives: {}", getNumObjectives());
        LOG.info("generations: {}", getGenerations());
        LOG.info("population: {}", getPopulationSize());
        LOG.info("requested solutions: {}", getNumSolutions());
        for (int i = 0; i < getNumObjectives(); i++) {
            LOG.info("objective {}: {}", i, getObjective(i).getName());
        }
        MoSolution.setSolutionsCount(0);

        double crossoverDistributionIndex = problem.getNumberOfVariables();
        crossover = new IntegerSBXCrossover(getCrossoverProbability(), crossoverDistributionIndex);

        double mutationProb = 1.0 / problem.getNumberOfVariables();
        double mutationDistributionIndex = problem.getNumberOfVariables();
        mutation = new IntegerPolynomialMutation(mutationProb, mutationDistributionIndex);

        selection = new NaryTournamentSelection(numSolutions, new DominanceComparator());
        System.out.println("mutation: " + mutationProb);
        System.out.println("crossover: " + getCrossoverProbability());
        moAlg = new NSGAIIBuilder(problem, crossover, mutation)
                .setSelectionOperator(selection)
                .setMaxEvaluations(this.getGenerations())
                .setPopulationSize(this.getPopulationSize())
                //.setSolutionListEvaluator(new MultithreadedSolutionListEvaluator(8, problem))
                .build();

        fireEvolutionStarted(this);
        LOG.info("starting evolution");
        //AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(moAlg).execute();
        try {
            moAlg.run();
        } catch (Exception e) {
            LOG.error("failed clustering with {} & {}", getObjective(0).getName(), getObjective(1).getName());
            Exceptions.printStackTrace(e);
        }
        List<Solution> moPop = ((NSGAII) moAlg).getResult();
        LOG.info("result size: {}", moPop.size());
        fireFinalResult(moPop);
        int i = 0;
        for (Solution s : moPop) {
            System.out.print(i + ": ");
            for (int j = 0; j < getNumObjectives(); j++) {
                if (j > 0) {
                    System.out.print(", ");
                }
                System.out.print(s.getObjective(j));
            }
            System.out.print("\n");
            System.out.println("prop: " + ((MoSolution) s).getProps().toString());
            i++;
        }
        //long computingTime = algorithmRunner.getComputingTime();
        //System.out.println("computing time: " + computingTime);
        LOG.info("explored solutions: {}", MoSolution.getSolutionsCount());
        /*
         * int numberOfDimensions = getNumObjectives();
         * Front frontA = new ArrayFront(numberOfPoints, numberOfDimensions);
         * Front frontB = new ArrayFront(numberOfPoints, numberOfDimensions);
         *
         * Hypervolume hypervolume = new Hypervolume();
         * hypervolume.execute(frontA, frontB); */
 /*
         * Individual[] pop = new Individual[moPop.size()];
         * for (int j = 0; j < moPop.size(); j++) {
         * MoSolution b = (MoSolution) moPop.get(j);
         * pop[j] = b.getIndividual();
         * }
         *
         * fireResultUpdate(pop); */
        fireResult(moPop);
    }

    private void fireResult(List<Solution> res) {
        SolTransformer trans = SolTransformer.getInstance();
        List<OpSolution> solutions = trans.transform(res, new ArrayList<>(res.size()));
        int i = 0;
        Individual[] ind = new Individual[solutions.size()];
        for (OpSolution sol : solutions) {
            ind[i++] = new SimpleIndividual(sol.getClustering());
        }
        for (EvolutionListener listener : evoListeners) {
            listener.resultUpdate(ind, true);
        }
    }

    @Override
    public void clearObjectives() {
        if (objectives != null && !objectives.isEmpty()) {
            objectives.clear();
        }
    }

    public void addMOEvolutionListener(OpListener listener) {
        moListeners.add(listener);
    }

    protected void fireEvolutionStarted(EvolutionMO evo) {
        if (moListeners != null) {
            for (OpListener listener : moListeners) {
                listener.started(evo);
            }
        }
    }

    /**
     * Fired when repetitive run of same datasets was finished
     */
    public void fireFinishedBatch() {
        if (moListeners != null) {
            for (OpListener listener : moListeners) {
                listener.finishedBatch();
            }
        }
    }

    protected void fireFinalResult(List<Solution> res) {
        SolTransformer trans = SolTransformer.getInstance();
        List<OpSolution> solutions = trans.transform(res, new LinkedList<>());
        if (solutions != null && solutions.size() > 0) {
            if (moListeners != null) {
                for (OpListener listener : moListeners) {
                    listener.finalResult(solutions);
                }
            }
        } else {
            throw new RuntimeException("transforming solutions failed");
        }
    }

    @Override
    public int getNumSolutions() {
        return numSolutions;
    }

    /**
     * Number of solutions to be returned from evolution
     *
     * @param numSolutions should be lower than population size
     */
    @Override
    public void setNumSolutions(int numSolutions) {
        this.numSolutions = numSolutions;
    }

    public boolean iskLimited() {
        return kLimit;
    }

    public void setkLimit(boolean kLimit) {
        this.kLimit = kLimit;
    }

}
