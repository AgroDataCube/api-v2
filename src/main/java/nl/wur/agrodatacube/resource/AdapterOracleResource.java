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
import nl.wur.agrodatacube.resource.query.ConfigurationParameter;

/**
 *
 * @author rande001
 */
public abstract class AdapterOracleResource extends AdapterResource {

    protected ArrayList<AdapterOracleResource> children;
    protected String[] linkColumns; // names of the columnsin the parent
    protected String parentName;
    private String[] columns; // komma separated list of column names of valid sql expressions.
    // private String[] extraColumns; // komma separated list of column names of valid sql names from linked resource.
    private String[] orderBy;
    private String geometryColumn;

    public AdapterOracleResource(String name) {
        super(name);
        addQueryParameter(new ConfigurationParameter("page_size", "int", null));
        addQueryParameter(new ConfigurationParameter("page_start", "int", null)); // todo check names
        addQueryParameter(new ConfigurationParameter("nr_of_hits", "int", null));

    }

    public ArrayList<AdapterOracleResource> getChildren() {
        return children;
    }

    public void addChildren(AdapterOracleResource child) {
        this.children.add(child);
    }

    public String[] getLinkColumns() {
        return linkColumns;
    }

    public void setLinkColumns(String linkColumns) {
        this.linkColumns = linkColumns.split(",");
    }

    public String getParentName() {
        return parentName;
    }

    public String[] getColumns() {
        return columns;
    }

    /**
     * Return the columns as a komma separated list to be used in a select
     * statement.
     *
     * @return
     */
    public String getColumnsForSelect() {
        String s = "";
        String komma = "";

        for (String c : getColumns()) {
            s = s.concat(komma).concat(c);
            komma = ",";
        }
        return s;
    }

    public void setColumns(String columns) {
        this.columns = new String[0];
        if (columns != null) {
            if (columns.trim().length() > 0) {
                this.columns = columns.split(",");
            }
        }
    }

    public abstract String getBaseQuery();

//    public String[] getExtraCcolumns() {
//        if (extraColumns == null) {
//            return new String[0];
//        }
//        return extraColumns;
//    }
//
//    public void setExtraColumns(String extraColumnsString) {
//        this.extraColumns = new String[0];
//        if (extraColumnsString != null) {
//            if (extraColumnsString.trim().length() > 0) {
//                this.extraColumns = extraColumnsString.split(",");
//            }
//        }
//    }
    public String[] getOrderBy() {
        if (orderBy != null) {
            return orderBy;
        }
        return new String[0];
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy.split(",");
    }

    public String getGeometryColumn() {
        return geometryColumn;
    }

    public void setGeometryColumn(String geometryColumn) {
        this.geometryColumn = geometryColumn;
    }

}
