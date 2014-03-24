import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Tokenizer {

	static HashSet<String> stopWords;
	static stemmerCode stem;

	// Read the stop words only once from the sw file
	static {
		try {
			String stw;
			stopWords = new HashSet<String>();
			stem = new stemmerCode();
			BufferedReader br = new BufferedReader(new FileReader("sw"));
			while ((stw = br.readLine()) != null) {
				// System.out.println("stw-->"+stw);
				stopWords.add(stw);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void tokenize(String commentString, Map<String, Term> termGlobalMap, Comment commentObj) {
		Map<String, Integer> termsMap = new HashMap<String, Integer>();
		Integer frequency;
		String commentID = "";
		Term termObj;

		if (commentString != null && commentString.trim() != "") {
			if (commentObj != null) {
				commentID = commentObj.commentId;
			}

			for (String token : commentString.toLowerCase().split("[^a-z]")) {
				if (!token.trim().equals("") && !stopWords.contains(token)) {
					token = stem.stem(token);

					// add the term to global term list
					termObj = termGlobalMap.get(token);
					if (termObj != null) {
						termObj.commentIdList.add(commentID);
					} else {
						termGlobalMap.put(token, new Term(commentID));
					}

					// update the frequency of the term in the map
					frequency = termsMap.get(token);
					if (frequency != null) {
						termsMap.put(token, ++frequency);
					} else {
						termsMap.put(token, new Integer(1));
					}

				}
			}

			commentObj.termsMap = termsMap;

		}

	}

}