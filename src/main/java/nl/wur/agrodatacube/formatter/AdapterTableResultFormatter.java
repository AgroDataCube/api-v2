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
    
    //
    // ISO8601 formats (https://nl.wikipedia.org/wiki/ISO_8601)
    //
    private SimpleDateFormat dateFormatter  = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timestampFormatter =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    protected String formatDate(Object date) {
        if (date == null) {
            return null;
        }
        return  dateFormatter.format((java.sql.Date) date);
    }
    
    protected String formatTimestamp(Object timestamp) {
        if (timestamp == null) {
            return null;
        }
        return  timestampFormatter.format((java.sql.Timestamp) timestamp);
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
     * @param tables
     * @param w
     * @throws Exception 
     */
    public abstract void format(ArrayList<AdapterResult> tables, Writer w)throws Exception;

   /**
    * Functionaliteit toegevoegd om bv bij CSV andere date formatter te gebruiken (bv voor RVO).
    * 
    * @param sdf 
    */
    protected void setDateFormatter(SimpleDateFormat sdf) {
        dateFormatter=sdf;
    }
    
}
