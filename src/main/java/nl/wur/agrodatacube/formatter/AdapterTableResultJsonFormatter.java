/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
*
* This class formats to json instead of geojson. Used when child queries are used.
 */
package nl.wur.agrodatacube.formatter;

import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterTableResult;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Yke
 */
public class AdapterTableResultJsonFormatter extends AdapterTableResultFormatter {

    public AdapterTableResultJsonFormatter() {
        super();

    }

    /**
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

    public void format(AdapterResult result, Writer w) throws Exception {
        AdapterTableResult table = (AdapterTableResult) result;
        //
        // If it did not succeed return an error
        //
        if (!table.didSucceed()) {
            w.write(" { \"status\" : " + JSONizer.toJson(table.getStatus()));
//            w.write(", \"query\" : " + JSONizer.toJson(table.getQueryString()));
            w.write("}"); 
            return;
        }

        //
        // Valid result.
        //
        Iterator<Object> iterator = table.getProps().keySet().iterator();

        String komma = ",";
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            w.write("\"" + name + "\" : \"");
            w.write(table.getProps().getProperty(name));
            w.write("\"");
            w.write(komma);
        }

        w.write("    [ ");
        String jsonRow = "";
        komma = " ";

        int j = 0;
        ArrayList<Object> row;
        while ((row = table.getRow(j)) != null) {
            w.write(komma);
            komma = ","; // new row so comma when not 1st
            jsonRow = "{ ";
            for (int i = 0; i < table.getColumnCount(); i++) {
                Object o = row.get(i);
                if (o != null) {
                    if (i > 0) {
                        jsonRow += ","; // new field so when when not 1st
                    }
                    jsonRow += "\"" + table.getColumnName(i) + "\" : ";
                    if (o instanceof java.sql.Date) {
                        jsonRow += "\""+formatDate(o)+"\"";
                    } else if (o instanceof java.sql.Timestamp) {
                        jsonRow += "\""+formatTimestamp(o)+"\"";
                    } else if (o instanceof java.lang.String) {
                        jsonRow += JSONizer.toJson(row.get(i));
                    } else if (o instanceof nl.wur.agrodatacube.result.AdapterTableResult) {
                        AdapterTableResultJsonFormatter f = new AdapterTableResultJsonFormatter();
                        jsonRow += f.format((AdapterTableResult) o);
                    } else {
                        jsonRow += row.get(i);
                    }
                }
            }
            jsonRow += "}";
            w.write(jsonRow);
            j++;
            w.flush();
        }
        w.write("]");
        w.flush();
    }

    @Override
    public void format(ArrayList<AdapterResult> tables,
            Writer w) throws Exception {
        String komma = "";
        for (AdapterResult t : tables) {
            w.write(komma);
            komma = ",";
            format((AdapterTableResult) t, w);
        }
    }

    public String formatValue(Object o) {
        if (o != null) {
            if (o instanceof Double) {
                return "" + o;
            } else if (o instanceof Integer) {
                return "" + o;
            } else if (o instanceof java.sql.Date) {
                return formatDate(o);
            } else {
                return "\"" + o + "\""; // TODO: Formatter ivm datum
            }
        }
        return null;
    }

}
