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
public class NDVIRecalculator extends Recalculator {

    @Override
    protected float calculate(byte b) {
        return java.lang.Byte.toUnsignedInt(b)/250.f;
    }
    
}
