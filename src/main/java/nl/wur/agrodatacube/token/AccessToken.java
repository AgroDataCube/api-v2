/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.token;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author rande001
 */
public abstract class AccessToken {

    public final static String ISSUED_TO = "issuedto";
    public final static String EXPIRY_DATE = "expirydate";
    public final static String ISSUED_DATE = "issueddate";
    public final static String REQUEST_LIMIT = "request_limit";
    public final static String RESOURCES = "resource";
    public final static String AREA_LIMIT = "area_limit";
    public final static String _WILDCARD_ = "*";

    // Constants for free tokens.
    public static final int FREE_MAX_REQUESTS = 25000;
    public static final double FREE_MAX_AREA = 100000000;  // in m2

    // Object part
    private String[] allowedResources;        // Resources(s) to which token allows access
    private Date expireDate;                  // When does token expire
    private Date issuedDate;                  // When does token expire
    private int requestLimit;                 // 0 is nolimit;
    private double areaLimit;                 // area limit
    private String stringValue;               // this as encrypted string
    private String issuedTo;                  // issued to ...

    public AccessToken() {
        super();
        this.issuedDate = new Date(System.currentTimeMillis());
        this.expireDate = AccessTokenFactory.getDefaultExpirationDate();
        this.allowedResources=new String[1];
        this.allowedResources[0]=_WILDCARD_; // access to everyting
    }

    /**
     * If now() > expire date then return false else true. TODO: if today == 22
     * feb 2018 0:0:0 and expiry dat also 22 feb 2018 :0:0:0 function still
     * returns true.
     *
     * @return
     */
    public boolean isExpired() {
        return (toDay().getTime().compareTo(this.getExpireDate()) > 0);
    }

    /**
     * See if this token allows access to the supplied resource.
     *
     * @param resource
     * @return
     */
    public TokenValidationResult allowsAccessTo(String resource) {
        return TokenValidator.tokenAllowsAccess(this, resource);
    }

    public String[] getAllowedResources() {
        return allowedResources;
    }

    public void setAllowedResources(String[] allowedResources) {
        this.allowedResources = allowedResources;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public void setRequestLimit(int requestLimit) {
        this.requestLimit = requestLimit;
    }

    public void setAreaLimit(double areaLimit) {
        this.areaLimit = areaLimit;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void setIssuedTo(String issuedTo) {
        this.issuedTo = issuedTo;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public boolean hasRequestLimit() {
        return getRequestLimit() > 0;
    }

    public int getRequestLimit() {
        return this.requestLimit;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public String getIssuedTo() {
        return this.issuedTo;
    }

    public Date getIssuedDate() {
        return this.issuedDate;
    }

    public void setIssuedDate(Date d) {
        this.issuedDate = d;
    }

    private static Calendar toDay() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);
        c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        return c;
    }

//    private static Calendar toMorrow() {
//        Calendar c = toDay();
//        c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
//        return c;
//    }
//
    public double getAreaLimit() {
        return areaLimit;
    }

}
