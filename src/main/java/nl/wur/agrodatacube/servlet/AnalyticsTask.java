
/*
* An analytics task is basically a predefined query that receives a geometry as argument.
 */
package nl.wur.agrodatacube.servlet;

import nl.wur.agrodatacube.formatter.AdapterTableResultFormatter;
import nl.wur.agrodatacube.formatter.AdapterTableResultGeoJsonFormatter;
import nl.wur.agrodatacube.formatter.bww.AdapterTableBWWJSONFormatter;

/**
 *
 * @author rande001
 */
public class AnalyticsTask {
    Integer                     suppliedEpsg = 28992;
    AdapterTableResultFormatter formatter    = null;
    String                      suppliedGeometry;
    String                      name;    // This is the name that is used in the config table to identify this task
    String                      ewktGeometry28992;    // The geometry transformed to 28992
    Integer                     perceelid;
    String                      token;
    String remoteIp;
    String requestURL;

    public AnalyticsTask() {
        formatter = new AdapterTableResultGeoJsonFormatter();
    }

    public String getEwktGeometry28992() {
        return ewktGeometry28992;
    }

    public void setEwktGeometry28992(String ewktGeometry28992) {
        this.ewktGeometry28992 = ewktGeometry28992;
    }

    public AdapterTableResultFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(AdapterTableResultFormatter formatter) {
        this.formatter = formatter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        if (name.toLowerCase().startsWith("bww-")) {
            formatter = new AdapterTableBWWJSONFormatter(this);
        }
    }

    public Integer getPerceelid() {
        return perceelid;
    }

    public void setPerceelid(Integer perceelid) {
        if (perceelid == null) {
            this.perceelid = 0;
        } else {
            this.perceelid = perceelid;
        }
    }

    public boolean isRunnable() {
        if (token == null) {
            return false;
        }

        if (suppliedGeometry == null) {
            return false;
        }

        return true;
    }

    public Integer getSuppliedEpsg() {
        return suppliedEpsg;
    }

    public void setSuppliedEpsg(Integer suppliedEpsg) {
        this.suppliedEpsg = suppliedEpsg;
    }

    public String getSuppliedGeometry() {
        return suppliedGeometry;
    }

    public void setSuppliedGeometry(String suppliedGeometry) {
        this.suppliedGeometry = suppliedGeometry;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String tokenString) {
        this.token = tokenString;
    }

    public boolean isValid() {
        if (suppliedGeometry == null) {
            return false;
        }

        if (suppliedGeometry.trim().length() == 0) {
            return false;
        }

        return true;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
