package com.company.Process_Mining;

import com.company.Process_Mining.Base_Data.Complete_Time_Activity;
import com.company.Process_Mining.Base_Data.Mining_Activity;
import com.company.Process_Mining.Base_Data.Mining_Resource;
import com.company.Process_Mining.Base_Data.Mining_User;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForLink;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.Node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.company.Process_Mining.Relation_Type.Related;
import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.*;

public class Mining_Logger {

    private HashMap<Integer, Mining_Activity> activity_hashmap;
    private HashMap<Mining_User, HashMap<Mining_User, Relation_Count>> user_relation_hashmap;
    private HashMap<Mining_Activity, Complete_Time_Activity> time_log_by_activity;
    private List<Transactional_Relation> All_Places;
    private List<Print_Place> Print_Places;
    private List<Print_Transition> Print_Transitions;
    private Integer PlaceID;
    private Integer Total_User_Relation_Count;
    private HashMap<Mining_Resource, List<Timed_Resource_Usage_By_Activity>> Timed_Mining_Activity_By_Resource;
    private HashMap<Mining_User, List<Timed_User_Usage_By_Activity>> Timed_Mining_Activity_per_User;


    private HashMap<Integer, List<Integer>> Transitions_To_Places;
    private HashMap<Integer, Relation_Places> Places_to_Relations;
    private HashMap<Integer, List<Integer>> Places_to_Transitions;


    public Mining_Logger(HashMap<Integer, Mining_Activity> activity_hashmap,
                         HashMap<Mining_User, HashMap<Mining_User, Relation_Count>> user_relation_hashmap,
                         Integer total_User_Relation_Count,
                         HashMap<Mining_User, List<Timed_User_Usage_By_Activity>> Timed_Mining_Activity_per_User,
                         HashMap<Mining_Resource, List<Timed_Resource_Usage_By_Activity>> Timed_Mining_Activity_By_Resource,
                         HashMap<Mining_Activity, Complete_Time_Activity> time_log_by_activity,
                         List<Transactional_Relation> all_Places) {
        Total_User_Relation_Count = total_User_Relation_Count;
        this.activity_hashmap = activity_hashmap;
        this.user_relation_hashmap = user_relation_hashmap;
        this.time_log_by_activity = time_log_by_activity;
        this.All_Places = all_Places;
        this.Timed_Mining_Activity_By_Resource = Timed_Mining_Activity_By_Resource;
        this.Timed_Mining_Activity_per_User = Timed_Mining_Activity_per_User;
        Print_Places = new ArrayList<>();
        Print_Transitions = new ArrayList<>();
        PlaceID = 0;

        Transitions_To_Places = new HashMap<>();
        Places_to_Relations = new HashMap<>();
        Places_to_Transitions = new HashMap<>();

    }

    public void LogMining() {

        GeneratePrintablePlaces();

        try {
            createPetriNet();
            createUserRelationGraph();
        } catch (IOException e) {

        }
    }

    private void GeneratePrintablePlaces() {


        for (Transactional_Relation Relation : All_Places) {
            if (!Transitions_To_Places.containsKey(Relation.getActivityID())) {
                Transitions_To_Places.put(Relation.getActivityID(), new ArrayList<>());
            }
            boolean Place_to_Relation_Found = false;
            for (Map.Entry<Integer, Relation_Places> Place_to_Relation : Places_to_Relations.entrySet()) {
                if (Place_to_Relation.getValue().has_Same_From(Relation.getConnected_To_Place()) &&
                        Place_to_Relation.getValue().has_Same_to(Relation.getConnected_To_Place())) {
                    Place_to_Relation_Found = true;
                    break;
                }
            }
            if (!Place_to_Relation_Found) {
                Integer newPlaceID = getUniquePlaceID();
                Places_to_Relations.put(newPlaceID, Relation.getConnected_To_Place());
                if (!Places_to_Transitions.containsKey(newPlaceID)) {
                    Places_to_Transitions.put(newPlaceID, new ArrayList<>());
                } else {
                    System.out.println("Routing ERROR: Place couldn´t be bound to Relation");
                }
            }
        }

        for (Transactional_Relation Relation : All_Places) {
            if (Relation.isFromTransaction()) {
                Integer Con_Transition_To_PlaceID = 0;
                for (Map.Entry<Integer, Relation_Places> Place_to_Relation : Places_to_Relations.entrySet()) {
                    Relation_Places toSearch = Place_to_Relation.getValue();
                    if (toSearch.has_Same_From(Relation.getConnected_To_Place()) && toSearch.has_Same_to(Relation.getConnected_To_Place())) {
                        Con_Transition_To_PlaceID = Place_to_Relation.getKey();
                        break;
                    }
                }
                if (!Transitions_To_Places.get(Relation.getActivityID()).contains(Con_Transition_To_PlaceID)) {
                    Transitions_To_Places.get(Relation.getActivityID()).add(Con_Transition_To_PlaceID);
                }
            } else {
                Integer Con_Place_To_TransitionID = 0;
                for (Map.Entry<Integer, Relation_Places> Place_to_Relation : Places_to_Relations.entrySet()) {
                    Relation_Places toSearch = Place_to_Relation.getValue();
                    if (toSearch.has_Same_From(Relation.getConnected_To_Place()) && toSearch.has_Same_to(Relation.getConnected_To_Place())) {
                        Con_Place_To_TransitionID = Place_to_Relation.getKey();
                        break;
                    }
                }
                if (!Places_to_Transitions.get(Con_Place_To_TransitionID).contains(Relation.getActivityID())) {
                    Places_to_Transitions.get(Con_Place_To_TransitionID).add(Relation.getActivityID());
                }
            }
        }
    }

    public void createUserRelationGraph() throws IOException {
        List<Node> Nodelist = new ArrayList<>();
        for (Map.Entry<Mining_User, HashMap<Mining_User, Relation_Count>> User_From : user_relation_hashmap.entrySet()) {
            for (Map.Entry<Mining_User, Relation_Count> User_to : User_From.getValue().entrySet()) {
                List<Attributes<? extends ForLink>> Attributes = new ArrayList<>();
                List<Attributes<? extends ForLink>> Style = new ArrayList<>();
                if (User_to.getValue().getRelation_type() == Related) {
                    Attributes.add(attr("weight", User_to.getValue().getCount() / Total_User_Relation_Count));
                    Attributes.add(attr("label", User_to.getValue().getCount() / Total_User_Relation_Count));
                    Nodelist.add(node(User_From.getKey().getName())
                            .link(to(node(User_to.getKey().getName()))
                                    .with(Attributes)));
                }
            }
        }
        Graph g = graph("UserRelation").directed()
                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                .linkAttr().with("class", "link-class")
                .with(Nodelist);
        Graphviz.fromGraph(g).height(300).render(Format.PNG).toFile(new File("MiningResults/UserRelation.png"));
    }

    public void createPetriNet() throws IOException {
        HashMap<Integer, MutableNode> Placelist = new HashMap<>();
        HashMap<Integer, MutableNode> TransitionList = new HashMap<>();

        /*List<Node> Resultlist = new ArrayList<>();
        for(Map.Entry<Integer,List<Integer>> Place_to_Transition: Places_to_Transitions.entrySet()){
            List<String> Transitionname = new ArrayList<>();
            for (Integer i :Place_to_Transition.getValue()) {
                Transitionname.add(activity_hashmap.get(i).getActivity_Name());
            }
            MutableNode n = mutNode(Place_to_Transition.getKey().toString());
            for (String string: Transitionname) {
                n.addlink(to(mutNode(string)));
                n.
            }
            Resultlist.add(n);
        }
        for(Map.Entry<Integer,List<Integer>> Transition_to_Place: Transitions_To_Places.entrySet()){
            List<String> PlaceIDs = new ArrayList<>();
            for (Integer i :Transition_to_Place.getValue()) {
                PlaceIDs.add(i.toString());
            }
            String name = activity_hashmap.get(Transition_to_Place.getKey()).getActivity_Name();
            Node n = node(name);
            for (String ID: PlaceIDs) {
                n.link(to(node(ID)));
            }
            Resultlist.add(n);
        }*/

        for (Map.Entry<Integer, List<Integer>> Place_to_Transition : Places_to_Transitions.entrySet()) {
            List<Attributes<? extends ForLink>> Attributes = new ArrayList<>();
            Placelist.put(Place_to_Transition.getKey(), mutNode("" + Place_to_Transition.getKey()).add(attr("shape1", Shape.CIRCLE)));
        }

        for (Map.Entry<Integer, List<Integer>> Transition_to_Place : Transitions_To_Places.entrySet()) {
            Integer visited = time_log_by_activity.get(activity_hashmap.get(Transition_to_Place.getKey())).getSingle_Instance_Activity_Time().size();
            TransitionList.put(Transition_to_Place.getKey(), mutNode(activity_hashmap.get(Transition_to_Place.getKey()).getActivity_Name() + "\n Visited: " + visited.toString()).add(attr("shape", Shape.BOX)));
        }

        for (Map.Entry<Integer, List<Integer>> Place_to_Transition : Places_to_Transitions.entrySet()) {
            List<MutableNode> toLink = new ArrayList<>();
            for (Integer TransitionID : Place_to_Transition.getValue()) {
                toLink.add(TransitionList.get(TransitionID));
            }
            MutableNode n = Placelist.get(Place_to_Transition.getKey());
            n.addLink(toLink);
            Placelist.replace(Place_to_Transition.getKey(), n);
        }

        for (Map.Entry<Integer, List<Integer>> Transition_to_Place : Transitions_To_Places.entrySet()) {
            List<MutableNode> toLink = new ArrayList<>();
            for (Integer PlaceID : Transition_to_Place.getValue()) {
                toLink.add(Placelist.get(PlaceID));
            }
            MutableNode n = TransitionList.get(Transition_to_Place.getKey());
            n.addLink(toLink);
            TransitionList.replace(Transition_to_Place.getKey(), n);
        }

        List<MutableNode> Resultlist = new ArrayList<>();
        for (Map.Entry<Integer, MutableNode> Place : Placelist.entrySet()) {
            Resultlist.add(Place.getValue());
        }

        for (Map.Entry<Integer, MutableNode> Transition : TransitionList.entrySet()) {
            Resultlist.add(Transition.getValue());
        }


        /*for (Map.Entry<Mining_User,HashMap<Mining_User,Relation_Count>> User_From : user_relation_hashmap.entrySet()) {
            for (Map.Entry<Mining_User,Relation_Count> User_to : User_From.getValue().entrySet()){
                List<Attributes<? extends ForLink>> Attributes = new ArrayList<>();
                List<Attributes<? extends ForLink>> Style = new ArrayList<>();
                if(User_to.getValue().getRelation_type() == Related) {
                    Attributes.add(attr("weight", User_to.getValue().getCount()));
                    Attributes.add(attr("label",User_to.getValue().getCount()));
                    Nodelist.add(node(User_From.getKey().getName())
                            .link(to(node(User_to.getKey().getName()))
                                    .with(Attributes)));
                }
            }
        }*/
        Graph g = graph("PetriNet").directed()
                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                .linkAttr().with("class", "link-class")
                .with(Resultlist);
        Graphviz.fromGraph(g).height(300).render(Format.PNG).toFile(new File("MiningResults/PetriNet.png"));
    }

    private Integer getUniquePlaceID() {
        PlaceID++;
        return PlaceID;
    }
}