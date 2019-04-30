/*
* Copyright 2019 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.datasource.metadata;

import nl.wur.agrodatacube.formatter.JSONizer;

/**
 *
 * @author rande001
 */
public class ColumnMetadata {

    private String columnName;
    private String description;
    private String units;
    private Integer decimals;

    public ColumnMetadata(String columnName) {
        this.columnName = columnName;
    }

    public boolean almostEmpty() {
        int i = 0;
        i += (columnName == null ? 0 : 1);
        i += (description == null ? 0 : 1);
        i += (units == null ? 0 : 1);
        
        return i <= 1;
    }
    public ColumnMetadata(String columnName, String description, String units, Integer decimals) {
        this.columnName = columnName;
        this.description = description;
        this.units = units;
        this.decimals = decimals;
    }

    public String toJson() {
        StringBuilder b = new StringBuilder();

        b.append("{ ");

        if (columnName != null) {
            b.append("\"column_name\" : ").append(JSONizer.toJson(columnName));
        }
        if (description != null) {
            b.append(",\"description\" : ").append(JSONizer.toJson(description));
        }
        if (units != null) {
            b.append(",\"units\" : ").append(JSONizer.toJson(units));
        }
        b.append("}");

        return b.toString();
    }

    public String getColumnName() {
        return columnName;
    }

    public int getDecimals() {
        return (decimals == null ? -1 : decimals);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setDecimals(Integer decimals) {
        if (decimals != null) {
        this.decimals = decimals;
        }
    }
}
