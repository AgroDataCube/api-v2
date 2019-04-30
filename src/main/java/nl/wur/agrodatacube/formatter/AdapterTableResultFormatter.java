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
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 *
 * @author Yke
 */
public abstract class AdapterTableResultFormatter extends AdapterResultFormatter {
    
    private SimpleDateFormat dateFormatter  = new SimpleDateFormat("yyyy-MM-dd");
    
//    protected String formatDate(java.sql.Date date) {
//        if (date == null) {
//            return null;
//        }
//        return dateFormatter.format(new java.util.Date(date.getTime()));
//    }
    
    protected String formatDate(Object date) {
        if (date == null) {
            return null;
        }
        return  dateFormatter.format((java.sql.Date) date);
    }
    
    /**
     * Format an AdapterTableResult to string.
     * @param table
     * @return
     * @throws Exception 
     */
    public String format(AdapterTableResult table) throws Exception {
        StringWriter writer = new StringWriter();
        format(table, writer);
        return writer.toString();        
    }
    
    /**
     * Format an AdapterTableResult and write the result to the writer.
     * @param table
     * @param w
     * @throws Exception 
     */
    
        
    public abstract void format(ArrayList<AdapterResult> tables, Writer w)throws Exception;
}
