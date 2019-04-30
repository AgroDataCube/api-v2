/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.result;

import java.util.Properties;

/**
 * @author Yke
 */
public abstract class AdapterResult {

    private String queryStrng;
    private java.util.Properties props;
    private Integer httpStatusCode;          // Allow deeper components to set the http status code. They have the knowledge what happended.
    private Double area;


    public AdapterResult() {
        status = "ok";
        props = new java.util.Properties();
    }
    private String status; // Ok of errormessage     

    public String getStatus() {
        return status;
    }

    public boolean didSucceed() {
        return "ok".equalsIgnoreCase(status);
    }

    public void setStatus(String status) {
        this.status = status;
        if (!didSucceed()) {
            clear();
        }
    }

    protected abstract void clear();

    public String getQueryString() {
        return queryStrng;
    }

    public void setQueryString(String queryStrng) {
        this.queryStrng = queryStrng;
    }

    public void addProperty(String name, String value) {
        if (value == null) {
            props.put(name, "unknown");
        } else {
            props.put(name, value);
        }
    }

    public Properties getProps() {
        return props;
    }

    public Double getArea() { return area;}
    
    public void setArea(double d) {
        area = d;        
    }

    public String getMimeType() {
        return "application/json";
    }

    public Integer getHttpStatusCode() {
        if (httpStatusCode == null) {
            if (didSucceed()) {
                return 200;
            } else {
                return 500;
            }
        }
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}
