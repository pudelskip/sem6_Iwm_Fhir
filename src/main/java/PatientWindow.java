import javafx.util.Pair;
import org.hl7.fhir.dstu3.model.Patient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PatientWindow {
    private JPanel panel1;
    private JTextField textFrom;
    private JButton filterButton;
    private JList list1;
    private JLabel id;
    private JLabel name;
    private JLabel gender;
    private JLabel birth;
    private JTextField textTo;
    private  FhirHelper f;
    private DefaultListModel<String> patientEventsList;

    public PatientWindow(Patient myPatient) {


        f = new FhirHelper();
        PatientEntry patientEntry = f.getPatientEverything(myPatient.getIdElement().getIdPart());
        ArrayList<Pair<Date,String>> patientEvents = patientEntry.getEvents();

        String name1 ="<unknown>";
        String given1=" <unknown>";
        String gender1="<unknown>";
        String birth1="<unknown";


        if(myPatient.getName().get(0).getFamily()!=null)
            name1=myPatient.getName().get(0).getFamily();
        if(myPatient.getName().get(0).getGiven()!=null)
            given1=myPatient.getName().get(0).getGiven().toString();
        if(myPatient.getGender() != null)
            gender1=myPatient.getGender().getDisplay();
        if(myPatient.getBirthDate() != null)
            birth1=myPatient.getBirthDate().toString();


        id.setText(myPatient.getIdElement().getIdPart());
        name.setText(name1+ " "+given1);
        gender.setText(gender1);
        birth.setText(birth1);
        patientEventsList = new DefaultListModel<>();
        list1.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list1.setLayoutOrientation(JList.VERTICAL);
        list1.setVisibleRowCount(-1);

        list1.setModel(patientEventsList);

        for (Pair<Date,String> dateStringPair: patientEvents){
            String addToList = "";
            addToList+=dateStringPair.getKey().toString()+ " - "+dateStringPair.getValue();
            patientEventsList.addElement(addToList);
        }



        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                patientEventsList.clear();

                Date from = patientEvents.get(0).getKey();
                Date to = patientEvents.get(patientEvents.size()-1).getKey();

                if(!textFrom.getText().equals("")) {
                    try {
                        from=new SimpleDateFormat("dd.MM.yyyy").parse(textFrom.getText());
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }

                if(!textTo.getText().equals("")) {
                    try {
                        to=new SimpleDateFormat("dd.MM.yyyy").parse(textTo.getText());
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }


                for (Pair<Date,String> dateStringPair: patientEvents){

                    if(dateStringPair.getKey().compareTo(from)>=0 && dateStringPair.getKey().compareTo(to)<=0) {
                        String addToList = "";
                        addToList += dateStringPair.getKey().toString() + " - " + dateStringPair.getValue();
                        patientEventsList.addElement(addToList);
                    }
                }


            }
        });
    }

    public JPanel getPanel1() {
        return panel1;
    }
}
