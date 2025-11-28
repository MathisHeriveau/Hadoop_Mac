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


public class JoinProdConcernerReducer extends Reducer<Text, Text, Text, Text> {
    private Text outKey = new Text();
    private Text outValue = new Text();

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

        String prodLib = null;
        List<String> comNums = new ArrayList<>();

        for (Text t : values) {
            String v = t.toString();
            if (v.startsWith("P:")) {
                prodLib = v.substring(2); 
            } else if (v.startsWith("C:")) {
                comNums.add(v.substring(2)); 
            }
        }

        if (prodLib == null) return;

        String libLower = prodLib.toLowerCase();
        boolean isMoules = libLower.contains("moule");
        boolean isBulots = libLower.contains("bulot");

        if (!(isMoules || isBulots)) {
            return;
        }

        for (String comNum : comNums) {
            outKey.set(comNum);
            outValue.set(prodLib);
            context.write(outKey, outValue);
        }
    }
}