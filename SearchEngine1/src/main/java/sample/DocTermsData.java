package sample;

import java.util.HashMap;

/**
 * a class that gives us the length of each doc and the terms places and number of appearances in the doc
 */
public class DocTermsData {
    double lenOfDoc;
    HashMap<String, TermPostingData> termsData; //key is term (that is both in query and doc)

    public DocTermsData() {
        termsData = new HashMap<>();
    }

    public DocTermsData(double lenOfDoc) {
        this.lenOfDoc = lenOfDoc;
        termsData = new HashMap<>();
    }

    /**
     *
     * @return the length of the doc
     */
    public double getLenOfDoc() {
        return lenOfDoc;
    }

    /**
     * adds term and the data about it to the main hash nap
     * @param term
     * @param data
     */
    public void addToTermsData(String term, TermPostingData data){
        this.termsData.put(term, data);
    }

    /**
     *
     * @return gets the main hashmap fot the user
     */
    public HashMap<String, TermPostingData> getTermsData() {
        return termsData;
    }

    /**
     *
     * @param lenOfDoc
     * sets the length of a certain doc
     */
    public void setLenOfDoc(double lenOfDoc) {
        this.lenOfDoc = lenOfDoc;
    }
}
