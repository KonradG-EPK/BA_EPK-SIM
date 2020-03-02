package com.company.Run;

import com.company.EPK.EPK;
import com.company.Simulation.Simulation_Base.Data.Discrete_Data.Loader.EPK_Loader;
import com.company.Simulation.Simulation_Base.Data.Discrete_Data.Loader.Resource_Loader;
import com.company.Simulation.Simulation_Base.Data.Discrete_Data.Loader.Settings_Loader;
import com.company.Simulation.Simulation_Base.Data.Discrete_Data.Loader.User_Loader;
import com.company.Simulation.Simulation_Base.Data.Discrete_Data.Resource;
import com.company.Simulation.Simulation_Base.Data.Shared_Data.Settings;
import com.company.Simulation.Simulation_Base.Data.Shared_Data.User;
import com.company.Simulation.Simulation_Discrete_Event.Discrete_Event_Simulator;
import com.company.Simulation.Simulation_Base.Data.Printer_Gate;
import com.company.Simulation.Simulation_Base.Data.Printer_Queue;
import com.company.Simulation.Simulation_Discrete_Event.Event_Calendar;

import java.util.List;

public class Discrete_Event_Generator {

    private EPK EPK;
    private Settings Settings;
    private EPK_Loader EPK_loader;
    private Settings_Loader Settings_loader;
    private Resource_Loader Resource_loader;
    private User_Loader User_loader;
    private Printer_Gate printer_Gate;
    private Printer_Queue printer_Queue;
    private List<User> Users;
    private List<Resource> Resources;
    private Discrete_Event_Simulator Simulation;
    private Event_Calendar event_Calendar;

    public Discrete_Event_Generator(String EPK_file, String Setting_file, String User_file, String Resource_file) {
        this.EPK_loader = new EPK_Loader(EPK_file);
        this.Settings_loader = new Settings_Loader(Setting_file);
        this.User_loader = new User_Loader(User_file);
        this.Resource_loader = new Resource_Loader(Resource_file);
        this.EPK = null;
        this.Simulation = null;
        this.printer_Queue = new Printer_Queue();
        this.printer_Gate = Printer_Gate.get_Printer_Gate();
        Thread T = new Thread();
        printer_Queue.setT(T);
    }

    public void generate() {
        this.EPK = EPK_loader.generate_EPK();
        this.Settings = Settings_loader.generate_Settings();
        this.Users = User_loader.generate_User_List();
        this.Resources = Resource_loader.generate_Resources();
        this.event_Calendar = new Event_Calendar(Settings);
        event_Calendar.fillCalendar();
        this.Simulation = new Discrete_Event_Simulator(this);
    }

    ;

    public void run() {
        Simulation.run();
    }

    public EPK getEPK() {
        return EPK;
    }

    public Settings getSettings() {
        return Settings;
    }

    public Printer_Queue getPrinter_Queue() {
        return printer_Queue;
    }

    public List<User> getUsers() {
        return Users;
    }

    public List<Resource> getResources() {
        return Resources;
    }

    public Event_Calendar getEvent_Calendar() {
        return event_Calendar;
    }

    //TODO Erstelle hier EPK Objekt und füttere dieses mit dem Graphen
    //TODO Erstelle hier das Simulation Discrete Event Simulator Object welches Settings erhällt, sowie die Liste an Events.
    //TODO starte ein Run Objekt das Evtl Loggt, den Printer erhällt und die EPK und Simulator Objekte.
    //TODO Über Simulator Objekt zugang zu den Instanzen,
    //TODO Über EPK Objekt zugang zur steuerung.

    //TODO 2.0 EPK Instanzierung evtl Auslagern auf extra Klasse um das Einlesen zu vereinfachen
    //TODO Settingsdatei kann hier ausgelesen werden (auch besser so), EPK generierung in eigener Datei (.epk) auslagern.
    //TODO Graphische Visualisierung mit Graphviz
    //TODO Wahrscheinlichkeitsverteilungen studieren und einfügen.
    //TODO Wahrscheinlichkeitsverteilugen an Gates binden.
    //TODO Bewertungsfunktionen (Auswahl) an die Run Methode koppeln (Auswahl welcher Prozess die Ressourcen erhällt).
    //TODO Bewertungsfunktionsauswahl erfolgt über Settings (Globale einstellung, bspw, Greedy/Bewertung oder Fifo, zunächst Fifo)

    //----------------------------------------------------------------------------------------------------------------------

    //TODO EPK instantieren, Settings auslesen, Simulator starten, Instanzen über jeweilige Statistische Methode instantieren,
    //TODO instanzierung dieser über die Simulation Event_List, Simulator verwaltet Simulationsdaten, Run Methode schreiben
    //TODO zum durchlaufen der Simulation
    //TODO Processes als Object mit Optionen an die Funktionen des EPK´s binden (Um von Threads zu lösen)
    //TODO Printermethode im Simulator führen (evtl als eigener Thread),


    //TODO PROCESS MINING!
}