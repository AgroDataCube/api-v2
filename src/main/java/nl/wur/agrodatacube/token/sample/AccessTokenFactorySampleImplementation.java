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
import nl.wur.agrodatacube.token.AccessTokenFactory;
import nl.wur.agrodatacube.formatter.JSONizer;
import static nl.wur.agrodatacube.token.sample.AccessTokenSampleImplementation.FREE_MAX_AREA;
import static nl.wur.agrodatacube.token.sample.AccessTokenSampleImplementation.FREE_MAX_REQUESTS;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * In this example a token is simply a JSON object unencrypted.
 *
 * @author Rande001
 */
public class AccessTokenFactorySampleImplementation extends AccessTokenFactory {

    /**
     * Singleton.
     */
    private static AccessTokenFactorySampleImplementation instance = new AccessTokenFactorySampleImplementation();
    
    private SimpleDateFormat sdf;
    
    private AccessTokenFactorySampleImplementation() {
        sdf = new SimpleDateFormat("yyyy-MM-dd");
    }
    
    public static AccessTokenFactory getInstance() {
        if (instance == null) {
            instance = new AccessTokenFactorySampleImplementation(); // 
        }
        return instance;
    }

    /**
     * Decrypt a token that is supplied by the user.
     *
     * @param tokenAsString
     * @return
     */
    @Override
    public nl.wur.agrodatacube.token.AccessToken decodeToken(
            String tokenAsString) {
        nl.wur.agrodatacube.token.AccessToken t = new AccessTokenSampleImplementation();
        t.setStringValue(tokenAsString);

        //
        // Parse the json object.
        //
        JsonElement jelement = new JsonParser().parse(tokenAsString);
        JsonObject jobject = jelement.getAsJsonObject();
        
        try {
            t.setAreaLimit(jobject.get(AccessToken.AREA_LIMIT).getAsDouble());
            t.setRequestLimit(jobject.get(AccessToken.REQUEST_LIMIT).getAsInt());
            t.setExpireDate(sdf.parse(jobject.get(AccessToken.EXPIRY_DATE).getAsString()));
            t.setIssuedDate(sdf.parse(jobject.get(AccessToken.ISSUED_DATE).getAsString()));
            t.setIssuedTo(jobject.get(AccessToken.ISSUED_TO).getAsString());
            
            JsonArray jsonResources = jobject.get(AccessToken.RESOURCES).getAsJsonArray();
            String[] resources = new String[jsonResources.size()];
            int i = 0;
            for (JsonElement r : jsonResources) {
                resources[i] = r.getAsString();
                i++;
            }
            t.setAllowedResources(resources);
        } catch (Exception e) {
            throw new RuntimeException(String.format("AccessTokenFactorySampleImplementation.decodeToken error in parsing token data from token %s", tokenAsString));
        }
        return t;
    }

    /**
     * Create a new token. Include issue date to force uniqueness of token.
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
        AccessTokenSampleImplementation t = new AccessTokenSampleImplementation();
        t.setIssuedTo(issuedTo);
        t.setRequestLimit(requestLimit);
        t.setAreaLimit(areaLimit);
        t.setExpireDate(expireDate);
        t.setAllowedResources(resources);
        
        Properties p = new Properties();
        p.put(AccessToken.AREA_LIMIT, t.getAreaLimit());
        p.put(AccessToken.ISSUED_DATE, sdf.format(t.getIssuedDate().getTime()));
        p.put(AccessToken.ISSUED_TO, t.getIssuedTo());
        p.put(AccessToken.REQUEST_LIMIT, t.getRequestLimit());
        p.put(AccessToken.EXPIRY_DATE, sdf.format(t.getExpireDate().getTime()));
        p.put(AccessToken.RESOURCES, t.getAllowedResources());
        t.setStringValue(JSONizer.toJson(p));
        return t;
    }

    /**
     * Create a default token that allows usesr access basd on ip. Limited to
     * all resources but 5000 visits (request daily). Valid until tonight next
     * midnight (== tomorrow at 0::0:0.00);
     *
     * @param ip
     * @return
     * @throws Exception
     */
    @Override
    public AccessToken createTokenForIp(String ip) throws Exception {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int year = c.get(Calendar.YEAR);
        int month = 4;// april mc.get(Calendar.MONTH);
        int day = 1;
        c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        String[] resources = {"*"};
        AccessToken t = createToken(resources, new Date(c.getTimeInMillis()), FREE_MAX_REQUESTS, FREE_MAX_AREA, "public (Open Data)");
        return t;
    }
    
}
