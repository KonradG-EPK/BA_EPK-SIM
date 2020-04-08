package com.company.UI;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafxgraph.fxgraph.graph.CellType;
import javafxgraph.fxgraph.graph.Graph;
import javafxgraph.fxgraph.graph.Model;
import javafxgraph.fxgraph.layout.base.Layout;
import javafxgraph.fxgraph.layout.random.RandomLayout;

import java.io.File;
import java.net.URL;

public class Main extends Application {


    Graph graph;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        URL url = new File("src/main/java/com/company/UI/Main.fxml").toURI().toURL();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(url);
        Parent root = loader.load();
        Scene scene = new Scene(root, 1024, 768);

        Borderpanecon borderpanecon = (Borderpanecon) loader.getController();
        BorderPane pane = borderpanecon.getCanvaspane();
        VBox Box = borderpanecon.getLeftbox();

        graph = new Graph(Box);

        pane.setCenter(graph.getScrollPane());

        //scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm()); not working

        primaryStage.setScene(scene);
        primaryStage.show();

        addGraphComponents();

        Layout layout = new RandomLayout(graph);
        layout.execute();
    }

    private void addGraphComponents() {

        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("Cell A", CellType.RECTANGLE);
        model.addCell("Cell B", CellType.RECTANGLE);
        model.addCell("Cell C", CellType.RECTANGLE);
        model.addCell("Cell D", CellType.TRIANGLE);
        model.addCell("Cell E", CellType.TRIANGLE);
        model.addCell("Cell F", CellType.RECTANGLE);
        model.addCell("Cell G", CellType.RECTANGLE);

        model.addEdge("Cell A", "Cell B");
        model.addEdge("Cell A", "Cell C");
        model.addEdge("Cell B", "Cell C");
        model.addEdge("Cell C", "Cell D");
        model.addEdge("Cell B", "Cell E");
        model.addEdge("Cell D", "Cell F");
        model.addEdge("Cell D", "Cell G");

        graph.endUpdate();

    }
}