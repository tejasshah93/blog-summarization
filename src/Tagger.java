import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/*
 * 
 * 
 * 	NN Noun, singular or mass
 NNS Noun, plural
 NNP Proper noun, singular
 NNPS Proper noun, plural

 * 
 * 
 * */
public class Tagger {
	
	private static  List<String> namedEntitySet = new ArrayList(Arrays.asList(Constants.NN, Constants.NNS, Constants.NNP, Constants.NNPS));

	public static int countNamedEntity(String commment) {
		List<HasWord> sentence = new ArrayList<HasWord>();
		MaxentTagger tagger = new MaxentTagger("models/wsj-0-18-bidirectional-nodistsim.tagger");
	  
	
		String[] commmentArray = commment.split(" ");

		for (String s: commmentArray) {
			HasWord h = new HasWord1(s);
			sentence.add(h);
		}

		ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);

		int count = 0;
		for (TaggedWord t : tSentence) {
			if (namedEntitySet.contains(t.tag())) {
				count++;
			}
		}
		//System.out.println(Sentence.listToString(tSentence, false));
		
		//System.out.println("---->>"+count);
		return count;
		

	}
}
