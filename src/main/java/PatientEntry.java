import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PatientEntry {

    private ArrayList<Pair<Date,String>> events;
    private HashMap<String, ArrayList<Pair<Date,Integer>>> measures;
    private HashMap<String, String> units;


    public PatientEntry(ArrayList<Pair<Date,String>> ev,HashMap<String, ArrayList<Pair<Date,Integer>>> m,HashMap<String, String> u) {

        events = new ArrayList<>(ev);
        measures = new HashMap<>(m);
        units = new HashMap<>(u);

    }

    public ArrayList<Pair<Date, String>> getEvents() {
        return events;
    }

    public HashMap<String, ArrayList<Pair<Date, Integer>>> getMeasures() {
        return measures;
    }

    public HashMap<String, String> getUnits() {
        return units;
    }

    public void setUnits(HashMap<String, String> units) {
        this.units = units;
    }
}
