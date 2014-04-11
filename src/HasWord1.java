
public class HasWord1 implements edu.stanford.nlp.ling.HasWord {
	
	private String label;
	
	public HasWord1(){
		
		label = new String();
	}
	
	public HasWord1(String label){
		
		this.label = label;
	}
	
	
	@Override
	public String word() {
		return this.label;
	}
	
	@Override
	public void setWord(String arg0) {
			this.label = arg0;
		
		
	}
}
