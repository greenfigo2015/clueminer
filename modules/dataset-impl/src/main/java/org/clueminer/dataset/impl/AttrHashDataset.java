package org.clueminer.dataset.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.clueminer.dataset.api.Attribute;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;

/**
 * A dataset with HashMap of attributes names for fast lookup
 *
 * @author Tomas Barton
 * @param <E>
 */
public class AttrHashDataset<E extends Instance> extends ArrayDataset<E> implements Dataset<E> {

    private static final long serialVersionUID = -240111429000747905L;
    protected HashMap<String, Integer> attrNames = new HashMap<>();

    public AttrHashDataset(int capacity) {
        //some default number of attributes
        super(capacity, 4);
    }

    public AttrHashDataset(int capacity, int attrCnt) {
        super(capacity, attrCnt);
    }

    /**
     * Set i-th attribute (column)
     *
     * @param i
     * @param attr
     */
    @Override
    public void setAttribute(int i, Attribute attr) {
        super.setAttribute(i, attr);
        attrNames.put(attr.getName(), i);
    }

    @Override
    public void setAttributes(Map<Integer, Attribute> attr) {
        for (Entry<Integer, Attribute> item : attr.entrySet()) {
            setAttribute(item.getKey(), item.getValue());
        }
    }

    /**
     *
     * @param attributeName
     * @param instanceIdx
     * @return
     */
    @Override
    public double getAttributeValue(String attributeName, int instanceIdx) {
        if (attrNames.containsKey(attributeName)) {
            int index = attrNames.get(attributeName);
            return get(instanceIdx, index);
        }
        throw new RuntimeException("attribute " + attributeName + " not found");
    }

    @Override
    public void setAttributeValue(String attributeName, int instanceIdx, double value) {
        if (attrNames.containsKey(attributeName)) {
            int index = attrNames.get(attributeName);
            //instance method should create new instace (row) if does not exist
            set(instanceIdx, index, value);
        } else {
            throw new RuntimeException("attribute " + attributeName + " not found");
        }
    }

    @Override
    public Attribute getAttribute(String attributeName) {
        int idx = attrNames.get(attributeName);
        return attributes[idx];
    }

    @Override
    public Dataset<E> duplicate() {
        AttrHashDataset<E> copy = new AttrHashDataset<>(this.size());
        copy.attrNames = this.attrNames;
        Map<Integer, Attribute> attrs = new HashMap<>(this.attributeCount());
        for (int i = 0; i < this.attributeCount(); i++) {
            attrs.put(i, attributes[i].duplicate());
        }
        copy.setAttributes(attrs);
        return copy;
    }
}