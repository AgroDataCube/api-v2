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
import nl.wur.agrodatacube.token.AccessTokenFactory;
import nl.wur.agrodatacube.properties.AgroDataCubeProperties;
import static nl.wur.agrodatacube.token.AccessToken.FREE_MAX_AREA;
import static nl.wur.agrodatacube.token.AccessToken.FREE_MAX_REQUESTS;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Rande001
 */
public class AccessTokenFactoryWURImplementation extends AccessTokenFactory {

    private static String ALGORITHM_KEY = "From Properties file"; 
    private static Algorithm algorithm = null;
    private static AccessTokenFactoryWURImplementation instance;

    public static AccessTokenFactory getInstance() {
        if (instance == null) {
            ALGORITHM_KEY =AgroDataCubeProperties.getValue("system.encryptionkey");
            instance = new AccessTokenFactoryWURImplementation(); // 
        }
        return instance;
    }

    static {
        try {
            algorithm = Algorithm.HMAC256(ALGORITHM_KEY);
        } catch (Exception ex) {
            throw new RuntimeException("AccessToken, Static part : " + ex.getMessage());
        }
    }

    private AccessTokenFactoryWURImplementation() {
        super();
    }

    @Override
    public nl.wur.agrodatacube.token.AccessToken decodeToken(String tokenAsString) {
        if (tokenAsString == null) {
            throw new RuntimeException("No token supplied, see https://agrodatacube.wur.nl/api/register.jsp !!!!!!!");
        }
        DecodedJWT jwt = JWT.decode(tokenAsString);
        AccessToken t = new AccessTokenWURImplementation();
        // t.setExpireDate(jwt.getExpiresAt());
//        t.setAllowedResources(jwt.getClaim("resource").asArray(String.class));
//        if (t.getAllowedResources() == null) {
//            t.setAllowedResources(new String[0]);
//        }
//        if (jwt.getClaim(AccessToken.REQUEST_LIMIT).isNull()) {
//            t.setRequestLimit(0);
//        } else {
//            t.setRequestLimit(jwt.getClaim(AccessToken.REQUEST_LIMIT).asInt());
//        }
        t.setIssuedTo(jwt.getClaim(AccessToken.ISSUED_TO).asString());
//        if (jwt.getClaim(AccessToken.AREA_LIMIT) != null) {
//            t.setAreaLimit(jwt.getClaim(AccessToken.AREA_LIMIT).asDouble());
//        }
        t.setStringValue(tokenAsString);
        return t;
    }

    /**
     * Create a new token. 
     * 
     * @param resources
     * @param expireDate
     * @param requestLimit
     * @param areaLimit
     * @param issuedTo
     * @return
     * @throws Exception 
     */
    @Override
    public AccessToken createToken(String[] resources,
            Date expireDate,
            int requestLimit,
            double areaLimit,
            String issuedTo) throws Exception {
        AccessTokenWURImplementation t = new AccessTokenWURImplementation();

        t.setIssuedTo(issuedTo);
//        t.setRequestLimit(requestLimit);
//        t.setAreaLimit(areaLimit);
//        t.setExpireDate(expireDate);
        t.setAllowedResources(resources);
        JWTCreator.Builder b = JWT.create().withArrayClaim("resource", t.getAllowedResources())
                // .withExpiresAt(t.getExpireDate())
                //                .withClaim(AccessToken.REQUEST_LIMIT, requestLimit)
                //                .withClaim(AccessToken.AREA_LIMIT, areaLimit)
                //                .withClaim(AccessToken.ISSUED_DATE, new Date(System.currentTimeMillis()))
                .withClaim(AccessToken.ISSUED_TO, issuedTo)
                .withIssuedAt(new Date(System.currentTimeMillis()));
        t.setToken(b); // differentiate string representation
        t.setStringValue(b.sign(algorithm));
        return t;
    }

    @Override
    public AccessToken createTokenForIp(String ip) throws Exception {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int year = c.get(Calendar.YEAR);
        int month = 4;// april mc.get(Calendar.MONTH);
        int day = 5;
        c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        String[] resources = {"*"};
        AccessToken t = createToken(resources, new Date(c.getTimeInMillis()), FREE_MAX_REQUESTS, FREE_MAX_AREA, "public (Open Data)");
        return t;
    }
}
