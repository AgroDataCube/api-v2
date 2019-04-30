/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */


/*
 * This formatter retuns the responses in a format which is recognized by the clients of the BWWGeoWebservice.
 * 
 * Request example 
 *
 * <PRE>
 *      {
 *	    "request": ["bww-sloten"],
 *	    "percelen": [
 *                  { "perceel": {
 *			"id": 785711,
 *			"format": "wkt",
 *			"epsg": 28992,
 *			"geom": "MULTIPOLYGON(((85586.4800000004 440594.52225,85649.6212499999 440642.022,85698.1561250016 440678.365625001,85718.3982499987 440693.317125,85721.9635000005 440695.502250001,85722.9986249991 440695.157249998,85725.5288750008 440690.211750001,85730.7043749988 440672.5,85736.3398750015 440654.328249998,85739.6752500013 440643.977249999,85741.5155000016 440640.871874999,85742.6656249985 440639.031750001,85733.6947499998 440633.626125,85687.9201250002 440599.697749998,85646.7459999993 440568.874749999,85625.5838750005 440553.46325,85616.0379999988 440547.482625,85613.1627499983 440546.102375001,85611.8976249993 440545.987374999,85581.9946249984 440585.321249999,85580.3843749985 440588.081625,85580.6145000011 440589.576749999,85582.339625001 440591.416875001,85586.4800000004 440594.52225)))"
 *		      }
 * 	            }
 *          ]
 *       }
 *
 * Response
 *
 * {
 *	"request": {
 *		"status": "ok",
 *		"percelen": [{
 *			"perceel": {
 *				"id": 785711,
 *				"responses": [{
 *					"response": {
 *						"request": "bww-sloten",
 *						"status": "ok",
 *						"codes": [{
 *							"ogc_fid": "907405",
 *							"lokaalid": "110574884",
 *							"breedteklasse": "0,5 - 3 meter",
 *                                                      "grens_lengte" :2.345678     
 *							"geom": "SRID=28992;LINESTRING(85724.7949071984 440699.804896059,85739.625 440645.768)"
 *						}, {
 *							"ogc_fid": "2968383",
 *							"lokaalid": "123154992",
 *							"breedteklasse": "6 - 12 meter",
 *                                                      "grens_lengte" :2.345678     
 *							"geom": "SRID=28992;POLYGON((85608.294501913 440542.557371886,85607.9172652518 440542.961365039,85578.0142652509 440582.295240039,85577.6757517737 440582.801858392,85576.0655017738 440585.562233393,85575.6093936438 440586.598535973,85575.3981384714 440587.710888846,85575.442569064 440588.842252442,85575.5445591929 440589.504883576,85577.597 440588.677,85578.564 440588.287,85586.382 440578.372,85591.53 440572.397,85594.072 440568.266,85601.254 440559.749,85603.733 440555.428,85611.232 440545.767,85612.631 440545.449,85614.197 440545.434,85609.436 440542.948,85608.294501913 440542.557371886))"
 *						}]
 *					}
 *				}]
 *			}
 *		}]
 *	}
 *}
 * </PRE>
 * 
 * This formatter only handles data for 1 field in stead of the multiple fields that are supported by BWW.
 */
package nl.wur.agrodatacube.formatter.bww;

import nl.wur.agrodatacube.formatter.AdapterTableResultFormatter;
import nl.wur.agrodatacube.formatter.JSONizer;
import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterTableResult;
import nl.wur.agrodatacube.servlet.AnalyticsTask;
import java.io.Writer;
import java.util.ArrayList;

/**
 * This class implements a agroDataCube table result formatter that produces the
 * same result as the formatter implemented in the bWWGeoservice.
 *
 * @author rande001
 */
public class AdapterTableBWWJSONFormatter extends AdapterTableResultFormatter {

    private AnalyticsTask task; // The task that produced this result

    public AdapterTableBWWJSONFormatter(AnalyticsTask t) {
        super();
        this.task = t;
    }

    @Override
    public void format(AdapterResult result, Writer w) throws Exception {

        AdapterTableResult table =  (AdapterTableResult) result;
        
        w.write(getHeader(table));

        //
        // Contents
        //
        StringBuilder geojsonResult = new StringBuilder("");
        int j = 0;
        ArrayList<Object> row;

        if (table.didSucceed()) {
            //
            // Each row is ajson object. So name : vale [, name : value]
            String komma = "";
            while ((row = table.getRow(j)) != null) {
                geojsonResult = geojsonResult.append(komma);
                geojsonResult.append("{");

                // remaining properties
                boolean first = true;
                for (int i = 0; i < table.getColumnCount(); i++) {
                    Object o = row.get(i);
                    if (o != null) {
                        if (!first) {
                            geojsonResult.append(",");
                        }
                        first = false;
                        // TODO: Numeriek geen quotes

                        if (row.get(i) instanceof java.lang.String) {
                            geojsonResult.append( "\"" + table.getColumnName(i) + "\" : " + (row.get(i) == null ? "null" : "\"" + row.get(i) + "\""));
                        } else if (row.get(i) instanceof java.sql.Date) {
                            geojsonResult.append(  "\"" + table.getColumnName(i) + "\" : " + (row.get(i) == null ? "null" : "\"" + formatDate(row.get(i)) + "\"")); // TODO: Formatter ivm datum
                        } else {
                            geojsonResult.append(  "\"" + table.getColumnName(i) + "\" : " + row.get(i));
                        }
                    }
                }
                komma = ",";

                //close properties and close feature
                geojsonResult.append("}");
                j++;
            }
        }

        w.write(geojsonResult.toString());
        w.write(getFooter());
        w.flush();
    }

    @Override
    public void format(ArrayList<AdapterResult> tables, Writer w) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String getHeader(AdapterTableResult table) {
        return "{  "
                + "	\"request\": { "
                + "		\"status\": \"ok\","
                + "		\"percelen\": [{"
                + "			\"perceel\": {"
                + "				\"id\": " + task.getPerceelid()
                + "				,\"responses\": [{"
                + "					\"response\": {"
                + "						\"request\": \"" + task.getName() + "\""
                + "						,\"status\": \""+JSONizer.toJson(table.getStatus())+"\","
                + "						\"codes\": [";

        
    }

    private String getFooter() {
        return "]}}]}}]}}";
    }

    @Override
    public String format(AdapterResult result) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
