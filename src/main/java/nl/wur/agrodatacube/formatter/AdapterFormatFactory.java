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

import nl.wur.agrodatacube.result.AdapterTableResult;
import nl.wur.agrodatacube.result.AdapterResult;
import nl.wur.agrodatacube.result.AdapterImageResult;
/**
 *
 * @author rande001
 */
public class AdapterFormatFactory {
 
    public static AdapterResultFormatter getDefaultFormatter(AdapterResult r) {
        if (r instanceof AdapterImageResult) {
            return new AdapterImageResultFormatter();
        }
        if (r instanceof AdapterTableResult) {
            
            //
            // When this is a child we need a AdapterTableResultJsonFormatter, however that is not available yet in the result.
            //
                        
            return new AdapterTableResultGeoJsonFormatter();
        }
        throw new RuntimeException(String.format("No default formatter for result class = %s.%s", r.getClass().getPackage(),r.getClass().getName()));
    }
}
