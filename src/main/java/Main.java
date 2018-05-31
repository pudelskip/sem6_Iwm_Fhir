import javax.swing.*;
import java.awt.*;

public class Main {


    public static void main(String[] args) {

        // obsercvatoins test  "3776058,3768691"
        FhirHelper f = new FhirHelper();
       // f.test();
       //f.getPatientEverything("3776058");
        JFrame frame = new JFrame("Fhir");
        frame.setPreferredSize(new Dimension(1000,600));
        frame.setContentPane(new MainWindow().getPanel1());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack(); //code wouldnt work if i comment out this line
        frame.setVisible(true);

    }

}
