package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * this class is useful for saving important data of the document, such as length and the text after the parser
 */
public class DocData {
    private int textLength; // number of words in ORIGINAL text
    private ArrayList<String> modifiedText; // consists only from terms in Terms[], no stopwords.
   // terms in the text, no duplicates and stopwords.
    //in each entry, key is a term and val is role:
    // 0 - regular word.
    // 1 - date
    //2 - number (for example 6%, 100 M Dollars, 12.5K...)
    //3 - entity
   private HashMap<String, Byte> terms;

    /**
     * empty constructor
     */
   public DocData (){
       this.modifiedText = new ArrayList<String>();
       this.terms = new HashMap<String,Byte>();
       //when using this constructor dont forget to set original text length
   }

    /**
     * constructor for document after processed in parser
     * @param originalLen the length of the original text
     * @param modifiedText the text after being proccessed in parser
     * @param terms all the unique terms in the doc and their roles.
     */
   public  DocData(int originalLen, ArrayList<String> modifiedText,HashMap<String, Byte> terms ){
       this.textLength = originalLen;
       this.modifiedText=modifiedText;
       this.modifiedText.remove(null);
       this.modifiedText.remove("");
       this.modifiedText.remove(" ");
       this.terms = terms;
   }

    /**
     * insert a term and his roll to the term list of the document
     * @param term
     * @param role
     */
   public void insertTerm(String term, Byte role){
       terms.put(term, role);
   }

    /**
     * return the text length of the document
     * @return
     */
    public int getTextLength() {
        return textLength;
    }

    /**
     * sets the text length of the document
     * @param textLength
     */
    public void setTextLength(int textLength) {
        this.textLength = textLength;
    }

    /**
     * return the text after the changes that been made by the parser
     * @return
     */
    public ArrayList<String> getModifiedText() {
        return modifiedText;
    }

    /**
     *
     * @param modifiedText
     */
    public void setModifiedText(ArrayList<String> modifiedText) {
       modifiedText.remove(null);
        this.modifiedText.clear();
        this.modifiedText.addAll(modifiedText);
    }

    public HashMap<String, Byte> getTerms() {
        return terms;
    }

    public void setTerms( HashMap<String, Byte> terms) {
        this.terms = terms;
    }
}
