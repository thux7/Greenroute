package service;

import model.ElectricVehicle;
import model.HybridVehicle;
import model.Vehicle;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.text.Normalizer;
import java.util.concurrent.TimeUnit;

public class GeminiPlannerService implements IAPlannerService {

    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    public Vehicle extractVehicleData(String freeText) throws Exception {
        String prompt = "Com base no seu conhecimento, forneça as especificações técnicas do veículo \"" + freeText.trim() + "\".\n" +
                "Responda exatamente no formato abaixo, com 10 campos separados por ponto e vírgula (;):\n" +
                "1. MODELO_DO_VEICULO (string)\n" +
                "2. Tempo de recarga de 0% a 100% em carregador AC (apenas número em minutos)\n" +
                "3. Tipo do conector (Tipo 2, CCS2 ou CHAdeMO)\n" +
                "4. Capacidade da bateria em kWh (apenas número)\n" +
                "5. Tempo de recarga em carregador DC rápido (apenas número em minutos)\n" +
                "6. Autonomia máxima em quilômetros no modo elétrico (apenas número)\n" +
                "7. Tipo de combustível (Elétrico, Gasolina, Etanol, Flex, Diesel ou Híbrido)\n" +
                "8. Capacidade do tanque de combustível em litros (apenas número ou 0 se não possuir)\n" +
                "9. Autonomia máxima em quilômetros no motor a combustão (apenas número ou 0 se não possuir)\n" +
                "10. Consumo de combustível em km/l no motor a combustão (apenas número ou 0 se não possuir)\n\n" +
                "Exemplo de saída: BYD DOLPHYN;420;CCS2;45;30;291;Elétrico;0;0;0\n" +
                "Não inclua unidades, explicações, quebras de linha ou qualquer texto adicional.";

        String response = callGemini(prompt);
        String[] parts = response.split(";");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        if (parts.length != 10) {
            String cleaned = response.replaceAll("\\s+", " ");
            String[] fallback = cleaned.split(";");
            for (int i = 0; i < fallback.length; i++) {
                fallback[i] = fallback[i].trim();
            }
            if (fallback.length == 10) {
                parts = fallback;
            } else {
                throw new IOException("Resposta inválida (esperado 10 campos): " + response);
            }
        }

        String model = parts[0].trim();
        int acChargeTime = parseInt(parts[1]);
        String connector = parts[2].trim();
        double batteryKwh = parseDouble(parts[3]);
        int dcFastCharge = parseInt(parts[4]);
        double electricRange = parseDouble(parts[5]);
        String fuelType = parts[6].trim();
        double fuelTank = parseDouble(parts[7]);
        double combustionRange = parseDouble(parts[8]);
        double fuelConsumption = parseDouble(parts[9]);

        System.out.println("=== CAMPOS EXTRAÍDOS PELA IA ===");
        System.out.println("Modelo: " + model);
        System.out.println("Tempo AC (min): " + acChargeTime);
        System.out.println("Conector: " + connector);
        System.out.println("Capacidade bateria (kWh): " + batteryKwh);
        System.out.println("Tempo DC rápido (min): " + dcFastCharge);
        System.out.println("Autonomia elétrica (km): " + electricRange);
        System.out.println("Tipo combustível: " + fuelType);
        System.out.println("Tanque (L): " + fuelTank);
        System.out.println("Autonomia combustão (km): " + combustionRange);
        System.out.println("Consumo combustão (km/l): " + fuelConsumption);
        System.out.println("==================================");

        StringBuilder missing = new StringBuilder();
        if (model.isEmpty()) missing.append("Modelo; ");
        if (acChargeTime == 0) missing.append("Tempo AC; ");
        if (connector.isEmpty()) missing.append("Conector; ");
        if (batteryKwh == 0) missing.append("Capacidade bateria; ");
        if (dcFastCharge == 0) missing.append("Tempo DC; ");
        if (electricRange == 0) missing.append("Autonomia elétrica; ");
        if (fuelType.isEmpty()) missing.append("Tipo combustível; ");

        if (missing.length() > 0) {
            System.err.println("ATENÇÃO: A IA não conseguiu extrair os seguintes campos: " + missing);
        }

        boolean isElectric = isElectricVehicle(fuelType, fuelTank, combustionRange);
        double electricConsumption = (electricRange > 0 && batteryKwh > 0) ? batteryKwh / electricRange : 0.15;
        electricConsumption = Math.round(electricConsumption * 10000.0) / 10000.0;

        if (isElectric) {
            return new ElectricVehicle(
                    0,
                    model,
                    electricRange,
                    100.0,
                    connector,
                    dcFastCharge,
                    electricConsumption,
                    acChargeTime
            );
        } else {
            return new HybridVehicle(
                    0,
                    model,
                    electricRange,
                    100.0,
                    electricConsumption,
                    acChargeTime,
                    fuelTank,
                    fuelConsumption,
                    fuelType
            );
        }
    }

    private boolean isElectricVehicle(String fuelType, double fuelTank, double combustionRange) {
        String normalized = Normalizer.normalize(fuelType, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        if (normalized.contains("eletrico")) {
            return true;
        }
        if (fuelTank == 0 && combustionRange == 0) {
            return true;
        }
        return false;
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
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IOException("Chave API não configurada! Defina GEMINI_API_KEY como variável de ambiente.");
        }

        JsonObject requestBody = new JsonObject();
        JsonObject content = new JsonObject();
        content.addProperty("role", "user");

        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        content.add("parts", new com.google.gson.JsonArray());
        content.getAsJsonArray("parts").add(part);

        requestBody.add("contents", new com.google.gson.JsonArray());
        requestBody.getAsJsonArray("contents").add(content);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                requestBody.toString()
        );

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("Erro na API: " + response.code() + " - " + errorBody);
            }

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

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}