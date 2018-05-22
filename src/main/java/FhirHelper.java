import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseBundle;

public class FhirHelper {


    FhirContext context= null;
    IGenericClient client= null;

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

    public void search(){
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .returnBundle(Bundle.class)
                .execute();

        for (Bundle.BundleEntryComponent entry : results.getEntry()) {

            if (entry.getResource() instanceof Patient) {
                Patient patient = (Patient) entry.getResource();
                System.out.println(patient.getId());


            }

        }

    }
}
