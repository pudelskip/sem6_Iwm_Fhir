import com.intellij.uiDesigner.core.GridConstraints;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.hl7.fhir.dstu3.model.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PatientWindow {
    private JPanel panel1;
    private JTextField textFrom;
    private JButton filterButton;
    private JList list1;
    private JTextField name;
    private JComboBox gender;
    private JTextField birth;
    private JTextField textTo;
    private JComboBox comboBox1;
    private JButton plotButton;
    private JLabel id;
    private JButton editButton;
    private JButton saveButton;
    private JTextField phone;
    private JLabel adress;
    private JComboBox versionBox;
    private JLabel update;
    private  FhirHelper f;
    private DefaultListModel<String> patientEventsList;
    private boolean editMode = false;
    private String oldName="";
    private String oldBirth="";
    private String oldPhone="";
    private String oldGender="";
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    Bundle versions;
    Patient currentPatient;

    private ActionListener myActionListener;



    public PatientWindow(Patient myPatient) {

        myActionListener=  new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                currentPatient = (Patient) versions.getEntry().get(versions.getEntry().size()-versionBox.getSelectedIndex()-1).getResource();
                showVersion();


            }
        };


        f = new FhirHelper();
        currentPatient=myPatient;
        versions = f.getPatientHistory(currentPatient.getIdElement().getIdPart());
        PatientEntry patientEntry = f.getPatientEverything(currentPatient.getIdElement().getIdPart());
        ArrayList<Pair<Date,String>> patientEvents = patientEntry.getEvents();
        HashMap<String, ArrayList<Pair<Date, Integer>>> measures = patientEntry.getMeasures();
        editMode=false;
        String name1 ="<unknown>";
        String given1=" <unknown>";
        String gender1="<unknown>";
        String birth1="<unknown>";
        String adress1="<unknown>";
        String phone1="<unknown>";
        gender.addItem("Male");
        gender.addItem("Female");
        gender.addItem("Other");
        gender.addItem("Unknown");



        if(currentPatient.getName().get(0).getFamily()!=null)
            name1=currentPatient.getName().get(0).getFamily();
        if(currentPatient.getName().get(0).getGiven()!=null)
            given1=currentPatient.getName().get(0).getGiven().toString();
        if(currentPatient.getGender() != null){
            gender1=currentPatient.getGender().getDisplay();
            if(gender1.toLowerCase().equals("female"))
                gender.setSelectedItem("Female");
            if(gender1.toLowerCase().equals("male"))
                gender.setSelectedItem("Male");
            if(gender1.toLowerCase().equals("other"))
                gender.setSelectedItem("Other");
            if(gender1.toLowerCase().equals("unknown"))
                gender.setSelectedItem("Unknown");

        }
        if(currentPatient.getBirthDate() != null)
            birth1=dateFormat.format(currentPatient.getBirthDate());
        if(currentPatient.getAddressFirstRep().toString() != null)

            adress1=currentPatient.getAddressFirstRep().getPostalCode() +", "+currentPatient.getAddressFirstRep().getCity()+", "+currentPatient.getAddressFirstRep().getState()+", "+currentPatient.getAddressFirstRep().getCountry();
        if(currentPatient.getTelecomFirstRep().getValue() != null)
            phone1=currentPatient.getTelecomFirstRep().getValue();


        Integer version = currentPatient.getIdElement().getVersionIdPartAsLong().intValue();
        versionBox.removeAllItems();
        for(int i=1;i<=versions.getEntry().size();i++){
            versionBox.addItem(String.valueOf(i));
        }
        versionBox.setSelectedItem(currentPatient.getIdElement().getVersionIdPart());
        update.setText(String.valueOf(currentPatient.getMeta().getLastUpdated()));
        id.setText(currentPatient.getIdElement().getIdPart()+" "+currentPatient.getIdElement().getVersionIdPart());
        name.setText(name1+ " "+given1);
        birth.setText(birth1);
        adress.setText(adress1);
        phone.setText(phone1);
        patientEventsList = new DefaultListModel<>();
        list1.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list1.setLayoutOrientation(JList.VERTICAL);
        list1.setVisibleRowCount(-1);
        name.setEditable(false);
        gender.setEditable(false);
        gender.setEnabled(false);

        birth.setEditable(false);
        phone.setEditable(false);

        list1.setModel(patientEventsList);

        for (Pair<Date,String> dateStringPair: patientEvents){
            String addToList = "";
            addToList+=dateStringPair.getKey().toString()+ " - "+dateStringPair.getValue();
            patientEventsList.addElement(addToList);
        }

        measures.forEach((key, value) ->{
            comboBox1.addItem(key);
        });




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

        plotButton.addActionListener(new ActionListener() {
            @Override

            public void actionPerformed(ActionEvent e) {

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
                String selected = (String) comboBox1.getSelectedItem();
                ArrayList<Pair<Date, Integer>> plotData = new ArrayList<>();

                for(Pair<Date, Integer> dataEntry: measures.get(selected)){
                    if(dataEntry.getKey().compareTo(from)>=0 && dataEntry.getKey().compareTo(to)<=0)
                        plotData.add(dataEntry);
                }
                plot(selected,plotData);


            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(!currentPatient.getIdElement().getVersionIdPart().equals(String.valueOf(versions.getEntry().size()))){
                    System.out.println("AAAA"+currentPatient.getIdElement().getVersionIdPart()+" "+String.valueOf(versions.getEntry().size()));
                    JOptionPane.showMessageDialog(panel1,"Please edit lastest version");
                    return;
                }

                if(!editMode){

                    name.setEditable(true);
                    birth.setEditable(true);
                    phone.setEditable(true);
                    gender.setEditable(true);
                    gender.setEnabled(true);
                    oldBirth=birth.getText();
                    oldName=name.getText();
                    oldPhone=phone.getText();
                    oldGender = (String) gender.getSelectedItem();
                    editMode=true;

                }else {

                    name.setEditable(false);
                    gender.setEditable(false);
                    birth.setEditable(false);
                    phone.setEditable(false);
                    gender.setEnabled(false);
                    editMode=false;
                    name.setText(oldName);
                    birth.setText(oldBirth);
                    gender.setSelectedItem(oldGender);
                    phone.setText(oldPhone);
                }





                panel1.revalidate();
                panel1.repaint();

            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(!editMode){
                    JOptionPane.showMessageDialog(panel1,"Please use edit mode");
                    return;
                }



                boolean update= true;
                if(!oldName.equals(name.getText())){
                    List<HumanName> temp = currentPatient.getName();
                    temp.get(0).setFamily(name.getText());
                    currentPatient.setName(temp);
                }
                if(!oldBirth.equals(birth.getText())){
                    try {
                        Date bdate = dateFormat.parse(birth.getText());
                        currentPatient.setBirthDate(bdate);

                    } catch (ParseException e1) {
                        JOptionPane.showMessageDialog(panel1,"Couldn't set date (bad format)e");
                        update=false;
                    }


                }
                if(!oldPhone.equals(phone.getText())){
                    ContactPoint temp = currentPatient.getTelecomFirstRep();
                    temp.setValue(phone.getText());
                    List tempL = currentPatient.getTelecom();
                    tempL.clear();
                    tempL.add(temp);
                    currentPatient.setTelecom(tempL);
                }

                if(!oldPhone.equals(phone.getText())){
                    ContactPoint temp = currentPatient.getTelecomFirstRep();
                    temp.setValue(phone.getText());
                    List tempL = currentPatient.getTelecom();
                    tempL.clear();
                    tempL.add(temp);
                    currentPatient.setTelecom(tempL);
                }

                if(!oldGender.equals((String) gender.getSelectedItem())){
                    String temp = (String) gender.getSelectedItem();
                    switch (temp){
                        case "Female":
                            currentPatient.setGender(Enumerations.AdministrativeGender.FEMALE);
                            break;
                        case "Male":
                            currentPatient.setGender(Enumerations.AdministrativeGender.MALE);
                            break;

                        case "Other":
                            currentPatient.setGender(Enumerations.AdministrativeGender.OTHER);
                            break;

                        case "Unknown":
                            currentPatient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
                            break;
                    }
                }



                if(update){
                    versionBox.removeActionListener(versionBox.getActionListeners()[0]);
                    f.upadtePatient(currentPatient);
                    versions = f.getPatientHistory(currentPatient.getIdElement().getIdPart());
                    versionBox.removeAllItems();
                    for(int i=1;i<=versions.getEntry().size();i++){
                        versionBox.addItem(String.valueOf(i));
                    }

                    currentPatient = (Patient) versions.getEntry().get(versions.getEntry().size()-1).getResource();
                    versionBox.setSelectedIndex(versions.getEntry().size()-1);
                    showVersion();
                    JOptionPane.showMessageDialog(panel1,"Updated");
                    versionBox.addActionListener(myActionListener);


                }
            }
        });


        versionBox.addActionListener(myActionListener);
    }

    public JPanel getPanel1() {
        return panel1;
    }

    public void showVersion(){
        String name1 ="<unknown>";
        String given1=" <unknown>";
        String gender1="<unknown>";
        String birth1="<unknown>";
        String adress1="<unknown>";
        String phone1="<unknown>";

        id.setText(currentPatient.getIdElement().getIdPart()+" "+currentPatient.getIdElement().getVersionIdPart());
        if(currentPatient.getName().get(0).getFamily()!=null)
            name1=currentPatient.getName().get(0).getFamily();
        if(currentPatient.getName().get(0).getGiven()!=null)
            given1=currentPatient.getName().get(0).getGiven().toString();
        if(currentPatient.getGender() != null){
            gender1=currentPatient.getGender().getDisplay();
            if(gender1.toLowerCase().equals("female"))
                gender.setSelectedItem("Female");
            if(gender1.toLowerCase().equals("male"))
                gender.setSelectedItem("Male");
            if(gender1.toLowerCase().equals("other"))
                gender.setSelectedItem("Other");
            if(gender1.toLowerCase().equals("unknown"))
                gender.setSelectedItem("Unknown");

        }
        if(currentPatient.getBirthDate() != null)
            birth1=dateFormat.format(currentPatient.getBirthDate());
        if(currentPatient.getAddressFirstRep().toString() != null)

            adress1=currentPatient.getAddressFirstRep().getPostalCode() +", "+currentPatient.getAddressFirstRep().getCity()+", "+currentPatient.getAddressFirstRep().getState()+", "+currentPatient.getAddressFirstRep().getCountry();
        if(currentPatient.getTelecomFirstRep().getValue() != null)
            phone1=currentPatient.getTelecomFirstRep().getValue();

        update.setText(String.valueOf(currentPatient.getMeta().getLastUpdated()));
        name.setText(name1);
        birth.setText(birth1);
        adress.setText(adress1);
        phone.setText(phone1);


    }

    private void plot(String measurement,ArrayList<Pair<Date, Integer>> data ){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame(measurement);

                frame.setSize(600, 400);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setVisible(true);

                TimeSeries series = new TimeSeries("Data");
                for(Pair<Date, Integer> dataEntry: data){
                    series.add(new Day(dataEntry.getKey()),dataEntry.getValue().floatValue());
                }
                TimeSeriesCollection dataset = new TimeSeriesCollection();
                dataset.addSeries(series);



                JFreeChart chart = ChartFactory.createTimeSeriesChart("Test Chart",
                        "x", "y", dataset,false, false, false);


                ChartPanel cp = new ChartPanel(chart);

                frame.getContentPane().add(cp);
            }
        });



    }



}
