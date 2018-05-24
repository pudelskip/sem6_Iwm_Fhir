import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;

import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import javafx.util.Pair;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBundle;

import java.util.*;

public class FhirHelper {


    FhirContext context= null;
    IGenericClient client= null;

    String[] examplePatients = {"152","IPS-examples-Patient-01","1321198","2035121"};
    public FhirHelper() {
        String serverBase = "http://hapi.fhir.org/baseDstu3";

        context = FhirContext.forDstu3();
        context.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        context.getRestfulClientFactory().setConnectionRequestTimeout(20*1000);
        context.getRestfulClientFactory().setSocketTimeout(20*1000);
        client = context.newRestfulGenericClient(serverBase);
        IParser parser = context.newXmlParser();

//        Bundle results = client
//                .search()
//                .forResource(Patient.class)
//                .where(Patient.IDENTIFIER.exactly().systemAndCode("https://fhir.nhs.uk/Id/nhs-number", "9876543210"))
//                .returnBundle(Bundle.class)
//                .execute();
    }

    public void test(){

            this.getPatientEverything("patient-1");
//        Bundle b =client
//        .search()
//        .forResource(Patient.class)
//        .where(new TokenClientParam("_id").exactly().code("IPS-examples-Patient-01"))
//        .returnBundle(Bundle.class).execute();
//
//        for (Bundle.BundleEntryComponent entry : b.getEntry()) {
//            if (entry.getResource() instanceof Patient) {
//                Patient patient = (Patient) entry.getResource();
//                patient.getName().get(0).getFamily();
//                List<HumanName> h =  patient.getName();
//
//            }
//        }
    }


    public Bundle search(int count,String familyName,String id){
        ArrayList<String> patients = new ArrayList<>();
        Bundle results=null;
        IQuery<IBaseBundle> query = client
                .search()
                .forResource(Patient.class)
                .count(count);
                //.where(new TokenClientParam("_sort").exactly().code("_id"));


        if(familyName!=null && familyName !="") {
            query = query.where(new StringClientParam("family").matches().value(familyName));

        }

        if(id!=null && id !=""){
            query = query.where(new TokenClientParam("_id").exactly().code(id));
        }


            results = query.returnBundle(Bundle.class).execute();


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

    public ArrayList<String> getPatientListFromBundle(Bundle b){
        ArrayList<String> patients = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : b.getEntry()) {
            if (entry.getResource() instanceof Patient) {
                Patient patient = (Patient) entry.getResource();
                String familyName="Uknown";
                if(patient.getName().size()>0)
                    familyName = patient.getName().get(0).getFamily();
                patients.add(patient.getIdElement().getIdPart()+": "+familyName);
            }
        }
        return patients;

    }


    public  ArrayList<Pair<Date,String>> getPatientEverything(String id){


            HashMap<Date,String> events = new HashMap<>();
            ArrayList<Pair<Date,String>> ev = new ArrayList<>();
            Parameters outParams = client
                    .operation()
                    .onInstance(new IdType("Patient", id))
                    .named("$everything")
                    .withNoParameters(Parameters.class).useHttpGet() // No input parameters
                    .execute();

            Bundle result = (Bundle) outParams.getParameterFirstRep().getResource();
        int pages=0;
        while(result.getLink(IBaseBundle.LINK_NEXT) != null) {
            pages+=1;
            if(pages>10) break;


//            System.out.println("Received " + result.getTotal()
//                    + " results. The resources are:");
            result.getEntry().forEach((entry) -> {
                Resource resource = entry.getResource();

                if (resource instanceof Patient) {
                    Patient patient = (Patient) resource;
//                    System.out.println(resource.getResourceType() + "/"
//                            + resource.getIdElement().getIdPart() + " "
//                    );
                }

                if (resource instanceof MedicationStatement) {
                    MedicationStatement mStatment = (MedicationStatement) resource;

                    try {
                        //CodeableConcept =-mStatment.getContained()
                        String medName = mStatment.getMedicationCodeableConcept().getText();
                        Period period = mStatment.getEffectivePeriod();

                        if (medName == null)
                            medName = "nameNotFound";

                        if (period != null) {
                            Date start = period.getStart();
                            Date end = period.getEnd();
                            if (start != null) {
                               // events.put(start, "Started taking medicine " + medName);
                                ev.add(new Pair<>(start, "Started taking medication " + medName));
                            }
                            if (end != null) {
                                //events.put(end, "Stopped taking medicine " + medName);
                                ev.add(new Pair<>(end, "Stopped taking medication " + medName));
                            }
//                            System.out.println(resource.getResourceType() + "/"
//                                    + resource.getIdElement().getIdPart() + " "
//                                    + period.getStart()
//                            );
                        }


                    } catch (Exception e) {
                       // System.out.print(e.toString());
                        e.printStackTrace();
                    }

                }

                if (resource instanceof Observation) {
                    Observation observation = (Observation) resource;
                    Date date = null;
                    String dateString = "";
                    String observationText = "";
                    String value = "";
                    try {
                        DateTimeType effectiveDate = observation.getEffectiveDateTimeType();
                        date = effectiveDate.getValue();
                        dateString = effectiveDate.toHumanDisplay();
                        observationText = observation.getCode().getText();
                        value=" (" +observation.getValueQuantity().getValue().toString()+" "+observation.getValueQuantity().getUnit()+")";
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    if (date != null) {
                      //  events.put(date, "Observation: " + observationText + " " + date);
                        ev.add(new Pair<>(date, "Observation: " + observationText+value ));
                    }
//                    System.out.println(resource.getResourceType() + "/"
//                            + resource.getIdElement().getIdPart() + " "
//                            + dateString
//
//                    );
                }

                if (resource instanceof Medication) {
                    Medication medication = (Medication) resource;
//                    System.out.println(resource.getResourceType() + "/"
//                            + resource.getIdElement().getIdPart() + " "
//
//                    );
                }

            });
            Bundle next = client.loadPage().next(result).execute();
            result=next;
        }
        ArrayList<Pair<Date,String>> sorted= sortEvents(ev);


        return sorted;

    }


    private ArrayList<Pair<Date,String>> sortEvents(ArrayList<Pair<Date,String>> events){
       events.sort(new Comparator<Pair<Date, String>>() {
            @Override
            public int compare(Pair<Date, String> o1, Pair<Date, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        return events;
    }
}
