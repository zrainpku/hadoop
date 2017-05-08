

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class wordCount{
 public static class WordMap extends Mapper<Object,Text,Text,IntWritable>{
  
	 //private final static IntWritable one=new IntWritable(1);
	 private  IntWritable result=new IntWritable();
	 private Text word = new Text();
  public void map(Object key,Text value,Context context)throws IOException,InterruptedException{
   
	  String line=value.toString();  
	 StringTokenizer itr = new StringTokenizer(line);
   
	
	 Map <String,Integer> map=new HashMap <String,Integer>();
	 
	 while (itr.hasMoreTokens()){
     	String str=itr.nextToken();
     	if(map.get(str)!=null){
     		map.put(str, map.get(str)+1);
     		
     	}
     	else{
     		map.put(str, 1);
     	}
     	
     }
     Set set =map.keySet();
     Iterator iter = set.iterator();
     while(iter.hasNext()){
     	String keys=(String)iter.next();
        word.set(keys);
     	result.set(map.get(keys));
         context.write(word, result);

     	
     	
     }
	 
	 
	 	 
  }
 }
 
 public static class WordReduce extends Reducer<Text,IntWritable,Text,IntWritable>{
  private IntWritable result =new IntWritable();
  public void reduce(Text key,Iterable<IntWritable>values,Context context)
  throws IOException,InterruptedException{
   int sum=0;
   for(IntWritable val:values)
   {
    sum+=val.get();
   }
   result.set(sum);
   context.write(key,result);
  }
 }
 
 
 public static void main(String[]args)throws Exception
 {
  Configuration conf =new Configuration();
 
  Job job =new Job(conf,"WordCount");
  
  job.setJarByClass(WordMap.class);
  job.setMapperClass(WordMap.class);
        job.setReducerClass(WordReduce.class);
  
  job.setOutputKeyClass(Text.class);
  job.setOutputValueClass(IntWritable.class);
  
  FileInputFormat.addInputPath(job,new Path(args[0]));
  FileOutputFormat.setOutputPath(job,new Path(args[1]));//setOutputPath(job,new Path(args[1]));
  
  System.exit(job.waitForCompletion(true)?0:1);
  
  
  
 }

}
