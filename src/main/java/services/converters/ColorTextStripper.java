/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.converters;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceRGBColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceRGBColor;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import webService.ReaderBenchServer;

public class ColorTextStripper extends PDFTextStripper {

    private static Logger logger = Logger.getLogger("");
    private PrintWriter out;

    public ColorTextStripper(PrintWriter out) throws IOException {
        super();
        this.out = out;
        setSuppressDuplicateOverlappingText(false);

        /*registerOperatorProcessor("CS", new org.apache.pdfbox.util.operator.SetStrokingColorSpace());
        registerOperatorProcessor("cs", new org.apache.pdfbox.util.operator.SetNonStrokingColorSpace());
        registerOperatorProcessor("SC", new org.apache.pdfbox.util.operator.SetStrokingColor());
        registerOperatorProcessor("sc", new org.apache.pdfbox.util.operator.SetNonStrokingColor());
        registerOperatorProcessor("SCN", new org.apache.pdfbox.util.operator.SetStrokingColor());
        registerOperatorProcessor("scn", new org.apache.pdfbox.util.operator.SetNonStrokingColor());
        registerOperatorProcessor("G", new org.apache.pdfbox.util.operator.SetStrokingGrayColor());
        registerOperatorProcessor("g", new org.apache.pdfbox.util.operator.SetNonStrokingGrayColor());
        registerOperatorProcessor("RG", new org.apache.pdfbox.util.operator.SetStrokingRGBColor());
        registerOperatorProcessor("rg", new org.apache.pdfbox.util.operator.SetNonStrokingRGBColor());
        registerOperatorProcessor("K", new org.apache.pdfbox.util.operator.SetStrokingCMYKColor());
        registerOperatorProcessor("k", new org.apache.pdfbox.util.operator.SetNonStrokingCMYKColor());*/
        addOperator(new SetStrokingColorSpace());
        addOperator(new SetNonStrokingColorSpace());
        addOperator(new SetStrokingDeviceCMYKColor());
        addOperator(new SetNonStrokingDeviceCMYKColor());
        addOperator(new SetNonStrokingDeviceRGBColor());
        addOperator(new SetStrokingDeviceRGBColor());
        addOperator(new SetNonStrokingDeviceGrayColor());
        addOperator(new SetStrokingDeviceGrayColor());
        addOperator(new SetStrokingColor());
        addOperator(new SetStrokingColorN());
        addOperator(new SetNonStrokingColor());
        addOperator(new SetNonStrokingColorN());
    }

    // added by Gabi
    public String getText(PDPage doc) throws IOException {
        StringWriter outputStream = new StringWriter();
        writeText(doc, outputStream);
        return outputStream.toString();
    }

    // added by Gabi
    public void writeText(PDPage page, Writer outputStream) throws IOException {
        PDDocument doc = new PDDocument();
        doc.addPage(page);
        writeText(doc, outputStream);
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        renderingMode.put(text, getGraphicsState().getTextState().getRenderingMode().intValue());
        strokingColor.put(text, getGraphicsState().getStrokingColorSpace().getDefaultDecode(8));
        nonStrokingColor.put(text, getGraphicsState().getNonStrokingColorSpace().getDefaultDecode(8));

        super.processTextPosition(text);
    }

    Map<TextPosition, Integer> renderingMode = new HashMap<>();
    Map<TextPosition, float[]> strokingColor = new HashMap<>();
    Map<TextPosition, float[]> nonStrokingColor = new HashMap<>();
    Map<String, Integer> charsPerColor = new HashMap<>();

    final static List<Integer> FILLING_MODES = Arrays.asList(0, 2, 4, 6);
    final static List<Integer> STROKING_MODES = Arrays.asList(1, 2, 5, 6);
    final static List<Integer> CLIPPING_MODES = Arrays.asList(4, 5, 6, 7);

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        /*logger.info("Outputez urmatorul text: " + text);
    	out.write(text);
    	// search for paragraphs
    	logger.info("Punct e la pozitia " + text.lastIndexOf('.') + "; text are lungimea = " + text.length());
    	if (text.trim().lastIndexOf('.') == text.trim().length() - 1) {
    		logger.info("Outputez linie noua");
    		out.write("\n");
    	}*/

        for (TextPosition textPosition : textPositions) {
            Integer charRenderingMode = renderingMode.get(textPosition);
            float[] charStrokingColor = strokingColor.get(textPosition);
            float[] charNonStrokingColor = nonStrokingColor.get(textPosition);

            String fillColor = toString(charNonStrokingColor);
            if (charsPerColor.get(fillColor) == null) {
                charsPerColor.put(fillColor, 1);
            } else {
                charsPerColor.put(fillColor, charsPerColor.get(fillColor) + 1);
            }

            StringBuilder textBuilder = new StringBuilder();
            textBuilder.append(textPosition)
                    .append("{");

            if (FILLING_MODES.contains(charRenderingMode)) {
                textBuilder.append("FILL:")
                        .append(toString(charNonStrokingColor))
                        .append(';');
            }

            if (STROKING_MODES.contains(charRenderingMode)) {
                textBuilder.append("STROKE:")
                        .append(toString(charStrokingColor))
                        .append(';');
            }

            if (CLIPPING_MODES.contains(charRenderingMode)) {
                textBuilder.append("CLIP;");
            }

            textBuilder.append("}");
            writeString(textBuilder.toString());
        }
    }

    String toString(float[] values) {
        if (values == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        switch (values.length) {
            case 1:
                builder.append("GRAY");
                break;
            case 3:
                builder.append("RGB");
                break;
            case 4:
                builder.append("CMYK");
                break;
            default:
                builder.append("UNKNOWN");
        }
        for (float f : values) {
            builder.append(' ')
                    .append(f);
        }

        return builder.toString();
    }

    public Map<String, Integer> getCharsPerColor() {
        return charsPerColor;
    }
}
