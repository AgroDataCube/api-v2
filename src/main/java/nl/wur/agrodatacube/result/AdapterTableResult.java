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

    public void addColumnMetadata(ArrayList<ColumnMetadata> m) {
        metadata.addAll(m);
    }

//    public AdapterTableResult(String[] columnNames) {
//        this();
//        this.columnNames = columnNames;
//        for (int i = 0; i < columnNames.length; i++) {
//            if ("geom".equalsIgnoreCase(columnNames[i])) {
//                geomColumn = i;
//                break;
//            }
//        }
//
//        for (int i = 0; i < columnNames.length; i++) {
//            if ("area".equalsIgnoreCase(columnNames[i])) {
//                areaColumn = i;
//                break;
//            }
//        }
//    }
    @Override

    protected void clear() {
        columnNames = new String[1];
        columnValues = new ArrayList<>();
    }

//    public void setColumnsNames(String[] names) {
//        columnNames = names;
//    }
    public void addRow(Object[] values) {
        addRow(new ArrayList<>(Arrays.asList(values)));
    }

    public void addRow(ArrayList<Object> values) {

        // todo area
        if (this.areaColumn >= 0) {
            if (values.get(this.areaColumn) != null) {
                setArea(getArea() + (Double) values.get(this.areaColumn));
            }
        }
        columnValues.add(values);
    }

//    /**
//     * Build an AdapterTableResult from a sql resultset. If this task has child
//     * resources also include them.
//     *
//     * @param rs
//     * @param pg
//     * @return
//     */
//    public static AdapterTableResult fromSQLResulSet(ResultSet rs, PostgresQuery pg) {
//        AdapterTableResult result = new AdapterTableResult();
//        try {
//
//            // set the column names
//            String[] columnNames = new String[rs.getMetaData().getColumnCount()];
//            int nColumns = rs.getMetaData().getColumnCount();
//            for (int i = 1; i <= nColumns; i++) {
//                columnNames[i - 1] = rs.getMetaData().getColumnLabel(i);
//            }
//            result = new AdapterTableResult(columnNames);
//
//            // add the values to a table (no paging yet)
//            while (rs.next()) {
//                ArrayList<Object> row = new ArrayList<>();
//                if (result.areaColumn >= 0) {
//                    result.area += (Double) rs.getObject(result.areaColumn + 1); // rs index is 1 based not 0 based.
//                }
//                for (int i = 1; i <= nColumns; i++) {
//                    row.add(rs.getObject(i));
//                }
//                result.columnValues.add(row);
//            }
//            rs.getStatement().close();
//            rs.close(); // Niet bij pagination
//        } catch (Exception e) {
//            result.setStatus(e.getMessage());
//        }
//        return result;
//    }
    public String getColumnName(int i) {
        return columnNames[i];
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

//    public void addRow(ResultSet rs) {
//
//    }
//
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
