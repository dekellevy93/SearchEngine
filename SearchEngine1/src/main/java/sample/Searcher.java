package sample;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class Searcher {
    private String workPlacePath;
    private String stem;
    private boolean semantic;// = false;
    private String query;
    private String qNum;
    private static int resultsFileNum = 1;
    private TreeMap<String, ArrayList<String>> qNumbersAndResults; //key is q num, val is results

    private String desc;
    private String narr;
    private double timeExec;

    public Searcher( String workPlacePath, String query, boolean toStem, boolean semantic) {
        this.workPlacePath = workPlacePath;
        this.stem = "";
        if (toStem)
            this.stem = "stem";
        this.query = query;
        this.semantic = semantic;
        qNumbersAndResults = new TreeMap<>();
    }


    public Searcher() {
        qNumbersAndResults = new TreeMap<>();
        desc = "";
        narr = "";
    }

    public ArrayList<String> search() throws IOException {
        long startTime = System.nanoTime();
        boolean stemToRank = false;
        if(stem.equals("stem")){
            stemToRank = true;
        }
        Ranker ranker = new Ranker(workPlacePath, query, desc, narr, semantic,stemToRank);
        ranker.setAvarageLengthOfDoc(getAvglenOfDoc());
        ranker.setNumOfDocsInCorpus(getNumOfDocsInCorpus());
        ArrayList<String> searchResults = ranker.rank();

        qNumbersAndResults.put(qNum, searchResults); // add results to map of results
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        this.timeExec = totalTime/1000000000.0;
        return searchResults;
    }

    public double getTimeExec() {
        return timeExec;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setNarr(String narr) {
        this.narr = narr;
    }

    public void resetResults(){
        this.qNumbersAndResults.clear();
    }

    /**
     * Save results to give destination. The results are in TREC_EVAL format
     * @param pathToSave destination to save to
     */
    public void saveResults(String pathToSave) throws IOException {
        StringBuilder results = new StringBuilder();
        for(String qNum : qNumbersAndResults.keySet()){ //for each query
            int rank = 1; //redundant
            for(String docNum : qNumbersAndResults.get(qNum)){ //for each result to the query
                results.append(qNum +" 0 "+docNum+" "+rank+" 42.38 mt\n");
                rank++;
            }
        }
        //write results to file
        FileWriter fr = new FileWriter(new File(pathToSave+"/results"+resultsFileNum+".txt"), true);
        resultsFileNum++;
        BufferedWriter bWriter = new BufferedWriter(fr);
        bWriter.write(results.toString());
        bWriter.close();
    }

    public TreeMap<String, ArrayList<String>> getqNumbersAndResults() {
        return qNumbersAndResults;
    }

    public void setStem(String stem) {
        this.stem = stem;
    }

    public void setSemantic(boolean semantic) {
        this.semantic = semantic;
    }

    public void setWorkPlacePath(String workPlacePath) {
        this.workPlacePath = workPlacePath;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setqNum(String qNum) {
        this.qNum = qNum;
    }

    public String getqNum() {
        return qNum;
    }

    /**
     * @param docs doc codes, the list is already sorted by relevance of docs. size is max 50.
     * @return for each doc code, top 5 of entities with grades. the order of docs is kept.
     */
    public ArrayList<EntGradesOfDoc> getEntGradesOfDocs(ArrayList<String> docs) throws FileNotFoundException {
        EntitiesRanker entitiesRanker = new EntitiesRanker(workPlacePath, stem);
        return entitiesRanker.getEntGradesOfDocs(docs);
        //to get Set<Entry<String, Double>> top5Grades call arr.get(i).getTop5Grades()
    }

    public double getAvglenOfDoc() throws FileNotFoundException {
        double counter = 0;
        double sum = 0;
        File docIndexPostingFile = new File(workPlacePath + "/" + stem + "DocsIndex.txt");
        FileReader fileReader=new FileReader(docIndexPostingFile);
        BufferedReader bufferedReaderr = null;
        try {
            bufferedReaderr=new BufferedReader(fileReader);
            String line;
            while((line = bufferedReaderr.readLine()) != null) {
                counter++;
                sum += getTextLenFromDocIndexLine(line);
            }
            return sum / counter;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReaderr != null) {
                try {
                    bufferedReaderr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
       /* Scanner docIndexScan = new Scanner(docIndexPostingFile);
        StringBuilder lineInDocIndex = new StringBuilder("");
        while (docIndexScan.hasNext()) {
            lineInDocIndex.append(docIndexScan.nextLine());
            counter++;
            sum += getTextLenFromDocIndexLine(lineInDocIndex.toString());
            lineInDocIndex.setLength(0);
        }
        return sum / counter;*/
    }

    public double getNumOfDocsInCorpus() throws FileNotFoundException {
        double counter = 0;
        File docIndexPostingFile = new File(workPlacePath + "/" + stem + "DocsIndex.txt");
        FileReader fileReader=new FileReader(docIndexPostingFile);
        BufferedReader bufferedReaderr = null;
        try {
            bufferedReaderr=new BufferedReader(fileReader);
           // String line;
            while((bufferedReaderr.readLine()) != null) {
                counter++;
            }
            return counter;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReaderr != null) {
                try {
                    bufferedReaderr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       /* Scanner docIndexScan = new Scanner(docIndexPostingFile);
        while (docIndexScan.hasNext()) {
            docIndexScan.nextLine();
            counter++; // count how many docs were indexed
        }*/
        return -1;
    }

    private int getTextLenFromDocIndexLine(String line) {
        String temp = line.substring(line.indexOf(">#") + 2);
        String temp2 = temp.substring(temp.indexOf("#") + 1);
        return Integer.parseInt(temp2.substring(temp2.indexOf("#") + 1));
    }

}
