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

public class PReducer2 extends Reducer<Text, Text, Text, Text> {
    private Text outValue = new Text("OK");

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

        boolean hasMoules = false;
        boolean hasBulots = false;

        for (Text t : values) {
            String lib = t.toString().toLowerCase();
            if (lib.contains("moule")) hasMoules = true;
            if (lib.contains("bulot")) hasBulots = true;
        }

        if (hasMoules && hasBulots) {
            context.write(key, outValue);
        }
    }
}