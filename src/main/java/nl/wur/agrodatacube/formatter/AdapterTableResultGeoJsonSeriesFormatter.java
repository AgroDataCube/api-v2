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

import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterTableResult;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Generate geojson but only 1 geometry an all other data is added as an object
 * array.
 *
 * @author rande001
 */
public class AdapterTableResultGeoJsonSeriesFormatter extends AdapterTableResultGeoJsonFormatter {

    public AdapterTableResultGeoJsonSeriesFormatter() {
        super();
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
     *                  [ {"key1": "value1", "key1": "value2"}
     *                  , {"key1": "value3", "key1": "value4"}
     *                  ]
     *              }
     *          },
     *      ]
     * }
     * </PRE>
     *
     * @param result
     * @param table
     * @param w
     * @throws Exception
     */
    @Override
    public void format(AdapterResult result, Writer w) throws Exception {
        AdapterTableResult table = (AdapterTableResult) result;
        if (!table.didSucceed()) {
            w.write("{\"status\" : \"" + table.getStatus() + "\"}");
            w.flush();
            return;
        }

        String geojsonResult = getHeader(table);
        if (table.getRowCount() == 0) {
            geojsonResult = geojsonResult.concat(getEnd());
            w.write(geojsonResult);
            w.flush();
            return;
        }

        //
        // So we have a result. Now add one feature
        //
        geojsonResult = geojsonResult.concat(" { \"type\": \"Feature\" \n");
        // geometry
        geojsonResult = geojsonResult.concat(" , \"geometry\" :  ");
        int geomIndex = table.getGeomIndex();
        if (geomIndex >= 0) {
            if (table.getRow(0).get(table.getGeomIndex()) != null) {
                geojsonResult = geojsonResult.concat((String) table.getRow(0).get(table.getGeomIndex()));
            } else {
                geojsonResult = geojsonResult.concat(" null ");
            }
        } else {
            geojsonResult = geojsonResult.concat(" null ");
        }

        geojsonResult = geojsonResult.concat(", \"properties\": [ ");

        String komma = " ";

        //
        // Add all other columns of all rows as objects to a collection
        //
        int j = 0;
        ArrayList<Object> row;

        while ((row = table.getRow(j)) != null) {

            //
            // New row so create object.
            //
            geojsonResult = geojsonResult.concat(komma);

            //
            // If this is next element add a comma for feature separation
            //
            komma = ",";

            // remaining properties
            boolean first = true;
            geojsonResult += " { ";
            for (int i = 0; i < table.getColumnCount(); i++) {
                if (i != geomIndex) {
                    Object o = row.get(i);
                    if (o != null) {
                        if (!first) {
                            geojsonResult += " , ";
                        }
                        first = false;
                        // TODO: Numeriek geen quotes

                        if (row.get(i) instanceof java.lang.String) {
                            geojsonResult += "\"" + table.getColumnName(i) + "\" : " + (row.get(i) == null ? "null" : "\"" + row.get(i) + "\"");
                        } else if (row.get(i) instanceof java.sql.Date) {
                            geojsonResult += "\"" + table.getColumnName(i) + "\" : " + (row.get(i) == null ? "null" : "\"" + formatDate(row.get(i)) + "\""); // TODO: Formatter ivm datum
                        } else {
                            geojsonResult += "\"" + table.getColumnName(i) + "\" : " + row.get(i);
                        }
                    }
                }
            }

            //close object
            geojsonResult += "}";
            j++;
        }
        geojsonResult += getEnd();
        w.write(geojsonResult);
        w.flush();
    }

    @Override
    protected String getEnd() {
        return "]}]}";
    }

}
