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
package org.clueminer.meta.api;

import java.util.Collection;
import java.util.LinkedHashMap;
import org.clueminer.utils.ServiceFactory;
import org.openide.util.Lookup;

/**
 *
 * @author deric
 */
public class DataStatsFactory extends ServiceFactory<DataStats> {

    private static DataStatsFactory instance;

    public static DataStatsFactory getInstance() {
        if (instance == null) {
            instance = new DataStatsFactory();
        }
        return instance;
    }

    private DataStatsFactory() {
        providers = new LinkedHashMap<>();
        Collection<? extends DataStats> list = Lookup.getDefault().lookupAll(DataStats.class);
        for (DataStats c : list) {
            for (String name : c.provides()) {
                providers.put(name, c);
            }
        }
        sort();
    }

    @Override
    public DataStats[] getAllArray() {
        return providers.values().toArray(new DataStats[0]);
    }
}
