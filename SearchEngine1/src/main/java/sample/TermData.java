package sample;

import java.util.HashMap;

public class TermData {
    /**
     * info about a term in the corpus.
     * totalTFInFolder: total frequency of the term in the corpus.
     * mapOfDocs: info about the docs that contains this term.
     * key of the map is doc.No, value is TermPosFreq field.
     */
    private int totalTFInFolder;
    private HashMap<String, TermPosFreq> mapOfDocs; //key is docNo value is pos and freq

    public TermData(int totalTFInFolder) {
        this.totalTFInFolder = totalTFInFolder;
        this.mapOfDocs = new HashMap<String,TermPosFreq>();
    }

    /**
     *
      * @param td
     * copy constructor - deep copy of anouther termData
     */
    public TermData(TermData td) {
        this.mapOfDocs = new HashMap<>();
        HashMap<String, TermPosFreq> tdMapOfDocs = td.getMapOfDocs();
        for(String key: tdMapOfDocs.keySet()){
            this.mapOfDocs.put(key, tdMapOfDocs.get(key));
        }
        this.totalTFInFolder = td.getTotalTFInFolder();

    }

    /**
     *
     * @return total frequency of the term in the corpus
     */
    public int getTotalTFInFolder() {
        return totalTFInFolder;
    }

    /**
     *
     * @param totalTFInFolder
     * set the total frequency of the term in the corpus
     */
    public void setTotalTFInFolder(int totalTFInFolder) {
        this.totalTFInFolder = totalTFInFolder;
    }

    /**
     * increase the total frequency of the term in the corpus by one
     */
    public void incBy1TotalFreqInFolder(){
        this.totalTFInFolder ++;
    }

    /**
     *
     * @return the info about the docs that contains the specific term.
     */
    public HashMap<String, TermPosFreq> getMapOfDocs() {
        return mapOfDocs;
    }

    /**
     *
     * @param mapOfDocs
     * sets the info about the docs that contains the specific term.
     */
    public void setMapOfDocs(HashMap<String, TermPosFreq> mapOfDocs) {
        this.mapOfDocs = mapOfDocs;
    }

    /**
     *
     * @param docNo
     * @param pos
     * @param freqInDoc
     * add term frequency and place of a specific term to the map of docs
     */
    public void addToMapOfDocs(String docNo, byte pos, int freqInDoc){
        this.mapOfDocs.put(docNo, new TermPosFreq(pos, freqInDoc));
    }

    /**
     *
     * @param docNo
     * increase the term frequency in a specific doc by one
     */
    public void add1ToFreqInDoc(String docNo){
        TermPosFreq tpf =  mapOfDocs.get(docNo);
        tpf.add1ToFreqInDoc();
        this.mapOfDocs.replace(docNo, tpf);
    }

    /**
     *
     * @param docNo
     * @return the term frequency of a specific term in a doc
     */
    public int getTfInDoc(String docNo){
        return mapOfDocs.get(docNo).getFreqInDoc();
    }

    /**
     *
     * @return get the number of docs indexed
     */
    public int getHowManyDocs(){
        return mapOfDocs.size();
    }

    /**
     *
     * @param docNo
     * @param tpf
     * add a term to a map of a specific dod
     */
    public void addToMapOfDocs(String docNo, TermPosFreq tpf){
        this.mapOfDocs.put(docNo, tpf);
    }

    /**
     *
     * @return prints the map of a specific doc
     */
    public String mapOfDocsToString(){
        StringBuilder str = new StringBuilder("");
        for(String key: mapOfDocs.keySet()){
            TermPosFreq tpf = mapOfDocs.get(key);
            str.append("&"+key+"#"+tpf.getPos()+"#"+tpf.getFreqInDoc());
        }
        return str.toString();
    }

    /**
     *
     * @return prints the map of a specific term (where it appears and how many times)
     */
    public String termDataToString(){ //whole line except the term itself
        StringBuilder str = new StringBuilder("");
        str.append(totalTFInFolder+"*"+getHowManyDocs()+mapOfDocsToString());
        return str.toString();
    }
}
