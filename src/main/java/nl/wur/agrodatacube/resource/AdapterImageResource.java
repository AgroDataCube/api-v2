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
    
    
}
