package services.complexity;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.Lang;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class used to define all factors to be used within the complexity evaluation
 * model
 *
 * @author Mihai Dascalu
 */
public class ComplexityIndices {

    static Logger logger = Logger.getLogger(ComplexityIndices.class);

    public static final int IDENTITY = -1;

    public static void computeComplexityFactors(AbstractDocument d) {
        d.setComplexityIndices(
                Arrays.stream(ComplexityIndexType.values())
                .filter(t -> t.getFactory() != null)
                .map(cat -> cat.getFactory())
                .flatMap(f -> f.build(d.getLanguage()).stream())
                .collect(Collectors.toMap(Function.identity(), f -> f.compute(d))));
    }
    
    public static List<ComplexityIndex> getIndices(Lang lang) {
        return Arrays.stream(ComplexityIndexType.values())
                .map(cat -> cat.getFactory())
                .flatMap(f -> f.build(lang).stream())
                .collect(Collectors.toList());
    }
    
    public static double[] getComplexityIndicesArray(AbstractDocument d) {
        return ComplexityIndices.getIndices(d.getLanguage()).stream()
                .mapToDouble(index -> d.getComplexityIndices().get(index))
                .toArray();
    }

    public static void main(String[] args) {
        List<ComplexityIndex> factors = getIndices(Lang.eng);
        factors.stream().forEachOrdered(f -> {
            System.out.println(f.getCategoryName() + "\t" + f.getAcronym() + "\t"
                    + f.getDescription());
        });

        System.out.println("TOTAL:" + factors.size() + " factors");
    }
}
