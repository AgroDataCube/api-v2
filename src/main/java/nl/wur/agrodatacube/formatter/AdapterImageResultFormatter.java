/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.formatter;

import nl.wur.agrodatacube.result.AdapterImageResult;
import nl.wur.agrodatacube.result.AdapterResult;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author rande001
 */
public class AdapterImageResultFormatter extends AdapterResultFormatter {

    private void doFormat(AdapterImageResult image, Writer writer) throws IOException {
        if (image.didSucceed()) {
            for (Byte b : image.getBytes()) {
                writer.write(b);
            }
        } else {
            for (Byte b : image.getStatus().getBytes()) {
                writer.write(b);
            }
        }
    }

    @Override
    public void format(AdapterResult result, Writer w) throws Exception {
        if (!result.didSucceed()) {
            w.write(" { \"status\" : " + JSONizer.toJson(result.getStatus()));
            w.write("}"); //todo
            return;
        }
        doFormat((AdapterImageResult) result, w);
    }

    @Override
    public Object format(AdapterResult result) throws Exception {
        AdapterImageResult image = (AdapterImageResult) result;

        if (image.didSucceed()) {
            return image.getBytes();
        } else {
            return " { \"status\" : " + JSONizer.toJson(image.getStatus() )+ "}";
        }
    }
}
