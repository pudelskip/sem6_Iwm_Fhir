import javax.swing.*;
import java.awt.*;

public class Main {


    public static void main(String[] args) {

        FhirHelper f = new FhirHelper();
        //f.getPatientEverything("IPS-examples-Patient-01");
        JFrame frame = new JFrame("Fhir");
        frame.setPreferredSize(new Dimension(800,600));
        frame.setContentPane(new MainWindow().getPanel1());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack(); //code wouldnt work if i comment out this line
        frame.setVisible(true);

    }

}
