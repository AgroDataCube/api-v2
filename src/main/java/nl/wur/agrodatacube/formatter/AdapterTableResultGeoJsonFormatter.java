/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.formatter;

import nl.wur.agrodatacube.datasource.metadata.ColumnMetadata;
import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterTableResult;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * @author rande001
 */
public class AdapterTableResultGeoJsonFormatter extends AdapterTableResultJsonFormatter {

    public AdapterTableResultGeoJsonFormatter() {
    }

    protected String getHeader(AdapterTableResult tableResult) {
        String result = "{ \"type\" : \"FeatureCollection\"  ";
//        result = result.concat(", \"query\" : ").concat(JSONizer.toJson(tableResult.getQueryString()));
        result = result.concat(",\"metadata\" : [");
        String komma = "";
        for (ColumnMetadata c : tableResult.getColumnMetadata()) {
            if (!c.almostEmpty()) {
                result = result.concat(komma);
                komma = ",";
                result = result.concat(c.toJson());
                
                // children ???
            }
        }
        
        result = result.concat("]");
        result = result.concat(",").concat("\"features\" : [ ");
        return result;
    }

    protected String getEnd() {
        return "]}";
    }

    /**
     * Format an adapterresult as geojson.
     *
     * <PRE>
     * {
     * "type": "FeatureCollection",
     *      "features": [
     *          {   "type": "Feature",
     *              "geometry": { "type": "Point", "coordinates": [102.0, 0.6] },
     *              "properties": {
     *                  "key": "value"
     *              }
     *          },
     *      ]
     * }
     * </PRE>
     *
     * @param w
     * @throws Exception
     */
    @Override
    public void format(AdapterResult result, Writer w) throws Exception {
        AdapterTableResult table = (AdapterTableResult) result;
        if (!table.didSucceed()) {
            w.write(" { \"status\" : " + JSONizer.toJson(table.getStatus()));
            w.write(", \"query\" : " + JSONizer.toJson(table.getQueryString()));
            w.write("}"); //todo
            w.flush();
            return;
        }
        String geojsonResult = getHeader(table);

        String komma = " ";

        //
        // Add the rows from the table, each row is an GeoJson feature.
        //
        int j = 0;
        ArrayList<Object> row;

        int[] decimals = table.getDecimalInformation();
        int geomIndex = table.getGeomIndex();
        while ((row = table.getRow(j)) != null) {

            //
            // New feature so create header.
            //
            geojsonResult = geojsonResult.concat(komma);

            //
            // If this is next element add a comma for feature separation
            //
            komma = ",";
            geojsonResult = geojsonResult.concat(" { \"type\": \"Feature\" \n");

            // geometry
            geojsonResult = geojsonResult.concat(" , \"geometry\" :  ");
            if (geomIndex >= 0) {
                if (row.get(table.getGeomIndex()) != null) {
                    geojsonResult = geojsonResult.concat((String) row.get(table.getGeomIndex()));
                } else {
                    geojsonResult = geojsonResult.concat(" null ");
                }
            } else {
                geojsonResult = geojsonResult.concat(" null ");
            }

            // remaining properties
            boolean first = true;
            geojsonResult = geojsonResult.concat(", \"properties\": { ");
            for (int i = 0; i < table.getColumnCount(); i++) {
                if (i != geomIndex) {
                    Object o = row.get(i);
                    if (o != null) {
                        if (!first) {
                            geojsonResult += ",";
                        }
                        first = false;
                        // TODO: Numeriek geen quotes

                        if (row.get(i) instanceof java.lang.String) {
                            geojsonResult += "\"" + table.getColumnName(i) + "\" : " + (row.get(i) == null ? "null" : "\"" + row.get(i) + "\"");
                        } else if (row.get(i) instanceof java.sql.Date) {
                            geojsonResult += "\"" + table.getColumnName(i) + "\" : " + (row.get(i) == null ? "null" : "\"" + formatDate(row.get(i)) + "\""); // TODO: Formatter ivm datum
                        } else if (row.get(i) instanceof java.sql.Timestamp) { // 2012-04-23T18:25:43.511Z preferred
                            geojsonResult += "\"" + table.getColumnName(i) + "\" : " + (row.get(i) == null ? "null" : "\"" + formatTimestamp(row.get(i)) + "\""); // TODO: Formatter ivm datum
                        } else if (row.get(i) instanceof AdapterTableResult) {
                            AdapterTableResultJsonFormatter f = new AdapterTableResultJsonFormatter();
                            geojsonResult += "\"" + table.getColumnName(i) + "\" : " + f.format((AdapterTableResult) row.get(i));
                        } else if (row.get(i) instanceof java.lang.Double) {
                            // 
                            // Round it to nr of decimals in metainformation
                            //

                            if (decimals[i] >= 0) {
                                geojsonResult += "\"" + table.getColumnName(i) + "\" : " + round((Double) row.get(i), decimals[i]);
                            } else {
                                geojsonResult += "\"" + table.getColumnName(i) + "\" : " + row.get(i);
                            }
                        } else if (row.get(i) instanceof java.lang.Float) {
                            // 
                            // Round it to nr of decimals in metainformation
                            //

                            if (decimals[i] >= 0) {
                                Double d = new Double((Float) row.get(i));
                                geojsonResult += "\"" + table.getColumnName(i) + "\" : " + round(d, decimals[i]);
                            } else {
                                geojsonResult += "\"" + table.getColumnName(i) + "\" : " + row.get(i);
                            }
                        } else {// Integer, Long, Decimal
                            geojsonResult += "\"" + table.getColumnName(i) + "\" : " + row.get(i);
                        }
                    }
                }
            }

            //close properties and close feature
            geojsonResult += "}}";
            j++;
        }
        geojsonResult += getEnd();
        w.write(geojsonResult);
        w.flush();
    }

    /**
     *
     * @param tables
     * @param w
     * @throws Exception
     */
    @Override
    public void format(ArrayList<AdapterResult> tables,
            Writer w) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Format een adapterresult als geojson.
     *
     * <PRE>
     * {
     * "type": "FeatureCollection",
     *      "features": [
     *          {
     *              "type": "Feature",
     *              "geometry": {
     *              "type": "Point",
     *              "coordinates": [102.0, 0.6]
     *          },
     *          "properties": {
     *              "prop0": "value0"
     *          }
     *          },
     *      ]
     * }
     * </PRE>
     *
     * @param table
     * @return
     * @throws Exception
     */
    @Override
    public Object format(AdapterResult table) throws Exception {
        StringWriter w = new StringWriter();
        format(table, w);
        w.flush();
        return w.toString();
    }

    /**
     * Round on dec decimals.
     *
     * @param get
     * @param dec
     * @return
     */
    private Double round(Double value, int dec) {
        double p = dec;
        p = Math.pow(10.d, p);
        return Math.round(value * p) / p;
    }

}
