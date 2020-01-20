package sample;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Indexer indexer = new Indexer();
        Searcher searcher = new Searcher();
        ArrayList[] dictionaryToShow = {new ArrayList()};

        StackPane root = new StackPane();
        primaryStage.setTitle("Welcome to BS! which is Ben-Gurion Searcher and definitely not BullShit");

        String[] paths = new String[4];
        Boolean[] toStem = new Boolean[2];
        Boolean[] wasSearchedForResults = new Boolean[2]; //wasSearchedForResults[1]: true if user typed text, false if user chose query file
        toStem[0] = false;
        toStem[1] = false;
        wasSearchedForResults[0]=false;
        wasSearchedForResults[1] = true;

        Button browseCorpus = new Button();
        browseCorpus.setText("Browse corpus");
        browseCorpus.setTranslateX(-200);
        browseCorpus.setTranslateY(-100);
        EventHandler<ActionEvent> corpusHandler = new EventHandler<ActionEvent>() {
            /**
             *  handler to corpus button
             * @param event
             */


            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser corpus = new DirectoryChooser();
                corpus.setTitle("Browse corpus");
                HBox browse = new HBox();
                browse.setSpacing(20);
                Scene scene = new Scene(browse, 350, 100);
                paths[0] = corpus.showDialog(primaryStage).getAbsolutePath();
                StackPane root = new StackPane();
                root.getChildren().add(browse);
            }
        };

        Button browsePostings = new Button();
        browsePostings.setText("Browse folder for posting files");
        browsePostings.setTranslateX(-161);
        browsePostings.setTranslateY(-70);
        EventHandler<ActionEvent> postingHandler = new EventHandler<ActionEvent>() {
            @Override
            /**
             *  handler to posting files button
             * @param event
             */
            public void handle(ActionEvent event) {
                DirectoryChooser corpus = new DirectoryChooser();
                corpus.setTitle("Browse folder for posting files");
                HBox browse = new HBox();
                browse.setSpacing(20);
                Scene scene = new Scene(browse, 350, 100);
                paths[1] = corpus.showDialog(primaryStage).getAbsolutePath();
                searcher.setWorkPlacePath(paths[1]);

            }
        };

        Button deletePostings = new Button();
        deletePostings.setText("Delete posting files and dictionary");
        deletePostings.setTranslateX(-150);
        deletePostings.setTranslateY(-40);
        EventHandler<ActionEvent> deletePostingHandler = new EventHandler<ActionEvent>() {
            @Override
            /**
             *  handler to delete dictionary and posting files button
             * @param event
             */
            public void handle(ActionEvent event) {
                if (paths[1] != null) {
                    File toDelete = new File(paths[1]);
                    File[] files = toDelete.listFiles();
                    if (files.length == 0) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "The directory is already empty", ButtonType.OK);
                        alert.showAndWait();
                    } else {
                        try {
                            FileUtils.cleanDirectory(toDelete);
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Posting files and dictionary deleted successfully", ButtonType.OK);
                            alert.showAndWait();
                        } catch (IOException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Wrong path! please select a different path", ButtonType.OK);
                            alert.showAndWait();
                        }
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Wrong path! please select a different path", ButtonType.OK);
                    alert.showAndWait();
                }
            }
        };

        Button loadDictionary = new Button();
        loadDictionary.setText("Load dictionary");
        loadDictionary.setTranslateX(-200);
        loadDictionary.setTranslateY(-10);
        EventHandler<ActionEvent> loadDictionaryHandler = new EventHandler<ActionEvent>() {
            /**
             *  handler to load the dictionary (according to stem or not stem dictionaries)
             * @param event
             */

            public void handle(ActionEvent event) {
                if (paths[1] != null) {
                    try {
                        dictionaryToShow[0] = indexer.getDictionaryInRam(paths[1], toStem[0]);
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Dictionary loaded to RAM", ButtonType.OK);
                        alert.showAndWait();
                    } catch (FileNotFoundException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "The dictionary does not exist", ButtonType.OK);
                        alert.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "You didn't insert a path to the dictionary!", ButtonType.OK);
                    alert.showAndWait();
                }
            }
        };

        Button showDictionary = new Button();
        showDictionary.setText("Show dictionary");
        showDictionary.setTranslateX(-200);
        showDictionary.setTranslateY(20);
        EventHandler<ActionEvent> showDictionaryHandler = new EventHandler<ActionEvent>() {
            @Override
            /**
             *  handler to show dictionary (according to stem or not stem dictionaries)
             * @param event
             */
            public void handle(ActionEvent event) {

                if (paths[1] != null) {

                    ArrayList<String> termsToAdd = new ArrayList<>();
                    ArrayList<String> frequencies = new ArrayList<>();

                    if (dictionaryToShow[0] != null) {
                        ObservableList<String> observableDictionary = FXCollections.observableArrayList(dictionaryToShow[0]);
                        for (int i = 0; i < observableDictionary.size(); i++) {
                            termsToAdd.add((getNameFromLine(observableDictionary.get(i))));
                            frequencies.add((String.valueOf(getTotalFreqFromDictionaryLine(observableDictionary.get(i)))));
                        }


                        TableView tableView = new TableView();

                        TableColumn<String, Tuple> terms = new TableColumn<>("Terms");
                        terms.setCellValueFactory(new PropertyValueFactory<>("term"));


                        TableColumn<String, Tuple> freq = new TableColumn<>("Frequencies");
                        freq.setCellValueFactory(new PropertyValueFactory<>("frequency"));
                        tableView.getColumns().addAll(terms, freq);

                        for (int i = 0; i < observableDictionary.size(); i++) {
                            tableView.getItems().add(new Tuple(termsToAdd.get(i), frequencies.get(i)));
                        }
                        tableView.setMinHeight(700);
                        VBox vbox = new VBox(tableView);
                        vbox.setMinHeight(700);
                        Scene secondScene = new Scene(vbox);

                        // New window (Stage)
                        Stage newWindow = new Stage();
                        newWindow.setTitle("Dictionary");
                        newWindow.setScene(secondScene);
                        // Set position of second window, related to primary window.
                        newWindow.setX(primaryStage.getX() + 200);
                        newWindow.setY(primaryStage.getY() - 200);
                        newWindow.setHeight(700);
                        newWindow.show();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "The dictionary does not exist", ButtonType.OK);
                        alert.showAndWait();
                    }
                }
            }
        };


        // create a checkbox
        CheckBox c = new CheckBox("Stem?");
        c.setTranslateX(-220);
        c.setTranslateY(65);
        // add checkbox
        root.getChildren().add(c);

        // create a event handler
        EventHandler<ActionEvent> checkBoxEvent = new EventHandler<ActionEvent>() {
            /**
             * handler for stem? button
             * @param e
             */
            public void handle(ActionEvent e) {
                if (c.isSelected()) {
                    toStem[0] = true;
                    searcher.setStem("true");
                } else {
                    toStem[0] = false;
                    searcher.setStem("false");

                }
            }

        };

        Button start = new Button();
        start.setText("Start BS-ing!");
        start.setTranslateX(-213);
        start.setTranslateY(100);
        EventHandler<ActionEvent> startHandler = new EventHandler<ActionEvent>() {
            /**
             * handler for start button
             * @param event
             */
            @Override
            public void handle(ActionEvent event) {
                if (paths[0] != null && paths[1] != null) {
                    long startTime = System.nanoTime();
                    try {
                        indexer.toIndex(paths[0], paths[1], toStem[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    long endTime = System.nanoTime();
                    long totalTime = endTime - startTime;
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "the process took: " + (double) totalTime / 1000000000 + " seconds, the number of unique terms in the dictionary is: " + indexer.getSizeOfDictionary()
                            + " and the number of docs indexed is: " + indexer.getNumOfIndexedDocs() + " and the total time to index all the terms: " + indexer.getTotalTimeToBuildIndex() + " minutes", ButtonType.OK);
                    alert.showAndWait();
                } else {
                    if (paths[0] == null) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Did not entered corpus path! please try again", ButtonType.OK);
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Did not entered posting files path! please try again", ButtonType.OK);
                        alert.showAndWait();
                    }
                }
            }
        };


        StringBuilder output = new StringBuilder("");

//Defining the Name text field
        final TextField query = new TextField();
        query.setPromptText("Type your query and press \"Search for docs\"");
        query.setTranslateX(103);
        query.setTranslateY(-100);
        query.setPrefWidth(264);
        query.setMaxWidth(264);
        paths[2] = query.getText();
        GridPane.setConstraints(query, 0, 0);
        root.getChildren().add(query);
//Defining the Last Name text field
        Button search = new Button("Search for docs");
        search.setTranslateX(20);
        search.setTranslateY(-70);
        GridPane.setConstraints(search, 1, 0);
        root.getChildren().add(search);

        query.textProperty().addListener((observable, oldValue, newValue) -> {
            wasSearchedForResults[1] = true; // search button will search from text field
        });
        /**
         * handler for Submit button
         * @param event
         */
//Setting an action for the Submit button
        search.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if(wasSearchedForResults[1]){ // search button will search from text field
                   // if (paths[2] != null) {
                        try {
                            if (paths[1] != null) {
                                if (toStem[0]) {
                                    searcher.setStem("stem");
                                } else {
                                    searcher.setStem("");
                                }
                                searcher.setQuery(query.getText());
                                searcher.setqNum("111");
                                searcher.resetResults();
                                output.append(query.getText() + "\n");
                                ArrayList<String> results = searcher.search();

                                Text header = new Text();
                                header.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
                                header.setX(40);
                                header.setY(20);
                                header.setText("Showing " + results.size() + " Results For: " + query.getText());

                                Text text = new Text();

                                //Setting the text to be added.
                                StringBuilder resultsText = new StringBuilder("");

                                for (String result : results)
                                    resultsText.append(result + "\n");
                                text.setText(resultsText.toString());
                                text.setX(50);
                                text.setY(38);

                                Group root1 = new Group(header, text);
                                wasSearchedForResults[0]=true;//we Searched!

                                //Creating a scene object
                                ScrollPane sp = new ScrollPane();
                                sp.pannableProperty().set(true);
                                sp.setContent(root1);
                                Scene scene = new Scene(sp, 900, 500);

                                //Setting title to the Stage
                                Stage stage = new Stage();
                                stage.setTitle("Search results");

                                //Adding scene to the stage
                                stage.setScene(scene);

                                //Displaying the contents of the stage
                                stage.show();

                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Didn't inserted posting files path! please try again", ButtonType.OK);
                                alert.showAndWait();
                            }
                        } catch (IOException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "wrong path! please try again", ButtonType.OK);
                            alert.showAndWait();
                        }
                   /* } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Did not entered query or path! please try again", ButtonType.OK);
                        alert.showAndWait();
                    }*/
                }
                else{// search button will search from file
                    if (paths[2] != null) {
                        try {
                            if (paths[1] != null) {
                                if (toStem[0]) {
                                    searcher.setStem("stem");
                                } else {
                                    searcher.setStem("");
                                }
                            }
                            if (paths[1] != null) {
                                ReadQuery readQuery = new ReadQuery(paths[2]);
                                HashMap<String, String> queries = readQuery.getQueries();
                                HashMap<String, String> descs = readQuery.getDescs();
                                HashMap<String, String> narrs = readQuery.getNarrs();
                                int indexForTexts = 0;
                                Text[] headers = new Text[queries.size()];
                                Text[] texts = new Text[queries.size()];
                                searcher.resetResults();
                                for (String qName : queries.keySet()) {
                                    String query = queries.get(qName);
                                    searcher.setQuery(query);
                                    searcher.setqNum(qName);
                                    searcher.setDesc(descs.get(qName));
                                    searcher.setNarr(narrs.get(qName));
                                    // output.append(query + "\n");
                                    // output.append(searcher.search().toArray().toString());

                                    ArrayList<String> results = searcher.search();
                                    StringBuilder resultsText = new StringBuilder("");
                                    for (String result : results)
                                        resultsText.append(result + "\n");

                                    headers[indexForTexts] = new Text();
                                    headers[indexForTexts].setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
                                    headers[indexForTexts].setX(40 + 300 * (indexForTexts));
                                    headers[indexForTexts].setY(20 + 20 * indexForTexts);
                                    headers[indexForTexts].setText("Showing " + results.size() + " Results For: " + query + " (" + qName + ")");

                                    texts[indexForTexts] = new Text();
                                    texts[indexForTexts].setText(resultsText.toString());
                                    texts[indexForTexts].setX(50 + 300 * (indexForTexts));
                                    texts[indexForTexts].setY(38 + 20 * indexForTexts);

                                    indexForTexts++;

                                }

                                wasSearchedForResults[0] = true; //we searched


                                //Creating a Group object
                                //     ScrollBar sch = new ScrollBar();
                                //      ScrollBar scv = new ScrollBar();

                                Group root1 = new Group();
                                for (Text text : texts)
                                    root1.getChildren().addAll(text);
                                for (Text header : headers)
                                    root1.getChildren().addAll(header);

                                //     root1.getChildren().addAll(scv,sch);

                                // root.getChildren().addAll(sc);
                                ScrollPane sp = new ScrollPane();
                                sp.pannableProperty().set(true);
                                sp.setContent(root1);
                                //Creating a scene object
                                //  Scene scene = new Scene(root1, 600, 300);
                                Scene scene = new Scene(sp, 900, 500);


                                //Setting title to the Stage
                                Stage stage = new Stage();
                                stage.setTitle("Search results");

                                //Adding scene to the stage
                                stage.setScene(scene);

                                //Displaying the contents of the stage
                                stage.show();


                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Didn't inserted posting files path! please try again", ButtonType.OK);
                                alert.showAndWait();
                            }
                        } catch (IOException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "wrong path! please try again", ButtonType.OK);
                            alert.showAndWait();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Didn't entered query or path for queries file! please try again", ButtonType.OK);
                        alert.showAndWait();
                    }

                }

            }
        });
        Button browseQueriesFolder = new Button();
        browseQueriesFolder.setText("Browse your queries file");
        browseQueriesFolder.setTranslateX(160);
        browseQueriesFolder.setTranslateY(-70);
        browseQueriesFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            /**
             *  handler to queries folder browser button
             * @param event
             */
            public void handle(ActionEvent event) {
                FileChooser queriesFile = new FileChooser();
                queriesFile.setTitle("Choose your queries file");
                HBox browse = new HBox();
                browse.setSpacing(20);
                Scene scene = new Scene(browse, 350, 100);
                paths[2] = queriesFile.showOpenDialog(primaryStage).getAbsolutePath();

                wasSearchedForResults[1] = false; // search button will search from file

            }
        });
        root.getChildren().add(browseQueriesFolder);


        Button browseResults = new Button();
        browseResults.setText("Browse folder and save results to it");
        browseResults.setTranslateX(70);
        browseResults.setTranslateY(-40);
        EventHandler<ActionEvent> browseHandler = new EventHandler<ActionEvent>() {
            @Override
            /**
             *  handler the place you want to save your results
             * @param event
             */
            public void handle(ActionEvent event) {
                DirectoryChooser corpus = new DirectoryChooser();
                corpus.setTitle("Browse saved results folder");
                HBox browse = new HBox();
                browse.setSpacing(20);
                Scene scene = new Scene(browse, 350, 100);
                paths[3] = corpus.showDialog(primaryStage).getAbsolutePath();
                if (paths[3] != null) {
                  //  if (paths[2] != null || query != null) {
                     //   if(paths[2] != null){
                            if(wasSearchedForResults[0]) {
                                //save the results from "queries.txt" to a text file in the path inside paths[3]
                                try {
                                    searcher.saveResults(paths[3]);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Results saved", ButtonType.OK);
                                alert.showAndWait();
                            }
                            else{
                                Alert alert = new Alert(Alert.AlertType.ERROR, "You didn't search for document yet! please try again", ButtonType.OK);
                                alert.showAndWait();
                            }
                        }
                      /*  else{
                            if(query != null) {
                                if (wasSearchedForResults[0]) {
                                    //save the results from query textField to a text file in the path inside paths[3]
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Results saved", ButtonType.OK);
                                    alert.showAndWait();
                                }
                                else{
                                    Alert alert = new Alert(Alert.AlertType.ERROR, "You didn't search for document yet! please try again", ButtonType.OK);
                                    alert.showAndWait();
                                }
                            }
                        }
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Didn't entered query or path for queries file! please try again", ButtonType.OK);
                        alert.showAndWait();
                    }
                } */else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Didn't inserted a path! please try again", ButtonType.OK);
                    alert.showAndWait();
                }
            }


        };
        browseResults.setOnAction(browseHandler);

        root.getChildren().add(browseResults);




        //Defining the Last Name text field
        Button showTopFive = new Button("Top 5 entities");
        showTopFive.setTranslateX(16);
        showTopFive.setTranslateY(20);
        GridPane.setConstraints(showTopFive, 1, 0);
        root.getChildren().

                add(showTopFive);

//Setting an action for the Submit button
        showTopFive.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            /**
             *  handler to show top five button
             * @param event
             */
            public void handle(ActionEvent event) {
                if(wasSearchedForResults[0]){ //we searched for results before so we can display entities
                    try {
                        DecimalFormat decimalFormatf = new DecimalFormat("#.###");
                        TreeMap<String, ArrayList<String>> qNumbersAndResults = searcher.getqNumbersAndResults();
                        int indexForTexts = 0;
                        Text[] headers = new Text[qNumbersAndResults.size()];
                        Text[] texts = new Text[qNumbersAndResults.size()];
                        for (String qNum : qNumbersAndResults.keySet()) { // for each query
                            //header text : "Top 5 entities for results for query number "+qNum
                            StringBuilder textPerQuery = new StringBuilder();
                            ArrayList<String> queryResults = qNumbersAndResults.get(qNum);
                            ArrayList<EntGradesOfDoc> entGradesOfDocs = searcher.getEntGradesOfDocs(queryResults); //graded entities for each doc
                            for(int i = 0; i < queryResults.size(); i++){ // for each result
                                String docNo = queryResults.get(i);
                                Set<Entry<String, Double>> topEntities = entGradesOfDocs.get(i).getTop5Grades();
                                textPerQuery.append(docNo+":\n");
                                for(Entry<String, Double> entry : topEntities){
                                    textPerQuery.append("\t"+entry.getKey()+" | "+decimalFormatf.format(entry.getValue())+"\n");
                                }
                            }
                            headers[indexForTexts] = new Text();
                            headers[indexForTexts].setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
                            headers[indexForTexts].setX(40 + 400 * (indexForTexts));
                            headers[indexForTexts].setY(20 + 20 * indexForTexts);
                            headers[indexForTexts].setText("Top 5 entities for results for query number "+qNum);

                            texts[indexForTexts] = new Text();
                            texts[indexForTexts].setText(textPerQuery.toString());
                            texts[indexForTexts].setX(50 + 400 * (indexForTexts));
                            texts[indexForTexts].setY(38 + 20 * indexForTexts);

                            indexForTexts++;
                        }

                        Group root1 = new Group();
                        for (Text text : texts)
                            root1.getChildren().addAll(text);
                        for (Text header : headers)
                            root1.getChildren().addAll(header);

                        //     root1.getChildren().addAll(scv,sch);

                        // root.getChildren().addAll(sc);
                        ScrollPane sp = new ScrollPane();
                        sp.pannableProperty().set(true);
                        sp.setContent(root1);
                        //Creating a scene object
                        //  Scene scene = new Scene(root1, 600, 300);
                        Scene scene = new Scene(sp, 900, 500);


                        //Setting title to the Stage
                        Stage stage = new Stage();
                        stage.setTitle("Search results");

                        //Adding scene to the stage
                        stage.setScene(scene);

                        //Displaying the contents of the stage
                        stage.show();


                    }

                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "You didn't search for document yet! please try again", ButtonType.OK);
                    alert.showAndWait();
                }

               // if ((search.getText() != null && !search.getText().isEmpty())) {

//                    try {
//                    here we going to run the function that checks the query
//                    here we gonna show the table with the docs returned
//                    } catch (IOException e) {
//                        Alert alert = new Alert(Alert.AlertType.ERROR, "Wrong path! please select a different path", ButtonType.OK);
//                        alert.showAndWait();
//
                    //show table with top 5 entities of the returned document
               /* } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "The document does not retrieved!", ButtonType.OK);
                    alert.showAndWait();
                }*/
            }
        });
        // create a checkbox
        CheckBox c1 = new CheckBox("use word to vector?");
        c1.setTranslateX(35);
        c1.setTranslateY(65);
        // add checkbox
        root.getChildren().add(c1);

        // create a event handler
        EventHandler<ActionEvent> checkBoxEvent1 = new EventHandler<ActionEvent>() {
            /**
             * handler for use word to vector? button
             * @param e
             */
            public void handle(ActionEvent e) {
                if (c1.isSelected()) {
                    toStem[1] = true;
                    searcher.setSemantic(true);
                } else {
                    toStem[1] = false;
                    searcher.setSemantic(false);
                }
            }

        };
        c1.setOnAction(checkBoxEvent1);


        deletePostings.setOnAction(deletePostingHandler);
        start.setOnAction(startHandler);
        c.setOnAction(checkBoxEvent);
        browseCorpus.setOnAction(corpusHandler);
        browsePostings.setOnAction(postingHandler);
        showDictionary.setOnAction(showDictionaryHandler);
        loadDictionary.setOnAction(loadDictionaryHandler);
        root.getChildren().add(loadDictionary);
        root.getChildren().add(showDictionary);
        root.getChildren().add(deletePostings);
        root.getChildren().add(browseCorpus);
        root.getChildren().add(browsePostings);
        root.getChildren().add(start);


        Scene scene = new Scene(root, 550, 250);

        primaryStage.setTitle("Welcome to BS! which is Ben-Gurion Searcher and definitely not BullShit");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    /**
     * @param line
     * @return term from specific line in the dictionary
     */
    private static String getNameFromLine(String line) {
        return line.substring(0, line.indexOf("*"));
    }

    /**
     * @param lineInDic
     * @return number of appearances of a specific term from specific line in the dictionary
     */
    private static int getTotalFreqFromDictionaryLine(String lineInDic) {
        String subStr = lineInDic.substring(lineInDic.indexOf("*") + 1);
        return Integer.parseInt(subStr);
    }

    /**
     * @param args
     * @throws IOException main function
     */
    public static void main(String[] args) throws IOException {

       launch(args);
       /* HashMap<String, String> toParser = new HashMap<>();
        toParser.put("query", "piracy");
        Parser parser = new Parser();
        HashMap<String, DocData> afterParse = parser.parse(toParser, true, new HashSet<>());
        ArrayList<String> queryWords = afterParse.get("query").getModifiedText();
        HashMap<String,Byte> wordsAndRoles=afterParse.get("query").getTerms();*/

/* Searcher searcher = new Searcher("D:\\ZData\\SearchEngine\\BigWorkplace", "Falkland petroleum exploration", false, true);
        ArrayList<String> s = searcher.search();
        searcher.setQuery(" British Chunnel impact  ");
        s = searcher.search();
        searcher.setQuery(" blood-alcohol fatalities ");
        s = searcher.search();*/




        //Indexer indexer = new Indexer();
        // indexer.mergeLetters("a", 5, "D:\\ZData\\SearchEngine\\workplace2");
// ReadQeury rq = new ReadQeury("D:\\ZData\\SearchEngine\\wtemp\\qu.txt");
        // HashMap<String, String> qMap = rq.getQueries();


        /*ArrayList<String> docs = new ArrayList<>();
        File docIndexPostingFile = new File("D:\\ZData\\SearchEngine\\wtemp\\DocsIndex.txt");
        Scanner docIndexScan = new Scanner(docIndexPostingFile);
        StringBuilder lineInDocIndex = new StringBuilder("");
        while(docIndexScan.hasNext()){
            lineInDocIndex.append(docIndexScan.nextLine());
            String name = lineInDocIndex.substring(lineInDocIndex.indexOf("<")+1, lineInDocIndex.indexOf(">"));
            docs.add(name);
            lineInDocIndex.setLength(0);
        }

      //  docs.add("FBIS3-830");
       // docs.add("FBIS3-2289");
      //  docs.add("FBIS3-840");
        Searcher searcher = new Searcher("", "D:\\ZData\\SearchEngine\\wtemp", false, false);
        ArrayList<EntGradesOfDoc> arr = searcher.getEntGradesOfDocs(docs);
        for(EntGradesOfDoc e : arr){
            Set<Map.Entry<String, Double>> top5Grades = e.getTop5Grades();
            int a = 1;
            int b = 2;
        }*/
    }
}
