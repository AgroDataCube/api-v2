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
 *
 * @author Rande001
 */
public class AdapterTableResultJsonSingleValueFormatter extends AdapterTableResultJsonFormatter {

    @Override
    public void format(AdapterResult result,
            Writer w) throws Exception {
AdapterTableResult table=(AdapterTableResult) result;
        //
        // If it did not succeed return an error
        //
        if (!table.didSucceed()) {
            w.write(" { \"status\" : " + JSONizer.toJson(table.getStatus()) + "}");
            return;
        }

        if (table.getColumnCount() > 1 | table.getRowCount() > 1) {
            w.write(String.format(" { \"status\" : Single value expected but got table with %d columns and %d rows }", table.getColumnCount(), table.getRowCount())); //todo
            return;
        }

        //
        // Valid result.
        //
        
        w.write("{ ");
        w.write(String.format("\"%s\" : ", table.getColumnName(0)));
        w.write(String.format(formatValue(table.getRow(0).get(0))));
        w.write(" }");
 
        w.flush();
    }

    @Override
    public void format(ArrayList<AdapterResult> tables,
            Writer w) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
