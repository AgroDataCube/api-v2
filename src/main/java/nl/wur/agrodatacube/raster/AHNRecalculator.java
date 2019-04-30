/*
* Copyright 2019 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.raster;

/**
 *
 * @author rande001
 */
public class AHNRecalculator extends Recalculator {

    /**
     * Since the AHN raster already fp no action is needed.
     * 
     * @param b
     * @return 
     */
    @Override
    protected float calculate(byte b) {
        throw new RuntimeException("This raster needs no recalculation so this method should not be called");
    }
    
}
