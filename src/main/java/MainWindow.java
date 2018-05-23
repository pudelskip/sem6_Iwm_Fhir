import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
        list1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    Patient p = (Patient) currentBundle.getEntry().get(index).getResource();
                    f.getPatientEverything(p.getIdElement().getIdPart());
                    System.out.print(index);
                }
            }
        });
        //list1.add(list);


//        list1.setSize(200,200);
//
//        list1.setVisible(true);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame newFrame=new JFrame();

                newFrame.setVisible(true);
                newFrame.setSize(new Dimension(500,400));

                currentBundle = f.search(20);
                ArrayList<String> patients = f.getPatientListFromBundle(currentBundle);
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
                    ArrayList<String> patients = f.getPatientListFromBundle(currentBundle);
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
                    ArrayList<String> patients = f.getPatientListFromBundle(currentBundle);
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
