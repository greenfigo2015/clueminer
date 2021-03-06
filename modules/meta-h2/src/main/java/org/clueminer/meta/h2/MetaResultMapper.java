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
package org.clueminer.meta.h2;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.clueminer.meta.api.MetaResult;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 *
 * @author Tomas Barton
 */
public class MetaResultMapper implements ResultSetMapper<MetaResult> {

    @Override
    public MetaResult map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new MetaResult(r.getInt("k"), r.getString("template"),
                r.getDouble("score"), r.getString("fingerprint"), r.getInt("hash"));
    }

}
