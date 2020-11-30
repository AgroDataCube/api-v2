/*
* Copyright 2020 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.formatter;

import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterTableResult;

/**
 *
 * @author Yke van Randen
 */
public class AdapterTableResultCSVFormatter extends AdapterTableResultFormatter {

    private String nullValue = "";

    @Override
    public void format(ArrayList<AdapterResult> tables, Writer w) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object format(AdapterResult result) throws Exception {
        StringWriter sw = new StringWriter();
        format(result, sw);
        return sw.toString();
    }

    @Override
    protected void format(AdapterResult result, Writer w) throws Exception {
        AdapterTableResult tableResult = (AdapterTableResult) result;
        if (!tableResult.didSucceed()) {
            w.write(" { \"status\" : " + JSONizer.toJson(tableResult.getStatus()));
//            w.write(", \"query\" : " + JSONizer.toJson(table.getQueryString()));
            w.write("}"); //todo
            w.flush();
            return;
        }

        //
        // Maak de header, negeer almost emtpy de metadata leveren we hier niet. Nadenken over hoe wel.
        //
        StringBuilder csvResult = new StringBuilder(tableResult.getColumnCount() * tableResult.getRowCount() * 5); // estimate of reuired space
        String komma = "";
        for (int i = 0; i < tableResult.getColumnCount(); i++) {
            csvResult = csvResult.append(komma);
            komma = ",";
            csvResult = csvResult.append(tableResult.getColumnName(i));
        }
        csvResult = csvResult.append("\n");
        //
        // Add the rows from the table, each row is an GeoJson feature.
        //
        int j = 0;
        ArrayList<Object> row;

        int[] decimals = tableResult.getDecimalInformation();
        int geomIndex = tableResult.getGeomIndex();
        while ((row = tableResult.getRow(j)) != null) {

            boolean first = true;
            for (int i = 0; i < tableResult.getColumnCount(); i++) {
                if (i != geomIndex) {
                    Object o = row.get(i);
                    if (!first) {
                        csvResult.append(",");
                    }
                    first = false;
                    // TODO: Numeriek geen quotes

                    if (o == null) {
                        csvResult.append(nullValue);
                    }
                    else if (row.get(i) instanceof java.lang.String) {
                        csvResult.append("" + (row.get(i) == null ? "" : "\"" + row.get(i) + "\""));
                    } else if (row.get(i) instanceof java.sql.Date) {
                        csvResult.append("" + (row.get(i) == null ? "" : formatDate(row.get(i)))); // TODO: Formatter ivm datum
                    } else if (row.get(i) instanceof AdapterTableResult) {
                        AdapterTableResultJsonFormatter f = new AdapterTableResultJsonFormatter();
                        csvResult.append("" + f.format((AdapterTableResult) row.get(i)));
                    } else if (row.get(i) instanceof java.lang.Double) {
                        // 
                        // Round it to nr of decimals in metainformation
                        //

                        if (decimals[i] >= 0) {
                            csvResult.append("" + round((Double) row.get(i), decimals[i]));
                        } else {
                            csvResult.append("" + row.get(i));
                        }
                    } else if (row.get(i) instanceof java.lang.Float) {
                        // 
                        // Round it to nr of decimals in metainformation
                        //

                        if (decimals[i] >= 0) {
                            Double d = new Double((Float) row.get(i));
                            csvResult.append(round(d, decimals[i]));
                        } else {
                            csvResult.append("" + row.get(i));
                        }
                    } else {// Integer, Long, Decimal
                        csvResult.append("" + row.get(i));
                    }
                }
            }
            csvResult = csvResult.append("\n");

            //close properties and close feature
            j++;
        }

        w.write(csvResult.toString());
        w.flush();
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

    /**
     * Voor RVO want die willen NULL als NULL en niet als lege string.
     *
     * @param nullValue
     */
    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    /**
     * RVO wil een ander date format ipv 20200220 20-2-2020.
     *
     * @param dateFormatter
     */
    @Override
    public void setDateFormatter(SimpleDateFormat dateFormatter) {
        super.setDateFormatter( dateFormatter);
    }
}
