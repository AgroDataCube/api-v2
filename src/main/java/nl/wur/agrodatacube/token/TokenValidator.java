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

import static nl.wur.agrodatacube.token.AccessToken._WILDCARD_;

/**
 *
 * @author rande001
 */
public class TokenValidator {

    /**
     * Return if the supplied String is a valid AgroDataCube token.
     *
     * @param tokenString
     * @return
     */
    public static boolean tokenIsValid(String tokenString) {
        try {
            AccessToken t = AccessTokenFactory.getInstance().decodeToken(tokenString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static TokenValidationResult tokenAllowsAccess(String token, String resource) {
        return tokenAllowsAccess(AccessTokenFactory.getInstance().decodeToken(token), resource);
    }    
    
    /**
     * Validate if token allows access to the resource (identified by a path).
     *
     * @param t
     * @param resource
     * @return
     */
    public static TokenValidationResult tokenAllowsAccess(AccessToken t, String resource) {

        /*
         * After may 1st 2018 no ip based tokens are generated.
         */
        if (t == null) {
            return new TokenValidationResult(
                    "No token supplied, see https://agrodatacube.wur.nl/api/register.jsp !!!!!!!\"}");
        }

        /*
        * If the token does not have a list of resources but only contains '*' it allows access to
        * all resources. From the early days (genesis phase).
         */
        if (t.getAllowedResources().length == 1) {
            if (AccessToken._WILDCARD_.equalsIgnoreCase(t.getAllowedResources()[0].trim())) {
                return new TokenValidationResult("");
            }
        }

        /*
         * See if there is an element in the allowedResources for this resource.
         */
        for (String s : t.getAllowedResources()) {
            if (s.equalsIgnoreCase(resource)) {
                return new TokenValidationResult("");
            }
            if (matches(s.split("/"), resource.split("/"), 0)) {
                return new TokenValidationResult("");
            }
        }

        return new TokenValidationResult("This token does not allow access to the requested resource(s)"); // false;
    }

    /*
     * Compare two arrays, they shoudl ahev same number of args and each element must be same in bot arrays or at least one of them is a '*'
     * TODO: Move to tokenvaldator
     */
    private static boolean matches(String[] resources, String[] token, int pos) {
        if (resources.length != token.length) {
            return false;
        }

        if (resources.length == 0) {
            return false;
        }

        for (int i = 0; i < resources.length; i++) {
            if (resources[i].equalsIgnoreCase(_WILDCARD_)) {
                ; // Ok no action
            } else if (token[i].equalsIgnoreCase(resources[i])) {
                ; // Ok no action
            } else {
                return false;
            }
        }

        return true;
    }

}
