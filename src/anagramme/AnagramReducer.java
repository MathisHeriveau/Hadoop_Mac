package src.anagramme;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class AnagramReducer extends Reducer<Text, Text, Text, Text> {

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

        Set<String> uniqueWords = new HashSet<>();

        for (Text val : values) {
            uniqueWords.add(val.toString());
        }

        if (uniqueWords.size() >= 2) {
            context.write(key, new Text(uniqueWords.toString()));
        }
    }
}
