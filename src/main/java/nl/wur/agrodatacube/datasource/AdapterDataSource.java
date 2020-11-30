/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.datasource;

import nl.wur.agrodatacube.exec.ExecutorTask;
import nl.wur.agrodatacube.resource.AdapterResource;
import nl.wur.agrodatacube.result.AdapterResult;
import java.util.Iterator;
import java.util.Map;
import nl.wur.agrodatacube.exception.InvalidParameterException;
import nl.wur.agrodatacube.resource.query.ConfigurationParameter;
import nl.wur.agrodatacube.servlet.WorkerParameter;

/**
 *
 * @author rande001
 */
public abstract class AdapterDataSource {

    private String name;
    private boolean containsRegistrationData = false;

    /**
     *
     * @param task
     * @return
     */
    public abstract AdapterResult execute(ExecutorTask task); // Add task etc.

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public boolean containsRegistrationData() {
        return containsRegistrationData;
    }

    /**
     *
     * @param f
     */
    public void setContainsRegistrationData(String f) {
        containsRegistrationData = Boolean.parseBoolean(f);
    }

    /**
     * Check if the resource supports the supplied parameters and if the values
     * are acceptable.If not raise exception.
     *
     * @param task
     */
    protected void validateQueryParameters(ExecutorTask task) {
        Iterator<Map.Entry<Object, Object>> it = task.getQueryParameters().entrySet().iterator();
        AdapterResource resource = task.getResource();
        while (it.hasNext()) {
            Map.Entry<Object, Object> currentParam = it.next();

            //
            // Ignore parameters starting wit _ they are to influence internal behaviour.
            //
            String parameterName = (String) currentParam.getKey();
            if (!parameterName.startsWith("_")) {
                ConfigurationParameter findQueryParameter = resource.findQueryParameter(parameterName);
                if (findQueryParameter == null) {
                    throw new InvalidParameterException(String.format("Parameter %s not supported for this resource !!!", parameterName));
                }
                WorkerParameter w = (WorkerParameter) currentParam.getValue();
                if (!findQueryParameter.valueIsValid((String) w.getValue())) {
                    throw new InvalidParameterException(String.format("Value %s is invalid for parameter %s !!!", w.getValue(), parameterName));
                }
            }
        }
    }

}
