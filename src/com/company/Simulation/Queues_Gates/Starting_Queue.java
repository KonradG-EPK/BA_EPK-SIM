package com.company.Simulation.Queues_Gates;

import com.company.EPK.EPK;
import com.company.Simulation.Data.Item;
import com.company.Simulation.Instance.Buy_Instance;
import com.company.Simulation.Instance.Order_Instance;
import com.company.Simulation.Instance.Rep_Instance;
import com.company.Simulation.Instance.Simulation_Instance;
import com.company.Simulation.Simulator;

import java.time.LocalTime;
import java.util.*;

import static com.company.Enums.Process_Status.Pending;

public class Starting_Queue implements Runnable {

    private Starting_Gate starting_gate;
    private Event_Gate event_gate;
    private Random random;
    private final Simulator simulator;
    private final EPK epk;
    private Thread SQ;
    private boolean not_killed;

    public Starting_Queue(Simulator simulator, EPK epk) {
        this.event_gate = Event_Gate.get_Event_Gate();
        this.starting_gate = Starting_Gate.getStarting_gate();
        this.SQ = null;
        this.random = new Random();
        this.simulator = simulator;
        this.not_killed = true;
        this.epk = epk;
    }

    public void setSQ(Thread SQ) {
        this.SQ = SQ;
    }

    public boolean isNot_killed() {
        return not_killed;
    }

    public void setNot_killed(boolean not_killed) {
        this.not_killed = not_killed;
    }

    public synchronized void run() {
        while (not_killed) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<Simulation_Instance> order_List;
            synchronized (Starting_Gate.getStarting_gate()) {
                order_List = Starting_Gate.getStarting_gate().getStarting_order().getStarting_Instances();
                // }

                if (order_List != null && !order_List.isEmpty()) {
                    //synchronized (order_List) {
                    Iterator<Simulation_Instance> o = order_List.iterator();
                    while (o.hasNext()) {
                        //TODO Iterator check
                        Simulation_Instance instance = o.next();
                        if (((Order_Instance) instance).getTime().isBefore(LocalTime.now())) {
                            //synchronized (Starting_Gate.getStarting_gate()) {
                            instance.getWorkflowMonitor().add_Workflow(Starting_Gate.getStarting_gate().getStarting_Event(), Pending);
                            epk.add_Instance(instance);
                            synchronized (Event_Gate.get_Event_Gate()) {
                                Event_Gate.get_Event_Gate().getEvent_List().add_transport_Process(instance);
                            }
                            //}
                            o.remove();
                        }
                    }
                    //}
                }

                //TODO Timing vom Instance chooser.
                //TODO IF Else Auflösen.
                try {
                    wait(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int instance_chooser = random.nextInt(100);

                if (instance_chooser <= 70) {
                    generateBuy();
                } else {
                    generateRep();
                }
            }
        }
    }

    private void generateRep() {

        int iD;
        Item rep_Item;

        synchronized (simulator) {
            iD = simulator.get_unique_caseID();
            rep_Item = simulator.generate_singleRandomItem();
        }
        Rep_Instance rep = new Rep_Instance(iD, rep_Item);
        synchronized (Starting_Gate.getStarting_gate()) {
            rep.getWorkflowMonitor().add_Workflow(Starting_Gate.getStarting_gate().getStarting_Event(), Pending);
        }
        synchronized (Event_Gate.get_Event_Gate()) {
            Event_Gate.get_Event_Gate().getEvent_List().add_transport_Process(rep);
        }
        synchronized (epk) {
            epk.add_Instance(rep);
        }
    }

    private void generateBuy() {

        int iD;
        List<Item> buy_Items;

        synchronized (simulator) {
            iD = simulator.get_unique_caseID();
            buy_Items = simulator.generate_severalRandomItems();
        }
        Buy_Instance buy = new Buy_Instance(iD, buy_Items);
        synchronized (Starting_Gate.getStarting_gate()) {
            buy.getWorkflowMonitor().add_Workflow(Starting_Gate.getStarting_gate().getStarting_Event(), Pending);
        }
        synchronized (Event_Gate.get_Event_Gate()) {
            Event_Gate.get_Event_Gate().getEvent_List().add_transport_Process(buy);
        }
        synchronized (epk) {
            epk.add_Instance(buy);
        }

    }
}

