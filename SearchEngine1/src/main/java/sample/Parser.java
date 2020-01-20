package sample;

import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Arrays;

public class Parser {
    private HashSet<String> stopwords = new HashSet<String>();

    public void setStopwords(HashSet<String> stopwords) {
        this.stopwords = stopwords;
    }

    public HashMap<String, DocData> Parse(File docsFile) {//Main parse function
        return new HashMap<String, DocData>();
    }

    /**
     * this is a wrapper parse function
     *
     * @param corpusPath
     * @param singleFile
     * @param toStem
     * @return ou
     * @throws FileNotFoundException
     */
    public HashMap<String, DocData> parse(String corpusPath, File singleFile, boolean toStem) throws FileNotFoundException {
        HashMap<String, DocData> toIndexer = new HashMap<String, DocData>();
        ReadFile reader = new ReadFile();
        HashMap<String, String> docsInFolder = reader.readCorpusFolder(corpusPath, singleFile);
        return parse(docsInFolder, toStem, stopwords);
    }

    /**
     * this is the main parser
     *
     * @param corpus
     * @param toStem
     * @param stopwords
     * @return
     */
    public HashMap<String, DocData> parse(HashMap<String, String> corpus, boolean toStem, HashSet<String> stopwords) {
        HashMap<String, DocData> toIndexer = new HashMap<String, DocData>();
        HashMap<String, String> months = new HashMap<String, String>();
        HashMap<String, String> numbers = new HashMap<String, String>();
/**
 * dictionaries for month and numbers
 */
        months.put("january", "01");
        months.put("jan", "01");
        months.put("february", "02");
        months.put("feb", "02");
        months.put("march", "03");
        months.put("mar", "03");
        months.put("april", "04");
        months.put("apr", "04");
        months.put("may", "05");
        months.put("june", "06");
        months.put("jun", "06");
        months.put("july", "07");
        months.put("jul", "07");
        months.put("august", "08");
        months.put("aug", "08");
        months.put("september", "09");
        months.put("sep", "09");
        months.put("october", "10");
        months.put("oct", "10");
        months.put("november", "11");
        months.put("nov", "11");
        months.put("december", "12");
        months.put("dec", "12");
        numbers.put("zero", "0");
        numbers.put("one", "1");
        numbers.put("two", "2");
        numbers.put("three", "3");
        numbers.put("four", "4");
        numbers.put("five", "5");
        numbers.put("six", "6");
        numbers.put("seven", "7");
        numbers.put("eight", "8");
        numbers.put("nine", "9");
        numbers.put("ten", "10");
        numbers.put("eleven", "11");
        numbers.put("twelve", "12");
        numbers.put("dozen", "12");

        Iterator it = corpus.entrySet().iterator();
        //int indexOfDocInFolder = 1;
        while (it.hasNext()) {
            HashMap<String, Byte> terms = new HashMap<String, Byte>();
            Map.Entry pair = (Map.Entry) it.next();
            String docContent = (String) pair.getValue();
            //System.out.println("doc: " + pair.getKey());
            //System.out.println(docContent);
            docContent = docContent.replaceAll("\\^%", "");
            docContent = docContent.replaceAll("[\\p{Punct}&&[^\\u0027\\u2010\\u002D\\u002E\\u0024\\u0025\\u002f\\u2215\\u2044\\u003C\\u003E\\u005B\\u005D]]", "");
            if (docContent.contains("[Text]")) {
                docContent = docContent.substring(docContent.indexOf("[Text]") + 6);

            }
//            while (docContent.contains("<") && (Character.isUpperCase(docContent.charAt(docContent.indexOf('<') + 1))||docContent.charAt(docContent.indexOf('<') + 1)=='/')) {
//                if (docContent.length() > 0 && docContent.contains(">") && (Character.isUpperCase(docContent.charAt(docContent.indexOf('>') - 1))||Character.isDigit(docContent.charAt(docContent.indexOf('>') - 1))))
//                    if (docContent.indexOf('<') > 0)
//                        docContent = docContent.substring(0, docContent.indexOf('<') - 1) + docContent.substring(docContent.indexOf('>') + 1);
//                    else {
//                        docContent = docContent.substring(0, docContent.indexOf('<')) + docContent.substring(docContent.indexOf('>') + 1);
//                        docContent = docContent.replace("<", "");
//                    }
//
//            }
            // System.out.println(docContent);
            ArrayList<String> doc = new ArrayList<String>(Arrays.asList(docContent.split(" ")));
            //System.out.println(doc);

            for (int j = 0; j < doc.size(); j++) { //remove the "null" cells
                if (doc.get(j).length() <= 0) {
                    doc.remove(j);
                    j--;
                }
            }
            int docLength = doc.size();
            for (int i = 0; i < doc.size(); i++) {
                if (doc.get(i).length() > 0 && ((doc.get(i).charAt(doc.get(i).length() - 1) == ']' || doc.get(i).charAt(0) == '['))) {
                    doc.remove(i);
                    i--;
                    continue;
                }
                while ((doc.get(i).length() > 0 && (doc.get(i).charAt(doc.get(i).length() - 1) == '.' || doc.get(i).charAt(doc.get(i).length() - 1) == ',' || doc.get(i).charAt(doc.get(i).length() - 1) == '-'))) {
                    doc.set(i, doc.get(i).substring(0, doc.get(i).length() - 1));
                }
                while (doc.get(i).length() > 0 && doc.get(i).charAt(0) == '-')
                    doc.set(i, doc.get(i).substring(1));

                /**
                 **this part is for classifying all the rules according to words
                 */
                doc.set(i, doc.get(i).replaceAll("[\\p{Punct}&&[^\\u0027\\u2010\\u002D\\u002E\\u0024\\u0025\\u002f]]", ""));
                if (!terms.containsKey(doc.get(i))) {
                    if (doc.get(i).contains("'")) {
                        if (doc.get(i).contains("''")) {
                            int indexOfPunct = doc.get(i).indexOf("'");
                            doc.add(i + 1, doc.get(i).substring(indexOfPunct + 2));
                            doc.set(i, doc.get(i).substring(0, indexOfPunct));
                        } else {
                            if (doc.get(i).charAt(0) == '\'')
                                doc.set(i, doc.get(i).substring(1));
                            if (doc.get(i).indexOf('\'') != -1 && doc.get(i).charAt(doc.get(i).length() - 1) == '\'')
                                doc.set(i, doc.get(i).substring(0, doc.get(i).length() - 1));
                        }
                    }
                    if (doc.get(i).contains("-")) {
                        if (doc.get(i).contains("----")) {
                            int indexOfPunct = doc.get(i).indexOf("-");
                            doc.add(i + 1, doc.get(i).substring(indexOfPunct + 4));
                            doc.set(i, doc.get(i).substring(0, indexOfPunct));
                        } else if (doc.get(i).contains("---")) {
                            int indexOfPunct = doc.get(i).indexOf("-");
                            doc.add(i + 1, doc.get(i).substring(indexOfPunct + 3));
                            doc.set(i, doc.get(i).substring(0, indexOfPunct));
                        } else if (doc.get(i).contains("--")) {
                            int indexOfPunct = doc.get(i).indexOf("-");
                            doc.add(i + 1, doc.get(i).substring(indexOfPunct + 2));
                            doc.set(i, doc.get(i).substring(0, indexOfPunct));
                        }
                    }
                    if (doc.get(i).contains(".")) {
                        if (doc.get(i).contains(".....")) {
                            int indexOfPunct = doc.get(i).indexOf(".");
                            doc.add(i + 1, doc.get(i).substring(indexOfPunct + 5));
                            doc.set(i, doc.get(i).substring(0, indexOfPunct));
                        } else if (doc.get(i).contains("....")) {
                            int indexOfPunct = doc.get(i).indexOf(".");
                            doc.add(i + 1, doc.get(i).substring(indexOfPunct + 4));
                            doc.set(i, doc.get(i).substring(0, indexOfPunct));
                        } else if (doc.get(i).contains("...")) {
                            int indexOfPunct = doc.get(i).indexOf(".");
                            doc.add(i + 1, doc.get(i).substring(indexOfPunct + 3));
                            doc.set(i, doc.get(i).substring(0, indexOfPunct));
                        } else if (doc.get(i).contains("..")) {
                            int indexOfPunct = doc.get(i).indexOf(".");
                            doc.add(i + 1, doc.get(i).substring(indexOfPunct + 2));
                            doc.set(i, doc.get(i).substring(0, indexOfPunct));
                        }
                        int indexOfPunct = doc.get(i).indexOf(".");
                        if (doc.get(i).length() <= 0) {
                            doc.remove(i);
                            i--;
                            continue;
                        }
                        if (!isNumeric(doc.get(i)))
                            if (indexOfPunct != -1)
                                if (doc.get(i).length() > 0)
                                    if (!doc.get(i).substring(indexOfPunct + 1).matches("[a-zA-Z]"))
                                        doc.set(i, doc.get(i).substring(0, indexOfPunct));
                    }
                    while (doc.size() > i + 2 && doc.get(i + 1).length() > 0 && doc.get(i + 2).length() > 0 && doc.get(i + 1).equals("-")) {
                        doc.set(i, doc.get(i) + "-" + doc.get(i + 2));
                        doc.remove(i + 1);//removing cells i+1, i+2
                        doc.remove(i + 1);
                    }
                    if (doc.get(i).length() > 0 && !Character.isDigit(doc.get(i).charAt(0)) && !(doc.get(i).charAt(0) == '$')) {
                        if (doc.get(i).length() != 0 && months.containsKey(doc.get(i).toLowerCase()) && (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).charAt(doc.get(i + 1).length() - 1) != 'l' && isPureNumeric(doc.get(i + 1)))) {
                            if (Double.parseDouble(doc.get(i + 1)) < 32) {
                                doc.set(i, months.get(doc.get(i).toLowerCase()) + "-" + doc.get(i + 1));
                                doc.remove(i + 1);
                            } else {
                                if (Double.parseDouble(doc.get(i + 1)) > 999 && Double.parseDouble(doc.get(i + 1)) < 2100) {
                                    doc.set(i, doc.get(i + 1) + "-" + months.get(doc.get(i).toLowerCase()));
                                    doc.remove(i + 1);
                                }
                            }
                        } else {
                            if (doc.get(i).length() != 0 && Character.isUpperCase((doc.get(i).charAt(0)))) {
                                while (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && Character.isUpperCase((doc.get(i + 1).charAt(0)))) {
                                    doc.set(i, doc.get(i) + " " + doc.get(i + 1));
                                    doc.remove(i + 1);//removing cell i+1
                                }
                            } else {
                                /**
                                 * the addition bellow is to determent whether this is a range and not a date
                                 */
                                if (doc.get(i).length() != 0 && isBetweenNumbers(doc, i)) {
                                    doc.set(i, doc.get(i + 1) + "-" + doc.get(i + 3) + "notADateMotherfucker");
                                    doc.remove(i + 1);//removing cells i+1, i+2, i+3
                                    doc.remove(i + 1);
                                    doc.remove(i + 1);
                                } else {
                                    if (doc.get(i).length() != 0 && numbers.containsKey(doc.get(i))) {
                                        doc.set(i, numbers.get(doc.get(i)));
                                        i--;
                                    }
                                }
                            }
                        }
                    }
                    //System.out.println(doc.get(i));
                    else {
                        /**
                         * from here and below we assume that we are facing numbers and not regular words
                         */
                        if (doc.get(i).length() != 0 && isNumeric(doc.get(i))) {
                            if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).matches("(\\d+\\/\\d+)") && isPureNumeric(doc.get(i))) {
                                doc.set(i, doc.get(i) + " " + doc.get(i + 1));
                                doc.remove(i + 1);
                            }
                            if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && (doc.get(i + 1).toLowerCase().equals("percentage") || doc.get(i + 1).toLowerCase().equals("percent"))) {
                                doc.set(i, doc.get(i) + "%");
                                doc.remove(i + 1);
                            } else {
                                if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && isPureNumeric(doc.get(i)) && Double.parseDouble(doc.get(i)) < 32 && months.containsKey(doc.get(i + 1).toLowerCase())) {
                                    if (Double.parseDouble(doc.get(i)) > 9)
                                        doc.set(i, months.get(doc.get(i + 1).toLowerCase()) + "-" + doc.get(i));
                                    else
                                        doc.set(i, months.get(doc.get(i + 1).toLowerCase()) + "-" + "0" + doc.get(i));
                                    doc.remove(i + 1);
                                } else {
//                                    if (isPrice(doc, doc.get(i), i) && (doc.size() - 1 <= i || !isLargePrice(doc.get(i), doc.get(i + 1)))) {
//                                        if (doc.get(i).charAt(0) == '$' && !(doc.get(i).toLowerCase().equals("million") || doc.get(i).toLowerCase().equals("trillion") || doc.get(i).toLowerCase().equals("billion") ||
//                                                doc.get(i).toLowerCase().charAt(doc.get(i).length() - 1) == 'm' || doc.get(i).toLowerCase().charAt(doc.get(i).length() - 1) == 'b')) {
//                                            String temp = doc.get(i).substring(1);
//                                            double numTerm = Double.parseDouble(temp);
//                                            if (numTerm < 1000000)
//                                                doc.set(i, doc.get(i).substring(1) + " Dollars");
//                                        }
//                                    }
                                    if(doc.size()==i+1){
                                        doc.add(i+1,",");
                                        doc.add(i+2,",");
                                        doc.add(i+3,",");
                                    }
                                    else
                                    if(doc.size()==i+2) {
                                        doc.add(i + 2, ",");
                                        doc.add(i + 3, ",");
                                    }
                                    else
                                    if(doc.size()==i+3) {
                                        doc.add(i + 3, ",");
                                    }

                                        if (doc.size() > i + 3 && doc.get(i + 1).length() > 0 && doc.get(i + 2).length() > 0 && doc.get(i + 3).length() > 0
                                            && (isNumeric(doc.get(i)))) {
                                        if (doc.get(i).charAt(0) != '$' && !(((doc.get(i).toLowerCase().equals("m") || (doc.get(i).toLowerCase().equals("bn"))) || doc.size() - 1 > i && doc.get(i + 1).toLowerCase().equals("dollars"))
                                                || ((doc.size() - 3 > i && (doc.get(i + 3).toLowerCase().equals("dollars") && doc.get(i + 2).toLowerCase().equals("u.s.") && (doc.get(i + 1).toLowerCase().equals("million") || doc.get(i + 1).toLowerCase().equals("billion") || doc.get(i + 1).toLowerCase().equals("trillion")))))
                                                || (doc.size() - 2 > i && ((doc.get(i + 2).toLowerCase().equals("dollars") && (doc.get(i + 1).toLowerCase().equals("m") || (doc.get(i + 1).toLowerCase().equals("bn")))))) || !isPureNumeric(doc.get(i)))) {
                                            double numTerm = parseTheInteger(doc, i);
                                            if (numTerm >= 1000000000) {
                                                numTerm = numTerm / 1000000000;
                                                isBillionRound(doc, i, numTerm);

                                            } else {
                                                if (numTerm >= 1000000) {
                                                    numTerm = numTerm / 1000000;
                                                    isMillionRound(doc, i, numTerm);
                                                } else if (numTerm >= 1000) {
                                                    numTerm = numTerm / 1000;
                                                    isThousandRound(doc, i, numTerm);
                                                }
                                                if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("billion")) {
                                                    isBillionRound(doc, i, numTerm);
                                                    doc.remove(i + 1);
                                                } else if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("million")) {
                                                    isMillionRound(doc, i, numTerm);
                                                    doc.remove(i + 1);
                                                } else if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("thousand")) {
                                                    isThousandRound(doc, i, numTerm);
                                                    doc.remove(i + 1);
                                                }
                                            }
                                        } else if (isPrice(doc, doc.get(i), i) && doc.size() - 1 > i && doc.get(i + 1).length() > 0 && !isLargePrice(doc.get(i), doc.get(i + 1))) {
                                            if (doc.get(i).charAt(0) == '$') {
                                                doc.set(i, doc.get(i).substring(1) + " Dollars");
                                            } else if (doc.get(i + 1).toLowerCase().equals("dollars")) {
                                                doc.set(i, doc.get(i) + " Dollars");
                                            }
                                            doc.remove(i + 1);
                                        } else {
                                            if (doc.get(i + 1).length() > 0 && doc.size() - 1 > i) {
                                                if (isLargePrice(doc.get(i), doc.get(i + 1))) {
                                                    if (doc.get(i).charAt(0) == '$') {
                                                        doc.set(i, doc.get(i).substring(1));
                                                        if (doc.get(i + 1).toLowerCase().equals("million")) {
                                                            doc.set(i, doc.get(i) + " M" + " Dollars");
                                                            doc.remove(i + 1);
                                                        } else if (doc.get(i + 1).toLowerCase().equals("billion")) {
                                                            priceToMillion(doc, i);
                                                            doc.remove(i + 1);
                                                        } else {
                                                            double numTerm = parseTheInteger(doc, i);
                                                            if (numTerm >= 1000000000) {
                                                                divideByBillion(doc, i, numTerm);

                                                            } else {
                                                                if (numTerm >= 1000000) {
                                                                    divideByMillion(doc, i, numTerm);
                                                                } else if (numTerm >= 1000) {
                                                                    divideByThousend(doc, i, numTerm);
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        if (doc.size() - 1 > i && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("dollars")) {
                                                            if ((doc.get(i).charAt(doc.get(i).length() - 1) == 'n' && doc.get(i).charAt(doc.get(i).length() - 2) == 'b')) {
                                                                doc.set(i, doc.get(i).substring(0, doc.get(i).length() - 2));
                                                                priceToMillion(doc, i);
                                                            } else {
                                                                if ((doc.get(i).charAt(doc.get(i).length() - 1) == 'm')) {
                                                                    doc.set(i, doc.get(i).substring(0, doc.get(i).length() - 1));
                                                                    double numTerm = parseTheInteger(doc, i);
                                                                    doc.set(i, numTerm + " M" + " Dollars");
                                                                } else {
                                                                    double numTerm = parseTheInteger(doc, i);
                                                                    if (numTerm >= 1000000) {
                                                                        divideByMillion(doc, i, numTerm);
                                                                    }
                                                                }
                                                            }
                                                            doc.remove(i + 1);
                                                        } else {
                                                            if (doc.size() > i + 3 && doc.get(i + 2).length() > 0 && doc.get(i + 3).length() > 0 && doc.get(i + 2).toLowerCase().equals("u.s.") && doc.get(i + 3).toLowerCase().equals("dollars")) {
                                                                if (doc.get(i + 1).toLowerCase().equals("million")) {
                                                                    doc.set(i, doc.get(i) + " M" + " Dollars");
                                                                    doc.remove(i + 1);//removing cells i+1, i+2, i+3
                                                                    doc.remove(i + 1);
                                                                    doc.remove(i + 1);

                                                                } else {
                                                                    if (doc.get(i + 1).toLowerCase().equals("billion")) {
                                                                        double numTerm;
                                                                        if (isComplaxNumber(doc.get(i))) {
                                                                            String[] parsedNam = doc.get(i).split(" ");
                                                                            numTerm = Double.parseDouble(parsedNam[0]) * 1000;
                                                                        } else
                                                                            numTerm = Double.parseDouble(doc.get(i)) * 1000;
                                                                        i = isUsDollarsAndRemove(doc, i, numTerm);
                                                                    } else {
                                                                        if (doc.get(i + 1).toLowerCase().equals("trillion")) {
                                                                            double numTerm;
                                                                            if (isComplaxNumber(doc.get(i))) {
                                                                                String[] parsedNam = doc.get(i).split(" ");
                                                                                numTerm = Double.parseDouble(parsedNam[0]) * 1000000;
                                                                            } else
                                                                                numTerm = Double.parseDouble(doc.get(i)) * 1000000;
                                                                            i = isUsDollarsAndRemove(doc, i, numTerm);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (doc.get(i).charAt(0) == '$') {
                                                    doc.set(i, doc.get(i).substring(1));
                                                    double numTerm = Double.parseDouble(doc.get(i));
                                                    if (numTerm >= 1000000) {
                                                        divideByMillion(doc, i, numTerm);
                                                    } else {
                                                        doc.set(i, doc.get(i) + " Dollars");
                                                    }
                                                }
                                            }
                                        }
//                                    } else {
//                                        if (doc.get(i).charAt(0) == '$') {
//                                            doc.set(i, doc.get(i).substring(1));
//                                            if (i != doc.size() && !(doc.get(i + 1).toLowerCase().equals("million") || doc.get(i + 1).toLowerCase().equals("billion") || doc.get(i + 1).toLowerCase().equals("trillion")))
//                                                doc.add(i + 1, "Dollars");
//                                            else {
//                                                doc.add(i + 2, "u.s.");
//                                                doc.add(i + 3, "Dollars");
//                                                i--;
//                                                continue;
//                                            }
//
//                                        }
//                                        if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("dollars")) {
//                                            if (doc.get(i + 1).toLowerCase().equals("million")) {
//                                                doc.set(i, doc.get(i) + " M" + " Dollars");
//                                                doc.remove(i + 1);//removing cells i+1, i+2, i+3
//                                                doc.remove(i + 1);
//                                                doc.remove(i + 1);
//
//                                            } else {
//                                                if (doc.get(i + 1).toLowerCase().equals("billion")) {
//                                                    double numTerm;
//                                                    if (isComplaxNumber(doc.get(i))) {
//                                                        String[] parsedNam = doc.get(i).split(" ");
//                                                        numTerm = Double.parseDouble(parsedNam[0]) * 1000;
//                                                    } else
//                                                        numTerm = Double.parseDouble(doc.get(i)) * 1000;
//                                                    i = isUsDollarsAndRemove(doc, i, numTerm);
//                                                } else {
//                                                    if (doc.get(i + 1).toLowerCase().equals("trillion")) {
//                                                        double numTerm;
//                                                        if (isComplaxNumber(doc.get(i))) {
//                                                            String[] parsedNam = doc.get(i).split(" ");
//                                                            numTerm = Double.parseDouble(parsedNam[0]) * 1000000;
//                                                        } else
//                                                            numTerm = Double.parseDouble(doc.get(i)) * 1000000;
//                                                        i = isUsDollarsAndRemove(doc, i, numTerm);
//                                                    } else {
//                                                        if (doc.size() > i + 1 && !(doc.get(i + 1).toLowerCase().equals("dollars"))) {
//                                                            double numTerm = parseTheInteger(doc, i);
//                                                            if (numTerm >= 1000000) {
//                                                                numTerm = numTerm / 1000000;
//                                                                isMillionRound(doc, i, numTerm);
//                                                            } else if (numTerm >= 1000) {
//                                                                numTerm = numTerm / 1000;
//                                                                isThousandRound(doc, i, numTerm);
//                                                            }
//                                                            if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("billion")) {
//                                                                isBillionRound(doc, i, numTerm);
//                                                                doc.remove(i + 1);
//                                                            } else if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("million")) {
//                                                                isMillionRound(doc, i, numTerm);
//                                                                doc.remove(i + 1);
//                                                            } else if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("thousand")) {
//                                                                isThousandRound(doc, i, numTerm);
//                                                                doc.remove(i + 1);
//                                                            }
//                                                        } else {
//                                                            double numTerm = parseTheInteger(doc, i);
//                                                            if (numTerm >= 1000000) {
//                                                                numTerm = numTerm / 1000000;
//                                                                if (numTerm % 1 == 0) {
//                                                                    doc.set(i, (int) numTerm + " M");
//                                                                } else {
//                                                                    doc.set(i, numTerm + " M");
//                                                                }
//                                                            } else if (numTerm >= 1000) {
//                                                                numTerm = numTerm / 1000;
//                                                                if (numTerm % 1 == 0) {
//                                                                    doc.set(i, (int) numTerm + " K");
//                                                                } else {
//                                                                    doc.set(i, numTerm + " K");
//                                                                }
//                                                            }
//                                                            if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("billion")) {
//                                                                isBillionRound(doc, i, numTerm);
//                                                                doc.remove(i + 1);
//                                                            } else if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("million")) {
//                                                                isMillionRound(doc, i, numTerm);
//                                                                doc.remove(i + 1);
//                                                            } else if (doc.size() > i + 1 && doc.get(i + 1).length() > 0 && doc.get(i + 1).toLowerCase().equals("thousand")) {
//                                                                isThousandRound(doc, i, numTerm);
//                                                                doc.remove(i + 1);
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        } else {
//                                            if (isNumeric(doc.get(i))) {
//                                                double numTerm = parseTheInteger(doc, i);
//                                                if (numTerm >= 1000000000) {
//                                                    numTerm = numTerm / 1000000000;
//                                                    if (numTerm % 1 == 0) {
//                                                        doc.set(i, (int) numTerm + " B");
//                                                    } else {
//                                                        doc.set(i, numTerm + " B");
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        if (doc.size() - 1 > i && doc.get(i + 1).toLowerCase().equals("dollars")) {
//                                            doc.set(i, doc.get(i) + " Dollars");
//                                            doc.remove(i + 1);
//                                        }
                                    }
                                }
                            }
                        } else {
                            /**
                             * remove punctuation only words
                             */
                            String punctOnly = "\\p{Punct}+";
                            if (doc.get(i).matches(punctOnly))
                                doc.remove(i);
                        }
                    }


                    /**
                     *from here and below we classify each term into 4 categories: dates, numbers, entities and regular words
                     */
                    if (i >= 0 && doc.get(i).length() > 0) {
                        if (isModifiedDate(doc.get(i))) {
                            //System.out.println("date " + doc.get(n));
                            terms.put(doc.get(i), (byte) 1);

                        } else {
                            if (isModifiedEntity(doc.get(i)) && !doc.get(i).endsWith("notADateMotherfucker")) {
                                //System.out.println("entity "+doc.get(n));
                                terms.put(doc.get(i), (byte) 3);
                            } else {
                                if (doc.get(i).endsWith("notADateMotherfucker"))
                                    doc.set(i, doc.get(i).substring(0, doc.get(i).length() - 20));
                                if (isModifiedNum(doc.get(i))) {

                                    //System.out.println("num "+term);
                                    terms.put(doc.get(i), (byte) 2);

                                } else { //regular word
                                    boolean putOnTerms = false;
                                    String base = doc.get(i);
                                    if (toStem) {
                                        Stemmer stmr = new Stemmer();
                                        stmr.setCurrent(doc.get(i));
                                        stmr.stem();
                                        base = stmr.getCurrent();
                                    }
                                    if (!(stopwords.contains(base.toLowerCase()))
                                            && (isSimpleWord(base.toLowerCase()))) { //not a stop word and is a simple word
                                        if (!terms.containsKey(base.toLowerCase()) && !terms.containsKey(base.toUpperCase())) {
                                            //first time we faced this word
                                            if (Character.isUpperCase(base.charAt(0))) {
                                                terms.put(base.toUpperCase(), (byte) 0);
                                                doc.set(i, base.toUpperCase());
                                                putOnTerms = true;
                                            } else {
                                                terms.put(base.toLowerCase(), (byte) 0);
                                                doc.set(i, base.toLowerCase());
                                                putOnTerms = true;
                                            }
                                        } else {
                                            if (terms.containsKey(base.toUpperCase())) {
                                                if (Character.isLowerCase(base.charAt(0))) {
                                                    terms.remove(base.toUpperCase());
                                                    terms.put(base.toLowerCase(), (byte) 0);
                                                    doc.set(i, base.toLowerCase());
                                                    putOnTerms = true;
                                                }
                                            }
                                        }
                                        // System.out.println("reg: "+base+" *"+putOnTerms+"*");
                                    } // not a stop word
                                    else { // a stop word
                                        doc.remove(i);
                                        i--;
                                    }
                                }// regular word
                            }
                        }
                    }
                }
            }
            /**
             * remove "null"s once again
             */
            for (int j = 0; j < doc.size(); j++) {
                if (doc.get(j).length() <= 0) {
                    doc.remove(j);
                    j--;
                }
            }
            DocData newDoc = new DocData(docLength, doc, terms);

            toIndexer.put((String) pair.getKey(), newDoc);
            it.remove(); // avoids a ConcurrentModificationException

        }
        return toIndexer;
    }

    /**
     * funcrion that takes a number and divides it by Billion and concat a 'B' next to him. if it is a round number, it cast it to int
     *
     * @param doc
     * @param i
     * @param numTerm
     */
    private void divideByBillion(ArrayList<String> doc, int i, double numTerm) {
        numTerm = numTerm / 1000000000;
        if (numTerm % 1 == 0)
            doc.set(i, (int) numTerm + " M" + " Dollars");
        else {
            doc.set(i, numTerm + " M" + " Dollars");
        }
    }

    /**
     * funcrion that takes a number and divides it by thousand and concat a 'K' next to him. if it is a round number, it cast it to int
     *
     * @param doc
     * @param i
     * @param numTerm
     */
    private void divideByThousend(ArrayList<String> doc, int i, double numTerm) {
        numTerm = numTerm / 1000;
        if (numTerm % 1 == 0)
            doc.set(i, (int) numTerm + " M" + " Dollars");
        else {
            doc.set(i, numTerm + " M" + " Dollars");
        }
    }

    /**
     * function that checks if a string is some kind of a price (according to the rules determined)
     *
     * @param doc
     * @param s
     * @param i
     * @return
     */
    private boolean isPrice(ArrayList<String> doc, String s, int i) {
        if (s.charAt(0) == '$')
            return true;
        else if (i + 1 >= doc.size())
            return false;
        else {
            if (doc.get(i + 1).toLowerCase().equals("dollars") || (i + 3 >= doc.size() && doc.get(i + 2).toLowerCase().equals("u.s.") && doc.get(i + 3).toLowerCase().equals("dollars")))
                return true;
        }
        return false;
    }

    /**
     * checks whether a word is a word that doesn't part of an entity according to the rules
     *
     * @param base
     * @return
     */
    private boolean isSimpleWord(String base) {
        if (base.length() < 1)
            return false;
        if (!Character.isLetter(base.charAt(0)))
            return false;
        char[] chars = base.toCharArray();
        for (char c : chars) {
            if (!(Character.isLetter(c) || c == '.' || c == '\'')) {
                return false;
            }
        }
        char c = base.charAt(0);
        if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
            return false;
        }

        return true;
    }


    /**
     * checks whether a string contains chars that includes numbers only
     *
     * @param str
     * @return
     */
    private static boolean isPureNumeric(String str) {
        return NumberUtils.isNumber(str);

//        return str.matches("(\\d+(\\.\\d+)?)");
    }

    /**
     * takes a number (could be a complax) and takes is whole part only
     *
     * @param doc
     * @param i
     * @return
     */
    private double parseTheInteger(ArrayList<String> doc, int i) {
        double numTerm;
        if (isComplaxNumber(doc.get(i))) {
            String[] parsedNam = doc.get(i).split(" ");
            numTerm = Double.parseDouble(parsedNam[0]);
        } else
            numTerm = Double.parseDouble(doc.get(i));
        return numTerm;
    }

    /**
     * funcrion that takes a number and divides it by thousand and concat a 'M' next to him. if it is a round number, it cast it to int
     *
     * @param doc
     * @param i
     * @param numTerm
     */
    private void divideByMillion(ArrayList<String> doc, int i, double numTerm) {
        numTerm = numTerm / 1000000;
        if (numTerm % 1 == 0)
            doc.set(i, (int) numTerm + " M" + " Dollars");
        else {
            doc.set(i, numTerm + " M" + " Dollars");
        }
    }

    /**
     * checks whether a term will categorized as a number (it could be a price or percentage)
     *
     * @param term
     * @return
     */
    private boolean isModifiedNum(String term) {
        String[] arr = term.split(" ");
        if (arr.length < 1)
            return false;
        if (arr[0].length() == 0)
            return false;
        if (((arr[0].charAt(arr[0].length() - 1) == 'K' || arr[0].charAt(arr[0].length() - 1) == 'B' || arr[0].charAt(arr[0].length() - 1) == 'M') && (isPureNumeric(arr[0].substring(0, arr[0].length() - 1)))) || (arr[0].charAt(arr[0].length() - 1) == '%' && (isPureNumeric(arr[0].substring(0, arr[0].length() - 1)))) || (isPureNumeric(arr[0])))
            return true;
        return (term.contains("-") && term.charAt(0) != '-' && term.charAt(term.length() - 1) != '-');

    }

    /**
     * checks whether a term will categorized as a entity (all capitals in the first char)
     *
     * @param term
     * @return
     */
    private boolean isModifiedEntity(String term) {
        term = term.replaceAll("-", " ");
        String[] arr = term.split(" ");
        int wordsCounter = 0;
        for (String word : arr
        ) {
            if (word.length() != 0) {
                wordsCounter++;
                if ((Character.isDigit(word.charAt(0)) || !(Character.isUpperCase(word.charAt(0)))))
                    return false;
            }
        }
        if (wordsCounter <= 1)
            return false;
        return true;
    }

    /**
     * checks whether a term will categorized as a date (with a complex regex that decide if its a date)
     *
     * @param term
     * @return
     */
    private boolean isModifiedDate(String term) {

        String regex1 = "^([0-9]{4})-(0[1-9]|1[012])$";
        String regex2 = "^(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";
        return term.matches(regex1) || term.matches(regex2);
    }

    /**
     * checks whether a number is a complax
     *
     * @param num
     * @return
     */
    private boolean isComplaxNumber(String num) {
        if (num.charAt(0) == '$')
            return false;
        if (num.charAt(num.length() - 1) == '/')
            return false;
        if (!num.contains(" ") && !num.contains("/"))
            return false;
        String[] parsedNum = num.split(" ");
        if (parsedNum[0].contains("/"))
            return false;
        int numOfSlash = 0;
        for (int i = 0; i < num.length(); i++) {
            if (num.charAt(i) == '/') {
                numOfSlash++;
                if (numOfSlash == 2)
                    return false;
            }
        }
        return true;
    }

    /**
     * cast to int (if its a round number) and determints if an expression is from the type: "X million u.s. dollars"
     *
     * @param doc
     * @param i
     * @param numTerm
     * @return
     */
    private int isUsDollarsAndRemove(ArrayList<String> doc, int i, double numTerm) {
        if (numTerm % 1 == 0) {
            doc.set(i, (int) numTerm + " M" + " Dollars");
        } else {
            doc.set(i, numTerm + " M" + " Dollars");
        }
        doc.remove(i + 1);//removing cells i+1, i+2, i+3
        doc.remove(i + 1);
        doc.remove(i + 1);
        return i;
    }

    /**
     * cast a number to int if necessary and transform it to a price in millions (from price in billions)
     *
     * @param doc
     * @param i
     */
    private void priceToMillion(ArrayList<String> doc, int i) {
        double numTerm = Double.parseDouble(doc.get(i).replaceAll("\\s+", "")) * 1000;
        if (numTerm % 1 == 0) {
            doc.set(i, (int) numTerm + " M" + " Dollars");
        } else {
            doc.set(i, doc.get(i).replaceAll("\\s+", "") + numTerm + " M" + " Dollars");
        }
    }

    /**
     * checks if a number round and cast it to int if so (For thousand)
     *
     * @param doc
     * @param i
     * @param numTerm
     */
    private void isThousandRound(ArrayList doc, int i, double numTerm) {
        if (numTerm % 1 == 0) {
            doc.set(i, (int) numTerm + "K");
        } else {
            doc.set(i, numTerm + "K");
        }
    }

    /**
     * checks if a number round and cast it to int if so (For millions)
     *
     * @param doc
     * @param i
     * @param numTerm
     */
    private void isMillionRound(ArrayList doc, int i, double numTerm) {
        if (numTerm % 1 == 0) {
            doc.set(i, (int) numTerm + "M");
        } else {
            doc.set(i, numTerm + "M");
        }
    }

    /**
     * checks if a number round and cast it to int if so (For billions)
     *
     * @param doc
     * @param i
     * @param numTerm
     */
    private void isBillionRound(ArrayList doc, int i, double numTerm) {
        if (numTerm % 1 == 0) {
            doc.set(i, (int) numTerm + "B");
        } else {
            doc.set(i, numTerm + "B");
        }
    }

    /**
     * checks if a certain string is some kind of number (price, date, percentage, etc.)
     *
     * @param str
     * @return
     */
    private boolean isNumeric(String str) {
        String temp = str;
        temp = temp.replaceAll(",", "");

        if (temp.charAt(0) == '$') {
            temp = temp.substring(1);
            if (temp.length() == 0)
                return false;
        }
        if (temp.length() == 0)
            return false;
        if ((temp.charAt(temp.length() - 1) == 'n' && ((temp.length() > 1) && temp.charAt(temp.length() - 2) == 'b'))) {
            temp = temp.substring(0, temp.length() - 2);
        }
        if (temp.length() == 0)
            return false;
        if (temp.charAt(temp.length() - 1) == 'm') {
            temp = temp.substring(0, temp.length() - 1);
        }
        String regex = "\\d+([.]\\d+|(\\s\\d+)?[/]\\d+)?";
        if (temp.matches(regex))
            return true;
        return false;
    }

    /**
     * checks if a several string assemble the expression "betweeb X and Y"
     *
     * @param doc
     * @param i
     * @return
     */
    private boolean isBetweenNumbers(ArrayList<String> doc, int i) {
        return (doc.get(i).toLowerCase().equals(("between")) && i < doc.size() - 3 && isNumeric(doc.get(i + 1)) && doc.get(i + 2).toLowerCase().equals("and")) && isNumeric(doc.get(i + 3));
    }

    /**
     * checks if a price is bigger or equals to one million dollars
     *
     * @param num
     * @param possibleValue
     * @return
     */
    private boolean isLargePrice(String num, String possibleValue) {

        String temp = num;
        String[] tempArray1 = temp.split(" ");
        if (tempArray1[0].contains("/"))
            return false;
        if (((temp.charAt(temp.length() - 1) == 'n' && temp.charAt(temp.length() - 2) == 'b') || temp.charAt(temp.length() - 1) == 'm') && temp.charAt(0) == '$')
            return false;
        if (temp.charAt(0) == '$') {
            temp = temp.substring(1);
        }
        if ((temp.charAt(temp.length() - 1) == 'n' && temp.charAt(temp.length() - 2) == 'b') || temp.charAt(temp.length() - 1) == 'm' || possibleValue.toLowerCase().equals("billion") || possibleValue.toLowerCase().equals("million")) {
            return true;
        } else {
            String[] tempArray = temp.split(" ");
            //System.out.println(tempArray[0]);
            if (Double.parseDouble(tempArray[0]) >= 1000000)
                return true;
        }

        return false;
    }


}

