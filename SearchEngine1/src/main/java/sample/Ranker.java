package sample;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Word2VecModel;
import com.sun.xml.internal.bind.v2.TODO;
import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Ranker {
    private final double factorForSemanticWords = 0.01;
    private final double factorForDescNarrWords = 0.14;
    private final double alpha = 0.98;
    private String workPlacePath;
    private boolean semantic;
    private boolean toStem;
    private String query;
    private double avarageLengthOfDoc;
    private double numOfDocsInCorpus;
    private final double k = 0.001;
    private final double b = 0.001;

    private String desc;
    private String narr;
    private HashSet<String> wordsToIgnore;

    /**
     * constructor for ranker
     * @param workPlacePath
     * @param query
     * @param semantic
     * @param toStem
     */
    public Ranker(String workPlacePath, String query, String desc, String narr, boolean semantic, boolean toStem) {
        this.workPlacePath = workPlacePath;
        this.semantic = semantic;
        this.query = query;
        this.toStem = toStem;
        this.desc = desc;
        this.narr = narr;
        this.wordsToIgnore = new HashSet<>();
        wordsToIgnore.add("information");
        wordsToIgnore.add("identify");
        wordsToIgnore.add("document");
        wordsToIgnore.add("documents");
        wordsToIgnore.add("discussing");
        wordsToIgnore.add("relevant");
        wordsToIgnore.add("discuss");
        wordsToIgnore.add("considered");
        wordsToIgnore.add("provide");
        wordsToIgnore.add("minimum");

    }

    /**
     * setter for the avarage Length Of Doc
     * @param avarageLengthOfDoc
     */
    public void setAvarageLengthOfDoc(double avarageLengthOfDoc) {
        this.avarageLengthOfDoc = avarageLengthOfDoc;
    }

    /**
     * setter for the num Of Docs In Corpus
     * @param numOfDocsInCorpus
     */
    public void setNumOfDocsInCorpus(double numOfDocsInCorpus) {
        this.numOfDocsInCorpus = numOfDocsInCorpus;
    }

    /**
     * setter for do we stem or not
     * @param toStem
     */
    public void setToStem(boolean toStem) {
        this.toStem = toStem;
    }

    /**
     * return the 50 best docs that we retrieve for a query
     * @return
     * @throws FileNotFoundException
     */
    public ArrayList<String> rank() throws FileNotFoundException {
        /*if (toStem) {
            Stemmer stmr = new Stemmer();
            stmr.setCurrent(this.query);
            stmr.stem();
            this.query = stmr.getCurrent();
        }*/
        HashSet<String> stopwords = getStopWords();
        HashMap<String, String> toParser = new HashMap<>();
        toParser.put("query", this.query);
        Parser parser = new Parser();
        HashMap<String, DocData> afterParse = parser.parse(toParser, false, stopwords);
        //key is term val is boolean - true if came from query, false if came from narr or desc
        ArrayList<String> queryWordsFromParser = afterParse.get("query").getModifiedText();
        ArrayList<Pair<String, Boolean>> queryWords = new ArrayList<>();
        for(String word: queryWordsFromParser)
            queryWords.add(new Pair<>(word, true));

        HashMap<String,Byte> wordsAndRoles=afterParse.get("query").getTerms();

        if(!this.desc.equals("")) {
            toParser.clear();
            toParser.put("desc", this.desc);
            afterParse = parser.parse(toParser, false, stopwords);
            ArrayList<String> descFromParser = afterParse.get("desc").getModifiedText();
            for(String word: descFromParser)
                queryWords.add(new Pair<>(word, false));

          //  queryWords.addAll(afterParse.get("desc").getModifiedText());
            wordsAndRoles.putAll(afterParse.get("desc").getTerms());
        }

        if(!this.narr.equals("")) {
            toParser.clear();
            toParser.put("narr", this.narr);
            afterParse = parser.parse(toParser, false, stopwords);
            ArrayList<String> narrFromParser = afterParse.get("narr").getModifiedText();
            for(String word: narrFromParser)
                queryWords.add(new Pair<>(word, false));

            //queryWords.addAll(afterParse.get("narr").getModifiedText());
            wordsAndRoles.putAll(afterParse.get("narr").getTerms());
        }

        if(!this.narr.equals("") || !this.desc.equals("")){
            removeNotRelevantWords(queryWords, wordsAndRoles);
        }

        HashMap<String, Double> termFreqInQuery = getTermFreqInQuery(queryWords,wordsAndRoles ); //key is term, val is freq in query
        if(semantic){
            addSemanticWords(termFreqInQuery, wordsAndRoles);
        }
        if(toStem){
            stemWords(wordsAndRoles, termFreqInQuery);
        }
        //Set<String> wordToSearch = termFreqInQuery.keySet();
        HashSet<String> wordsToSearchInDates = new HashSet<>();
        HashSet<String> wordsToSearchInEntity = new HashSet<>();
        HashSet<String> wordsToSearchInNumbers = new HashSet<>();
        HashSet<String> [] wordsToSearchInLetters = new HashSet[26];
        assignWordsToSearch(wordsAndRoles, wordsToSearchInDates, wordsToSearchInEntity,
                wordsToSearchInNumbers, wordsToSearchInLetters);

        HashMap<String, DocTermsData> docAndQueryInCommon = buildDocAndQueryInCommon(wordsToSearchInDates,
                wordsToSearchInEntity, wordsToSearchInNumbers, wordsToSearchInLetters); // key is docNo

        HashMap<String, Double> docsAndGrades = getDocsAndGrades(docAndQueryInCommon,
                termFreqInQuery); // key is docNo val is grade


        ArrayList<String> rankedDocuments = getRankedDocs(docsAndGrades);

        //System.out.println("done");

        return rankedDocuments;
    }



    private ArrayList<String> getRankedDocs(HashMap<String, Double> docsAndGrades) {
        Comparator<Map.Entry<String, Double>> valueComparator = new Comparator<Map.Entry<String,Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
                Double v1 = e1.getValue();
                Double v2 = e2.getValue();
                return v2.compareTo(v1);
            }
        };

        Set<Map.Entry<String, Double>> entries = docsAndGrades.entrySet();
        // Sort method needs a List, so let's first convert Set to List in Java
        List<Map.Entry<String, Double>> listOfEntries = new ArrayList<Map.Entry<String, Double>>(entries);

        // sorting HashMap by values using comparator
        Collections.sort(listOfEntries, valueComparator);
        LinkedHashMap<String, Double> sortedByValue = new LinkedHashMap<String, Double>(listOfEntries.size());

        // copying entries from List to Map, only top 50
        int i = 1;
        for(Map.Entry<String, Double> entry : listOfEntries){
            if(i <= 50) {
                sortedByValue.put(entry.getKey(), entry.getValue());
                i++;
            }
        }

        //HashMap after sorting entries by values

       // Set<Map.Entry<String, Double>> sorted = sortedByValue.entrySet();

        ArrayList<String> rankedDocuments = new ArrayList<>(sortedByValue.keySet());
        return rankedDocuments;

    }

    private HashMap<String, Double> getDocsAndGrades(HashMap<String, DocTermsData> docAndQueryInCommon,
                                                     HashMap<String, Double> termFreqInQuery) {
        //System.out.println(termFreqInQuery.keySet());
        HashMap<String, Double> docsAndGrades = new HashMap<>();
        for(String docNo : docAndQueryInCommon.keySet()){
            double sum = 0;
            DocTermsData docTermsData = docAndQueryInCommon.get(docNo);
            double len = docTermsData.getLenOfDoc();
            HashMap<String, TermPostingData> termsData = docTermsData.getTermsData();
            //System.out.println(termsData.keySet());
            for(String term :  termsData.keySet()){
                double freqInQuery = termFreqInQuery.get(term.toLowerCase());
                TermPostingData termPostingData = termsData.get(term.toLowerCase());
                double freqInDoc = termPostingData.getFreqInDoc();
                double howManyDocs = termPostingData.getHowManyDocs();
                int pos = termPostingData.getPos(); //0 or 1
                sum = sum + ((alpha * ((freqInQuery * (k+1) * freqInDoc) / freqInDoc + k * (1 - b + b * len / avarageLengthOfDoc)))
                        * Math.log(numOfDocsInCorpus/howManyDocs) + (1-alpha) * (1-pos));

                //BM25+
             /*   double idf =  Math.log(numOfDocsInCorpus - howManyDocs + 0.5/howManyDocs + 0.5);

                sum = sum + alpha * ( idf * freqInQuery * ( ((k+1) * freqInDoc) / (freqInDoc + k * (1 - b + b * len / avarageLengthOfDoc)))
                        +  (1-alpha) * (1-pos)) ;*/


            // Math.log(2)
            }
            docsAndGrades.put(docNo, sum);
        }
        return docsAndGrades;
    }

    private HashMap<String, DocTermsData> buildDocAndQueryInCommon(HashSet<String> wordsToSearchInDates,
                                                                   HashSet<String> wordsToSearchInEntity,
                                                                   HashSet<String> wordsToSearchInNumbers,
                                                                   HashSet<String>[] wordsToSearchInLetters) throws FileNotFoundException {
        String stem = "";
        if(toStem){
            stem = "stem";
        }

        HashMap<String, DocTermsData>docAndQueryInCommon = new HashMap<>();

        if(wordsToSearchInDates.size() > 0){
            File datesPostingFile = new File(workPlacePath+"/"+stem+"dates.txt");
            searchInPostingFile(datesPostingFile, wordsToSearchInDates, docAndQueryInCommon);
        }

        if(wordsToSearchInEntity.size() > 0){
            File entityPostingFile = new File(workPlacePath+"/"+stem+"entity.txt");
            searchInPostingFile(entityPostingFile, wordsToSearchInEntity, docAndQueryInCommon);
        }

        if(wordsToSearchInNumbers.size() > 0){
            File numbersPostingFile = new File(workPlacePath+"/"+stem+"numbers.txt");
            searchInPostingFile(numbersPostingFile, wordsToSearchInNumbers, docAndQueryInCommon);
        }

        for(int i = 0; i < wordsToSearchInLetters.length; i++){
            if(wordsToSearchInLetters[i].size() > 0){
                StringBuilder postingName = new StringBuilder();
                char firstChar = (char)(i+97);
                postingName.append(stem);
                postingName.append(firstChar);
                File letterPostingFile = new File(workPlacePath+"/"+postingName+".txt");
                searchInPostingFile(letterPostingFile, wordsToSearchInLetters[i], docAndQueryInCommon);
            }
        }

        //add len of doc to each doc in map
        addLenOfDoc(docAndQueryInCommon);

        return docAndQueryInCommon;
    }

    private void addLenOfDoc(HashMap<String, DocTermsData> docAndQueryInCommon) throws FileNotFoundException {
        String stem = "";
        if(toStem){
            stem = "stem";
        }
        File docIndexPostingFile = new File(workPlacePath+"/"+stem+"DocsIndex.txt");
        Scanner docIndexScan = new Scanner(docIndexPostingFile);
        StringBuilder lineInDocIndex = new StringBuilder("");
        while(docIndexScan.hasNext()){
            lineInDocIndex.append(docIndexScan.nextLine());
            String docNo = getNameFromLine(lineInDocIndex.toString());
            if(docAndQueryInCommon.containsKey(docNo)){ //need to set doc len
                docAndQueryInCommon.get(docNo).setLenOfDoc(getTextLenFromDocIndexLine(lineInDocIndex.toString()));
            }

            lineInDocIndex.setLength(0);
        }
    }

    private void searchInPostingFile(File postingFile,
                                     HashSet<String> wordsToSearch,
                                     HashMap<String, DocTermsData> docAndQueryInCommon) throws FileNotFoundException {

        //we remove found words from the list in order to not scan the entire posting.
        //the postings are sorted so if we pass max letter, we do not continue scanning
      //  System.out.println("search in "+postingFile.getName());
      //  System.out.println("need to search "+wordsToSearch);

        char maxChar = getMaxChar(wordsToSearch);
        FileReader fileReader=new FileReader(postingFile);
        BufferedReader bufferedReaderr = null;
        try {
            bufferedReaderr=new BufferedReader(fileReader);
            String line;
            while((line = bufferedReaderr.readLine()) != null && wordsToSearch.size() > 0 && Character.toLowerCase(line.charAt(1)) <= maxChar){
                String termName = getNameFromLine(line).toLowerCase();
                if(wordsToSearch.contains(termName)){ //this term is also in query
               //     System.out.println("found: "+termName);
                    HashMap<String, TermPosFreq> mapOfDocs = getMapOfDocsOfLine(line);// key is docNo
                    double howManyDocsWithTerm = mapOfDocs.size();
                    for(String docNo : mapOfDocs.keySet()){
                        TermPosFreq tpf = mapOfDocs.get(docNo);
                        TermPostingData termPostingData = new TermPostingData(tpf.getFreqInDoc(),
                                howManyDocsWithTerm, tpf.getPos());
                        if(docAndQueryInCommon.containsKey(docNo)){ //we already added the doc to the map, need to update him
                            docAndQueryInCommon.get(docNo).addToTermsData(termName, termPostingData);
                        }
                        else{ //initiate new doc to map
                            docAndQueryInCommon.put(docNo, new DocTermsData());
                            docAndQueryInCommon.get(docNo).addToTermsData(termName, termPostingData);
                        }
                    }
                    wordsToSearch.remove(termName);
                }
            }
           // System.out.println("finished with file");

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


    }

    private char getMaxChar(HashSet<String> wordsToSearch) {
        char maxChar = 'a';
        for(String word : wordsToSearch){
            char c = Character.toLowerCase(word.charAt(0));
            if(c > maxChar)
                maxChar = c;

        }
        return maxChar;
    }

    private void assignWordsToSearch(HashMap<String, Byte> wordsAndRoles, HashSet<String> wordsToSearchInDates,
                                     HashSet<String> wordsToSearchInEntity, HashSet<String> wordsToSearchInNumbers,
                                     HashSet<String>[] wordsToSearchInLetters) {
        for(int i = 0; i < wordsToSearchInLetters.length; i++){
            wordsToSearchInLetters[i] = new HashSet<>();
        }
        for(String term : wordsAndRoles.keySet()){
            switch (wordsAndRoles.get(term)) {
                case -1:
                    break;
                case 0: // regular word
                    char firstChar = term.charAt(0);
                    int index = (Character.toLowerCase(firstChar)-97);
                    wordsToSearchInLetters[index].add(term.toLowerCase());
                    break;
                case 1: //date
                    wordsToSearchInDates.add(term);
                    break;
                case 2: //number
                    wordsToSearchInNumbers.add(term.toLowerCase());
                    break;
                case 3: //entity
                    wordsToSearchInEntity.add(term.toLowerCase());
                    break;
            }
        }
    }

    private void stemWords(HashMap<String, Byte> wordsAndRoles, HashMap<String, Double> termFreqInQuery) {
        Stemmer stmr;// = new Stemmer();
        HashMap<String, Byte> tempWordsAndRoles = new HashMap<>();
        tempWordsAndRoles.putAll(wordsAndRoles);

        HashMap<String, Double> tempTermFreqInQuery = new HashMap<>();
        tempTermFreqInQuery.putAll(termFreqInQuery);

        for(String term: tempWordsAndRoles.keySet()){
            if(tempWordsAndRoles.get(term) == (byte)0){
                String tempTerm = term;
                double freqInQTemp = termFreqInQuery.get(tempTerm.toLowerCase());
                termFreqInQuery.remove(tempTerm.toLowerCase());
                wordsAndRoles.remove(tempTerm);
                stmr = new Stemmer();
                stmr.setCurrent(tempTerm);
                stmr.stem();
                tempTerm = stmr.getCurrent();
                termFreqInQuery.put(tempTerm.toLowerCase(), freqInQTemp);
                wordsAndRoles.put(tempTerm, (byte)0);
            }
        }
/*
        Set<String> terms = termFreqInQuery.keySet();
        Set<String> tempTerms = new HashSet<>();
        for(String term : terms) {
            String t = term;
            tempTerms.add(t);
        }
        for(String term : tempTerms) {
            if(wordsAndRoles.get(term) == (byte)0) {
                String tempTerm = term;
                double freqInQTemp = termFreqInQuery.get(tempTerm);
                termFreqInQuery.remove(tempTerm);
                wordsAndRoles.remove(tempTerm);
                stmr = new Stemmer();
                stmr.setCurrent(tempTerm);
                stmr.stem();
                tempTerm = stmr.getCurrent();
                termFreqInQuery.put(tempTerm, freqInQTemp);
                wordsAndRoles.put(tempTerm, (byte)0);
            }
        }*/
    }

    private void removeNotRelevantWords(ArrayList<Pair<String, Boolean>> queryWords, HashMap<String, Byte> wordsAndRoles) {
        for(int i = 0; i < queryWords.size(); i++){
            if(wordsToIgnore.contains(queryWords.get(i).getKey().toLowerCase())){
                queryWords.remove(i);
                i--;
            }
        }
        HashMap<String, Byte> tempWordsAndRoles = new HashMap<>();
        for(String word : wordsAndRoles.keySet()){
            if(!wordsToIgnore.contains(word.toLowerCase())){
                tempWordsAndRoles.put(word, wordsAndRoles.get(word));
            }
        }
        wordsAndRoles.clear();
        wordsAndRoles.putAll(tempWordsAndRoles);

    }

    private void addSemanticWords(HashMap<String, Double> termFreqInQuery, HashMap<String, Byte> wordsAndRoles) {
        ArrayList<String> matches = new ArrayList<>();
        WordToVector wordToVector = new WordToVector();
        for(String originalTerm : wordsAndRoles.keySet()){
            String term = originalTerm.toLowerCase();
            if(wordsAndRoles.get(originalTerm) == (byte)0){ //regular word
                List<Searcher.Match> semanticWords = wordToVector.word2vec(term);
                for(Searcher.Match match : semanticWords){
                    if(!match.match().toLowerCase().equals(term)){

                        termFreqInQuery.put(match.match(),
                                match.distance()*termFreqInQuery.get(term)*factorForSemanticWords);

                       // wordsAndRoles.put(match.match(), (byte)0);
                        matches.add(match.match());
                    }
                }
            }
        }
        for(String match : matches){
            wordsAndRoles.put(match, (byte)0);
        }
    }

    private HashMap<String, Double> getTermFreqInQuery(ArrayList<Pair<String, Boolean>> queryWords,
                                                       HashMap<String, Byte> wordsAndRoles) {
        HashMap<String, Double> termFreqInQuery = new HashMap<>();
        for(String term : wordsAndRoles.keySet()){
            double count = 0;
            for(int i = 0; i < queryWords.size(); i++){
                if(term.toLowerCase().equals(queryWords.get(i).getKey().toLowerCase()))
                    if(queryWords.get(i).getValue() == true)
                         count ++;
                    else
                        count += factorForDescNarrWords;
            }
            termFreqInQuery.put(term.toLowerCase(), count);
        }

        return termFreqInQuery;
    }

    /**
     * get the stop words
     * @return
     * @throws FileNotFoundException
     */
    private HashSet<String> getStopWords() throws FileNotFoundException {
        HashSet<String> stopwords = new HashSet<>();
        File[] files = new File(workPlacePath).listFiles();
        for (File file : files) {
            if(file.getName().equals("stop_words.txt")){
                //stopwords
                Scanner scan = new Scanner(file);
                while (scan.hasNext()) {
                    stopwords.add(scan.next());
                }
                scan.close();
            }

        }
        return stopwords;
    }

    private String getNameFromLine(String line){
        return line.substring(line.indexOf("<")+1, line.indexOf(">"));
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

    private int getTextLenFromDocIndexLine(String line) {
        String temp = line.substring(line.indexOf(">#")+2);
        String temp2 = temp.substring(temp.indexOf("#")+1);
        return Integer.parseInt(temp2.substring(temp2.indexOf("#")+1));
    }
}
