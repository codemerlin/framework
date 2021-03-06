/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.odoo.orm;

import java.util.List;

import com.odoo.orm.types.OEOneToMany;

public class OEO2MRecord {
	OEColumn mCol = null;
	int mId = 0;
	OEDatabase mDatabase = null;

	public OEO2MRecord(OEDatabase oeDatabase, OEColumn col, int id) {
		mDatabase = oeDatabase;
		mCol = col;
		mId = id;
	}

	public List<OEDataRow> browseEach() {
		OEOneToMany m2o = (OEOneToMany) mCol.getType();
		String column = mDatabase.tableName() + "_id";
		OEDatabase db = (OEDatabase) m2o.getDBHelper();
		return db.select(column + " = ? ", new String[] { mId + "" });
	}

    public List<OEDataRow> browseEach(String column_name) {
        OEOneToMany m2o = (OEOneToMany) mCol.getType();
        String column = column_name;
        OEDatabase db = (OEDatabase) m2o.getDBHelper();
        return db.select(column + " = ? ", new String[] { mId + "" });
    }

    public OEDataRow browseAt(int index) {
		List<OEDataRow> list = browseEach();
		if (list.size() == 0) {
			return null;
		}
		return list.get(index);
	}

}
