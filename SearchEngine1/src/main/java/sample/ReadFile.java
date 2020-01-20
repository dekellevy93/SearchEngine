package sample;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class ReadFile {
    /**
     * Reads a corpus file and splits it to doc.no and the text in the doc.
     * @param path path of the file to read
     * @param docsFile the file to read
     * @return map of key doc.no and val text in the doc.
     * @throws FileNotFoundException
     */
    public HashMap<String, String> readCorpusFolder(String path, File docsFile) throws FileNotFoundException {
        HashMap<String, String> docs = new HashMap<String, String>();
        StringBuilder textContent = new StringBuilder("");
        StringBuilder docContent = new StringBuilder("");
        StringBuilder docNO = new StringBuilder("");
        StringBuilder line = new StringBuilder();
        String lineTemp;
        StringBuilder docNoLineTrim1 = new StringBuilder("");
        StringBuilder docNoLineTrim2 = new StringBuilder("");

        FileReader fileReader=new FileReader(docsFile);
        BufferedReader bufferedReaderr = null;
        try {
            bufferedReaderr=new BufferedReader(fileReader);
            while((lineTemp = bufferedReaderr.readLine()) != null) {
                line.append(lineTemp);
                if (line.toString().contains("<DOC>")) { //new documen
                    // if(scan.hasNext()) {
                    //   scan.nextLine();
                    line.setLength(0);
                    line.append(bufferedReaderr.readLine()); // DocNO
                    docNoLineTrim1.append(line.substring(7));
                    docNoLineTrim2.append(docNoLineTrim1.substring(0, docNoLineTrim1.length() - 8));
                    docNO.setLength(0);
                    docNO.append(docNoLineTrim2.toString().trim());
                    //  System.out.println(docNO);
                    //  docContent.append("\n" + scan.useDelimiter("</DOC>").next()); // entire doc
                    // System.out.println(docContent.length());
                    //  System.out.println(docContent);
                    line.setLength(0);
                    Boolean docHasText = false;
                    while (!line.toString().contains("</DOC>") && (lineTemp = bufferedReaderr.readLine()) != null) {
                        line.setLength(0);
                        line.append(lineTemp);
                        if (line.toString().contains("<TEXT>")) {
                            docHasText = true;
                            while (!line.toString().contains("</TEXT>")) {
                                line.setLength(0);
                                if ((lineTemp = bufferedReaderr.readLine()) != null) {
                                    line.append(lineTemp);
                                    if (!line.toString().contains("</TEXT>")) {
                                        if (line.length() > 0 && line.charAt(line.length() - 1) != ' ') { // add space at the end of the line
                                            line.append(" ");
                                        }
                                        int indexOpen = line.indexOf("<");
                                        int indexClose = line.indexOf(">");
                                        while (indexClose != -1 && indexOpen != -1) {
                                            String s=line.substring(0, indexOpen) + " " + line.substring(indexClose + 1);
                                            line.setLength(0);
                                            line.append(s);
                                            indexOpen = line.indexOf("<");
                                            indexClose = line.indexOf(">");
                                        }
                                        textContent.append(line);
                                    }
                                } else {
                                    docHasText = false;
                                    break;
                                }
                                //  if(file.getName().equals("FB396070"))
                                //  System.out.println(line);
                            }
                        }
                    }
                    if (!docHasText) {
                        docs.put(docNO.toString(), "");
                        //     System.out.println("reached here "+ counter + " "+ docNO);
                        //    counter++;
                    } else {
                        docs.put(docNO.toString(), textContent.toString());
                    }
                    line.setLength(0);
                    docNO.setLength(0);
                    docNoLineTrim1.setLength(0);
                    docNoLineTrim2.setLength(0);
                    docContent.setLength(0);
                    textContent.setLength(0);
                }
            }
            return docs;

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
        return docs;

        // System.out.println(docsFile.getName());
      /*  Scanner scan = new Scanner(docsFile);
        while (scan.hasNext()) {
            // scan.useDelimiter("<DOC>").next();
            line.append(scan.nextLine());
            if (line.toString().contains("<DOC>")) { //new documen
                // if(scan.hasNext()) {
                //   scan.nextLine();
                line.setLength(0);
                line.append(scan.nextLine()); // DocNO
                docNoLineTrim1.append(line.substring(7));
                docNoLineTrim2.append(docNoLineTrim1.substring(0, docNoLineTrim1.length() - 8));
                docNO.setLength(0);
                docNO.append(docNoLineTrim2.toString().trim());
                //  System.out.println(docNO);
                //  docContent.append("\n" + scan.useDelimiter("</DOC>").next()); // entire doc
                // System.out.println(docContent.length());
                //  System.out.println(docContent);
                line.setLength(0);
                Boolean docHasText = false;
                while (!line.toString().contains("</DOC>") && scan.hasNext()) {
                    line.setLength(0);
                    line.append(scan.nextLine());
                    if (line.toString().contains("<TEXT>")) {
                        docHasText = true;
                        while (!line.toString().contains("</TEXT>")) {
                            line.setLength(0);
                            if (scan.hasNextLine()) {
                                line.append(scan.nextLine());
                                if (!line.toString().contains("</TEXT>")) {
                                    if (line.length() > 0 && line.charAt(line.length() - 1) != ' ') { // add space at the end of the line
                                        line.append(" ");
                                    }
                                    int indexOpen = line.indexOf("<");
                                    int indexClose = line.indexOf(">");
                                    while (indexClose != -1 && indexOpen != -1) {
                                        String s=line.substring(0, indexOpen) + " " + line.substring(indexClose + 1);
                                        line.setLength(0);
                                        line.append(s);
                                        indexOpen = line.indexOf("<");
                                        indexClose = line.indexOf(">");
                                    }
                                    textContent.append(line);
                                }
                            } else {
                                docHasText = false;
                                break;
                            }
                            //  if(file.getName().equals("FB396070"))
                            //  System.out.println(line);
                        }
                    }
                }
                if (!docHasText) {
                    docs.put(docNO.toString(), "");
                    //     System.out.println("reached here "+ counter + " "+ docNO);
                    //    counter++;
                } else {
                    docs.put(docNO.toString(), textContent.toString());
                }
                line.setLength(0);
                docNO.setLength(0);
                docNoLineTrim1.setLength(0);
                docNoLineTrim2.setLength(0);
                docContent.setLength(0);
                textContent.setLength(0);
            }
        }
        scan.close();*/
        //return docs;
    }


}


