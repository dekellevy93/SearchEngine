package sample;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Word2VecModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WordToVector {
  /* public static void main(String[] args) {
       WordToVector w = new WordToVector();
        w.word2vec("tables");
    }*/
    /**
     * return the 10 most closest words to the term
     * @param term
     * @return
     */
    public List<Searcher.Match> word2vec(String term) {
        List<Searcher.Match> matchList = new ArrayList<>();
        try {
            Word2VecModel model = Word2VecModel.fromTextFile(new File("SearchEngine1\\word2vec.c.output.model.txt"));//need to insert the path to thew JAR
            com.medallia.word2vec.Searcher semanticSearcher = model.forSearch();
            int resultsNumber = 7;

            matchList = semanticSearcher.getMatches(term, resultsNumber);
            for (com.medallia.word2vec.Searcher.Match match : matchList) {
                match.match();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (com.medallia.word2vec.Searcher.UnknownWordException e) {
            //doesnt know the term
        }
        return matchList;
    }
}
