package src.bigramme;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class BGCDriver extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("two parameter : [input] [output]");
			System.exit(-1);
		}

		Job job1 = Job.getInstance();
		job1.setJobName("Bigramme Count");

		job1.setJarByClass(BGCDriver.class);
		job1.setMapperClass(BGCMapper1.class);
		job1.setReducerClass(BGCReducer1.class);
		job1.setCombinerClass(BGCReducer1.class);

		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(IntWritable.class);

		job1.setInputFormatClass(TextInputFormat.class);
		job1.setOutputFormatClass(TextOutputFormat.class);

		Path inputFilePath = new Path(args[0]);
		Path outputFilePath = new Path(args[1]);
		Path outputTemp = new Path(inputFilePath + "/temp");

		FileInputFormat.setInputDirRecursive(job1, true);

		FileInputFormat.setInputPaths(job1, inputFilePath);
		FileOutputFormat.setOutputPath(job1, outputTemp);

		FileSystem fs = FileSystem.get(getConf());

		if (fs.exists(outputTemp)) {
			fs.delete(outputTemp, true);
		}
		if (fs.exists(outputFilePath)) {
			fs.delete(outputFilePath, true);
		}

		Job job2 = Job.getInstance(getConf());
		job2.setJobName("Bigramme sorting");

		FileInputFormat.setInputPaths(job2, outputTemp);
		FileOutputFormat.setOutputPath(job2, outputFilePath);

		job2.setJarByClass(BGCDriver.class);
		job2.setMapperClass(BGCMapper2.class);
		job2.setReducerClass(BGCReducer2.class);
		job2.setCombinerClass(BGCReducer2.class);

		job2.setInputFormatClass(KeyValueTextInputFormat.class);

		job2.setOutputKeyClass(IntWritable.class);
		job2.setOutputValueClass(Text.class);
		job2.setSortComparatorClass(IntComparator.class);

		JobControl jobControl = new JobControl("MR_Chaine");
		ControlledJob controlledJob1 = new ControlledJob(getConf());
		controlledJob1.setJob(job1);
		jobControl.addJob(controlledJob1);

		ControlledJob controlledJob2 = new ControlledJob(getConf());
		controlledJob2.setJob(job2);
		// make job2 dependent on job1
		controlledJob2.addDependingJob(controlledJob1);
		// add the job to the job control
		jobControl.addJob(controlledJob2);

		Thread jobControlThread = new Thread(jobControl);
		jobControlThread.start();

		while (!jobControl.allFinished()) {
			System.out.println("Jobs in waiting state: "
					+ jobControl.getWaitingJobList().size());
			System.out.println("Jobs in ready state: "
					+ jobControl.getReadyJobsList().size());
			System.out.println("Jobs in running state: "
					+ jobControl.getRunningJobList().size());
			System.out.println("Jobs in success state: "
					+ jobControl.getSuccessfulJobList().size());
			System.out.println("Jobs in failed state: "
					+ jobControl.getFailedJobList().size());

			try {
				Thread.sleep(5000);
			} catch (Exception e) {

			}

		}

		//System.exit(0);
		return (job2.waitForCompletion(true) ? 0 : 1);

	}

	public static void main(String[] args) throws Exception {
		BGCDriver bGCDriver = new BGCDriver();
		int res = ToolRunner.run(bGCDriver, args);
		System.exit(res);
	}
}
