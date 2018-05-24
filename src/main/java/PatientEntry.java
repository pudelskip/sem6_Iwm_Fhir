import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PatientEntry {

    private ArrayList<Pair<Date,String>> events;
    private HashMap<String, ArrayList<Pair<Date,String>>> measures;


    public PatientEntry(ArrayList<Pair<Date,String>> ev,HashMap<String, ArrayList<Pair<Date,String>>> m) {

        events = new ArrayList<>(ev);
        measures = new HashMap<>(m);

    }

    public ArrayList<Pair<Date, String>> getEvents() {
        return events;
    }

    public HashMap<String, ArrayList<Pair<Date, String>>> getMeasures() {
        return measures;
    }
}
