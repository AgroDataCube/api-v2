/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.resource;

/**
 *
 * @author rande001
 */
public class AdapterImageResource extends AdapterResource {

    private String coverageName;
    private String outputFormat;
    private double areaLimit = 100000000.d; // default 10000 ha limit due to problems with WCS when larger areas. Can be redefined by config.json.

    public AdapterImageResource(String name) {
        super(name);
    }

    public void setCoverageName(String name) {
         coverageName=name;
    }


    public String getCoverageName() {
        return coverageName;
    }

    @Override
    public boolean requiresGeometry() {
        return true;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public double getAreaLimit() {
        return areaLimit;
    }

    public void setAreaLimit(double areaLimit) {
        this.areaLimit = areaLimit;
    }
    
    
}
