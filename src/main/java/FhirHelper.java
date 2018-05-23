import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;

import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseBundle;

import java.util.ArrayList;

public class FhirHelper {


    FhirContext context= null;
    IGenericClient client= null;

    String[] examplePatients = {"IPS-examples-Patient-01","1321198","2035121"};
    public FhirHelper() {
        String serverBase = "http://hapi.fhir.org/baseDstu3";

        context = FhirContext.forDstu3();
        context.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        client = context.newRestfulGenericClient(serverBase);
        IParser parser = context.newXmlParser();

//        Bundle results = client
//                .search()
//                .forResource(Patient.class)
//                .where(Patient.IDENTIFIER.exactly().systemAndCode("https://fhir.nhs.uk/Id/nhs-number", "9876543210"))
//                .returnBundle(Bundle.class)
//                .execute();
    }

    public Bundle search(int count){
        ArrayList<String> patients = new ArrayList<>();

        Bundle results = client
                .search()
                .forResource(Patient.class)
                .count(count)
                .returnBundle(Bundle.class)
                .execute();

        return results;
    }

    public Bundle getNextBundle(Bundle b){
        Bundle nextBudle = b;
        if(b.getLink(IBaseBundle.LINK_NEXT) != null)
            nextBudle= client.loadPage().next(b).execute();
        return nextBudle;

    }

    public Bundle getPreviousBundle(Bundle b){
        Bundle prevBudle = b;
        if(b.getLink(IBaseBundle.LINK_PREV) != null)
        prevBudle = client.loadPage().previous(b).execute();
        return prevBudle;

    }

    public ArrayList<String> getPatientListFromBudle(Bundle b){
        ArrayList<String> patients = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : b.getEntry()) {
            if (entry.getResource() instanceof Patient) {
                Patient patient = (Patient) entry.getResource();
                patients.add(patient.getId().toString());
                System.out.println(patient.getId());
            }
        }
        return patients;

    }


    public void getPatientEverything(String id){

            // Invoke $everything on our Patient
            // See http://hapifhir.io/doc_rest_client.html#Extended_Operations
            Parameters outParams = client
                    .operation()
                    .onInstance(new IdType("Patient", id))
                    .named("$everything")
                    .withNoParameters(Parameters.class).useHttpGet() // No input parameters
                    .execute();

            // FHIR normally returns a 'Parameters' resource to an operation, but
            // in case of a single resource response, it just returns the resource
            // itself. This is why it seems that we have to fish a Bundle out of the
            // resulting Params result - HAPI needs to update for the FHIR shortcut
            Bundle result = (Bundle) outParams.getParameterFirstRep().getResource();

            System.out.println("Received " + result.getTotal()
                    + " results. The resources are:");
            result.getEntry().forEach((entry) -> {
                Resource resource = entry.getResource();

                if (resource instanceof Patient) {
                    Patient patient = (Patient) resource;
                    System.out.println(resource.getResourceType() + "/"
                            + resource.getIdElement().getIdPart() + " "
                            +patient.addName().getFamily()

                    );
                }

                if (resource instanceof MedicationStatement) {
                    MedicationStatement mStatment = (MedicationStatement) resource;
                    try {
                        Period period = mStatment.getEffectivePeriod();
                        if(period != null)
                            System.out.println(resource.getResourceType() + "/"
                                    + resource.getIdElement().getIdPart() + " "
                                    + period.getStart()
                            );
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                }

                if (resource instanceof Observation) {
                    Observation observation = (Observation) resource;
                }

                if (resource instanceof Medication) {
                    Medication medication = (Medication) resource;
                }



            });


    }
}
