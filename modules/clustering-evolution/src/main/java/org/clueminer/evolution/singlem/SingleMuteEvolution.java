package org.clueminer.evolution.singlem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clueminer.clustering.ClusteringExecutorCached;
import org.clueminer.clustering.aggl.HACLW;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.Executor;
import org.clueminer.clustering.api.evolution.Evolution;
import org.clueminer.clustering.api.evolution.Individual;
import org.clueminer.clustering.api.evolution.Population;
import org.clueminer.evolution.attr.TournamentPopulation;
import org.clueminer.evolution.multim.MultiMuteEvolution;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Barton
 */
@ServiceProvider(service = Evolution.class)
public class SingleMuteEvolution extends MultiMuteEvolution implements Runnable, Evolution, Lookup.Provider {

    private static final String name = "single-mute";
    private static final Logger logger = Logger.getLogger(SingleMuteEvolution.class.getName());
    private HashSet<String> tabu;
    private boolean isFinished = true;
    private Population<? extends Individual> population;


    public SingleMuteEvolution() {
        //cache normalized datasets
        this.exec = new ClusteringExecutorCached();
        init();
    }

    public SingleMuteEvolution(Executor executor) {
        this.exec = executor;
        init();
    }

    private void init() {
        algorithm = new HACLW();
        instanceContent = new InstanceContent();
        lookup = new AbstractLookup(instanceContent);
        prepare();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Individual createIndividual() {
        return new SingleMuteIndividual(this);
    }

    @Override
    public void run() {
        clean();
        int stdMethods = standartizations.size();

        if (ph != null) {
            int workunits = getGenerations();
            logger.log(Level.INFO, "stds: {0}", stdMethods);
            logger.log(Level.INFO, "distances: {0}", dist.size());
            logger.log(Level.INFO, "linkages: {0}", linkage.size());
            ph.start(workunits);
            ph.progress("starting " + getName() + "evolution...");
        }

        time.a = System.currentTimeMillis();
        LinkedList<Individual> children = new LinkedList<>();
        population = new TournamentPopulation(this, populationSize, SingleMuteIndividual.class);
        avgFitness.a = population.getAvgFitness();
        Individual best = population.getBestIndividual();
        bestFitness.a = best.getFitness();
        ArrayList<Individual> selected = new ArrayList<>(populationSize);

        for (int g = 0; g < generations && !isFinished; g++) {

            // clear collection for new individuals
            children.clear();

            // apply mutate operator
            for (int i = 0; i < population.size(); i++) {
                Individual current = population.getIndividual(i).deepCopy();
                current.mutate();
                if (current.isValid()) {
                    if (!isItTabu(current.toString())) {
                        // put mutated individual to the list of new individuals
                        children.add(current);
                    }
                }
            }
            double fitness;
            logger.log(Level.INFO, "gen: {0}, num children: {1}", new Object[]{g, children.size()});
            for (Individual child : children) {
                child.countFitness();
                child.getFitness();
            }
            selected.clear();
            // merge new and old individuals
            for (int i = children.size(); i < population.size(); i++) {
                Individual tmpi = population.getIndividual(i).deepCopy();
                tmpi.countFitness();
                selected.add(tmpi);
            }

            for (Individual ind : children) {
                fitness = ind.getFitness();
                if (!Double.isNaN(fitness)) {
                    selected.add(ind);
                }
            }

            // sort them by fitness (thanks to Individual implements interface Comparable)
            Individual[] newIndsArr = selected.toArray(new Individual[0]);
            //  for (int i = 0; i < newIndsArr.length; i++) {
            //      System.out.println(i + ": " + newIndsArr[i].getFitness());
            //  }
            if (maximizedFitness) {
                Arrays.sort(newIndsArr, Collections.reverseOrder());
            } else {
                //natural ordering
                Arrays.sort(newIndsArr);
            }

            int indsToCopy;
            if (newIndsArr.length > population.size()) {
                indsToCopy = population.size();
            } else {
                indsToCopy = newIndsArr.length;
            }
            if (ph != null) {
                ph.progress(indsToCopy + " new individuals in population. generation: " + g);
            }
            if (indsToCopy > 0) {
                //System.out.println("copying " + indsToCopy);
                //TODO: old population should be sorted as well? take only part of the new population?
                System.arraycopy(newIndsArr, 0, population.getIndividuals(), 0, indsToCopy);
            } else {
                logger.log(Level.WARNING, "no new individuals in generation = {0}", g);
                //    throw new RuntimeException("no new individuals");
            }

            // print statistic
            // System.out.println("gen: " + g + "\t bestFit: " + pop.getBestIndividual().getFitness() + "\t avgFit: " + pop.getAvgFitness());
            Individual bestInd = population.getBestIndividual();
            Clustering<Cluster> clustering = bestInd.getClustering();
            instanceContent.add(clustering);
            fireBestIndividual(g, population);
            if (ph != null) {
                ph.progress(g);
            }
        }

        time.b = System.currentTimeMillis();
        population.sortByFitness();
        avgFitness.b = population.getAvgFitness();
        best = population.getBestIndividual();
        bestFitness.b = best.getFitness();
        fireFinalResult(generations, best, time, bestFitness, avgFitness);

        finish();
    }

}
