/*
* Copyright 2019 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.result;

/**
 *
 * @author rande001
 */
public class ResultParameter {
    
    private static final String[] knownResultParameters = { "result","page_size","page_offset","output_epsg" };
    
    public static boolean isResultParameter(String name ) {
        for (String s : knownResultParameters) {
            if (s.equalsIgnoreCase(name)) return true;
        }
        return false;
    }
}
