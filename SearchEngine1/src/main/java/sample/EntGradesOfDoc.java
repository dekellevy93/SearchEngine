package sample;

import java.util.*;
import java.util.Map.Entry;

public class EntGradesOfDoc {
    private String docNo;
    private HashMap<String, Double> entGrades;
    private Set<Entry<String, Double>> top5Grades;


    public EntGradesOfDoc(String docNo) {
        this.docNo = docNo;
        entGrades = new HashMap<>();
    }

    public void addGrade(String term, double grade){
        entGrades.put(term, grade);
    }

    public void sortGrades(){
        this.top5Grades = sortTop5Grades();
    }

    /**
     * Use this when you want to get set of top 5 entities of doc sorted by grade.
     * DO NOT use sortTop5Grades and sortGrades!
     * @return
     */
    public Set<Entry<String, Double>> getTop5Grades (){
        return top5Grades;
    }

    private Set<Entry<String, Double>> sortTop5Grades (){
        Comparator<Map.Entry<String, Double>> valueComparator = new Comparator<Entry<String,Double>>() {
            @Override
            public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
                Double v1 = e1.getValue();
                Double v2 = e2.getValue();
                return v2.compareTo(v1);
            }
        };

        Set<Entry<String, Double>> entries = entGrades.entrySet();
        // Sort method needs a List, so let's first convert Set to List in Java
        List<Entry<String, Double>> listOfEntries = new ArrayList<Entry<String, Double>>(entries);

        // sorting HashMap by values using comparator
        Collections.sort(listOfEntries, valueComparator);
        LinkedHashMap<String, Double> sortedByValue = new LinkedHashMap<String, Double>(listOfEntries.size());

        // copying entries from List to Map, only top 5
        int i = 1;
        for(Entry<String, Double> entry : listOfEntries){
            if(i <= 5) {
                sortedByValue.put(entry.getKey(), entry.getValue());
                i++;
            }
        }

        //HashMap after sorting entries by values

        return sortedByValue.entrySet();

        //syntax Example:
        /*for(Entry<String, String> mapping : entrySetSortedByValue){
            System.out.println(mapping.getKey() + " ==> " + mapping.getValue());
        }*/
    }
}
