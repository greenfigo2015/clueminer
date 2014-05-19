package org.clueminer.clustering.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import org.clueminer.dataset.api.Instance;
import org.openide.util.Lookup;

/**
 * Clustering is a set of clusters where each of clusters have to implement
 * Dataset interface.
 *
 * @author Tomas Barton
 * @param <T>
 */
public interface Clustering<T extends Cluster> extends Cloneable, Serializable, Iterable<T>, Collection<T> {

    /**
     * Human readable name of the clustering
     *
     * @return basic information about clustering
     */
    String getName();

    /**
     * @return number of clusters in clustering
     */
    @Override
    int size();

    /**
     * Get i-th item
     *
     * @param i
     * @return
     */
    T get(int i);

    /**
     * Inserts Dataset (a cluster) into Clustering (a set of clusters)
     *
     * @param d
     */
    void put(Cluster<? extends Instance> d);

    /**
     * Inserts Cluster at i-th position
     *
     * @param index
     * @param d
     */
    void put(int index, Cluster<Instance> d);

    /**
     * Return true if Dataset exists at index
     *
     * @param index
     * @return
     */
    boolean hasAt(int index);

    /**
     * Merge all given Datasets into the first one
     *
     * @param datasets
     */
    void merge(Cluster<Instance>... datasets);

    /**
     * Name of i-th cluster
     *
     * @param i
     * @return label of cluster (e.g. number as string)
     */
    String getClusterLabel(int i);

    /**
     * In case of hard assignments should be equal to total number of elements
     * (instances) in the original dataset
     *
     * @return total number of elements (in all clusters)
     */
    int instancesCount();

    /**
     * Iterator over all instances in clustering regardless assignment to a
     * cluster
     *
     * @return instances iterator
     */
    Iterator<Instance> instancesIterator();

    /**
     * Computes centroid for the whole dataset (clustering)
     *
     * @return centroid for all clusters
     */
    Instance getCentroid();

    /**
     *
     * @return sizes of all clusters
     */
    Integer[] clusterSizes();

    /**
     * Return ID of item's cluster
     *
     * @param instanceId
     * @return
     */
    int assignedCluster(int instanceId);

    /**
     * Create new cluster with given ID
     *
     * @param clusterId
     * @return newly created cluster
     */
    Cluster<? extends Instance> createCluster(int clusterId);

    /**
     * Create cluster with new ID (starting from 0)
     *
     * @return
     */
    Cluster<? extends Instance> createCluster();

    /**
     * Lookup is used for retrieving objects associated with this clustering
     * result
     *
     * @return lookup instance for accessing related objects (Dataset,
     * hierarchical clustering etc.)
     */
    public Lookup getLookup();

    /**
     * Add object to lookup
     *
     * @param instance
     */
    public void lookupAdd(Object instance);

    /**
     * Removes object from lookup
     *
     * @param instance
     */
    public void lookupRemove(Object instance);

}
