/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters;

import data.CVStructure;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.nd4j.linalg.primitives.Triple;
//import org.datavec.api.berkeley.Triple;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class CVPDFTextStripper extends PDFTextStripper {

    private final CVStructure cvStructure;
    private final Map<Double, Integer> charsPerSize;
    private final Map<String, Integer> charsPerFont;

    public CVPDFTextStripper() throws IOException {
        super();

        cvStructure = new CVStructure();
        charsPerSize = new HashMap<>();
        charsPerFont = new HashMap<>();

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

    @Override
    protected void processTextPosition(TextPosition text) {

        String fontName = text.getFont().getName();
//        if (text.getUnicode().hashCode() != 32) {
//            String toWrite = text.getUnicode() + " (" + text.getUnicode().hashCode() + "), x="
//                    + text.getX() + ", y=" + text.getY() + ", Height=" + text.getHeight()
//                    + ", width=" + text.getWidth() + ", font=" + text.getFontSizeInPt() + ", font=" + text.getFont().toString()
//                    + "\n";
//            try {
//                Files.write(Paths.get("./fileName.txt"), toWrite.getBytes(), StandardOpenOption.APPEND);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        if (text.getUnicode().hashCode() != 32 && !fontName.contains("Wingdings")) {
            if (text.getY() != cvStructure.getLastYCoord()) {
                int start = 0;
                for (int i = 0; i < fontName.length(); i++) {
                    if (fontName.charAt(i) == '+') {
                        start = i + 1;
                    }
                }
                cvStructure.addYCoord(new Triple(text.getY(), text.getFontSizeInPt(), fontName.substring(start)));
            }
        }

        if (!charsPerFont.containsKey(fontName)) {
            charsPerFont.put(fontName, 1);
        } else {
            charsPerFont.put(fontName, charsPerFont.get(fontName) + 1);
        }

        double fontSize = text.getFontSize();
        if (!charsPerSize.containsKey(fontSize)) {
            charsPerSize.put(fontSize, 1);
        } else {
            charsPerSize.put(fontSize, charsPerSize.get(fontSize) + 1);
        }
        super.processTextPosition(text);
    }

    public CVStructure getCvStructure() {
        return cvStructure;
    }

    public Map<Double, Integer> getCharsPerSize() {
        return charsPerSize;
    }

    public Map<String, Integer> getCharsPerFont() {
        return charsPerFont;
    }

}
