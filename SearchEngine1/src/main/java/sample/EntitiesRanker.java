package sample;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * used when a user wants the top 5 entities - rank them and returns them
 */
public class EntitiesRanker {
    private static final double alpha = 0.92;
    private String workPlacePath;
    private String stem;
    public EntitiesRanker ( String workPlacePath, String stem){
        this.workPlacePath = workPlacePath;
        this.stem =stem;
    }

    /**
     *
     * @param docs
     * @return arary list of the ranks of the entities in the docs
     * @throws FileNotFoundException
     */
    public ArrayList<EntGradesOfDoc> getEntGradesOfDocs(ArrayList<String> docs) throws FileNotFoundException {
        double[] totalNumberOfDocsArr = new double[1];
        HashSet<String> docsNoSet = new HashSet<String>(docs);
        HashMap<String, Integer> lenOfDocs = this.getMapOfDocsAndTextLen(docs, totalNumberOfDocsArr); //key is docNo
        double totalNumberOfDocs = totalNumberOfDocsArr[0];
        HashMap<String, EntGradesOfDoc> entGradesOfDocsHash = new HashMap<>();//key is docNo
        ArrayList<EntGradesOfDoc> entGradesOfDocs = new ArrayList<>();

        for(String docNo: docs){
            entGradesOfDocsHash.put(docNo, new EntGradesOfDoc(docNo));  //iniate entGradesOfDocsHash
        }

        calculateGrades(entGradesOfDocsHash, lenOfDocs, totalNumberOfDocs);

        for(int i = 0; i < docs.size(); i++){
            String docNo = docs.get(i);
            entGradesOfDocsHash.get(docNo).sortGrades();

            entGradesOfDocs.add(entGradesOfDocsHash.get(docNo));
        }
        return entGradesOfDocs;
    }

    private void calculateGrades(HashMap<String, EntGradesOfDoc> entGradesOfDocsHash,
                                 HashMap<String, Integer> lenOfDocs,
                                 double totalNumberOfDocs ) throws FileNotFoundException {
        FileReader fileReader=new FileReader(new File(workPlacePath+"/"+stem+"entity.txt"));
        BufferedReader bufferedReaderr = null;
        try {
            bufferedReaderr=new BufferedReader(fileReader);
            String lineInentity;
            while((lineInentity = bufferedReaderr.readLine()) != null) {
                String term = getNameFromLine(lineInentity);
                HashMap<String, TermPosFreq>  mapOfDocsOfTerm = getMapOfDocsOfLine(lineInentity);// key is docNo
                double countHowManyTimesPosIs0 = countHowManyTimesPosIs0(mapOfDocsOfTerm);
                for(String docNo : mapOfDocsOfTerm.keySet()){
                    if(entGradesOfDocsHash.containsKey(docNo)) {
                        TermPosFreq tpf = mapOfDocsOfTerm.get(docNo);
                        int freq = tpf.getFreqInDoc();
                        int lenOfDoc = lenOfDocs.get(docNo);
                        double numberOfDocsWithTermInIt = (double) mapOfDocsOfTerm.size();

                        double tf = (double) ((double) freq / (double) lenOfDoc);
                        double temp = totalNumberOfDocs / numberOfDocsWithTermInIt;

                        double idf = Math.log(temp) / Math.log(2);

                        double madad = countHowManyTimesPosIs0 / numberOfDocsWithTermInIt;

                        double grade = alpha * tf * idf + (1.0 - alpha) * madad;
                        entGradesOfDocsHash.get(docNo).addGrade(term, grade);
                    }
                }

            }
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

      /*  File entityPostingFile = new File(workPlacePath+"/"+stem+"entity.txt");
        Scanner entityScan = new Scanner(entityPostingFile);
        StringBuilder lineInentity = new StringBuilder("");
        while(entityScan.hasNext()){
            lineInentity.append(entityScan.nextLine());
            String term = getNameFromLine(lineInentity.toString());
            HashMap<String, TermPosFreq>  mapOfDocsOfTerm = getMapOfDocsOfLine(lineInentity.toString());// key is docNo
            double countHowManyTimesPosIs0 = countHowManyTimesPosIs0(mapOfDocsOfTerm);
            for(String docNo : mapOfDocsOfTerm.keySet()){
                if(entGradesOfDocsHash.containsKey(docNo)) {
                    TermPosFreq tpf = mapOfDocsOfTerm.get(docNo);
                    int freq = tpf.getFreqInDoc();
                    int lenOfDoc = lenOfDocs.get(docNo);
                    double numberOfDocsWithTermInIt = (double) mapOfDocsOfTerm.size();

                    double tf = (double) ((double) freq / (double) lenOfDoc);
                    double temp = totalNumberOfDocs / numberOfDocsWithTermInIt;

                    double idf = Math.log(temp) / Math.log(2);

                    double madad = countHowManyTimesPosIs0 / numberOfDocsWithTermInIt;

                    double grade = alpha * tf * idf + (1.0 - alpha) * madad;
                    entGradesOfDocsHash.get(docNo).addGrade(term, grade);
                }
            }
            lineInentity.setLength(0);
        }*/
    }

    private double countHowManyTimesPosIs0(HashMap<String, TermPosFreq> mapOfDocsOfTerm) {
        double counter = 0;
        for(String docNo : mapOfDocsOfTerm.keySet()){
            TermPosFreq tpf = mapOfDocsOfTerm.get(docNo);
            byte pos = tpf.getPos();
            if(pos == 0)
                counter++;
        }
        return counter;
    }

    private HashMap<String, TermPosFreq> getMapOfDocsOfLine(String line) {
        HashMap<String, TermPosFreq> mapOfDocsOfLine = new HashMap<>();// key is docNo
        int currIndexOfLine = 0;
        while(line.indexOf("&", currIndexOfLine) != -1){
            //line = line.substring(line.indexOf("&")+1);
            currIndexOfLine = line.indexOf("&", currIndexOfLine)+1;
            // String docNo = line.substring(0, line.indexOf("#"));
            String docNo = line.substring(currIndexOfLine, line.indexOf("#",currIndexOfLine));

            //  line = line.substring(line.indexOf("#") + 1);
            currIndexOfLine = line.indexOf("#",currIndexOfLine) + 1;

            Integer poss = Integer.parseInt(line.substring(currIndexOfLine, line.indexOf("#",currIndexOfLine)));
            byte pos = poss.byteValue();
            currIndexOfLine = line.indexOf("#",currIndexOfLine) + 1;
            int freqIndex = line.indexOf("&", currIndexOfLine);
            int freq;
            if (freqIndex == -1) // end of line
                freq = Integer.parseInt(line.substring(currIndexOfLine));
            else
                freq = Integer.parseInt(line.substring(currIndexOfLine, freqIndex));
            TermPosFreq tpf = new TermPosFreq(pos, freq);

            mapOfDocsOfLine.put(docNo, tpf);

          /*  Integer poss = Integer.parseInt(line.substring(0, line.indexOf("#")));
            byte pos = poss.byteValue();
            line = line.substring(line.indexOf("#") + 1);
            int freqIndex = line.indexOf("&");
            int freq;
            if (freqIndex == -1) // end of line
                freq = Integer.parseInt(line);
            else
                freq = Integer.parseInt(line.substring(0, freqIndex));
            TermPosFreq tpf = new TermPosFreq(pos, freq);

            mapOfDocsOfLine.put(docNo, tpf);*/

        }

        return mapOfDocsOfLine;
    }
        /*HashMap<String, TermPosFreq> mapOfDocsOfLine = new HashMap<>();// key is docNo
        while(line.indexOf("&") != -1){
            line = line.substring(line.indexOf("&")+1);
            String docNo = line.substring(0, line.indexOf("#"));

            line = line.substring(line.indexOf("#") + 1);
            Integer poss = Integer.parseInt(line.substring(0, line.indexOf("#")));
            byte pos = poss.byteValue();
            line = line.substring(line.indexOf("#") + 1);
            int freqIndex = line.indexOf("&");
            int freq;
            if (freqIndex == -1) // end of line
                freq = Integer.parseInt(line);
            else
                freq = Integer.parseInt(line.substring(0, freqIndex));
            TermPosFreq tpf = new TermPosFreq(pos, freq);

            mapOfDocsOfLine.put(docNo, tpf);

        }

        return mapOfDocsOfLine;
    }*/


    private HashMap<String, Integer> getMapOfDocsAndTextLen(ArrayList<String> docs, double [] totalNumberOfDocs) throws FileNotFoundException {
        HashSet<String> docsNo = new HashSet<String>(docs);
        HashMap<String, Integer> lenOfDocs = new HashMap<>();
        File docIndexPostingFile = new File(workPlacePath+"/"+stem+"DocsIndex.txt");
        Scanner docIndexScan = new Scanner(docIndexPostingFile);
        StringBuilder lineInDocIndex = new StringBuilder("");
        while(docIndexScan.hasNext()){
            lineInDocIndex.append(docIndexScan.nextLine());
            totalNumberOfDocs[0]++; // count how many docs were indexed
            String name = getNameFromLine(lineInDocIndex.toString());
            if(docsNo.contains(name)) {
                int textLen = getTextLenFromDocIndexLine(lineInDocIndex.toString());
                lenOfDocs.put(name, textLen);
            }
            lineInDocIndex.setLength(0);
        }

        return lenOfDocs;
    }

    private int getTextLenFromDocIndexLine(String line) {
        String temp = line.substring(line.indexOf(">#")+2);
        String temp2 = temp.substring(temp.indexOf("#")+1);
        return Integer.parseInt(temp2.substring(temp2.indexOf("#")+1));
    }


    private String getNameFromLine(String line){
        return line.substring(line.indexOf("<")+1, line.indexOf(">"));
    }
}
