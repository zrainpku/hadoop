package graph; 
    
   import java.util.ArrayList; 
   import java.util.List; 
    
   public class Node  
   { 
	   
	   String id;//the id of current node 
	   List<String> destNodes = new ArrayList<String>();// dest nodes from current node 
	   public double oldPR;// old pagerank value of the current node 
	   public double newPR;// new pagerank value of the current node 
	   int numDest;//number of dest nodes 
	   private Node() 
	   { 
	     
	   } 
	    
	   public Node(String id) 
	   { 
	    this.id = id; 
	   } 
	   /*format is :  "nodeid oldPr newPR numDest destNode1 destNode2 ... " 
	    *  
	    * */ 
	    public static String toTextWithoutID(Node node) 
	    { 
	     StringBuffer tempBuffer = new StringBuffer(); 
	     tempBuffer.append(node.oldPR); 
	     tempBuffer.append("\t" + node.newPR); 
	     tempBuffer.append("\t" + node.numDest); 
	     for(String dest:node.destNodes) 
	     { 
	      tempBuffer.append("\t" + dest); 
	     } 
	       
	     return tempBuffer.toString(); 

	    } 
	    public static  Node InstanceFromString(String nodeTuple) 
	    { 
	     Node node = new Node(); 
	     String[] result = nodeTuple.split("\t"); 
	     node.id = result[0]; 
	     if(result.length == 2) 
	     { 
	      node.id = result[0]; 
	      node.newPR = Double.valueOf(result[1]); 
	     }else if(result.length > 4) 
	     { 
	      node.id = result[0]; 
	      node.oldPR = Double.valueOf(result[1]); 
	      
	      node.newPR = Double.valueOf(result[2]); 
	      node.numDest = Integer.valueOf(result[3]); 
	      for(int i = 4; i < result.length; i++) 
	      { 
	       node.destNodes.add(result[i]); 
	      } 
	      assert(node.numDest == node.destNodes.size()); 
	     } 
	      
	     return node; 
	    } 
	     
	    public String getID() 
	    { 
	     return this.id; 
	    } 
	     
	    public double getOldPR() 
	    { 
	     return this.oldPR; 
	    } 
	     
	    public double getNewPR() 
	    { 
	     return this.newPR; 
	    } 
	     
	    public int getNumDest() 
	    { 
	     return this.numDest; 
	    } 
	    public List<String> getDestNodes() 
	    { 
	     return this.destNodes; 
	    } 
	     
	   } 

