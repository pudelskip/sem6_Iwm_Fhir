import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IBasicClient;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;

import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBundle;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FhirHelper {


    FhirContext context= null;
    IGenericClient client= null;
    String serverBase;

    String[] examplePatients = {"152","IPS-examples-Patient-01","1321198","2035121"};
    public FhirHelper() {

        if(Constants.FHIR_BASE=="")
            throw new RuntimeException("No fhir base found");
        else
            serverBase= Constants.FHIR_BASE;

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

        //    this.getPatientEverything("patient-1");
        Bundle b =client
        .search()
        .forResource(Patient.class)
        .where(new TokenClientParam("_id").exactly().code("06eb35fc-09e6-48b4-a311-47633f6c4769"))
        .returnBundle(Bundle.class).execute();

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

    public void upadtePatient(Patient p){
        client.update().resource(p).execute();
    }

    public Bundle getPatientHistory(String id){
        Bundle bundle =null;
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(5000, TimeUnit.MILLISECONDS);
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setConnectionManager(connectionManager);
        CloseableHttpClient ourClient = builder.build();
        HttpGet httpGet = new HttpGet(Constants.FHIR_BASE + "/Patient/"+id+"/_history");
        HttpResponse status = null;
        try {
            status = ourClient.execute(httpGet);
            String responseContent = IOUtils.toString(status.getEntity().getContent());
            IOUtils.closeQuietly(status.getEntity().getContent());
            if(status!= null && status.getStatusLine().getStatusCode()==200)

                bundle =context.newJsonParser().parseResource(Bundle.class, responseContent);
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        return bundle;

    }


    public  PatientEntry getPatientEverything(String id){

            HashMap<String, ArrayList<Pair<Date,Integer>>> measures = new HashMap<>();
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

                if (resource instanceof MedicationRequest) {
                    MedicationRequest mStatment = (MedicationRequest) resource;

                    try {
                        //CodeableConcept =-mStatment.getContained()
                        String medName = mStatment.getMedicationCodeableConcept().getText();
                        //Period period = mStatment.getEffectivePeriod();
                        mStatment.getAuthoredOn();

                        if (medName == null)
                            medName = "nameNotFound";

                        if (mStatment.getAuthoredOn()!= null) {

                               // events.put(start, "Started taking medicine " + medName);
                                ev.add(new Pair<>(mStatment.getAuthoredOn(), "Started taking medication " + medName));

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
                    int valueInt = 0;
                    try {
                        DateTimeType effectiveDate = observation.getEffectiveDateTimeType();
                        date = effectiveDate.getValue();
                        dateString = effectiveDate.toHumanDisplay();

                        if(observationText==null){
                            int dsa=1;
                        System.out.println(observation.getCode().getText());}
                    }  catch (FHIRException e) {
                        System.out.println("cannot extract date (Some fhir exception that doesnt even work)");
                    } catch (NullPointerException e) {
                        System.out.println("cannot extract date (null)");
                    }

                    try {
                        value=" (" +observation.getValueQuantity().getValue().toString()+" "+observation.getValueQuantity().getUnit()+")";
                        valueInt = observation.getValueQuantity().getValue().intValue();
                        observationText = observation.getCode().getText();
                        if (date != null) {
                            if(measures.containsKey(observationText)) {
                                measures.get(observationText).add(new Pair(date,valueInt));
                            }else{
                                measures.put(observationText,new ArrayList<Pair<Date, Integer>>());
                                measures.get(observationText).add(new Pair(date,valueInt));
                            }
                            ev.add(new Pair<>(date, "Observation: " + observationText+value+" - "+ observation.getId()));
                        }




                    } catch (NullPointerException e) {
                        //value=" (" +observation.getComponent().get(0).getValueQuantity().getValue()+" "+observation.getComponent().get(0).getValueQuantity().getUnit()+")";
                        System.out.println("No value");
                        try {
                            for(int i=0;i<observation.getComponent().size();i++){
                                System.out.println(observation.getComponent().get(i).getValueQuantity().getValue().toString());
                                value=" (" +observation.getComponent().get(i).getValueQuantity().getValue().toString()+" "+observation.getComponent().get(0).getValueQuantity().getUnit()+")";
                                valueInt = observation.getComponent().get(i).getValueQuantity().getValue().intValue();
                                observationText = observation.getComponent().get(i).getCode().getText();
                                if (date != null) {
                                    if(measures.containsKey(observationText)) {
                                        measures.get(observationText).add(new Pair(date,valueInt));
                                    }else{
                                        measures.put(observationText,new ArrayList<Pair<Date, Integer>>());
                                        measures.get(observationText).add(new Pair(date,valueInt));
                                    }
                                    ev.add(new Pair<>(date, "Observation: " + observationText+value+" - "+ observation.getId()));
                                }

                            }

                        } catch (NullPointerException e1) {
                            System.out.println("WOW,everything failed now there is nothing more that can be done");
                        }catch (FHIRException e2) {
                            System.out.println("No value2 (Some fhir exception that doesnt even work)");
                        }



                    } catch (FHIRException e) {
                        System.out.println("No value (Some fhir exception that doesnt even work)");
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
        PatientEntry patientEntry = new PatientEntry(sorted,measures);


        return patientEntry;

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
