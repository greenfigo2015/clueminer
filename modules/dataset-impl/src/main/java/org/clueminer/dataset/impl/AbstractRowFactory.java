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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import org.clueminer.dataset.api.Attribute;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dataset.api.InstanceBuilder;
import org.clueminer.exception.ParserError;

/**
 * Common methods for all instance builders.
 *
 * @author deric
 * @param <E>
 */
public abstract class AbstractRowFactory<E extends Instance> implements InstanceBuilder<E> {

    protected final Dataset<E> dataset;
    public static final int DEFAULT_CAPACITY = 5;
    protected DecimalFormat decimalFormat;
    /**
     * values considered as missing values
     */
    protected static HashSet<String> missing = new HashSet<>(2);

    public AbstractRowFactory(Dataset<E> dataset) {
        this.dataset = dataset;
    }

    public AbstractRowFactory(Dataset<E> dataset, char decimalPointChar) {
        this.dataset = dataset;
        decimalFormat = setupFormat(decimalPointChar);
    }

    private DecimalFormat setupFormat(char decimalPointChar) {
        //some locales (e.g. French, Czech) have ',' as a decimal separator
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(decimalPointChar);
        //symbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(symbols);
        return df;
    }

    @Override
    public E create(double[] values) {
        E row = build(values);
        dataset.add(row);
        return row;
    }

    @Override
    public E create(double[] values, Object classValue) {
        E row = create(values);
        row.setClassValue(classValue);
        return row;
    }

    @Override
    public E create(double[] values, String classValue) {
        E row = build(values, classValue);
        dataset.add(row);
        return row;
    }

    @Override
    public E build(double[] values, String classValue) {
        E row = build(values);
        row.setClassValue(classValue);
        return row;
    }

    @Override
    public E build() {
        return build(DEFAULT_CAPACITY);
    }

    /**
     * Build and add Instance to Dataset
     *
     * @return
     */
    @Override
    public E create() {
        E row = build();
        dataset.add(row);
        return row;
    }

    @Override
    public E createCopyOf(E orig, Dataset<E> parent) {
        E copy = createCopyOf(orig);
        copy.setParent(parent);
        return copy;
    }

    /**
     * Creates a new DataRow with the given initial capacity.
     *
     * @param size
     */
    @Override
    public E create(int size) {
        E row = build(size);
        dataset.add(row);
        return row;
    }

    @Override
    public E create(String[] values) throws ParserError {
        Map<Integer, Attribute> attrs = dataset.getAttributes();
        E row = create(values.length);
        for (int i = 0; i < values.length; i++) {
            set(values[i], attrs.get(i), row);
        }
        return row;
    }

    @Override
    public E create(String[] values, Object classValue) throws ParserError {
        E inst = create(values);
        inst.setClassValue(classValue);
        return inst;
    }

    /**
     * Creates a data row from an array of Strings. If the corresponding
     * attribute is nominal, the string is mapped to its index.
     *
     * @param strings
     * @param attributes
     * @return
     * @see FileDataRowReader
     */
    @Override
    public E create(String[] strings, Attribute[] attributes) throws ParserError {
        E row = create(strings.length);
        for (int i = 0; i < strings.length; i++) {
            set(strings[i], attributes[i], row);
        }
        return row;
    }

    public static double string2Double(String str, DecimalFormat df) throws ParserError {
        if (str == null) {
            return Double.NaN;
        }

        try {
            //default English numbers
            if (df == null) {
                return Double.parseDouble(str);
            } else {
                Number num = df.parse(str);
                return num.doubleValue();
            }
        } catch (NumberFormatException | ParseException ex) {
            throw new ParserError(ex);
        }
    }

    /**
     * Generic type convertor. Supported types should be initialized in
     * <code>dispatch</code> variable in child class.
     *
     * @param value
     * @param attr
     * @param row
     */
    @Override
    public void set(Object value, Attribute attr, E row) throws ParserError {
        if (value == null) {
            if (attr.allowMissing()) {
                set(attr.getMissingValue(), attr, row);
            } else {
                throw new RuntimeException("missing value not allowed for " + attr.getName());
            }
        } else if (attr.isNominal()) {
            row.set(attr.getIndex(), attr.getMapping().mapString((String.valueOf(value).trim())));
        } else {
            dispatch(value, attr, row);
        }
    }

    /**
     * Internal conversion to appropriate object type
     *
     * @param value
     * @param attr
     * @param row
     * @throws ParserError
     */
    protected abstract void dispatch(Object value, Attribute attr, E row) throws ParserError;

    @Override
    public HashSet<String> getMissing() {
        return missing;
    }

    @Override
    public void setMissing(HashSet<String> missing) {
        this.missing = missing;
    }

    @Override
    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    @Override
    public void setDecimalFormat(DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }
}
