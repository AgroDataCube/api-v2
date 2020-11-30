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
import java.io.Writer;

/**
 *
 * @author rande001
 */
public abstract class AdapterResultFormatter {

    public  static AdapterResultFormatter createFormatter(String formatterName) {
        if ("series".equalsIgnoreCase(formatterName)) {
            return new AdapterTableResultGeoJsonSeriesFormatter();
        }
        if ("csv".equalsIgnoreCase(formatterName)) {
            return new AdapterTableResultCSVFormatter();
        }
        throw new RuntimeException(String.format("Unable to create a formatter for %s !", formatterName));
    }

    /**
     * Return a string containing either the result in result or or the error message in result.
     * 
     * @param result
     * @return
     * @throws Exception 
     */
    public abstract Object format(AdapterResult result)throws Exception;
    
    /**
     * Write the result of result to the writer. This should not be public/protected.
     * 
     * @param result
     * @param w 
     * @throws java.lang.Exception 
     */
    protected abstract void format(AdapterResult result,Writer w) throws Exception; // todo no eception but in result.
}
