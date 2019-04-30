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
import java.util.Date;

/**
 *
 * @author rande001
 */
public class AccessTokenBuilderWUR {

    /**
     * Build a token.
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String[] resources = {"/bww/*"};
        Date expireDate = AccessTokenFactory.getDefaultExpirationDate();
        AccessToken t = AccessTokenFactoryWURImplementation.getInstance().createToken(resources, expireDate, 10, 0., "yke.vanranden@wur.nl");
        System.out.println(t.toString());        

    }
}
