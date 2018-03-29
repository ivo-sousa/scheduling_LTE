package tese;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class ExportToExcel {

    private static final char DEFAULT_SEPARATOR = ',';

    public static void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }

    public static void writeLine(Writer w, List<String> values, char separators) throws IOException {
        writeLine(w, values, separators, ' ');
    }

    //https://tools.ietf.org/html/rfc4180
    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }

    public static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());


    }
    
    
    
    public static void main(String[] args) throws Exception {

        String csvFile = "C:/Users/alunos/Desktop/abc.csv";
        FileWriter writer = new FileWriter(csvFile);

        ExportToExcel.writeLine(writer, Arrays.asList("a", "b", "c", "d"));

        //custom separator + quote
        ExportToExcel.writeLine(writer, Arrays.asList("aaa", "bb,b", "cc,c"), ',', '"');

        //custom separator + quote
        ExportToExcel.writeLine(writer, Arrays.asList("aaa", "bbb", "cc,c"), '|', '\'');

        //double-quotes
        ExportToExcel.writeLine(writer, Arrays.asList("aaa", "bbb", "cc\"c"));


        writer.flush();
        writer.close();

    }

}