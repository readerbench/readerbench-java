package services.converters;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

public class ColorTextStripper extends PDFTextStripper
{
    public ColorTextStripper() throws IOException
    {
        super();
        setSuppressDuplicateOverlappingText(false);

        registerOperatorProcessor("CS", new org.apache.pdfbox.util.operator.SetStrokingColorSpace());
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
        registerOperatorProcessor("k", new org.apache.pdfbox.util.operator.SetNonStrokingCMYKColor());
    }

    @Override
    protected void processTextPosition(TextPosition text)
    {
        renderingMode.put(text, getGraphicsState().getTextState().getRenderingMode());
        strokingColor.put(text, getGraphicsState().getStrokingColor().getColorSpaceValue());
        nonStrokingColor.put(text, getGraphicsState().getNonStrokingColor().getColorSpaceValue());

        super.processTextPosition(text);
    }

    Map<TextPosition, Integer> renderingMode = new HashMap<TextPosition, Integer>();
    Map<TextPosition, float[]> strokingColor = new HashMap<TextPosition, float[]>();
    Map<TextPosition, float[]> nonStrokingColor = new HashMap<TextPosition, float[]>();
    Map<String, Integer> charsPerColor = new HashMap<String, Integer>();

    final static List<Integer> FILLING_MODES = Arrays.asList(0, 2, 4, 6);
    final static List<Integer> STROKING_MODES = Arrays.asList(1, 2, 5, 6);
    final static List<Integer> CLIPPING_MODES = Arrays.asList(4, 5, 6, 7);

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException
    {
        for (TextPosition textPosition: textPositions)
        {
            Integer charRenderingMode = renderingMode.get(textPosition);
            float[] charStrokingColor = strokingColor.get(textPosition);
            float[] charNonStrokingColor = nonStrokingColor.get(textPosition);
            
            String fillColor = toString(charNonStrokingColor);
            if (charsPerColor.get(fillColor) == null) {
            	charsPerColor.put(fillColor, 1);
            }
            else {
            	charsPerColor.put(fillColor, charsPerColor.get(fillColor) + 1);
            }

            StringBuilder textBuilder = new StringBuilder();
            textBuilder.append(textPosition.getCharacter())
                       .append("{");

            if (FILLING_MODES.contains(charRenderingMode))
            {
                textBuilder.append("FILL:")
                           .append(toString(charNonStrokingColor))
                           .append(';');
            }

            if (STROKING_MODES.contains(charRenderingMode))
            {
                textBuilder.append("STROKE:")
                           .append(toString(charStrokingColor))
                           .append(';');
            }

            if (CLIPPING_MODES.contains(charRenderingMode))
            {
                textBuilder.append("CLIP;");
            }

            textBuilder.append("}");
            writeString(textBuilder.toString());
        }
    }

    String toString(float[] values)
    {
        if (values == null)
            return "null";
        StringBuilder builder = new StringBuilder();
        switch(values.length)
        {
        case 1:
            builder.append("GRAY"); break;
        case 3:
            builder.append("RGB"); break;
        case 4:
            builder.append("CMYK"); break;
        default:
            builder.append("UNKNOWN");
        }
        for (float f: values)
        {
            builder.append(' ')
                   .append(f);
        }

        return builder.toString();
    }

	public Map<String, Integer> getCharsPerColor() {
		return charsPerColor;
	}
}