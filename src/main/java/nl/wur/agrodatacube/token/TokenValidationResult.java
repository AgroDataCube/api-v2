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

/**
 *
 * @author rande001
 */
public class TokenValidationResult {

    private String error = "";

    public TokenValidationResult(String error) {
        if (error != null) {
            this.error = error.trim();
        } else {
            this.error = "";
        }
    }

    public String getError() {
        return error;
    }

    public boolean isOk() {

        if (error == null) {
            return true;
        }

        return error.trim().length() == 0;
    }
}
