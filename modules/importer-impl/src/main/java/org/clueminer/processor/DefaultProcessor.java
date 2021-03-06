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
package org.clueminer.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.clueminer.dataset.api.Attribute;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dataset.impl.ArrayDataset;
import org.clueminer.io.importer.api.AttributeDraft;
import org.clueminer.io.importer.api.InstanceDraft;
import org.clueminer.processor.spi.Processor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts preloaded data into actual dataset structure.
 *
 * @author Tomas Barton
 * @param <D> draft type
 * @param <E> target row class type
 */
@ServiceProvider(service = Processor.class)
public class DefaultProcessor<D extends InstanceDraft, E extends Instance> extends AbstractProcessor<D, E> implements Processor<D> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProcessor.class);

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DefaultProcessor.class, "DefaultProcessor.displayName");
    }

    @Override
    protected Dataset<E> createDataset(ArrayList<AttributeDraft> inputAttr) {
        Dataset<E> data = new ArrayDataset(container.getInstanceCount(), inputAttr.size());
        data.setDataType(container.getDataType());
        return data;
    }

    @Override
    protected Map<Integer, Integer> attributeMapping(ArrayList<AttributeDraft> inputAttr) {
        //set attributes
        int index = 0;
        Map<Integer, Integer> inputMap = new HashMap<>();

        for (AttributeDraft attrd : inputAttr) {
            //create just input attributes
            Attribute attr = dataset.attributeBuilder().build(attrd.getName(), attrd.getType(), attrd.getRole());
            attr.setIndex(index);
            dataset.setAttribute(index, attr);
            LOG.info("setting attr {} at pos {}", attr.getName(), attr.getIndex());
            inputMap.put(attrd.getIndex(), index);
            index++;
        }
        return inputMap;
    }

}
