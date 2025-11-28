package src.projet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;


public class JoinCommandeMagasinReducer extends Reducer<Text, Text, Text, Text> {

    private HashMap<String, String> magasinMap = new HashMap<>();

    @Override
    protected void setup(Context ctx) throws IOException {
        Path[] cacheFiles = ctx.getLocalCacheFiles();
        if (cacheFiles != null && cacheFiles.length > 0) {
            for (Path p : cacheFiles) {
                BufferedReader br = new BufferedReader(new FileReader(p.toString()));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(";");
                    magasinMap.put(parts[0], parts[1]); 
                }
                br.close();
            }
        }
    }

    @Override
    protected void reduce(Text comNum, Iterable<Text> values, Context ctx)
            throws IOException, InterruptedException {

        boolean isValid = false;
        String magNum = null;

        for (Text v : values) {
            String s = v.toString();
            if (s.equals("OK")) {
                isValid = true;
            } else if (s.startsWith("MAGNUM:")) {
                magNum = s.substring(7);
            }
        }

        if (!isValid || magNum == null) return;

        String magNom = magasinMap.get(magNum);
        if (magNom != null) {
            ctx.write(comNum, new Text(magNom));
        }
    }
}
