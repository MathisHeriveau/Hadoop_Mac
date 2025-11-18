package src.bigramme;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class BGCMapper1 extends Mapper<LongWritable, Text, Text, IntWritable> {

	private final static IntWritable one = new IntWritable(1);
	private Text word = new Text();

	@Override
	protected void map(LongWritable key, Text value,
			Mapper<LongWritable, Text, Text, IntWritable>.Context context)
			throws IOException, InterruptedException {

		String line = value.toString();
		StringTokenizer tokenizer = new StringTokenizer(line.toLowerCase());
		String previous = null;
		while (tokenizer.hasMoreTokens()) {
			String str = tokenizer.nextToken();

			if (((!str.equals("")) && (str != null) && (str
					.matches("^[a-zA-Z]*$")))) {
				if (previous != null) {
					word.set(previous + " " + str);
					context.write(word, one);
				}
				previous = str;
			}
		}
	}

}