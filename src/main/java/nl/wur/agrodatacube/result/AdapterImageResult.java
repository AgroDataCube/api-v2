/*
* Copyright 2018 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.result;

/**
 *
 * @author rande001
 */
public class AdapterImageResult extends AdapterResult {

    byte[] imageData = null;
    // int imageLength = 0;

    public AdapterImageResult() {
        super();
    }

    /**
     *
     */
    @Override
    protected void clear() {
        imageData = null;
    }

    public void addImageData(byte[] bytes, int len) {
       
        try {
            if (imageData == null) {
                imageData = new byte[len];
                System.arraycopy(bytes, 0, imageData, 0, len);
            } else {
                 // System.out.println(String.format("ImageLength = %d, len = %d", imageData.length,len));
                byte[] b = imageData;
                imageData = new byte[len + imageData.length];
                System.arraycopy(b, 0, imageData, 0, b.length);
                System.arraycopy(bytes, 0, imageData,b.length, len);
            }
        } catch (Exception e) {
            throw new RuntimeException("AdapterImageResult.addImageData : ".concat(e.getMessage()));
        }
    }

    public byte[] getBytes() {
        if (imageData == null) {
            throw new RuntimeException("Empty image in AdapterImageResult.getBytes()");
        }
        return imageData;
    }

    @Override
    public String getMimeType() {
        if (didSucceed()) {
            return "image/tiff";
        }
        return super.getMimeType(); 
    }
    
    
}
