package src.projet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Driver {

    public static void main(String[] args) throws Exception {

        // Script gives: produit, concerner, commande, magasin, output
        if (args.length != 5) {
            System.err.println("Usage: Driver <produit> <concerner> <commande> <magasin> <output>");
            System.exit(1);
        }

        Path produitPath   = new Path(args[0]);
        Path concernerPath = new Path(args[1]);
        Path commandePath  = new Path(args[2]);
        Path magasinPath   = new Path(args[3]);

        // The script injects "/output" as the last arg
        Path finalOutputRoot = new Path(args[4]);

        Path job1Out = new Path(finalOutputRoot, "job1");
        Path job2Out = new Path(finalOutputRoot, "job2");
        Path job3Out = new Path(finalOutputRoot, "job3");

        Configuration conf = new Configuration();


        /***** JOB 1 *****/
        Job job1 = Job.getInstance(conf, "Job1_Join_Produit_Concerner");
        job1.setJarByClass(Driver.class);

        MultipleInputs.addInputPath(job1, produitPath, TextInputFormat.class, PMapper1.class);
        MultipleInputs.addInputPath(job1, concernerPath, TextInputFormat.class, PMapper2.class);

        job1.setReducerClass(PReducer1.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);

        FileOutputFormat.setOutputPath(job1, job1Out);

        if (!job1.waitForCompletion(true)) System.exit(1);


        /***** JOB 2 *****/
        Job job2 = Job.getInstance(conf, "Job2_Filter_Moules_Bulots");
        job2.setJarByClass(Driver.class);

        job2.setMapperClass(PMapper3.class);
        job2.setReducerClass(PReducer2.class);

        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job2, job1Out);
        FileOutputFormat.setOutputPath(job2, job2Out);

        if (!job2.waitForCompletion(true)) System.exit(1);


        /***** JOB 3 *****/
        Job job3 = Job.getInstance(conf, "Job3_Final_Join_Filtered_Commandes");
        job3.setJarByClass(Driver.class);


        MultipleInputs.addInputPath(job3, commandePath, TextInputFormat.class, PMapper4.class);

        job3.addCacheFile(magasinPath.toUri());


        MultipleInputs.addInputPath(job3, job2Out, TextInputFormat.class, PMapper5.class);

        job3.setReducerClass(PReducer3.class);
        job3.setOutputKeyClass(Text.class);
        job3.setOutputValueClass(Text.class);

        FileOutputFormat.setOutputPath(job3, job3Out);

        if (!job3.waitForCompletion(true)) System.exit(1);
    }
}
