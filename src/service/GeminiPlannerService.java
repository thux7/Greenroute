package service;

import model.ElectricVehicle;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;

public class GeminiPlannerService implements IAPlannerService {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;
    private OkHttpClient client = new OkHttpClient();

    @Override
    public ElectricVehicle extractVehicleData(String freeText) throws Exception {
        String prompt = "Extraia os seguintes dados de um veículo elétrico a partir do texto abaixo e retorne um JSON com os campos: modelo, autonomiaMaxima (número), cargaBateriaAtual (número), tipoConector (string), tempoRecargaRapida (número), consumoKwhPorKm (número), tempoRecargaCompleta (número). Texto: " + freeText;
        String response = callGemini(prompt);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        String model = json.get("modelo").getAsString();
        double maxRange = json.get("autonomiaMaxima").getAsDouble();
        double battery = json.get("cargaBateriaAtual").getAsDouble();
        String connector = json.get("tipoConector").getAsString();
        int fastCharge = json.get("tempoRecargaRapida").getAsInt();
        double consumption = json.get("consumoKwhPorKm").getAsDouble();
        int fullCharge = json.get("tempoRecargaCompleta").getAsInt();
        return new ElectricVehicle(0, model, maxRange, battery, connector, fastCharge, consumption, fullCharge);
    }

    @Override
    public String planRoute(String vehicleModel, String destination, double currentBattery) {
        String prompt = "Planeje uma rota para o veículo " + vehicleModel +
                " com bateria atual de " + currentBattery + "%. Destino: " + destination +
                ". Considere clima, trânsito e dicas de recarga. Dê uma resposta descritiva em português.";
        try {
            return callGemini(prompt);
        } catch (Exception e) {
            return "Erro ao obter planejamento: " + e.getMessage();
        }
    }

    private String callGemini(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        JsonObject content = new JsonObject();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        content.add("parts", new com.google.gson.JsonArray());
        content.getAsJsonArray("parts").add(part);
        requestBody.add("contents", new com.google.gson.JsonArray());
        requestBody.getAsJsonArray("contents").add(content);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), requestBody.toString());

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Erro na API: " + response.code());
            String jsonResponse = response.body().string();
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String text = root.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
            return text;
        }
    }
}