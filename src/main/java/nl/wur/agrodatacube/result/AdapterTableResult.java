/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.result;

import java.math.BigDecimal;
import nl.wur.agrodatacube.datasource.metadata.ColumnMetadata;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Yke
 */
public class AdapterTableResult extends AdapterResult {

    private String[] columnNames;
    private ArrayList<ArrayList<Object>> columnValues;
    private int geomColumn = -1;
    private int areaColumn = -1;
    private ArrayList<ColumnMetadata> metadata;

    public AdapterTableResult() {
        super();
        setArea(0.d);
        columnValues = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public void addColumnMetadata(ColumnMetadata m) {
        metadata.add(m);
    }

    /**
     * Sometimes duplicates. So only add if not present. 
     * @param m 
     */
    public void addColumnMetadata(ArrayList<ColumnMetadata> m) {
        
        for (ColumnMetadata newMetadata : m) {
            int i = 0;
            boolean found= false;
            for (i = 0; i < metadata.size(); i++) {
                found= false;
                ColumnMetadata oldMetaData = metadata.get(i);
                if (oldMetaData.getColumnName().equalsIgnoreCase(newMetadata.getColumnName())) {
                    found= true;
                    break;
                }
            }
            if (! found) {
                metadata.add(newMetadata);
            }
        }
//        metadata.addAll(m);
    }

    @Override
    protected void clear() {
        columnNames = new String[1];
        columnValues = new ArrayList<>();
    }

    public void addRow(Object[] values) {
        addRow(new ArrayList<>(Arrays.asList(values)));
    }

    /**
     * Oracle returns a BigDecimal for area.
     * 
     * @param values 
     */
    public void addRow(ArrayList<Object> values) {
        if (this.areaColumn >= 0) {
            if (values.get(this.areaColumn) != null) 
                if (values.get(this.areaColumn).getClass()==java.math.BigDecimal.class) {
                    BigDecimal bd = (BigDecimal) values.get(this.areaColumn);
                    Double d = bd.doubleValue();
                    setArea(getArea() + d);
                } else if (values.get(this.areaColumn).getClass()==java.lang.Double.class) {
                    setArea(getArea() + (Double) values.get(this.areaColumn));
            }
        }
        columnValues.add(values);
    }

    public String getColumnName(int i) {
        return columnNames[i].toLowerCase(); // Oracle returns uppercase.
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public ArrayList<Object> getRow(int i) {
        if (i >= columnValues.size()) {
            return null;
        }
        return columnValues.get(i);
    }

    public int getGeomIndex() {
        return (geomColumn);
    }

    public int getRowCount() {
        return columnValues.size();
    }

    public void setColumnCount(int i) {
        columnNames = new String[i];
    }

    public void setColumnName(int i, String columnName) {
        columnNames[i] = columnName;

        if ("geom".equalsIgnoreCase(columnName)) {
            geomColumn = i;
        }

        if ("area".equalsIgnoreCase(columnName)) {
            areaColumn = i;
        }
    }

    public int[] getDecimalInformation() {
        int[] result = new int[getColumnCount()];
        int i = 0;
        for (String s : columnNames) {
            result[i] = -1; // decimal info can be ignored
            for (ColumnMetadata q : metadata) {
                if (s.equalsIgnoreCase(q.getColumnName())) {
                    result[i] = q.getDecimals();
                    // break results in wong resuls
                }
            }
            i++;
        }
        return result;
    }

    public ArrayList<ColumnMetadata> getColumnMetadata() {
        return metadata;
    }
}
