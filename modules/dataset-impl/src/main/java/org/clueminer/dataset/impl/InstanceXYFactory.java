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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clueminer.attributes.BasicAttrRole;
import org.clueminer.dataset.api.Attribute;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dataset.api.InstanceBuilder;
import org.clueminer.dataset.api.MetaStore;
import static org.clueminer.dataset.impl.AbstractRowFactory.string2Double;
import org.clueminer.dataset.row.Tools;
import org.clueminer.dataset.row.XYInstance;
import org.clueminer.exception.EscapeException;
import org.clueminer.exception.ParserError;

/**
 *
 * @author deric
 * @param <E>
 */
public class InstanceXYFactory<E extends Instance> extends AbstractRowFactory<E> implements InstanceBuilder<E> {

    private static final int DEFAULT_CAPACITY = 5;

    public InstanceXYFactory(Dataset<E> dataset) {
        super(dataset);
    }

    public InstanceXYFactory(Dataset<E> dataset, char decimalChar) {
        super(dataset, decimalChar);
    }

    @Override
    public E createCopyOf(E orig) {
        XYInstance row = new XYInstance(orig.size());
        row.setId(orig.getId());
        row.setIndex(orig.getIndex());
        row.setClassValue(orig.classValue());
        return (E) row;
    }

    @Override
    public E build(int capacity) {
        return (E) new XYInstance(capacity);
    }

    @Override
    public E build(double[] values) {
        XYInstance row = new XYInstance(values.length);
        for (int i = 0; i < values.length; i++) {
            row.set(i, values[i]);
        }
        return (E) row;
    }

    @Override
    public E create(String[] strings, Attribute[] attributes) throws ParserError {
        XYInstance dataRow = (XYInstance) create(strings.length);
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] != null) {
                strings[i] = strings[i].trim();
            }
            if ((strings[i] != null) && (strings[i].length() > 0)) {
                if (attributes[i].isNominal()) {
                    try {
                        String unescaped = Tools.unescape(strings[i]);
                        dataRow.set(attributes[i].getIndex(), attributes[i].getMapping().mapString(unescaped));
                    } catch (EscapeException ex) {
                        Logger.getLogger(DoubleArrayFactory.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    dataRow.set(attributes[i].getIndex(), string2Double(strings[i], this.decimalFormat));
                }
            } else {
                dataRow.set(attributes[i].getIndex(), Double.NaN);
            }
        }
        return (E) dataRow;
    }

    @Override
    public void set(Object value, Attribute attr, E row) throws ParserError {
        if (attr.getRole() == BasicAttrRole.INPUT) {
            //TODO: use dispatcher from parent class?
            if (value instanceof JsonArray) {
                JsonArray ary = (JsonArray) value;
                if (ary.size() == 2) {
                    JsonElement x = ary.get(0);
                    JsonElement y = ary.get(1);
                    ((XYInstance) row).put(x.getAsDouble(), y.getAsDouble());
                } else {
                    XYInstance inst = (XYInstance) row;
                    for (JsonElement elem : ary) {
                        if (elem.isJsonArray()) {
                            JsonArray a = elem.getAsJsonArray();
                            if (a.size() == 2) {
                                //only 2D data are supported
                                inst.put(a.get(0).getAsDouble(), a.get(1).getAsDouble());
                            } else {
                                throw new ParserError("don't know how to process " + a);
                            }
                        }
                    }
                }
            } else {
                //primitive object
                throw new ParserError("don't know how to process " + value);
            }
        } else if (attr.getRole() == BasicAttrRole.META) {
            MetaStore meta = dataset.getMetaStore();
            meta.set(attr, row.getIndex(), value);
        }
    }

    @Override
    protected void dispatch(Object value, Attribute attr, E row) throws ParserError {
        //needed in case that set is not overriden
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
