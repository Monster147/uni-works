package grpcservices;

import com.google.api.client.util.ArrayMap;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AppMain {

    public static Map<String, Double> detectLabels(String gsURI) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();
        Map<String, Double> labelsWithScores = new ArrayMap<>();

        // Obtém imagem diretamente de um ficheiro: para testes locais
        // ByteString imgBytes = ByteString.readFrom(new FileInputStream("cat.jpg"));
        //Image img = Image.newBuilder().setContent(imgBytes).build();
        // Obtém imagem diretamente do serviço Storage usando um gs URI (gs://...) para o Blob com imagem
        Image img = Image.newBuilder()
                .setSource(ImageSource.newBuilder().setImageUri(gsURI).build())
                .build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                } else {
                    // For full list of available annotations, see http://g.co/cloud/vision/docs
                    for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                        double rounded = Math.round(annotation.getScore() * 100.0) / 100.0;
                        labelsWithScores.put(annotation.getDescription(), rounded);
//                        annotation.getAllFields()
//                                .forEach((k, v) -> System.out.format("%s : %s%n", k, v.toString()));
                    }
                }
            }
        }
        return labelsWithScores;
    }

    static List<String> TranslateLabels(List<java.lang.String> labels) {
        List<java.lang.String> labelsTranslated = null;
        try {
            Translate translateService = TranslateOptions.getDefaultInstance().getService();
            labelsTranslated = new ArrayList<>();
            for (java.lang.String label : labels) {
                Translation translation = translateService.translate(
                        label,
                        Translate.TranslateOption.sourceLanguage("en"),
                        Translate.TranslateOption.targetLanguage("pt-PT"));
                labelsTranslated.add(translation.getTranslatedText());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return labelsTranslated;
        }
    }

}
