
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;


import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class XMLParser {

	Map<String, Term> termMapGlobal;
	private JsonParser parser = new JsonParser();
	Map<String, Comment> allComments;
	Map<Long, User> userMap;
	public Set<Long> userIdList;
	static List<Cluster> allClusterList = new ArrayList<Cluster>(); 
	//List<List<HasWord>> sentences;
	Map<String, Integer> Sentence_Weight = new HashMap<String, Integer>();

	public XMLParser() {
		termMapGlobal = new HashMap<String, Term>();
		allComments = new HashMap<String, Comment>();
		userMap = new HashMap<Long, User>();
		
	}

	private static void printHashMap(Map<String, Term> indexMap) {
		Term term;

		for (String k : indexMap.keySet()) {
			String s = "";
			term = indexMap.get(k);
			for (String k1 : term.commentIdList) {
				s += k1 + ",";

			}

			System.out.println(k + " - " + s);
		}
	}

	private void printCommentMap() {

		for (Comment c : allComments.values()) {
			System.out.println("comment Id : " + c.commentId);
			System.out.println("--------------------------------");
			if (c.replies != null) {

				for (Reply r : c.replies) {
					System.out.println("-----------------******---------------");
					System.out.println(r.message + r.authorId);
				}
			}

		}
	}

	private static void printHashMap2(Map<String, Integer> indexMap) {
		Term term;

		for (String k : indexMap.keySet()) {
			System.out.println(k + " - " + indexMap.get(k));
		}
	}
	
	
	private static void printCluster()
	{
		for(int i=0;i<allClusterList.size();i++)
		{
			System.out.println("Cluster with ID " + i + " and Weight = " + allClusterList.get(i).weight );
			Iterator<String> reply = allClusterList.get(i).commentIdList.iterator();
			while (reply.hasNext()) {
				String j = reply.next();
				System.out.println(j);
			}
			System.out.print("\n");
		}

	}

	private void parseJSONFile() {
		try {
			// prints json file names
		//	System.out.println("File: \t");
			JsonElement jsonElement = parser.parse(new FileReader("dataset/Blog 1/commentsDataset.json"));
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			readJSONFile(jsonObject);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			// e.getStackTrace();
		}
	}

	private void readJSONFile(JsonObject jsonObject) {

		//System.out.println("In readJSON " + " - ");
		//JsonArray value = jsonObject.  get(Constants.COMMENTS).getAsJsonArray();
	//	JsonObject jo = (JsonObject) value.get(0);
		JsonObject ja = (JsonObject) jsonObject.entrySet().iterator().next().getValue().getAsJsonObject().get(Constants.COMMENTS);
		JsonArray allComments = ja.get(Constants.DATA).getAsJsonArray();

		// This loop is to iterate over all the comments
		Iterator<JsonElement> msg = allComments.iterator();
		int i = 0;
		while (msg.hasNext()) {

			//System.out.println("COMMENT " + i++ + " by ");

			decodeComment(msg.next());
		}
		
		/*
		 * 
		 * 
		 *  This is the place where you need to write your code 
		 *  Call a method that calculates the IDF 
		 *  then a method that clusters all the comments 
		 *  
		 *  After all this I would call the CalculateTermWeight that would consider the weights that you  assign to clusters
		 * 
		 * 
		 * */
		
		
	 
		//printCommentMap();
	
	
	}

	private void CalculateTermWeight() {
		// TODO Auto-generated method stub
		// Iterate over the terms map and calculate the weight
		
		Integer commentsCount = allComments.size();
		Double idf ; 
		
		for (Map.Entry<String, Term> entry : termMapGlobal.entrySet()){
			Term termObj =  entry.getValue();
			String term = entry.getKey();
			Integer weight = 0 ;
			Set<String> commentIdSet = termObj.commentIdList ; 
			
			//idf =  Math.log10((double)commentsCount / (double)commentIdSet.size());
			
			for(String commentId : commentIdSet){
				
				Comment commentObj = allComments.get(commentId);
			
				// get the author weight 
				// TODO : decide the factor for this weight i suggest 0.3
				weight += commentObj.termsMap.get(term) * userMap.get(commentObj.authorId).authority;
				
				// The likes count for the comment 
				weight +=commentObj.likesCount;
				
				// If the comment has replies 
				if(commentObj.replies != null){
					weight +=  commentObj.replies.size();
				}else{//TODO: what would be the weight if the comment has no reply
					
				
				}	
				
				// Count the number of the named entities in the comment 
				weight += commentObj.countOfNamedEntites;
					
				
				//System.out.println("cluster Id -->"+commentObj.clusterId);
				if(commentObj.clusterId!=null){
				//	System.out.println("i am here -------------------");
					weight +=(int) ((commentObj.termsMap.get(term) * allClusterList.get(commentObj.clusterId).weight * 100 )+ 0.5);
				}
			}
			termObj.weight = weight;
			//System.out.println(term + "---->" + weight);
			
		  
		}
		
		
		//sortMap ();
	}
	
    private class TermComparator implements Comparator<String>{
		@Override
		public int compare(String arg0, String arg1) 
		{
			Integer t1 =  Sentence_Weight.get(arg0);
			Integer t2 = Sentence_Weight.get(arg1);
			int i = t2.compareTo(t1);
			return i==0 ? 1:i;
		}
	}

	
public int sortMap (){
	
		int Threshold = 1000000000;
	    TermComparator termComparator = new TermComparator();
		TreeMap<String, Integer> sortedByValues = new TreeMap<String, Integer>(termComparator);
		for (Map.Entry<String,Integer > entry : Sentence_Weight.entrySet()){
			sortedByValues.put(entry.getKey(),entry.getValue());
		}
	    int c=0;
		//System.out.println("Sentences and their weights : ");
		for (Map.Entry<String, Integer> entry : sortedByValues.entrySet()){
			c++;
			//System.out.println(entry.getKey() +" - "+entry.getValue());
			if(c==11)
				Threshold=entry.getValue();
		}
		return Threshold;
	}

	private void decodeComment(JsonElement comment) {
		String name = "No Name";
		Integer autherId;
		;
		JsonObject commentJsonObject = comment.getAsJsonObject();
		Comment commentObj = new Comment();
		User userObj = new User();

		try {

			// If it is not the FB user
			try {
				commentObj.authorId = Long.parseLong(commentJsonObject.get(Constants.FROM).getAsJsonObject().get(Constants.ID).getAsString());

			} catch (Exception e) {
				commentObj.authorId = new Random().nextLong();
			}
			// TODO: extract all other information about user such as top
			// Commentor

			// Add the user to the global user Map
			userObj.userId = commentObj.authorId;
			userObj.authority = 5;
			userMap.put(commentObj.authorId, userObj);

			// TODO: Remove this later
		try {
				name = commentJsonObject.get(Constants.FROM).getAsJsonObject().get(Constants.NAME).getAsString();
			} catch (Exception e) {
				// autherId = -1;

			}

			// get the commentId
			commentObj.commentId = commentJsonObject.get(Constants.ID).getAsString();

			// TODO: populate userIdlist from reply file
			// TODO: get the reply count
			readReplies(commentObj);

			commentObj.likesCount = Integer.parseInt(commentJsonObject.get(Constants.LIKECOUNT).getAsString());
			String message = commentJsonObject.get(Constants.MESSAGE).getAsString();

		//	System.out.print(name + " -----" + commentObj.authorId + " " + commentObj.likesCount + " " + commentObj.commentId);

			// commentObj.likesCount = likesCount;

			Tokenizer.tokenize(message, termMapGlobal, commentObj);

		   // Count the number of named entity in the comment 
			commentObj.countOfNamedEntites = Tagger.countNamedEntity(message);

			allComments.put(commentObj.commentId, commentObj);
			//System.out.println("term map---------------------");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// printHashMap2(commentObcommentJsonObject.termsMap);
	}

	private void readReplies(Comment commentObj) {
		try {
			JsonElement jsonElement = parser.parse(new FileReader("dataset/Blog 1/replies/replyFor-"+commentObj.commentId+".json"));
			JsonArray replyArray = jsonElement.getAsJsonObject().get(Constants.DATA).getAsJsonArray();
			if (replyArray != null) {
				userIdList = new HashSet<Long>();
				commentObj.replies = new ArrayList<Reply>();
				Iterator<JsonElement> reply = replyArray.iterator();
				while (reply.hasNext()) {
					JsonElement j = reply.next();
					decodeReply(j, commentObj);
				}

			} else {
				commentObj.replies = null;

			}

		} catch (Exception ex) {
			 ex.printStackTrace();
			commentObj.replies = null;

		}

	}

	private void decodeReply(JsonElement nextReply, Comment commentObj) {
		// TODO Auto-generated method stub
		try {
			JsonObject replyJsonObj = nextReply.getAsJsonObject();
			Reply replyObj = new Reply();
			replyObj.replyId = replyJsonObj.get(Constants.ID).getAsString();
			replyObj.authorId = Long.parseLong(replyJsonObj.get(Constants.FROM).getAsJsonObject().get(Constants.ID).getAsString());
			replyObj.message = replyJsonObj.get(Constants.MESSAGE).getAsString();
			replyObj.likesCount = Integer.parseInt(replyJsonObj.get(Constants.LIKECOUNT).getAsString());

			// Add this reply object to comment object
			commentObj.replies.add(replyObj);
			
			// TODO : Extract the other information about the replier
			User userObj = new User();
			userObj.userId = replyObj.authorId;
			userObj.authority = 5;
			
			// Do not consider the commentor for authority calculation if the reply is given by the 
			// author itself 
			// + 
			// the consider only unique user in the reply
			if(!userIdList.contains(userObj.userId) && commentObj.authorId!=userObj.userId){
				User userObj2 = userMap.get(commentObj.authorId);
				userObj2.authority += userObj.authority;
				userIdList.add(userObj.userId);
			}
			
			if(userMap.get(userObj.userId)!=null){
				userMap.put(userObj.userId, userObj);
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		

	}
	private void AddWeightToSentence() throws JsonIOException, JsonSyntaxException, IOException
	{
		JsonObject jsonObjectBlogLevel = parser.parse(new FileReader("dataset/Blog 1/blogDataset.json")).getAsJsonObject();
		String body = jsonObjectBlogLevel.get("blog").getAsJsonObject().get("body").getAsString();
		//System.out.println(body);
		String[] sentences = body.split("\\.");
		for(int i=0;i<sentences.length;i++)
		{
		//	System.out.println(sentences[i]);
			List<String> words = Tokenizer.tokenize(sentences[i]);
			//for(int j=0;j<words.size();j++)
				//System.out.print(words.get(j) + " ");
			//System.out.println();
			int sum =0;
			for(int j=0;j<words.size();j++)
			{
				if(termMapGlobal.containsKey(words.get(j)))
					sum += 	termMapGlobal.get(words.get(j)).weight;
			}
			Sentence_Weight.put(sentences[i], new Integer(sum/sentences[i].length()));
		}
		int Threshold  = sortMap();
		BufferedWriter writer = null;
		
	    writer = new BufferedWriter( new FileWriter("Summary"));
	    for(int i=0;i<sentences.length;i++)
	    {
			//System.out.println(sentences[i] + "\n **** END ***** \n");
	    	if(Sentence_Weight.get(sentences[i])>=Threshold)
	    		writer.write(sentences[i]+".");
	    }
	
	    writer.close();
		
	}
	private void CreateCluster()
	{	
		/* Initialize first cluster */
		Cluster c1 = new Cluster();
		for (String k : allComments.keySet()) {
			c1.vector =  getCommentVector(allComments.get(k));
			c1.commentIdList = new HashSet<String>();
			c1.commentIdList.add(allComments.get(k).commentId);
			allComments.get(k).clusterId=0;
			break;
		}
		allClusterList.add(c1); 

		// Set Threshold 
		Double THRESHOLD = 0.045; 
		/*More the CosTheta, closer the vectors, 
		 Lower the Threshold, high likelihood of vectors to accomodate in a cluster,
		 hence less will be the number of clusters formed */

		int cluster_id=0;

		for (String k : allComments.keySet()) {
			Comment comment = allComments.get(k);
			Integer n = allClusterList.size();
			Double Max_Value = 0.0;

			for(int i=0; i<n; i++)
			{
				/* Compare each comment with all cluster centroids */
				Double CosTheta = compareCommentVectors((HashMap<String, Double>) allClusterList.get(i).vector,getCommentVector(comment));
				if(CosTheta >= Max_Value) //To select closest centroid
				{
					Max_Value = CosTheta;
					cluster_id = i;
				}
			}
			/*If the distance between comment and cluster is greater than a THRESHOLD(i.e CosTheta >= Threshold),
			it implies that the comment can be included within that cluster,
			else make another cluster.
			 */
			if(Max_Value >= THRESHOLD)
			{
				comment.clusterId = cluster_id;	
				allClusterList.get(cluster_id).commentIdList.add(comment.commentId);
				/* Recalculate centroid in the below two lines. */
				allClusterList.add(cluster_id, MeanCentroid(cluster_id));
				allClusterList.remove(cluster_id+1);
				allComments.put(comment.commentId,comment);
				//System.out.println("Count "  + "Cluster ID : " + allComments.get(k).clusterId );//+ allClusterList.get(cluster_id).commentIdList);
			}
			else
			{
				// form a new cluster
				comment.clusterId=allClusterList.size();
				Cluster c = new Cluster();
				c.vector = (getCommentVector(comment));
				c.commentIdList = new HashSet<String>();
				c.commentIdList.add(comment.commentId);
				allClusterList.add(c);
				allComments.put(comment.commentId,comment);
				//System.out.println("Count new "  + "Cluster ID : " + allComments.get(k).clusterId );//+ allClusterList.get(cluster_id).commentIdList);
			}
		}
		AssignClusterWeight();
		CalculateTermWeight();
	}
	private void AssignClusterWeight()
	{
		Double sum =0.0;
		int div = 0;

		for(int i=0;i<allClusterList.size();i++)
		{
			Cluster c = allClusterList.get(i);
			Set<String> S = new HashSet<String>(c.commentIdList);
			Iterator<String> id = S.iterator();
			while (id.hasNext()) {
				String commentId = id.next();
				int freq = allComments.get(commentId).termsMap.size();
				double sim = compareCommentVectors(getCommentVector(allComments.get(commentId)), (HashMap<String, Double>)c.vector);
				sum += (double)freq*sim;
			}

			for( String k : allComments.keySet())
			{
				div += allComments.get(k).termsMap.size();
			}
			c.weight = (sum/(double)div);
		}

	}
	private double compareCommentVectors( HashMap<String,Double> m1, HashMap<String,Double> m2)
	{
		Double modA = VectorModulus(m1);
		Double modB = VectorModulus(m2);
		Double  sum = 0.0,v1,v2;
		for(String k : m1.keySet())
		{
			if(m2.containsKey(k))
			{
				v1 = m1.get(k);
				v2 = m2.get(k);
				sum += (v1*v2);	
			}
		}
		return (sum / (modA * modB)); //(A.B / (||A||* ||B||))

	}
	private double VectorModulus( HashMap<String,Double> v)
	{
		Double sum = 0.0;
		for(String k : v.keySet())
		{
			sum += v.get(k)*v.get(k);
		}
		return Math.sqrt(sum);	
	}
	private HashMap<String,Double> getCommentVector(Comment commentObj)
	{
		Map<String,Double> VectorMap = new HashMap<String,Double>();
		for (String k : commentObj.termsMap.keySet()) {
			Integer tf = commentObj.termsMap.get(k);
			Double idf = Math.log(allComments.size() / termMapGlobal.get(k).commentIdList.size());
			VectorMap.put(k, (double)tf*idf);
		}
		return (HashMap<String, Double>) VectorMap;
	}
	private Cluster MeanCentroid(int Id)
	{
		Map<String,Double> mean = new HashMap<String,Double>();
		Set<String> IDs =new HashSet<String>(allClusterList.get(Id).commentIdList);

		/* Iterate on all comments within the cluster */
		Iterator<String> id = IDs.iterator();
		while (id.hasNext()) {
			String commentId = id.next();
			Map<String,Double> vec = getCommentVector(allComments.get(commentId));
			for(String j : vec.keySet())
			{
				if(mean.containsKey(j))
				{
					Double val = mean.get(j) + vec.get(j);
					mean.put(j,val);
				}
				else
					mean.put(j, vec.get(j));
			}
		}
		int N = IDs.size();
		Double val=0.0;
		for(String k: mean.keySet())
		{	
			val=mean.get(k)/(double)N;
			mean.put(k,val);
		}
		Cluster c = new Cluster();
		c.vector=mean;
		c.commentIdList = allClusterList.get(Id).commentIdList;
		return c;
	}

	public static void main(String[] args) throws JsonIOException, JsonSyntaxException, IOException {
		XMLParser xmlParser = new XMLParser();
		xmlParser.parseJSONFile();
	//	System.out.println("global map---------------------");
		//printHashMap(xmlParser.termMapGlobal);
		//System.out.println("Total no of words in all comments: " + xmlParser.termMapGlobal.size());
		//System.out.println("Total no of all comments: " + xmlParser.allComments.size());
		xmlParser.CreateCluster();
		xmlParser.AddWeightToSentence();
		
		//printCluster();
	}

}