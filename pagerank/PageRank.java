package pageranktwo;

import graph.Node; 

import java.io.BufferedReader; 
import java.io.IOException; 
import java.io.InputStreamReader; 
import java.net.URI;

import org.apache.hadoop.conf.Configuration; 
import org.apache.hadoop.fs.FSDataInputStream; 
import org.apache.hadoop.fs.FileSystem; 
import org.apache.hadoop.fs.Path; 
import org.apache.hadoop.io.DoubleWritable; 
import org.apache.hadoop.io.IntWritable; 
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.mapreduce.Job; 
import org.apache.hadoop.mapreduce.Mapper; 
import org.apache.hadoop.mapreduce.Reducer; 
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class PageRank  
{ 
 public static int numberofNodes = 10;//??? number of nodes 
 public static double d = 0.85; 
 public static double threshold = 1e-15;//stop threshold 
 public static int maxiter = 3; 
 public static String fileDirPath = "/"; 
 public static String outputPath = "hdfs://Master:9000/output0/"; 
 public static String inputFile = "hdfs://Master:9000/input0/"; 
 
 /** 
  * @param args 
  */ 
 public static void main(String[] args) throws Exception 
 { 
  int iterator = 0,k=2; 
  String chkpath;
  
  do 
  { 
	  long begin = System.currentTimeMillis();
	
	  
   Job pageRankJob = PageRank.getPageRankJob(PageRank.getPageRankInputPath(iterator),PageRank.getPageRankOutputPath(iterator)); 
   
   pageRankJob.waitForCompletion(true); 
   
   Job chkjob = PageRank.getCHKJob(PageRank.getCHKInputPath(iterator),PageRank.getCHKOutputPath(iterator)); 
    
   chkjob.waitForCompletion(true); 
    
   chkpath =PageRank.getCHKOutputPath(iterator) + "/part-r-00000"; 
  
   iterator ++; 
   
   long end = System.currentTimeMillis() - begin;
   System.out.println("第"+iterator+"轮耗时：" + end + "毫秒");
 // }while(iterator<5);
 }while(!PageRank.canStopIteration(chkpath,PageRank.threshold,PageRank.maxiter,iterator));
 
  //删除中间结果文件 
  //PageRank.deleteAllTempPaths(iterator); 
 } 
  
 //get a pagerank job  
 public static Job getPageRankJob(String inputPath,String outputPath) throws Exception 
 { 
  Configuration conf = new Configuration(); 
     Job job = new Job(conf, "pagerank"+inputPath); 
  job.setJarByClass(PageRank.class); 
  job.setMapperClass(PageRankMapper.class); 
   
  job.setReducerClass(PageRankReducer.class); 
  job.setOutputKeyClass(Text.class); 
  job.setOutputValueClass(Text.class); 
  job.setNumReduceTasks(1); 
  FileInputFormat.addInputPath(job, new Path(inputPath)); 
  FileOutputFormat.setOutputPath(job, new Path(outputPath)); 
   
  return job; 
 } 
 //get chk job 
 public static Job getCHKJob(String inputPath,String outputPath) throws Exception 
 { 
	 
  Configuration conf = new Configuration(); 
     Job job = new Job(conf, "pagerank_chk"+inputPath); 
  job.setJarByClass(PageRank.class); 
  job.setMapperClass(CHKMapper.class); 
    
  job.setReducerClass(CHKReducer.class);
  
  
  job.setOutputKeyClass(IntWritable.class); 
  job.setOutputValueClass(DoubleWritable.class); 
   //no change
  FileInputFormat.addInputPath(job, new Path(inputPath)); 
  //FileInputFormat.addInputPath(job, new Path(otherArgs[0])); 
  FileOutputFormat.setOutputPath(job, new Path(outputPath)); 
  //FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
  return job; 
 } 
 public static class PageRankMapper extends Mapper<Object,Text,Text,Text> 
 { 
  public void map(Object key, Text value,Context context ) throws IOException, 
InterruptedException  
     { 
   String tupleNode = value.toString(); 
   Node currentNode = Node.InstanceFromString(tupleNode); 
   currentNode.oldPR = currentNode.newPR; 
   //if(currentNode.getNumDest()==0)currentNode.newPR=
   context.write(new Text(currentNode.getID()), new Text(Node.toTextWithoutID(currentNode))); 
    
      //handle every destNode 
   for(String destNode:currentNode.getDestNodes()) 
   { 
    String outPR = new Double(currentNode.newPR/currentNode.getNumDest()).toString(); 
    context.write(new Text(destNode), new Text(outPR )); 
   } 
     } 
 } 
  
 public static class PageRankReducer extends Reducer<Text,Text,Text,Text> 
 { 
  public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException 
  { 
   Node currentNode = new Node(key.toString()); 
   double inPR = 0.0; 
   
   for(Text val:values) 
   { 
    String[] temp =  val.toString().split("\t"); 
    if(temp.length == 1) 
    { 
     inPR += Double.valueOf(temp[0]); 
    }else if(temp.length >= 4) 
    	
    { 
        currentNode  = Node.InstanceFromString(key.toString() + "\t" +   val.toString()); 
       }  //else if

   }//for 
       
      currentNode.newPR = (1-PageRank.d)/PageRank.numberofNodes +   PageRank.d*inPR ; 
      context.write(new Text(currentNode.getID()), new  Text(Node.toTextWithoutID(currentNode))); 
     } 
    } //
     
    public static class CHKMapper extends Mapper<Object,Text,IntWritable,DoubleWritable> 
    { 
     public void map(Object key, Text value,Context context ) throws IOException, InterruptedException  
        { 
      String tupleNode = value.toString(); 
      Node currentNode = Node.InstanceFromString(tupleNode); 
      Double partialResidual = (currentNode.newPR - currentNode.oldPR)*(currentNode.newPR - currentNode.oldPR); 
      IntWritable one = new IntWritable(1); 
      context.write(one,new DoubleWritable(partialResidual));   
        } 
    } 
     
    public static class CHKReducer extends Reducer<IntWritable,DoubleWritable,IntWritable,DoubleWritable> 
    { 
    	  public void reduce(IntWritable key, Iterable<DoubleWritable> values, Context context) 
    	throws IOException, InterruptedException 
    	  { 
    	   Double residual = 0.0; 
    	   for(DoubleWritable patialResidual:values) 
    	   { 
    	    residual += patialResidual.get(); 
    	   } 
    	   context.write(key, new DoubleWritable(residual)); 
    	  } 
    	 } 
    	  
    	 /* 
    	  * check if can stop the iteration. there are two posibilities to stop.
    * 1.we get the max iteration times 
     * 2.the residual is not larger than than threshold  
     */ 
    public static boolean canStopIteration(String residualPath,double threshold,   int maxIteration,int currentIteration) throws IOException 
    { 
    	System.out.println("zhurui###########@@@#############");
    	
     if(currentIteration >= maxIteration) 
     { 
      return true; 
     } 
     //get the residual value 
      Configuration conf = new Configuration(); 
      // change no 
      //FileSystem fs = FileSystem.get(conf); 
      FileSystem fs = FileSystem.get(URI.create("hdfs://Master:9000/"),conf);
      FSDataInputStream fsi = fs.open(new Path(residualPath)); 
     // Path dfs = new Path(residualPath);
      //FileSystem fileSystem = dfs.getFileSystem(config);
      
      BufferedReader reader = new  BufferedReader (new InputStreamReader(fsi)); 
         String temp =  reader.readLine(); 
      String[] results = temp.split("\t"); 
      double residual = Double.valueOf(results[1].trim()); 
       
     if(residual <= threshold) 
     { 
    	 System.out.println("zhurui@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
      return true; 
     }  
     return false; 
    } 
       /* 
        * get the pagerank input path by iteration 
    */ 
    public static String getPageRankInputPath(int iteration) 
    { 
     if(iteration == 0) 
     { 
      return PageRank.inputFile; 
     } 
     else 
     { 
      return PageRank.outputPath + "/output_" + (iteration-1); 
     } 
    } 
    /* 
        * get the pagerank output path by iteration 
        */ 
    public static String getPageRankOutputPath(int iteration) 
    { 
    	
    	return PageRank.outputPath + "/output_" + iteration; 
    } 
     
    public static String getCHKInputPath(int iteration) 
    { 
     return PageRank.outputPath + "/output_" + iteration; 
    } 
     
    public static String getCHKOutputPath(int iteration) 
    { 
     return PageRank.outputPath + "/chk_output_" + iteration; 
    } 
    /* 
     * delete all temp result path 
     *  
     */ 
     
    public static void deleteAllTempPaths(int currentIter)throws IOException 
    { 
     if(currentIter > 3) 
     { 
       Configuration conf = new Configuration(); 
       FileSystem fs = FileSystem.get(conf); 
      for(int i = 1; i < currentIter-1; i++) 
      { 
       Path tempPageRankOutputPath = new 
   Path(PageRank.getPageRankOutputPath(i)); 
       Path tempCHKPath = new Path(PageRank.getCHKOutputPath(i)); 
       fs.delete(tempPageRankOutputPath,true); 
       fs.delete(tempCHKPath,true); 

      } 
     } 
    } 
   } 
    
    
    
   
