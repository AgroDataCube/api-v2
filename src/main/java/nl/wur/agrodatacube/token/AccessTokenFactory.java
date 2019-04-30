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

import nl.wur.agrodatacube.token.wur.AccessTokenFactoryWURImplementation;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Rande001
 */
public abstract class AccessTokenFactory {

    /*
     Create using injection or ...
     */
    private static AccessTokenFactory instance = null;

    /**
     * Return a singleton for the TokenFactory to be used. You can use
     * AccessTokenFactorySampleImplementation.getInstance() or create your own.
     *
     * @return
     */
    public static AccessTokenFactory getInstance() {
        if (instance == null) {
            // instance = AccessTokenFactorySampleImplementation.getInstance(); // 
            instance = AccessTokenFactoryWURImplementation.getInstance();
        }
        return instance;
    }

    /**
     * Create a token based on the string supplied by the user (provided it in
     * the http header probably).
     *
     * @param tokenAsString
     * @return
     */
    public abstract AccessToken decodeToken(String tokenAsString);

    public abstract AccessToken createToken(String[] resources,
            Date expireDate,
            int requestLimit,
            double areaLimit,
            String issuedTo) throws Exception;

    public abstract AccessToken createTokenForIp(String ip) throws Exception;

    /**
     * The default expirationdate is now + 1 year.
     * 
     * @return
     */
    public static Date getDefaultExpirationDate() {
        Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR) + 1, c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)); 
        return new Date(c.getTimeInMillis());
    }

}
