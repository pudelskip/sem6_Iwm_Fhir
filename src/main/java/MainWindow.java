import org.hl7.fhir.dstu3.model.Bundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodListener;
import java.util.ArrayList;

public class MainWindow {
    private JList list1;
    private JPanel panel1;
    private JTextField textField1;
    private JButton searchButton;
    private JButton previousButton;
    private JButton nextButton;
    private  DefaultListModel<String> patientList;
    private FhirHelper f;
    private Bundle currentBundle;

    public MainWindow() {



        f = new FhirHelper();

        patientList = new DefaultListModel<>();
        list1.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list1.setLayoutOrientation(JList.VERTICAL);
        list1.setVisibleRowCount(-1);
       // JScrollPane listScroller = new JScrollPane(list1);
        //listScroller.setPreferredSize(new Dimension(250, 80));
        list1.setModel(patientList);

       JList<String> list = new JList<>(patientList);
        list.setBounds(0,40, 75,75);
        //list1.add(list);


        list1.setSelectionBackground(Color.CYAN);
//
//
//        list1.setSize(200,200);
//
//        list1.setVisible(true);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentBundle = f.search(20);
                ArrayList<String> patients = f.getPatientListFromBudle(currentBundle);
                for (String p: patients){
                    patientList.addElement(p);
                }

            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentBundle!=null){
                    Bundle temp = f.getNextBundle(currentBundle);
                    currentBundle=temp;
                    patientList.clear();
                    ArrayList<String> patients = f.getPatientListFromBudle(currentBundle);
                    for (String p: patients){
                        patientList.addElement(p);
                    }

                }

            }
        });
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentBundle!=null){
                    Bundle temp = f.getPreviousBundle(currentBundle);
                    currentBundle=temp;
                    patientList.clear();
                    ArrayList<String> patients = f.getPatientListFromBudle(currentBundle);
                    for (String p: patients){
                        patientList.addElement(p);
                    }

                }
            }
        });
    }

    public JPanel getPanel1() {
        return panel1;
    }

    public void addToPatientList(String val){
        if(patientList!=null)
            patientList.addElement(val);
    }
}
