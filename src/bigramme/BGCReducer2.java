package src.bigramme;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class BGCReducer2 extends Reducer<IntWritable, Text, IntWritable, Text> {
	Text word = new Text();

	@Override
	protected void reduce(IntWritable key, Iterable<Text> values,
			Reducer<IntWritable, Text, IntWritable, Text>.Context context)
			throws IOException, InterruptedException {
		for (Text value : values) {
            word = value;
            context.write(key, word);
        }
	}

}