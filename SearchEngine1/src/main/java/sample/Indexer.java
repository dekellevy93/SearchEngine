package sample;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Indexer{

    //BufferedWriter out = new BufferedWriter(new FileWriter(datesPosting));
    //private boolean alreadyDidIndexBefore = false; // do not create postings for numbers, dates, entities if we already
    // run the index before (with/without stem)
    private int sizeOfDictionary = 0;
    private int numOfIndexedDocs = 0;
    private double totalTimeToBuildIndex = 0;

    /**
     * returns how man docs were indexed
     * @return
     */
    public int getNumOfIndexedDocs() {
        return numOfIndexedDocs;
    }

    public double getTotalTimeToBuildIndex() {
        return totalTimeToBuildIndex;
    }

    /**
     * creates posint files
     * @param corpusPath path to corpus
     * @param workPlacePath path to posting files directory
     * @param toStem whether to stem or not
     * @throws IOException
     */
    public void toIndex (String corpusPath, String workPlacePath, boolean toStem) throws IOException {
        long startTime = System.nanoTime();

        this.sizeOfDictionary = 0;
        numOfIndexedDocs = 0;
        HashSet<Character> wantedLetters = new HashSet<Character>();
        wantedLetters.add('a');
        wantedLetters.add('b');
        wantedLetters.add('c');
        wantedLetters.add('d');
        wantedLetters.add('e');
        wantedLetters.add('f');
        wantedLetters.add('i');
        wantedLetters.add('l');
        wantedLetters.add('m');
        wantedLetters.add('p');
        wantedLetters.add('r');
        wantedLetters.add('s');
        wantedLetters.add('t');


        initiatePostingFiles(toStem, workPlacePath, wantedLetters);

        File[] files = new File(corpusPath).listFiles();
        HashSet<String> stopwords = createStopwords(files, workPlacePath);

        Parser parser = new Parser();
        parser.setStopwords(stopwords);

        final int threshold = 10; // every 10 folders - write to posting files
        int currThreshhold = 0;
        final int entityNumberThreshold = 120; // every 122 folders create new entity and numbers posting file
        final int wordThreshold = 150; // every 150 folders create new letter posting files, for the followin letters;
        //a, b, c, d, e, f,i,  l, m, p, r, s, t
        //0, 1, 2, 3, 5, 11, 12, 14, 16, 17, 18;
        //char [] wantedLetters = new char [] {'a', 'b', 'c', 'd', 'f', 'l', 'm', 'p', 'r', 's', 't'};




        int currEtityNumberThreshold = 0;
        int currEntityNumberPostingFile = 1;

        int currWordThreshold = 0;
        int currWordPostingFile = 1;

        TreeMap<String, TermData> dateDic = new TreeMap<>();
        TreeMap<String, TermData> numberDic = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        TreeMap<String, TermData> entityDic = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        TreeMap<String, TermData> a_zWordsDic [] = new TreeMap[26];
        HashMap<String, DocInFolderData> docsDataIndex = new HashMap<>(); //key is docNo
        intializeWordArrMap(a_zWordsDic);

        String stem ="";
        if(toStem)
            stem = "stem"; // for making files with or without stemming

        for (File bigFolder : files) {
            if (bigFolder.isDirectory()) { //corpus, the entire docs are inside this folder
                File[] corpuseFolders = bigFolder.listFiles(); //1800 curpuse folders
                for (File folder : corpuseFolders) {

                    currThreshhold++;
                    currEtityNumberThreshold++;
                    currWordThreshold++;
                    //System.out.println("Directory: " + folder.getName());
                    HashMap<String, DocData> folderInfo = parser.parse(corpusPath,
                            folder.listFiles()[0], toStem);
                  //  System.out.println("Directory: "+folder.getName()+" size: "+folderInfo.size());


                    for (String docNo : folderInfo.keySet()) {
                        DocData docData = folderInfo.get(docNo);
                        // System.out.println("docNo in indexer "+docNo);
                        HashMap<String, Byte> termUniqes = docData.getTerms(); // uniqe terms and their role
                        //  System.out.println("term uniq size is "+termUniqes.size());
                        ArrayList<String> modifiedText = docData.getModifiedText();
                        HashMap<String, Integer> termFreq = new HashMap<>(); // coonsist of uniqe terms and their freq
                        int modifiedTextSize = modifiedText.size();
                        DocInFolderData docInFolderData = new DocInFolderData(termUniqes.size(), modifiedTextSize);
                        int maxTF = 0; // later assign to doc map
                        String termInModText;
                        for (int i = 0; i < modifiedTextSize; i++) {
                            termInModText = (modifiedText.get(i));
                            if (termUniqes.containsKey(termInModText.toLowerCase())
                                    || termUniqes.containsKey(termInModText.toUpperCase())
                                    || termUniqes.containsKey(termInModText)) {
                                byte whereFoundFirst = 1;// 0 if first found at the first half of the text, else 1
                                boolean isFrstMatch = false;
                                if (!termFreq.containsKey(termInModText)) {// first time we found this term in the doc
                                    isFrstMatch = true; // first time we found this term
                                    if (modifiedTextSize / (i + 1) > 1) {
                                        whereFoundFirst = 0;// else it is 1
                                    }
                                    termFreq.put(termInModText.toString(), 1);
                                }
                                int tfOfTermInDoc = 0;

                                //if(!termUniqes.containsKey(termInModText.toString())){
                                //    break;
                                // }
                                byte termRole = -1;
                                if (!termUniqes.containsKey(termInModText)) {
                                    if (termUniqes.containsKey(termInModText.toLowerCase())) {
                                        termRole = termUniqes.get(termInModText.toLowerCase());
                                        termInModText = termInModText.toLowerCase();
                                    } else if (termUniqes.containsKey(termInModText.toUpperCase())) {
                                        termRole = termUniqes.get(termInModText.toUpperCase());
                                        termInModText = termInModText.toUpperCase();
                                    }
                                } else {
                                    termRole = termUniqes.get(termInModText);
                                }

                                switch (termRole) {
                                    case -1:
                                        break;
                                    case 0: // regular word
                                        //   System.out.println("Doc: "+docNo +" regular word: " + termInModText);
                                        tfOfTermInDoc = termIsWord(termInModText, docNo, whereFoundFirst, a_zWordsDic);
                                        break;
                                    case 1: //date
                                        //  System.out.println("date: "+termInModText +" doc no "+ docNo);
                                        termIsDateOrNumberOrEntity(termInModText, docNo, whereFoundFirst, dateDic);
                                        tfOfTermInDoc = dateDic.get(termInModText).getTfInDoc(docNo);
                                        break;
                                    case 2: //number
                                        termIsDateOrNumberOrEntity(termInModText, docNo, whereFoundFirst, numberDic);
                                        tfOfTermInDoc = numberDic.get(termInModText).getTfInDoc(docNo);
                                        break;
                                    case 3: //entity
                                        termIsDateOrNumberOrEntity(termInModText, docNo, whereFoundFirst, entityDic);
                                        tfOfTermInDoc = entityDic.get(termInModText).getTfInDoc(docNo);
                                        break;
                                }
                                if (tfOfTermInDoc > maxTF)
                                    maxTF = tfOfTermInDoc;
                            }

                        }


                        docInFolderData.setMaxTF(maxTF);
                        docsDataIndex.put(docNo, docInFolderData);

                    }//for each doc in folder

                    if (currThreshhold == threshold) {
                    /*int countSizes = 0; //debug
                    countSizes += dateDic.size() + numberDic.size() + entityDic.size();
                    for(int i = 0; i < a_zWordsDic.length; i++)
                        countSizes += a_zWordsDic[i].size();
                    System.out.println("total map sizes: "+countSizes);*/

                        createPostingFiles(docsDataIndex, dateDic, numberDic, entityDic, a_zWordsDic, currEntityNumberPostingFile,
                                toStem, workPlacePath, currWordPostingFile, wantedLetters);
                        resetTreeMaps(docsDataIndex, dateDic, numberDic, entityDic, a_zWordsDic);
                        currThreshhold = 0;


                    }

                    if (currEtityNumberThreshold == entityNumberThreshold) { // need to create another posting file for entity
                        currEntityNumberPostingFile++;

                        File anotherEntityNumberPostingFile = new File(workPlacePath + "/" +stem+"entity" + currEntityNumberPostingFile + ".txt");
                        anotherEntityNumberPostingFile.createNewFile();

                        anotherEntityNumberPostingFile = new File(workPlacePath +  "/" +stem+"numbers" + currEntityNumberPostingFile + ".txt");
                        anotherEntityNumberPostingFile.createNewFile();

                        currEtityNumberThreshold = 0;
                    }

                    if (currWordThreshold == wordThreshold) {// need to create more posting files for words
                        currWordPostingFile++;
                        for (char c : wantedLetters) {
                            StringBuilder postingName = new StringBuilder();
                            postingName.append(stem);
                            postingName.append(c);
                            postingName.append(currWordPostingFile);
                            File postingFile = new File(workPlacePath + "/" + postingName + ".txt");
                            postingFile.createNewFile();
                            //postingName.setLength(0);

                        }
                        currWordThreshold = 0;
                    }
       /*         System.out.println("folder info: ");
                for(String key : docsDataIndex.keySet()){
                    DocInFolderData td = docsDataIndex.get(key);
                    System.out.println(key+":");
                    System.out.println(td.toString());
                }
                System.out.println("*****************************************************************************");
                System.out.println("Dates: ");
                for(String key : dateDic.keySet()){
                    TermData td = dateDic.get(key);
                    System.out.println(key+":");
                    HashMap<String, TermPosFreq> mapOfDocs = td.getMapOfDocs();
                    for(String doc : mapOfDocs.keySet()){
                        TermPosFreq tpf =  mapOfDocs.get(doc);
                        System.out.println(doc+": pos: "+tpf.getPos()+" freq: "+tpf.getFreqInDoc());
                    }
                }*/
             /*   System.out.println("*****************************************************************************");
                System.out.println("\nnumbers: ");
                ArrayList<String> keysOfdateDic = new ArrayList<String>(dateDic.keySet());
                System.out.println(keysOfdateDic);*/
                /*for(String key : numberDic.keySet()){
                    TermData td = numberDic.get(key);
                    System.out.println(key+":");
                    HashMap<String, TermPosFreq> mapOfDocs = td.getMapOfDocs();
                    for(String doc : mapOfDocs.keySet()){
                        TermPosFreq tpf =  mapOfDocs.get(doc);
                        System.out.println(doc+": pos: "+tpf.getPos()+" freq: "+tpf.getFreqInDoc());
                    }
                }*/
              /*  System.out.println("*****************************************************************************");
                System.out.println("\nentities: ");
                for(String key : entityDic.keySet()) {
                    TermData td = entityDic.get(key);
                    System.out.println(key + ":");
                    HashMap<String, TermPosFreq> mapOfDocs = td.getMapOfDocs();
                    for (String doc : mapOfDocs.keySet()) {
                        TermPosFreq tpf = mapOfDocs.get(doc);
                        System.out.println(doc + ": pos: " + tpf.getPos() + " freq: " + tpf.getFreqInDoc());
                    }
                }*/
              /*  System.out.println("*****************************************************************************");
                System.out.println("\nletter B: ");
                for(String key : a_zWordsDic[1].keySet()) {
                    TermData td =  a_zWordsDic[1].get(key);
                    System.out.println(key + ":");
                    HashMap<String, TermPosFreq> mapOfDocs = td.getMapOfDocs();
                    for (String doc : mapOfDocs.keySet()) {
                        TermPosFreq tpf = mapOfDocs.get(doc);
                        System.out.println(doc + ": pos: " + tpf.getPos() + " freq: " + tpf.getFreqInDoc());
                    }
                }*/
                }

            }
        }
        //done iterating through folders
        // write the remaining maps to posting files
        if(currThreshhold < threshold) {
            createPostingFiles(docsDataIndex, dateDic, numberDic, entityDic, a_zWordsDic,
                                currEntityNumberPostingFile,toStem, workPlacePath, currWordPostingFile, wantedLetters);
            resetTreeMaps(docsDataIndex, dateDic, numberDic, entityDic, a_zWordsDic);
            //currThreshhold = 0;
        }

        //merge entity and numbers and selected letters postings to 1.
        mergeEntitysNumbers(stem+"entity", currEntityNumberPostingFile, workPlacePath);
        mergeEntitysNumbers(stem+"numbers", currEntityNumberPostingFile, workPlacePath);
        StringBuilder nameOfPosting = new StringBuilder("");
        for(char c : wantedLetters){
            nameOfPosting.append(stem);
            nameOfPosting.append(c);
           // mergeEntitysNumbers(nameOfPosting.toString(), currWordPostingFile, workPlacePath);
            mergeLetters(nameOfPosting.toString(), currWordPostingFile, workPlacePath);
            nameOfPosting.setLength(0);
        }

       removeEntityAppearsInOnlyOneDoc(workPlacePath, stem);

        //finally, create the dictionary posting file
        createDictionary(workPlacePath, stem);

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        totalTimeToBuildIndex = (double)totalTime/1000000000/60;
    }



    /**
     * upload the dictionary to ram
     * @param workPlacePath path to posting files directory
     * @param toStem whether to stem or not
     * @return array list of lines in dictionary , each line is in format: term [Tab] TotalFrequency
     * @throws FileNotFoundException
     */
    public ArrayList<String> getDictionaryInRam(String workPlacePath, boolean toStem) throws FileNotFoundException {
        ArrayList<String> dictionary = new ArrayList<>(); // name \t total freq
        String stem ="";
        if(toStem)
            stem = "stem";
        File dictionaryPostingFile = new File(workPlacePath+"/"+stem+"Dictionary.txt");
     /*   Scanner dicScan = new Scanner(dictionaryPostingFile);
        StringBuilder lineInDic = new StringBuilder("");
        StringBuilder cellInDicArrList=  new StringBuilder("");
        while(dicScan.hasNext()){
            lineInDic.append(dicScan.nextLine());
            cellInDicArrList.append(getNameFromLine(lineInDic.toString())+"*");
            cellInDicArrList.append(getTotalFreqFromDictionaryLine(lineInDic));
            dictionary.add(cellInDicArrList.toString());

            cellInDicArrList.setLength(0);
            lineInDic.setLength(0);
        }*/
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader((dictionaryPostingFile)));
            String availalbe;
            StringBuilder cellInDicArrList=  new StringBuilder("");
            while((availalbe = br.readLine()) != null) {
                cellInDicArrList.append(getNameFromLine(availalbe)+"*");
                cellInDicArrList.append(getTotalFreqFromDictionaryLine(new StringBuilder(availalbe)));
                dictionary.add(cellInDicArrList.toString());
                cellInDicArrList.setLength(0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return dictionary;
    }

    /**
     * trim the name of the term from ine in dictionary file
     * @param lineInDic line in dictionary file
     * @return name of the term
     */
    private String getTotalFreqFromDictionaryLine(StringBuilder lineInDic) {
        //System.out.println(lineInDic);
        String subStr = lineInDic.substring(lineInDic.indexOf(">*")+2);
        return subStr.substring(0, subStr.indexOf("*"));
    }

    /**
     * Creates the dictionary
     * @param workPlacePath path to posting files directory
     * @param stem whether to stem or not
     * @throws IOException
     */
    private void createDictionary(String workPlacePath, String stem) throws IOException {
        int filewWritersIndex = 0;
        FileWriter [] frArray = new FileWriter[30];
        BufferedWriter [] bwArray = new BufferedWriter[30];

        //System.out.println("starting merging dates and numbers");

        Path datesPath = Paths.get(workPlacePath+"/"+stem+"dates.txt");
        ArrayList<String> dateLines = new ArrayList<>();
        dateLines.addAll(Files.readAllLines(datesPath, Charset.defaultCharset())); // lines in dates posting file
        sizeOfDictionary += dateLines.size();

        ArrayList<String> numberLines = new ArrayList<>();
        File numbersPostingFile = new File(workPlacePath+"/"+stem+"numbers.txt");
        FileReader numbersFileReader=new FileReader(numbersPostingFile);
        BufferedReader numbersBufferedReader = null;

        File entityPostingFile = new File(workPlacePath+"/"+stem+"entity.txt");
        FileReader entityFileReader=new FileReader(entityPostingFile);
        BufferedReader entityBufferedReader = null;

        //Scanner entityScan = new Scanner(entityPostingFile);

        //Scanner numberScan = new Scanner(numbersPostingFile);
       // StringBuilder lineInNumbers = new StringBuilder(numberScan.nextLine());
        try {
            numbersBufferedReader = new BufferedReader(numbersFileReader);
            entityBufferedReader = new BufferedReader(entityFileReader);

            StringBuilder lineInNumbers = new StringBuilder("");
            String numberLineTemp;
            //if(numberScan.hasNext())
             //   lineInNumbers.append(numberScan.nextLine());
            if ((numberLineTemp = numbersBufferedReader.readLine()) != null) {
                lineInNumbers.append(numberLineTemp);
            }

            while(lineInNumbers.length() > 0 && !Character.isLetter(lineInNumbers.toString().charAt(1)) && ((numberLineTemp = numbersBufferedReader.readLine()) != null)){
                numberLines.add(lineInNumbers.toString());
                lineInNumbers.setLength(0);
                lineInNumbers.append(numberLineTemp);
            }
            sizeOfDictionary += numberLines.size();

            StringBuilder sortedNumbers = mergeSortDateNumbersForDictionary(dateLines, numberLines,
                    stem+"dates.txt", stem+"numbers.txt", 0,0);

            File dictionaryPostingFile = new File(workPlacePath+"/"+stem+"Dictionary.txt");
            frArray[filewWritersIndex] = new FileWriter(dictionaryPostingFile, true);
            bwArray[filewWritersIndex] = new BufferedWriter(frArray[filewWritersIndex]);
            bwArray[filewWritersIndex].write(sortedNumbers.toString());
            bwArray[filewWritersIndex].close();
            filewWritersIndex++;

            //System.out.println("finished merging dates and numbers");

          //  FileWriter fr = new FileWriter(dictionaryPostingFile, true);
          //  BufferedWriter bWriter = new BufferedWriter(fr);
          //  bWriter.write(sortedNumbers.toString());
           // bWriter.close(); //****//

            //System.out.println("starting merging numbers, entities and each letter");


            StringBuilder lineInEntity = new StringBuilder("");
            String entityLineTemp;

            for(char chr = 'a'; chr <= 'z'; chr++ ){
                ArrayList<String> numbersWithCurrChar = new ArrayList<>();
                if((!lineInNumbers.toString().equals("")) && ((lineInNumbers.toString().charAt(1) == chr)  || (lineInNumbers.toString().charAt(1) == (chr-32))))
                     numbersWithCurrChar.add(lineInNumbers.toString()); //because we lost the first line
                lineInNumbers.setLength(0);
                numbersWithCurrChar.addAll(getLinesWithChar(numbersBufferedReader, lineInNumbers, chr)); //set lineInNum to the line we will lose
                sizeOfDictionary += numbersWithCurrChar.size();

                ArrayList<String> entityWithCurrChar = new ArrayList<>();
                if(!lineInEntity.toString().equals("") &&((lineInEntity.toString().charAt(1) == chr) ||
                (lineInEntity.toString().charAt(1) == (chr-32))))
                    entityWithCurrChar.add(lineInEntity.toString());//because we lost the first line
                lineInEntity.setLength(0);
                entityWithCurrChar.addAll(getLinesWithChar(entityBufferedReader, lineInEntity, chr));//set lineInEnt to the line we will lose
                sizeOfDictionary += entityWithCurrChar.size();

                StringBuilder pathName = new StringBuilder(workPlacePath+"/");
                pathName.append(stem);
                pathName.append(chr);
                pathName.append(".txt");
                //File wordFileWithCurrChar = new File(pathName.toString());
               // Scanner scanWordsWithCurrChar = new Scanner(wordFileWithCurrChar);
                Path wordWithCurrCharPath = Paths.get(pathName.toString());
                ArrayList<String> wordsWithCurrChar = new ArrayList<>();
                wordsWithCurrChar.addAll(Files.readAllLines(wordWithCurrCharPath, Charset.defaultCharset()));

                sizeOfDictionary += wordsWithCurrChar.size();

                StringBuilder mergedChar = mergeSort3FilesForDic(wordsWithCurrChar, numbersWithCurrChar, entityWithCurrChar
                ,chr, stem);

                frArray[filewWritersIndex] = new FileWriter(dictionaryPostingFile, true);
                bwArray[filewWritersIndex] = new BufferedWriter(frArray[filewWritersIndex]);
                bwArray[filewWritersIndex].write(mergedChar.toString());
                bwArray[filewWritersIndex].close();
                filewWritersIndex++;
                //bWriter.write(mergedChar.toString());
        }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (numbersBufferedReader != null) {
                try {
                    numbersBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (entityBufferedReader != null) {
                try {
                    entityBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //entityScan.close();
        //numberScan.close();
       // bWriter.close();
        //System.out.println("done merging numbers, entities and each letter");
    }

    /**
     * Merges sorts 3 array lists.
     * @param wordsWithCurrChar
     * @param numbersWithCurrChar
     * @param entityWithCurrChar
     * @param chr a to z
     * @param stem whether to stem or not
     * @return string builder that contains the merged info from the 3 arrays
     */
    private StringBuilder mergeSort3FilesForDic(ArrayList<String> wordsWithCurrChar,
                                                ArrayList<String> numbersWithCurrChar,
                                                ArrayList<String> entityWithCurrChar, char chr, String stem) {
        StringBuilder merged = new StringBuilder("");
        StringBuilder nameOfPosting = new StringBuilder(stem);
        nameOfPosting.append(chr);
        nameOfPosting.append(".txt");

        int aIndex = 0; //wordsWithCurrChar
        int bIndex = 0; //numbersWithCurrChar
        int cIndex = 0; //entityWithCurrChar

        for(int j = 0; j < wordsWithCurrChar.size() + numbersWithCurrChar.size() + entityWithCurrChar.size(); j++) {
            //if one of them is empty, merge sort the other two
            if (aIndex == wordsWithCurrChar.size()) {
                merged.append(mergeSortDateNumbersForDictionary(
                        numbersWithCurrChar, entityWithCurrChar, stem+"numbers.txt", stem+"entity.txt"
                , bIndex, cIndex));
                break;
            }
            else if (bIndex == numbersWithCurrChar.size()) {
                merged.append(mergeSortDateNumbersForDictionary(
                        wordsWithCurrChar, entityWithCurrChar, nameOfPosting.toString(), stem+"entity.txt"
                ,aIndex, cIndex));
                break;
            }
            else if (cIndex == entityWithCurrChar.size()) {
                merged.append(mergeSortDateNumbersForDictionary(
                        wordsWithCurrChar, numbersWithCurrChar, nameOfPosting.toString(), stem+"numbers.txt"
                ,aIndex, bIndex));
                break;
            }
            else{ // merge sort them all
                String wordNameOnly = getNameFromLine(wordsWithCurrChar.get(aIndex)).toLowerCase();
                String numberNameOnly = getNameFromLine(numbersWithCurrChar.get(bIndex)).toLowerCase();
                String entityNameOnly = getNameFromLine(entityWithCurrChar.get(cIndex)).toLowerCase();

                String minName = getMinStringFrom3Strings(wordNameOnly, numberNameOnly, entityNameOnly);
                if(wordNameOnly.equals(minName)){
                    merged.append(getNameTotalTFFromLine(wordsWithCurrChar.get(aIndex)) + "*"+nameOfPosting+"\n");
                    aIndex++;
                }
                else if(numberNameOnly.equals(minName)){
                    merged.append(getNameTotalTFFromLine(numbersWithCurrChar.get(bIndex)) + "*"+stem+"numbers.txt\n");
                    bIndex++;
                }
                else{
                    merged.append(getNameTotalTFFromLine(entityWithCurrChar.get(cIndex)) + "*"+stem+"entity.txt\n");
                    cIndex++;
                }
            }
        }

        return merged;
    }

    /**
     * compares 3 strings
     * @param str1
     * @param str2
     * @param str3
     * @return the string who is alphabetically comes first
     */
    private String getMinStringFrom3Strings(String str1, String str2, String str3) {
        String minString = str2;
        if(str1.compareTo(minString) < 0){
            minString = str1;
        }
        if(str3.compareTo(minString) < 0){
            minString = str3;
        }
        return minString;
    }

    /**
     * returns how many unique terms are in the dictionary
     * @return
     */
    public int getSizeOfDictionary() {
        return sizeOfDictionary;
    }

    /**
     * Returns array lists that conatins the lines in the scanner that start with chr
     * @param postingFileScan
     * @param lineInNumbersOrEnt the line that we will lose during the scan
     * @param chr a-z
     * @return
     */
    private ArrayList<String> getLinesWithChar(BufferedReader postingFileScan, StringBuilder lineInNumbersOrEnt, char chr) throws IOException {
        ArrayList<String> linesWithChar = new ArrayList<>();
        String lineTemp;
        if((lineTemp = postingFileScan.readLine()) != null){
            StringBuilder lineInScan = new StringBuilder(lineTemp);
            while(lineInScan.length() > 0 &&
                    (lineInScan.toString().charAt(1) == chr || lineInScan.toString().charAt(1) == (chr-32))
                    && (lineTemp = postingFileScan.readLine()) != null){
                linesWithChar.add(lineInScan.toString());
                lineInScan.setLength(0);
                if((lineTemp != null)) {
                    lineInScan.append(lineTemp);
                }
            }
            if(lineInScan.length() > 0 && !(lineInScan.toString().charAt(1) == chr
                    || lineInScan.toString().charAt(1) == (chr-32)))
                 lineInNumbersOrEnt.append(lineInScan);
            //else
              //  linesWithChar.add(lineInNumbersOrEnt.toString());
            else{
                if(lineInScan.length() > 0 &&
                        (lineInScan.toString().charAt(1) == chr || lineInScan.toString().charAt(1) == (chr-32)))
                    linesWithChar.add(lineInScan.toString());
            }
        }
        return linesWithChar;
    }



    /**
     * merge sorts two posting files
     * @param dateLines first posting file lines
     * @param numberLines second posting file lines
     * @param nameA name of the first posting file
     * @param nameB name of the second posting file
     * @param aSartingIndex starting index to merge from the first array
     * @param bStartingIndex starting index to merge from the second array
     * @returns string builder that contains the merged info from the 2 arrays
     */
    private StringBuilder mergeSortDateNumbersForDictionary(ArrayList<String> dateLines, ArrayList<String> numberLines,
                                                            String nameA, String nameB,
                                                            int aSartingIndex, int bStartingIndex) {
        StringBuilder merged = new StringBuilder("");
        int aIndex = aSartingIndex; //dateLines
        int bIndex = bStartingIndex; //numberLines

        for(int j = aSartingIndex+bStartingIndex; j < dateLines.size() + numberLines.size(); j++) {
            if (aIndex == dateLines.size()) {
                //toReturn.add(tempDatesStrings.get(bIndex++));

             //   merged.append(getNameTotalTFFromLine(numberLines.get(bIndex)) + "\tnumbers.txt\n");
                merged.append(getNameTotalTFFromLine(numberLines.get(bIndex)) + "*"+nameB+"\n");
                bIndex++;
            } else if (bIndex == numberLines.size()) {
                // toReturn.add(keysOfdateDic.get(aIndex++));

               // merged.append("<" + keysOfdateDic.get(aIndex) + ">");
               // merged.append(dateDic.get(keysOfdateDic.get(aIndex)).termDataToString() + "\n");
               // merged.append(getNameTotalTFFromLine(dateLines.get(aIndex)) + "\tdates.txt\n");
                merged.append(getNameTotalTFFromLine(dateLines.get(aIndex)) + "*"+nameA+"\n");
                aIndex++;
            } else {
                String nameOnlyDate = getNameFromLine(dateLines.get(aIndex));
               // System.out.println("numberLines.get index b is "+numberLines.get(bIndex));
                String nameOnlyNumber = getNameFromLine(numberLines.get(bIndex));

                if (nameOnlyDate.toLowerCase().compareTo(nameOnlyNumber.toLowerCase()) < 0) {
                    //toReturn.add(keysOfdateDic.get(aIndex++));

                  //  merged.append("<" + keysOfdateDic.get(aIndex) + ">");
                  //  merged.append(dateDic.get(keysOfdateDic.get(aIndex)).termDataToString() + "\n");
                    merged.append(getNameTotalTFFromLine(dateLines.get(aIndex)) + "*"+nameA+"\n");
                    aIndex++;
                }
                else{
                    // toReturn.add(tempDatesStrings.get(bIndex++));

                    merged.append(getNameTotalTFFromLine(numberLines.get(bIndex)) + "*"+nameB+"\n");
                    bIndex++;
                }
            }

        }

        return merged;

    }

    /**
     * trims the line to get the term's total tf
     * @param line
     * @return the term's total tf
     */
    private String getNameTotalTFFromLine(String line) {// seperate by Tab
        String onlyName = getNameFromLine(line);
        String totalTF = line.substring(line.indexOf(">")+1, line.indexOf("*"));

        return "<"+onlyName+">*"+totalTF;
    }

    /**
     * removes from entity posting files entities that appears in only one document
     * @param workPlacePath path to posting files directory
     * @throws IOException
     */
    private void removeEntityAppearsInOnlyOneDoc(String workPlacePath, String stem) throws IOException {
        //System.out.println("removing entities "); // might consume heap

        Path path = Paths.get(workPlacePath+"/"+stem+"entity.txt");
        ArrayList<String> entityLines = new ArrayList<>();
        entityLines.addAll(Files.readAllLines(path, Charset.defaultCharset()));

         StringBuilder updatedEntityLines = new StringBuilder("");

        //sort lines in entity and remove those who appears in only one doc
        for(String line: entityLines){
            String line_HowManyDocs = line.substring(line.indexOf("*")+1, line.indexOf("&"));
            if(!line_HowManyDocs.equals("1")){
                updatedEntityLines.append(line+"\n");
            }
        }
        File entityFileToDelete = new File(workPlacePath+"/"+stem+"entity.txt");
        entityFileToDelete.delete();

        File entityFileToCreate = new File(workPlacePath+"/"+stem+"entity.txt");
        entityFileToCreate.createNewFile();

        FileWriter fr = new FileWriter(entityFileToCreate, true);
        BufferedWriter bWriter = new BufferedWriter(fr);
        bWriter.write(updatedEntityLines.toString());
        bWriter.close();

        //System.out.println("done removig entities");
    }

    private void mergeLetters(String postingName, int k, String workPlacePath) throws IOException {
        File postingFileToCreate = new File(workPlacePath+"/"+postingName+".txt");
        postingFileToCreate.createNewFile();

        FileWriter fr = new FileWriter(postingFileToCreate, true);
        BufferedWriter bWriter = new BufferedWriter(fr);

        final int threshold = 40000; //every 40k merged lines, write them to posting file
        int countMergedLines = 0;

        StringBuilder merged = new StringBuilder("");
        ArrayList<String> [] allLines = new ArrayList[k];
        int totalLength = 0;
        //initaite all lines
        for(int i = 0; i < k; i++){
            Path path = Paths.get(workPlacePath+"/"+postingName+(i+1)+".txt");
            allLines[i] = new ArrayList<>();
            allLines[i].addAll(Files.readAllLines(path, Charset.defaultCharset()));
            totalLength += allLines[i].size();
            //System.out.println("size of "+postingName+(i+1)+" is "+allLines[i].size());
        }
        int [] indexes = new int[k];
        for(int i = 0; i < totalLength;){
            String minName = getMinName(allLines, indexes);
            StringBuilder minNameInfo = new StringBuilder("");
            int p =0; // index to each fileLines of allLines
            for(ArrayList<String> fileLines : allLines){ // find who contains this min name
                if(indexes[p] < allLines[p].size()){
                    String currLineInFileLines = fileLines.get(indexes[p]);
                    String nameFromLine = getNameFromLine(currLineInFileLines);
                    if(nameFromLine.toLowerCase().equals(minName.toLowerCase())){
                        String updated = updateTwoLines(minNameInfo.toString(), currLineInFileLines);
                        minNameInfo.setLength(0);
                        minNameInfo.append(updated); // note that minNameInfo is empty in the first time
                        indexes[p] ++;
                        i++;
                        //deal with Capital and non-capital
                        String minNameInUpperCase = minName.toUpperCase();
                        String minNameInLowerCase = minName.toLowerCase();
                        String nameFromLineInUpperCase = nameFromLine.toUpperCase();
                        String nameFromLineInLowerCase = nameFromLine.toLowerCase();
                        //if((minName.equals(minNameInUpperCase) && nameFromLine.equals(nameFromLineInLowerCase))
                        //|| (nameFromLine.equals(nameFromLineInUpperCase) && minName.equals(minNameInLowerCase))){
                        if(!(minName.equals(minNameInUpperCase) && nameFromLine.equals(nameFromLineInUpperCase))){
                            minName = minNameInLowerCase;
                        }

                    }
                }
                p++;
            }
            merged.append("<"+minName+">"+minNameInfo+"\n");
            countMergedLines ++;

            if(countMergedLines == threshold){
                bWriter.write(merged.toString());
                merged.setLength(0);
                countMergedLines = 0;
            }
            //   merged.append(minNameInfo+"\n");
        }
        // now delete all the temp posting files and create the whole one.
        for(int i = 0; i < k; i++){
            File postingFileToDelete = new File(workPlacePath+"/"+postingName+(i+1)+".txt");
            postingFileToDelete.delete();
        }

        if(countMergedLines < threshold) { // write the remaining merged lines
            bWriter.write(merged.toString());
            merged.setLength(0);
        }
        bWriter.close();

    }


    /**
     * merge posting files from the same kind, to one
     * for example: merge entity1 entity2 entity3 to entity
     * @param postingName posting name, for example "entity" or "numbers"
     * @param k how many posting files from the same kind are there
     * @param workPlacePath path to posting files directory
     * @throws IOException
     */
    private void  mergeEntitysNumbers(String postingName, int k, String workPlacePath) throws IOException { //postingName is entity or numbers, k is how many posting files of their kind
        //System.out.println("Strating merging "+postingName+". there are "+k+" of them");
        File postingFileToCreate = new File(workPlacePath+"/"+postingName+".txt");
        postingFileToCreate.createNewFile();

        FileWriter fr = new FileWriter(postingFileToCreate, true);
        BufferedWriter bWriter = new BufferedWriter(fr);

        final int threshold = 40000; //every 40k merged lines, write them to posting file
        int countMergedLines = 0;

        StringBuilder merged = new StringBuilder("");
        ArrayList<String> [] allLines = new ArrayList[k];
        int totalLength = 0;
        //initaite all lines
        for(int i = 0; i < k; i++){
            Path path = Paths.get(workPlacePath+"/"+postingName+(i+1)+".txt");
            allLines[i] = new ArrayList<>();
            allLines[i].addAll(Files.readAllLines(path, Charset.defaultCharset()));
            totalLength += allLines[i].size();
            //System.out.println("size of "+postingName+(i+1)+" is "+allLines[i].size());
        }
        int [] indexes = new int[k];
        for(int i = 0; i < totalLength;){
            String minName = getMinName(allLines, indexes);
            StringBuilder minNameInfo = new StringBuilder("");
            int p =0; // index to each fileLines of allLines
            for(ArrayList<String> fileLines : allLines){ // find who contains this min name
                if(indexes[p] < allLines[p].size()){
                    String currLineInFileLines = fileLines.get(indexes[p]);
                    if(getNameFromLine(currLineInFileLines).equals(minName)){
                        String updated = updateTwoLines(minNameInfo.toString(), currLineInFileLines);
                        minNameInfo.setLength(0);
                        minNameInfo.append(updated); // note that minNameInfo is empty in the first time
                        indexes[p] ++;
                        i++;
                    }
                }
                p++;
            }
            merged.append("<"+minName+">"+minNameInfo+"\n");
            countMergedLines ++;

            if(countMergedLines == threshold){
                bWriter.write(merged.toString());
                merged.setLength(0);
                countMergedLines = 0;
            }
         //   merged.append(minNameInfo+"\n");
        }
        // now delete all the temp posting files and create the whole one.
        for(int i = 0; i < k; i++){
            File postingFileToDelete = new File(workPlacePath+"/"+postingName+(i+1)+".txt");
            postingFileToDelete.delete();
        }

        if(countMergedLines < threshold) { // write the remaining merged lines
            bWriter.write(merged.toString());
            merged.setLength(0);
        }
        bWriter.close();

    }

    /**
     * merge the info about the same term, that appears in two posting files from the same kind.
     * @param minNameInfo only info of the first term
     * @param line name+info of the second term
     * @return name + merged info
     */
    private String updateTwoLines(String minNameInfo, String line) {
        StringBuilder updatedInfo = new StringBuilder("");
        if(minNameInfo.equals(""))
            return line.substring(line.indexOf(">")+1);
        else{
           // String name = getNameFromLine(line);

            int line_TotalTF = Integer.parseInt(line.substring(line.indexOf(">")+1, line.indexOf("*")));
            int line_HowManyDocs = Integer.parseInt(line.substring(line.indexOf("*")+1, line.indexOf("&")));
            StringBuilder line_mapOfDocs = new StringBuilder(line.substring(line.indexOf("&")));

            int minNameInfo_TotalTF = Integer.parseInt(minNameInfo.substring(0, minNameInfo.indexOf("*")));
            int minNameInfo_HowManyDocs = Integer.parseInt(minNameInfo.substring(minNameInfo.indexOf("*")+1, minNameInfo.indexOf("&")));
            StringBuilder  minNameInfo_mapOfDocs = new StringBuilder( minNameInfo.substring( minNameInfo.indexOf("&")));

            int updated_HowManyDocs = line_HowManyDocs + minNameInfo_HowManyDocs;
            int updated_TotalTF = line_TotalTF + minNameInfo_TotalTF;
            minNameInfo_mapOfDocs.append(line_mapOfDocs);

            updatedInfo.append(updated_TotalTF+"*"+updated_HowManyDocs+minNameInfo_mapOfDocs);
            return updatedInfo.toString();
        }
    }

    /**
     * compares the term names from simple-word posting file that starts with the same letter
     * @param allLines
     * @param indexes
     * @return the term name that alphabetically comses first
     */
    private String getMinName(ArrayList<String>[] allLines, int[] indexes) {
        StringBuilder minName = new StringBuilder("");
        boolean iniatedMinName = false;
        int k = indexes.length;
        int p = 0; // index to each fileLines of allLines
        for(ArrayList<String> fileLines : allLines) { // find who contains this min name
            if (indexes[p] < allLines[p].size()) {
                String currLineInFileLines = fileLines.get(indexes[p]);
                String nameInLine = getNameFromLine(currLineInFileLines);
                if(!iniatedMinName){
                    minName.setLength(0);
                    minName.append(nameInLine);
                    iniatedMinName = true;
                }
                else {
                    if (nameInLine.toLowerCase().compareTo(minName.toString().toLowerCase()) < 0) { // do to lower case for case sensitivity
                        minName.setLength(0);
                        minName.append(nameInLine);
                    }
                }
            }
            p++;
        }
        return minName.toString();

     /*   StringBuilder minName = new StringBuilder("zzzzzzzz");
        int k = indexes.length;
        int p = 0; // index to each fileLines of allLines
        for(ArrayList<String> fileLines : allLines) { // find who contains this min name
            if (indexes[p] < allLines[p].size()) {
                String currLineInFileLines = fileLines.get(indexes[p]);
                String nameInLine = getNameFromLine(currLineInFileLines);
                if(nameInLine.toLowerCase().compareTo(minName.toString().toLowerCase()) < 0 ){ // do to lower case for case sensitivity
                    minName.setLength(0);
                    minName.append(nameInLine);
                }
            }
            p++;
        }
        return minName.toString();*/

    }

    /**
     * get only the term name from a line
     * @param line
     * @return
     */
    private String getNameFromLine(String line){
       // System.out.println(line);
        return line.substring(line.indexOf("<")+1, line.indexOf(">"));
    }

    /**
     * reset the helper tree maps
     * @param docsDataIndex tree map for docsIndex
     * @param dateDic tree map for date posting file
     * @param numberDic tree map for numbers posting file
     * @param entityDic tree map for entity posting file
     * @param a_zWordsDic tree map for simple words posting file
     */
    private void resetTreeMaps(HashMap<String, DocInFolderData> docsDataIndex, TreeMap<String, TermData> dateDic, TreeMap<String, TermData> numberDic, TreeMap<String, TermData> entityDic, TreeMap<String, TermData>[] a_zWordsDic) {
        dateDic.clear();
        numberDic.clear();
        entityDic.clear();
        clearTreeMapArray(a_zWordsDic);
        docsDataIndex.clear();// = new HashMap<>(); //key is docNo
    }


    /**
     * creates all the posting files and docs index
     * @param docsDataIndex tree map for docsIndex
     * @param dateDic tree map for date posting file
     * @param numberDic tree map for numbers posting file
     * @param entityDic tree map for entity posting file
     * @param a_zWordsDic tree map for simple words posting file
     * @param currEntityNumberPostingFile
     * @param toStem whether to stem or not
     * @param workPlacePath path to posting files directory
     * @param currWordPostingFile
     * @param wantedLetters the letters that their posting files are splitted
     * @throws IOException
     */
    private void createPostingFiles(HashMap<String, DocInFolderData> docsDataIndex, TreeMap<String, TermData> dateDic,
                                    TreeMap<String, TermData> numberDic, TreeMap<String, TermData> entityDic,
                                    TreeMap<String, TermData>[] a_zWordsDic, int currEntityNumberPostingFile,
                                    boolean toStem , String workPlacePath, int currWordPostingFile,
                                   HashSet<Character> wantedLetters) throws IOException {

        String stem ="";
        if(toStem)
            stem = "stem";
        writeToDocsIndex(docsDataIndex, workPlacePath, stem);
        //if !did index before...
     //   if(!alreadyDidIndexBefore) {
            createDatesOrNumbersOrEntityPostingFile(dateDic, workPlacePath, stem+"dates");
            createDatesOrNumbersOrEntityPostingFile(numberDic, workPlacePath, stem+"numbers" + currEntityNumberPostingFile);
            createDatesOrNumbersOrEntityPostingFile(entityDic, workPlacePath, (stem+"entity" + currEntityNumberPostingFile));
       // }
       // end of if
        createAtoZPostingFiles(a_zWordsDic, workPlacePath, stem, currWordPostingFile, wantedLetters);
    }

    /**
     * creates simple-words posting files
     * @param a_zWordsDic
     * @param workPlacePath
     * @param stem
     * @param currWordPostingFile
     * @param wantedLetters
     * @throws IOException
     */
    private void createAtoZPostingFiles(TreeMap<String, TermData>[] a_zWordsDic, String workPlacePath, String stem,
                                       int currWordPostingFile,  HashSet<Character> wantedLetters ) throws IOException {
        for(int i = 0; i < a_zWordsDic.length; i++){
            char letter = (char)(i+'a');
            int currPFile = currWordPostingFile;
            if(!wantedLetters.contains(letter))
                currPFile = -1;

           // System.out.println(currPFile);
          //  System.out.println("pos "+currWordPostingFile);
            StringBuilder pathBuilder = new StringBuilder(workPlacePath);
            pathBuilder.append("/"+stem);
            pathBuilder.append(letter);
            //pathBuilder.append((currPFile)+".txt");
            if(currPFile != -1)
                pathBuilder.append(currPFile);
            pathBuilder.append(".txt");
           // System.out.println(pathBuilder);

          //  Path path = Paths.get(workPlacePath+"/"+stem+(letter)+(currPFile)+".txt");
            Path path = Paths.get(pathBuilder.toString());
            StringBuilder tempPostingLine = new StringBuilder("");
            ArrayList<String> tempPostingStrings = new ArrayList<>();
            List<String> postingFileLines = Files.readAllLines(path, Charset.defaultCharset());
            for(String line : postingFileLines) {
                String wordString = line.substring(line.indexOf("<") + 1, line.indexOf(">"));
                String wordInUpperCase = wordString.toUpperCase();
                String wordInLowerCase = wordString.toLowerCase();
                if(!a_zWordsDic[i].containsKey(wordInUpperCase) && !a_zWordsDic[i].containsKey(wordInLowerCase)){
                    tempPostingLine.append(line);
                    tempPostingStrings.add(tempPostingLine.toString());
                    tempPostingLine.setLength(0);
                }
                else{ //line is already in the dic, need to update it.
                    int line_TotalTF = Integer.parseInt(line.substring(line.indexOf(">")+1, line.indexOf("*")));
                    int line_HowManyDocs = Integer.parseInt(line.substring(line.indexOf("*")+1, line.indexOf("&")));
                    StringBuilder line_mapOfDocs = new StringBuilder(line.substring(line.indexOf("&")));
                    if(a_zWordsDic[i].containsKey(wordInLowerCase)){ //line is in dic as lower case
                        TermData termDataInDic = a_zWordsDic[i].get(wordInLowerCase);
                        int updated_HowManyDocs = line_HowManyDocs + termDataInDic.getHowManyDocs();
                        int updated_TotalTF = line_TotalTF + termDataInDic.getTotalTFInFolder();
                        line_mapOfDocs.append(termDataInDic.mapOfDocsToString());

                        if(wordString.equals(wordInUpperCase)){ // line is in upper case in text
                            tempPostingLine.append("<"+wordInLowerCase+">"+updated_TotalTF+"*"+updated_HowManyDocs+line_mapOfDocs);
                        }
                        else{ //lower case in dic and lower case in text
                            tempPostingLine.append("<"+wordString+">"+updated_TotalTF+"*"+updated_HowManyDocs+line_mapOfDocs);
                        }
                        a_zWordsDic[i].remove(wordInLowerCase);
                    }
                    else{//line is in dic as upper case
                        TermData termDataInDic = a_zWordsDic[i].get(wordInUpperCase);
                        int updated_HowManyDocs = line_HowManyDocs + termDataInDic.getHowManyDocs();
                        int updated_TotalTF = line_TotalTF + termDataInDic.getTotalTFInFolder();
                        line_mapOfDocs.append(termDataInDic.mapOfDocsToString());

                        tempPostingLine.append("<"+wordString+">"+updated_TotalTF+"*"+updated_HowManyDocs+line_mapOfDocs);
                        a_zWordsDic[i].remove(wordInUpperCase);
                    }
                    tempPostingStrings.add(tempPostingLine.toString());
                    tempPostingLine.setLength(0);
                }
            }

            //now merge between dateDic and tempDates
         //   File wordFileToDelete = new File(workPlacePath+"/"+stem+(letter)+(currPFile)+".txt");
            File wordFileToDelete = new File(pathBuilder.toString());
            wordFileToDelete.delete();

            //File wordFile = new File(workPlacePath+"/"+stem+(letter)+(currPFile)+".txt");
            File wordFile = new File(pathBuilder.toString());
            wordFile.createNewFile();
            // now merge and then write string builder to new date file
            ArrayList<String> keysOfdateDic = new ArrayList<String>(a_zWordsDic[i].keySet()); //sorted keys of dateDic
            StringBuilder merged = mergeSort(a_zWordsDic[i], keysOfdateDic, tempPostingStrings);

            FileWriter fr = new FileWriter(wordFile, true);
            BufferedWriter bWriter = new BufferedWriter(fr);
            bWriter.write(merged.toString());
            bWriter.close();

        }
    }

    /**
     * creates dates or numbers or entity posting file, according to the parameters that are sent
     * @param dateDic the tree map of the wanted term role
     * @param workPlacePath  path to posting files directory
     * @param nameOfPosting name of the posting - date, numbers, entity
     * @throws IOException
     */
    private void createDatesOrNumbersOrEntityPostingFile(TreeMap<String, TermData> dateDic, String workPlacePath,
                                                         String nameOfPosting) throws IOException {
        StringBuilder tempDatesLine = new StringBuilder("");
        ArrayList<String> tempDatesStrings = new ArrayList<>();
       // HashMap<String, String> tempDates = new HashMap<>();

        Path path = Paths.get(workPlacePath+"/"+nameOfPosting+".txt");
        List<String> datesFileLines = Files.readAllLines(path, Charset.defaultCharset());
        for(String line : datesFileLines){
            String dateString = line.substring(line.indexOf("<")+1, line.indexOf(">"));
            if(dateDic.containsKey(dateString)){
                int line_TotalTF = Integer.parseInt(line.substring(line.indexOf(">")+1, line.indexOf("*")));
                int line_HowManyDocs = Integer.parseInt(line.substring(line.indexOf("*")+1, line.indexOf("&")));
                StringBuilder line_mapOfDocs = new StringBuilder(line.substring(line.indexOf("&")));

                TermData termDataInDic = dateDic.get(dateString);
                int updated_HowManyDocs = line_HowManyDocs + termDataInDic.getHowManyDocs();
                int updated_TotalTF = line_TotalTF + termDataInDic.getTotalTFInFolder();
                line_mapOfDocs.append(termDataInDic.mapOfDocsToString());

                tempDatesLine.append("<"+dateString+">"+updated_TotalTF+"*"+updated_HowManyDocs+line_mapOfDocs);
                tempDatesStrings.add(tempDatesLine.toString());
               // tempDates.put(dateString, tempDatesLine.toString());
                tempDatesLine.setLength(0);

                dateDic.remove(dateString);
            }
            else{
                tempDatesLine.append(line);
                tempDatesStrings.add(tempDatesLine.toString());
               // tempDates.put(dateString, tempDatesLine.toString());
                tempDatesLine.setLength(0);
            }

        }

        //now merge between dateDic and tempDates
        File dateFileToDelete = new File(workPlacePath+"/"+nameOfPosting+".txt");
        dateFileToDelete.delete();

        File dateFile = new File(workPlacePath+"/"+nameOfPosting+".txt");
        dateFile.createNewFile();
        // now merge and then write string builder to new date file
        ArrayList<String> keysOfdateDic = new ArrayList<String>(dateDic.keySet()); //sorted keys of dateDic
        StringBuilder merged = mergeSort(dateDic, keysOfdateDic, tempDatesStrings);

        FileWriter fr = new FileWriter(dateFile, true);
        BufferedWriter bWriter = new BufferedWriter(fr);
        bWriter.write(merged.toString());
        bWriter.close();
    }

    /**
     * merge sort two posting files that represents the same role,
     * first is as tree-map, secons is as array list
     * @param dateDic
     * @param keysOfdateDic
     * @param tempDatesStrings
     * @return string builder that contains the merged info
     */
    private StringBuilder mergeSort(TreeMap<String, TermData> dateDic, ArrayList<String> keysOfdateDic
            , ArrayList<String> tempDatesStrings) {

        StringBuilder merged = new StringBuilder("");
        int aIndex = 0; //keysOfdateDic
        int bIndex = 0; //tempDatesStrings

        for(int j = 0; j < keysOfdateDic.size() + tempDatesStrings.size(); j++) {
            if (aIndex == keysOfdateDic.size()) {
                //toReturn.add(tempDatesStrings.get(bIndex++));

                merged.append(tempDatesStrings.get(bIndex) + "\n");
                bIndex++;
            } else if (bIndex == tempDatesStrings.size()) {
                // toReturn.add(keysOfdateDic.get(aIndex++));

                merged.append("<" + keysOfdateDic.get(aIndex) + ">");
                merged.append(dateDic.get(keysOfdateDic.get(aIndex)).termDataToString() + "\n");
                aIndex++;
            } else {
                String wholeLine = tempDatesStrings.get(bIndex);
                String termOnly = wholeLine.substring(wholeLine.indexOf("<")+1, wholeLine.indexOf(">"));
                if (keysOfdateDic.get(aIndex).toLowerCase().compareTo(termOnly.toLowerCase()) < 0) { //do to lower case for case senstivity
                    //toReturn.add(keysOfdateDic.get(aIndex++));

                    merged.append("<" + keysOfdateDic.get(aIndex) + ">");
                    merged.append(dateDic.get(keysOfdateDic.get(aIndex)).termDataToString() + "\n");
                    aIndex++;
                }
                else{
                    // toReturn.add(tempDatesStrings.get(bIndex++));

                    merged.append(tempDatesStrings.get(bIndex) + "\n");
                    bIndex++;
                }
            }

        }

        return merged;

    }


    /**
     * write new indexed docs to docs index
     * @param docsDataIndex
     * @param workPlacePath path to posting files directory
     * @param stem whether to stem or not
     * @throws IOException
     */
    private void writeToDocsIndex(HashMap<String, DocInFolderData> docsDataIndex, String workPlacePath, String stem) throws IOException {
        numOfIndexedDocs += docsDataIndex.size();
        File docsIndex = new File(workPlacePath+"/"+stem+"DocsIndex.txt");
        FileWriter fr = new FileWriter(docsIndex, true);
        BufferedWriter bWriter = new BufferedWriter(fr);
        StringBuilder docsInFolderDateToString = getStringOfDocsDataIndexMap(docsDataIndex);
        bWriter.write(docsInFolderDateToString.toString());
        bWriter.close();
    }

    /**
     * returns string builder that contains all the data of the current map of docs data index.
     * @param docsDataIndex
     * @return
     */
    private StringBuilder getStringOfDocsDataIndexMap(HashMap<String, DocInFolderData> docsDataIndex) {
        StringBuilder str = new StringBuilder("");
        for(String docNo : docsDataIndex.keySet()){
            DocInFolderData docData = docsDataIndex.get(docNo);
            str.append("<"+docNo+">#"+docData.getMaxTF()+"#"+docData.getUniquTermsAmount()+"#"+docData.getTextLength()+"\n");
        }

        return str;
    }

    /**
     * iniatlize array of tree map, each one represents words that start with a different letter from the alphabet.
     * @param a_zWordsDic array of tree map to iniatlize
     */
    private void intializeWordArrMap(TreeMap<String, TermData>[] a_zWordsDic) {
        for(int i = 0; i < a_zWordsDic.length; i++)
            a_zWordsDic[i] = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * clear array of tree map, each one represents words that start with a different letter from the alphabet.
     * @param a_zWordsDic array of tree map to clear
     */
    private void clearTreeMapArray(TreeMap<String, TermData>[] a_zWordsDic) {
        for(int i = 0; i < a_zWordsDic.length; i++)
            a_zWordsDic[i].clear();
    }


    /**
     * what to do if the term we got from the parser has the role of a word
     * @param word
     * @param docNo
     * @param whereFoundFirst
     * @param dic
     * @return
     */
    private int termIsWord(String word, String docNo, byte whereFoundFirst, TreeMap<String, TermData> [] dic){
        int tfOfTermInDoc =0;
        char firstChar = word.charAt(0);
        String wordInUpperCase = word.toUpperCase();
        String wordInLowerCase = word.toLowerCase();

        int index = (Character.toLowerCase(firstChar)-97);
        //System.out.println("word is "+word+". First char is "+firstChar+" index is "+ (Character.toLowerCase(firstChar)-97));
       // System.out.println("word is "+word);
        if (!dic[index].containsKey(wordInLowerCase) && !dic[index].containsKey(wordInUpperCase)) {
            //first time we faced this word in the folder
            TermData wordData = new TermData(1);
            wordData.addToMapOfDocs(docNo, whereFoundFirst, 1);
            dic[index].put(word, wordData);
            tfOfTermInDoc = dic[index].get(word).getTfInDoc(docNo);
        }
        else {// exists in dic[i]. deal with Capital letters
            if (dic[index].containsKey(wordInUpperCase)) {
                if (Character.isLowerCase(firstChar)) {
                    TermData termData = new TermData(dic[index].get(wordInUpperCase)); //backup
                    dic[index].remove(wordInUpperCase);
                    dic[index].put(wordInLowerCase, termData);
                    if (!dic[index].get(wordInLowerCase).getMapOfDocs().containsKey(docNo)) { // doc id isnt in list of docs
                        dic[index].get(wordInLowerCase).addToMapOfDocs(docNo, whereFoundFirst, 1);
                    } else { // already exists in folder and in the doc, add 1 to tf in doc
                        dic[index].get(wordInLowerCase).add1ToFreqInDoc(docNo);
                    }
                    dic[index].get(wordInLowerCase).incBy1TotalFreqInFolder();
                    tfOfTermInDoc = dic[index].get(wordInLowerCase).getTfInDoc(docNo);
                }
                else{ // first char is upper case
                    if (!dic[index].get(wordInUpperCase).getMapOfDocs().containsKey(docNo)) { // doc id isnt in list of docs
                        dic[index].get(wordInUpperCase).addToMapOfDocs(docNo, whereFoundFirst, 1);
                    } else { // already exists in folder and in the doc, add 1 to tf in doc
                        dic[index].get(wordInUpperCase).add1ToFreqInDoc(docNo);
                    }
                    dic[index].get(wordInUpperCase).incBy1TotalFreqInFolder();
                    tfOfTermInDoc = dic[index].get(wordInUpperCase).getTfInDoc(docNo);
                }
            }
            else{
                if (dic[index].containsKey(wordInLowerCase)) {
                    if (!dic[index].get(wordInLowerCase).getMapOfDocs().containsKey(docNo)) { // doc id isnt in list of docs
                        dic[index].get(wordInLowerCase).addToMapOfDocs(docNo, whereFoundFirst, 1);
                    } else { // already exists in folder and in the doc, add 1 to tf in doc
                        dic[index].get(wordInLowerCase).add1ToFreqInDoc(docNo);
                    }
                    dic[index].get(wordInLowerCase).incBy1TotalFreqInFolder();
                    tfOfTermInDoc = dic[index].get(wordInLowerCase).getTfInDoc(docNo);
                }
            }
        }

        return  tfOfTermInDoc;
    }


    /**
     * what to do if the term we got from the parser has the role of a date, number or entity
     * @param term
     * @param docNo
     * @param whereFoundFirst
     * @param dic
     * @return
     */
    private void termIsDateOrNumberOrEntity(String term, String docNo, byte whereFoundFirst, TreeMap<String, TermData> dic){
        if(!dic.containsKey(term)){ // first time this term is in folder
            TermData dateData = new TermData(0);
            dateData.addToMapOfDocs(docNo, whereFoundFirst, 1);
            dic.put(term, dateData);
        }
        else{
            if(!dic.get(term).getMapOfDocs().containsKey(docNo)){ // doc id isnt in list of docs
                dic.get(term).addToMapOfDocs(docNo, whereFoundFirst, 1);
            }
            else{ // already exists in folder and in the doc, add 1 to tf in doc
                dic.get(term).add1ToFreqInDoc(docNo);
            }
        }
        dic.get(term).incBy1TotalFreqInFolder();
    }


    /**
     * create stopwords from the stopword file
     * @param files
     * @return
     * @throws FileNotFoundException
     */
    private HashSet<String> createStopwords(File[] files, String workPlacePath) throws IOException {
        File stopwordsFile = new File(workPlacePath+"/stop_words.txt");
        boolean alreadyExists = true;
        if(stopwordsFile.createNewFile()){
            alreadyExists = false;
        }

        StringBuilder stopWords = new StringBuilder("");


        HashSet<String> stopwords = new HashSet<>();
        for (File folder : files) {
            if (!folder.isDirectory()) {
                //stopwords
                FileReader fileReader=new FileReader(folder);
                BufferedReader bufferedReaderr = null;
                try {
                    bufferedReaderr=new BufferedReader(fileReader);
                    String line;
                    while((line = bufferedReaderr.readLine()) != null) {
                        stopWords.append(line+"\n");
                        stopwords.add(line);
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
                /*Scanner scan = new Scanner(folder);
                while (scan.hasNext()) {
                    String line = scan.next();
                    stopWords.append(line+"\n");
                    stopwords.add(line);
                }
                scan.close();*/
            }

        }
        if(!alreadyExists) {
            FileWriter fr = new FileWriter(stopwordsFile, true);
            BufferedWriter bWriter = new BufferedWriter(fr);
            bWriter.write(stopWords.toString());
            bWriter.close();
        }

        return stopwords;
    }


    /**
     * create empty posting files
     * @param toStem
     * @param workPlacePath
     * @param wantedLetters
     * @throws IOException
     */
    private void initiatePostingFiles(boolean toStem, String workPlacePath, HashSet<Character> wantedLetters) throws IOException {
        String stem ="";
        if(toStem)
            stem = "stem"; // for making files with or without stemming
        StringBuilder postingName = new StringBuilder();
        for(char alphabet = 'a'; alphabet <= 'z'; alphabet++ )
        {
            postingName.append(stem);
            postingName.append(alphabet);
            if(wantedLetters.contains(alphabet))
                postingName.append("1");

            File postingFile = new File(workPlacePath+"/"+postingName+".txt");
            postingFile.createNewFile();
            postingName.setLength(0);
        }
        File postingFile = new File(workPlacePath+"/"+stem+"Dictionary.txt");
        postingFile.createNewFile();
        postingFile = new File(workPlacePath+"/"+stem+"numbers1.txt");
        postingFile.createNewFile();

        postingFile = new File(workPlacePath+"/"+stem+"dates.txt");
        postingFile.createNewFile();
       // if( !postingFile.createNewFile()){
        //    alreadyDidIndexBefore = true;
       // }
        postingFile = new File(workPlacePath+"/"+stem+"entity1.txt");
        postingFile.createNewFile();
        postingFile = new File(workPlacePath+"/"+stem+"DocsIndex.txt");
        postingFile.createNewFile();

       // System.out.println(alreadyDidIndexBefore);

        //    out = new BufferedWriter(new FileWriter(workPlacePath+"/dates.txt"));

    }
}
