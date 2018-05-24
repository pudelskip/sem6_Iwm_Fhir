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
    private JTextField textField2;
    private DefaultListModel<String> patientList;
    private FhirHelper f;
    private Bundle currentBundle;

    public MainWindow() {



        f = new FhirHelper();

        patientList = new DefaultListModel<>();

        list1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    Patient p = (Patient) currentBundle.getEntry().get(index).getResource();
                    JFrame frame=new JFrame("Patient Details");
                    frame.setPreferredSize(new Dimension(800,600));
                    frame.setContentPane(new PatientWindow(p).getPanel1());
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.pack(); //code wouldnt work if i comment out this line
                    frame.setVisible(true);

                    System.out.print(index);
                }
            }
        });
        patientList = new DefaultListModel<>();
        list1.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list1.setLayoutOrientation(JList.VERTICAL);
        list1.setVisibleRowCount(-1);

        list1.setModel(patientList);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                patientList.clear();

                currentBundle = f.search(20,textField1.getText(),textField2.getText());
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
