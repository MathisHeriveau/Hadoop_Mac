package src.anagramme;

import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class AnagramMapper extends Mapper<Object, Text, Text, Text> {

    private Text signature = new Text();
    private Text wordOut = new Text();

    @Override
    protected void map(Object key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString().toLowerCase().replaceAll("[^a-z]", " ");
        StringTokenizer itr = new StringTokenizer(line);

        while (itr.hasMoreTokens()) {
            String word = itr.nextToken();

            // Ignore bruit : mots trop courts
            if (word.length() < 3) continue;

            char[] letters = word.toCharArray();
            java.util.Arrays.sort(letters);

            signature.set(new String(letters));
            wordOut.set(word);

            context.write(signature, wordOut);
        }
    }

}
