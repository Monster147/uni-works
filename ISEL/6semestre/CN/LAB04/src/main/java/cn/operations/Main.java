package cn.operations;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Main {
    private static Firestore db;
    private static String collectionName = "ocupacoes";

    public void main() throws Exception {
        initializeFirestore();
        runApp();
        db.close();
    }

    private static void initializeFirestore() throws Exception {
        GoogleCredentials credentials =
                GoogleCredentials.getApplicationDefault();

        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId("firestore-cn-lab4-standard").setCredentials(credentials)
                .build();
        db = options.getService();
    }

    public static void runApp() throws Exception{
        Scanner scanner = new Scanner(System.in);

        while (true){
            System.out.println("0. Qual é o nome da coleção? (Uso apenas após o input de um documento e reinicio da app)");
            System.out.println("1. Carregar dados CSV para Firestore");
            System.out.println("2. Apresentar documento por ID (ex: Lab4-2017)");
            System.out.println("3. Apagar campo de um documento");
            System.out.println("4. Interrogação simples - documentos de uma freguesia");
            System.out.println("5. Interrogação composta - critérios múltiplos");
            System.out.println("6. Documentos com eventos em fevereiro de 2017");
            System.out.println("7. Documentos com eventos entre duas datas");
            System.out.println("99. Sair");
            System.out.print("Escolha: ");
            String option = scanner.nextLine();
            switch (option) {
                case "0":
                    setupCollectionName(scanner);
                    break;
                case "1":
                    loadCSVToFirestore(scanner);
                    break;
                case "2":
                    printDocumentByID(scanner);
                    break;
                case "3":
                    deleteFieldFromDocument(scanner);
                    break;
                case "4":
                    queryByFreguesia(scanner);
                    break;
                case "5":
                    complexQuery(scanner);
                    break;
                case "6":
                    queryFebruary2017();
                    break;
                case "7":
                    queryBetweenDates(scanner);
                    break;
                case "99":
                    System.out.println("Saindo...");
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    public static void setupCollectionName(Scanner scanner) throws Exception{
        System.out.print("Qual é o nome da coleção: ");
        String input = scanner.nextLine();
        if(input.isEmpty()) collectionName = "ocupacoes";
        else collectionName = input;
    }

    public static void loadCSVToFirestore(Scanner scanner) throws Exception{
        System.out.print("Caminho do ficheiro CSV (padrão: OcupacaoEspacosPublicos.csv): ");
        String pathnameCsv = scanner.nextLine();
        if (pathnameCsv.isEmpty()) {
            pathnameCsv = "OcupacaoEspacosPublicos.csv";
        }

        System.out.print("Nome da coleção (padrão: ocupacoes): ");
        collectionName = scanner.nextLine();
        if (collectionName.isEmpty()) {
            collectionName = "ocupacoes";
        }

        insertDocuments(pathnameCsv, db, collectionName);
    }

    public static OcupacaoTemporaria convertLineToObject (String line) throws Exception {
        String[] cols = line.split(",");
        OcupacaoTemporaria ocup = new OcupacaoTemporaria();
        ocup.ID = Integer.parseInt(cols[0]);
        ocup.location = new Localizacao();
        ocup.location.point = new GeoPoint(Double.parseDouble(cols[1]), Double.parseDouble(cols[2]));
        ocup.location.coord = new Coordenadas();
        ocup.location.coord.X = Double.parseDouble(cols[1]);
        ocup.location.coord.Y = Double.parseDouble(cols[2]);
        ocup.location.freguesia = cols[3];
        ocup.location.local = cols[4];
        ocup.event = new Evento();
        ocup.event.evtID = Integer.parseInt(cols[5]);
        ocup.event.nome = cols[6];
        ocup.event.tipo = cols[7];
        ocup.event.details = new HashMap<String, String>();
        if (!cols[8].isEmpty()) ocup.event.details.put("Participantes", cols[8]);
        if (!cols[9].isEmpty()) ocup.event.details.put("Custo", cols[9]);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        ocup.event.dtInicio = formatter.parse(cols[10]);
        ocup.event.dtFinal = formatter.parse(cols[11]);
        ocup.event.licenciamento = new Licenciamento();
        ocup.event.licenciamento.code = cols[12];
        ocup.event.licenciamento.dtLicenc = formatter.parse(cols[13]);
        return ocup;
    }

    public static void insertDocuments(String pathnameCSV, Firestore db, String collectionName)
            throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(pathnameCSV));
        CollectionReference colRef = db.collection(collectionName);
        String line;
        while ((line = reader.readLine()) != null) {
            OcupacaoTemporaria ocup = convertLineToObject(line);
            DocumentReference docRef = colRef.document("Lab4-" + ocup.ID);
            ApiFuture<WriteResult> resultFut = docRef.set(ocup);
            WriteResult result = resultFut.get();
            System.out.println("Update time : " + result.getUpdateTime());
        }
    }

    // 2a.
    public static void printDocumentByID(Scanner scanner) throws ExecutionException, InterruptedException {
        System.out.print("Introduza o ID do documento (ex: 2017): ");
        String id = scanner.nextLine();
        String docId = "Lab4-" + id;

        DocumentSnapshot doc = db.collection(collectionName).document(docId).get().get();

        if (doc.exists()) {
            System.out.println("\n=== Documento: " + docId + " ===");
            System.out.println(doc.getData());
        } else {
            System.out.println("Documento não encontrado!");
        }
    }

    // 2b.
    public static void deleteFieldFromDocument(Scanner scanner) throws Exception{
        System.out.print("Introduza o ID do documento a ser removido (ex: 2017): ");
        String id = scanner.nextLine();
        String docId = "Lab4-" + id;

        DocumentReference docRef = db.collection(collectionName).document(docId);
        DocumentSnapshot doc = docRef.get().get();

        if (!doc.exists()) {
            System.out.println("Erro: Documento não existe.");
            return;
        }

        System.out.print("Introduza o nome do campo a apagar: ");
        String fieldName = scanner.nextLine();

        if (doc.contains(fieldName)) {
            docRef.update(fieldName, FieldValue.delete()).get();
            System.out.println("Campo '" + fieldName + "' apagado com sucesso.");
            return;
        }

        for (String key : doc.getData().keySet()) {
            Object value = doc.get(key);

            if (value instanceof Map) {
                Map<?, ?> subMap = (Map<?, ?>) value;

                if (subMap.containsKey(fieldName)) {
                    System.out.println(
                            "Erro: O campo '" + fieldName + "' está dentro de '" + key + "'.\n" +
                                    "Use o caminho completo: '" + key + "." + fieldName + "'"
                    );
                    return;
                }
            }
        }

        System.out.println("Erro: Campo '" + fieldName + "' não encontrado no documento.");
    }

    public static void queryByFreguesia(Scanner scanner) throws Exception{
        System.out.print("Introduza o nome da freguesia: ");
        String freguesia = scanner.nextLine();

        Query query = db.collection(collectionName).whereEqualTo("location.freguesia", freguesia);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        System.out.println("\n=== Documentos na freguesia '" + freguesia + "' ===");
        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            System.out.println(doc.getId() + ": " + doc.getData());
        }
    }

    public static void complexQuery(Scanner scanner) throws Exception {
        System.out.print("Introduza o ID mínimo: ");
        int minID = Integer.parseInt(scanner.nextLine());

        System.out.print("Introduza o nome da freguesia: ");
        String freguesia = scanner.nextLine();

        System.out.print("Introduza o tipo de evento: ");
        String tipo = scanner.nextLine();

        Query query = db.collection(collectionName)
                .whereGreaterThan("ID", minID)
                .whereEqualTo("location.freguesia", freguesia)
                .whereEqualTo("event.tipo", tipo);


        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

        System.out.println("\n=== Documentos com ID maior que " + minID + ", freguesia '" + freguesia + "' e tipo de evento '" + tipo + "' ===");
        for (QueryDocumentSnapshot doc : documents) {
            System.out.println(doc.getId() + ": " + doc.getData());
        }
    }

    public static void queryFebruary2017() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Query query = db.collection(collectionName)
                .whereGreaterThanOrEqualTo("event.dtInicio", formatter.parse("01/02/2017"))
                .whereLessThanOrEqualTo("event.dtFinal", formatter.parse("28/02/2017"));

        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

        System.out.println("\n=== Documentos com eventos em fevereiro de 2017 ===");
        for (QueryDocumentSnapshot doc : documents) {
            System.out.println(doc.getId() + ": " + doc.getData());
        }
    }

    public static void queryBetweenDates(Scanner scanner) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        System.out.print("Introduza a data de início (dd/MM/yyyy): ");
        String startDateStr = scanner.nextLine();
        System.out.print("Introduza a data de fim (dd/MM/yyyy): ");
        String endDateStr = scanner.nextLine();

        Query query = db.collection(collectionName)
                .whereGreaterThanOrEqualTo("event.dtInicio", formatter.parse(startDateStr))
                .whereLessThanOrEqualTo("event.dtFinal", formatter.parse(endDateStr));

        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

        System.out.println("\n=== Documentos com eventos entre " + startDateStr + " e " + endDateStr + " ===");
        for (QueryDocumentSnapshot doc : documents) {
            System.out.println(doc.getId() + ": " + doc.getData());
        }
    }
}
