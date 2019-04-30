/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.token.wur;

import nl.wur.agrodatacube.token.AccessToken;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import java.util.Date;

/**
 *
 * @author rande001
 */
public class AccessTokenWURImplementation extends AccessToken {
    
    private Builder token;
    
    /**
     * Create a new token with a given limits
     *
     * @param allowedResource
     * @param expireDate
     * @param requestLimit
     * @param areaLimit
     */
    private AccessTokenWURImplementation(String issuedTo,
            String[] allowedResource,
            Date expireDate,
            int requestLimit,
            double areaLimit) {
        this();
        this.setAllowedResources (allowedResource);
        this.setExpireDate (expireDate);
        this.setRequestLimit(requestLimit);
        this.setAreaLimit(areaLimit);
        this.setIssuedTo(issuedTo);
        this.token = JWT.create().withArrayClaim("resource", this.getAllowedResources())
                .withExpiresAt(this.getExpireDate())
                .withClaim(REQUEST_LIMIT, this.getRequestLimit())
                .withClaim(AREA_LIMIT, this.getAreaLimit())
                .withClaim(ISSUED_DATE, new Date(System.currentTimeMillis()))
                .withClaim(ISSUED_TO, this.getIssuedTo())
                .withIssuedAt(new Date(System.currentTimeMillis())); // differentiate string representation
    }

    protected AccessTokenWURImplementation() {
        super();
    }    
    
    protected void setToken(Builder token) {
        this.token = token;
    }
}
