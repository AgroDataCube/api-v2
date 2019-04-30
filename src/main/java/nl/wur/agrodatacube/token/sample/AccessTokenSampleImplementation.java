/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.token.sample;

import nl.wur.agrodatacube.token.AccessToken;
import com.auth0.jwt.JWTCreator.Builder;
import java.util.Date;

/**
 *
 * @author rande001
 */
public class AccessTokenSampleImplementation extends AccessToken {

    private Builder token;

    
        /**
     * Create a new token with a given limits
     *
     * @param allowedResource
     * @param expireDate
     * @param requestLimit
     * @param areaLimit
     */
    private AccessTokenSampleImplementation(String issuedTo,
            String[] allowedResource,
            Date expireDate,
            int requestLimit,
            double areaLimit) {
        this();
        this.setAllowedResources(allowedResource);
        this.setExpireDate(expireDate);
        this.setRequestLimit(requestLimit);
        this.setAreaLimit(areaLimit);
        this.setIssuedTo(issuedTo);
        // builder bestaat nog niet dus token is nullthis.stringValue = this.toString();
    }

    public AccessTokenSampleImplementation() {
        super();
    }

    
   
}
