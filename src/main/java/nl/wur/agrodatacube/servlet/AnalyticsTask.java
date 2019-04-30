/*
 * An analytics task is basically a predefined query that receives a geometry as argument.
 */
package nl.wur.agrodatacube.servlet;

import nl.wur.agrodatacube.formatter.bww.AdapterTableBWWJSONFormatter;
import nl.wur.agrodatacube.formatter.AdapterTableResultFormatter;
import nl.wur.agrodatacube.formatter.AdapterTableResultGeoJsonFormatter;

/**
 *
 * @author rande001
 */

public class AnalyticsTask {

    String suppliedGeometry;
    Integer suppliedEpsg = 28992;
    String name;                     // This is the name that is used in the config table to identify this task
    String ewktGeometry28992;         // The geometry transformed to 28992
    Integer perceelid;
    AdapterTableResultFormatter formatter = null;
    String token;

    public AnalyticsTask() {
        formatter = new AdapterTableResultGeoJsonFormatter();
    }
    
    public String getSuppliedGeometry() {
        return suppliedGeometry;
    }

    public void setSuppliedGeometry(String suppliedGeometry) {
        this.suppliedGeometry = suppliedGeometry;
    }

    public Integer getSuppliedEpsg() {
        return suppliedEpsg;
    }

    public void setSuppliedEpsg(Integer suppliedEpsg) {
        this.suppliedEpsg = suppliedEpsg;
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

    public String getEwktGeometry28992() {
        return ewktGeometry28992;
    }

    public void setEwktGeometry28992(String ewktGeometry28992) {
        this.ewktGeometry28992 = ewktGeometry28992;
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
    
    public boolean isValid() {
        if (suppliedGeometry == null) {
            return false;
        }
        
        if (suppliedGeometry.trim().length()==0) {
            return false;
        }
        
        return true;
    }

    public AdapterTableResultFormatter getFormatter() {
        return formatter;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String tokenString) {
        this.token = tokenString;
    }
    
    public boolean isRunnable() {
        if (token==null) {
            return false;
        }
        
        if (suppliedGeometry ==null) {
            return false;
        }
        return true;
    }
}
