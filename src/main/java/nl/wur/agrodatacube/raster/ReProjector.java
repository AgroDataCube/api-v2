/*
* Copyright 2019 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.raster;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.Operations;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.parameter.GeneralParameterValue;

/**
 * Change the CRS of a raster and
 *
 * @author rande001
 */
public class ReProjector {

    Recalculator calc;

    public ReProjector(Recalculator calc) {
        this.calc = calc;
    }

    /**
     * @param inputData the raster as it is received from the WCS. Values have
     * to be recalculated (byte to float) using a recalculator and if necessary
     * a reprojection is done.
     * 
     * @param output_epsg
     * @param NODATAVALUE
     * @return
     *
     * @throws Exception
     */
    public byte[] convertRasterToFloatAndReproject(byte[] inputData, String output_epsg, int NODATAVALUE) throws Exception {

        final org.opengis.referencing.crs.CoordinateReferenceSystem sourceCRS = org.geotools.referencing.CRS.decode("EPSG:28992", true);

        final Hints hint = new Hints();
        hint.put(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, sourceCRS);
        GeoTiffReader reader = new GeoTiffReader(new ByteArrayInputStream(inputData), hint);
        GridCoverage2D inputCoverage = reader.read(null);

        GridCoverage2D newRaster;

        // Convert to float.
        Raster inputRaster = inputCoverage.getRenderedImage().getData();
        if (inputRaster.getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE) {
            //
            // Some fixed issues
            //
            int bands = 1;
            int[] bandOffsets = {0};
            float[] newValues = new float[bands];

            int rows = (int) Math.round(inputCoverage.getGridGeometry().getGridRange2D().getSize().getHeight());
            int columns = (int) Math.round(inputCoverage.getGridGeometry().getGridRange2D().getSize().getWidth());
            System.out.println(String.format("Rows = %d, columns = %d", rows, columns));

            //
            // Create a new geotiff
            //
            // final File geotiff = new File("d:/temp/1202018-20180703-float.tif");
            GridCoverageFactory factory = new GridCoverageFactory();
            SampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_FLOAT, columns, rows, bands, columns * bands, bandOffsets);
            DataBuffer buffer = new java.awt.image.DataBufferFloat(columns * rows * bands);
            ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            ColorModel colorModel = new ComponentColorModel(colorSpace, false, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_FLOAT);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, null);
            BufferedImage bi = new BufferedImage(colorModel, raster, false, null);
            ReferencedEnvelope envelope
                    = new ReferencedEnvelope(inputCoverage.getGridGeometry().getEnvelope2D().getMinX(), inputCoverage.getGridGeometry().getEnvelope2D().getMaxX(), inputCoverage.getGridGeometry().getEnvelope2D().getMinY(), inputCoverage.getGridGeometry().getEnvelope2D().getMaxY(), sourceCRS);
//        System.out.println(
//                String.format("LL = (%f, %f), UR = (%f,%f)", inputCoverage.getGridGeometry().getEnvelope2D().getMinX(), inputCoverage.getGridGeometry().getEnvelope2D().getMinY(), inputCoverage.getGridGeometry().getEnvelope2D().getMaxX(), inputCoverage.getGridGeometry().getEnvelope2D().getMaxY()));

            byte[] b = new byte[inputRaster.getNumDataElements()];
            for (int columnNr = 0; columnNr < columns; columnNr++) {
                for (int rowNr = 0; rowNr < rows; rowNr++) {
                    try {
                        inputRaster.getDataElements(columnNr, rowNr, b);
                        if (java.lang.Byte.toUnsignedInt(b[0]) == NODATAVALUE) {
                            newValues[0] = -9999.f;
                        } else {
//                        newValues[0] = java.lang.Byte.toUnsignedInt(b[0]) / 250.f;
                            newValues[0] = calc.calculate(b[0]);
                        }
                        raster.setDataElements(columnNr, rowNr, newValues);
                    } catch (Exception e) {
                        throw new RuntimeException(String.format("convertNDVIRaster: Rows = %d, Columns = %d, rowNr = %d, columnNr = %d", rows, columns, rowNr, columnNr));
                    }
                }
            }
            newRaster = factory.create("newRaster", bi, envelope);
        } else {
            newRaster = inputCoverage;
        }

        //
        // Now see if we need to transform. Only if output_epsg != 28992
        //
        if (!"28992".equalsIgnoreCase(output_epsg)) {
            final org.opengis.referencing.crs.CoordinateReferenceSystem targetCRS = org.geotools.referencing.CRS.decode("EPSG:".concat(output_epsg), true);
            GridCoverage2D covtransformed = (GridCoverage2D) Operations.DEFAULT.resample(newRaster, targetCRS);
            newRaster = covtransformed;
        }

        //
        // write the new geotiff
        //
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GeoTiffWriter writer = new GeoTiffWriter(outputStream);
        writer.write(newRaster, new GeneralParameterValue[0]);
        return outputStream.toByteArray();
    }

}
