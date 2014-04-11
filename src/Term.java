
import java.util.HashSet;
import java.util.Set;


public class Term {
	public Set<String> commentIdList;
	public Integer weight;
	public Integer idf;

	
	public Term (String commentId){
		this.commentIdList = new HashSet<String>();
		this.commentIdList.add(commentId);
		this.weight = 0;
		
	}
}