package com.company.UI.EPKUI;

import com.company.EPK.EPK_Node;
import com.company.EPK.Event_Con_Split;
import com.company.Enums.Contype;
import com.company.Enums.Split_Decide_Type;
import com.company.UI.javafxgraph.fxgraph.cells.UI_View_Gen;
import com.dlsc.formsfx.model.structure.*;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.util.List;

import static com.company.Enums.Split_Decide_Type.SINGLE_RANDOM;

public class UI_OR_Split extends Event_Con_Split implements UI_Instantiable {

    private VBox Box;
    private VBox Rightbox;
    private ObservableList<EPK_Node> nodelist;
    private UI_EPK EPK;
    private IntegerProperty UI_ID;
    private List<Split_Decide_Type> Decide_Types;
    private IntegerField UI_ID_FIELD;
    private SingleSelectionField<Split_Decide_Type> Decide_Type;
    private SingleSelectionField<EPK_Node> UI_NEXT_ELEMENTS_FIELD;
    private FormRenderer ID_UI;
    private FormRenderer NEXT_ELEMS_UI;
    private FormRenderer DECIDE_UI;


    public UI_OR_Split(int ID, UI_EPK EPK, VBox Rightbox) {

        super(null, ID, Contype.EAGER_OR, SINGLE_RANDOM);
        this.Box = new VBox();
        this.Rightbox = Rightbox;
        this.EPK = EPK;
        UI_ID = new SimpleIntegerProperty(ID);
        UI_ID_FIELD = Field.ofIntegerType(UI_ID).label("ID").editable(false);
        this.nodelist = FXCollections.observableArrayList();
        nodelist.addListener((ListChangeListener<EPK_Node>) e -> {
            while (e.next()) {
                if (e.wasRemoved()) {
                    Rightbox.getChildren().clear();
                    UI_Instantiable Nodeview = (UI_Instantiable) ((UI_View_Gen) EPK.getActive_Elem()).getNodeView();
                    Rightbox.getChildren().add(Nodeview.Get_UI());
                }
            }
        });

        ID_UI = new FormRenderer(
                Form.of(
                        Group.of(
                                UI_ID_FIELD)));

        this.Decide_Types = EPK.getAll_Decide_Types();
        Decide_Type = Field.ofSingleSelectionType(Decide_Types).editable(true).label("Aufteilungsoption").tooltip("Wahrschienlichkeitsverteilung zur Weitergabe der" +
                "Instanzen an die nachfolgenden Elemente");
        DECIDE_UI = new FormRenderer(Form.of(Group.of(Decide_Type)));

        //TODO Wahrschienlichkeitsoptionen in Decide Type Box einfügen und bei Update Instanzieren.
    }

    @Override
    public List<EPK_Node> getNodelist() {
        return nodelist;
    }

    @Override
    public VBox Get_UI() {
        Box.getChildren().clear();
        Box.getChildren().add(ID_UI);
        Box.getChildren().add(new Separator());
        UI_NEXT_ELEMENTS_FIELD = Field.ofSingleSelectionType(nodelist).label("Nachfolger");
        NEXT_ELEMS_UI = new FormRenderer(Form.of(Group.of(UI_NEXT_ELEMENTS_FIELD)));
        Box.getChildren().add(NEXT_ELEMS_UI);
        Box.getChildren().add(new Separator());
        Button btn = new Button("Verbindung entfernen");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                EPK_Node Node = UI_NEXT_ELEMENTS_FIELD.getSelection();
                if (Node != null) {
                    nodelist.remove(Node);
                    EPK.getActive_Elem().getEPKNode().getNext_Elem().remove(Node);
                    EPK.getModel().removeEdge(getID(), Node.getID());
                    EPK.getGraph().endUpdate();
                }
            }
        });
        Box.getChildren().add(btn);
        Box.getChildren().add(new Separator());
        Box.getChildren().add(DECIDE_UI);
        return Box;
    }

    @Override
    public void save_Settings() {

    }

    @Override
    public int get_Next_Elem_ID() {
        return 0;
    }

    @Override
    public String toString() {
        return "OR-Split [" +
                "ID: " + UI_ID_FIELD.getValue() + "]";
    }
}