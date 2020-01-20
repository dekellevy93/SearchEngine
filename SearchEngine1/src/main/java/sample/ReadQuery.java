package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class ReadQuery {
    private String pathToQueryFile;
    private HashMap<String, String> descs; //key is query number
    private HashMap<String, String> narrs;

    public ReadQuery (String path){
        this.pathToQueryFile = path;
        descs = new HashMap<>();
        narrs = new HashMap<>();
    }

    public HashMap<String, String> getQueries() throws FileNotFoundException { //key is q num, val is q
        HashMap<String, String> queriesMap = new HashMap<>();
        File queryFile = new File(pathToQueryFile);
        Scanner queryScan = new Scanner(queryFile);
        StringBuilder lineQuery = new StringBuilder("");
        StringBuilder qNumber = new StringBuilder("");
        StringBuilder qDesc = new StringBuilder("");
        StringBuilder qNarr = new StringBuilder("");

        StringBuilder qTitle = new StringBuilder("");
        while(queryScan.hasNext()){
            lineQuery.append(queryScan.nextLine());
            String line = lineQuery.toString();
            if(line.startsWith("<num")){
                qNumber.append(line.substring(line.indexOf(">")+10));
                line = queryScan.nextLine(); //title
                qTitle.append(line.substring(line.indexOf(">")+2));
                queriesMap.put(qNumber.toString(),qTitle.toString());
               // qNumber.setLength(0);
                qTitle.setLength(0);
            }
            if(line.startsWith("<desc")){
                line = queryScan.nextLine(); //start desc
                while(!line.startsWith("<")){ //end of desc
                    if (line.length() > 0 && line.charAt(line.length() - 1) != ' ') { // add space at the end of the line
                        line +=(" ");
                    }
                    qDesc.append(line);
                    line = queryScan.nextLine();
                }
                descs.put(qNumber.toString(), qDesc.toString());
                qDesc.setLength(0);
            }
            if(line.startsWith("<narr")){
                line = queryScan.nextLine(); //start narr
                boolean relevant = true; //drop non-relevant narratives
                while(!line.startsWith("<")){ //end of narr
                    if(line.contains("not relevant")){
                        relevant = false;
                    }
                    if (line.length() > 0 && line.charAt(line.length() - 1) != ' ') { // add space at the end of the line
                        line +=(" ");
                    }
                    qNarr.append(line);
                    line = queryScan.nextLine();
                }
                if(!relevant)
                    qNarr.setLength(0);
                narrs.put(qNumber.toString(), qNarr.toString());
                qNarr.setLength(0);
                qNumber.setLength(0);
            }


            lineQuery.setLength(0);

        }
        return queriesMap;
    }

    public HashMap<String, String> getDescs() {
        return descs;
    }

    public HashMap<String, String> getNarrs() {
        return narrs;
    }
}
