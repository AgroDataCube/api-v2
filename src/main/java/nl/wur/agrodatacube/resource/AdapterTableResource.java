/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.resource;

import java.util.ArrayList;

/**
 * A table resource gets its data from a database.
 *
 * @author rande001
 */
public class AdapterTableResource extends AdapterPostgresResource {

    private String tableName;
    private String rasterColumn;
    

    public AdapterTableResource(String name) {
        super(name);
        children = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    @Override
    public String getBaseQuery() {
        return "select ".concat(getColumnsForSelect()).concat(" from ").concat(getTableName());
    }

    public boolean isRasterResource() {
        return getRasterColumn() != null;
    }

    public boolean isGeometryResource() {
        return getGeometryColumn() != null;
    }

    public String getRasterColumn() {
        return rasterColumn;
    }

    public void setRasterColumn(String rasterColumn) {
        this.rasterColumn = rasterColumn;
    }

}
