package service;

import model.ElectricVehicle;
import model.HybridVehicle;
import model.Vehicle;
import model.City;
import model.ChargingStation;
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

    private double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }

    private int clampInt(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
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

    private String[] parseCsvResponse(String response, int expected) throws IOException {
        String cleaned = response.replaceAll("```[a-zA-Z]*\\s*", "").replaceAll("```", "").trim();
        String[] parts = cleaned.split(";");
        if (parts.length < expected) {
            String[] lines = cleaned.split("\n");
            for (String line : lines) {
                String[] candidate = line.trim().split(";");
                if (candidate.length == expected) {
                    parts = candidate;
                    break;
                }
            }
        }
        if (parts.length != expected) {
            throw new IOException("Resposta inválida (esperado " + expected + " campos): " + response);
        }
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

    @Override
    public Vehicle extractVehicleData(String freeText) throws Exception {
        String prompt = "Extraia os seguintes dados do texto abaixo e responda exatamente no formato CSV com 11 campos separados por ponto e vírgula (;):\n" +
                "1. MODELO_DO_VEICULO (string)\n" +
                "2. Tempo de recarga de 0% a 100% em carregador AC (apenas número em minutos)\n" +
                "3. Tipo do conector (Tipo 2, CCS2 ou CHAdeMO)\n" +
                "4. Capacidade da bateria em kWh (apenas número)\n" +
                "5. Tempo de recarga em carregador DC rápido (apenas número em minutos)\n" +
                "6. Autonomia máxima em quilômetros no modo elétrico (apenas número)\n" +
                "7. Tipo de combustível (Elétrico, Gasolina, Etanol, Flex, Diesel ou Híbrido)\n" +
                "8. Capacidade do tanque de combustível em litros (apenas número ou 0 se não possuir)\n" +
                "9. Autonomia máxima em quilômetros no motor a combustão (apenas número ou 0 se não possuir)\n" +
                "10. Consumo de combustível em km/l no motor a combustão (apenas número ou 0 se não possuir)\n" +
                "11. Porcentagem atual da bateria (apenas número entre 0 e 100)\n\n" +
                "Se algum dado não for mencionado, use 0 ou vazio (string vazia para modelo).\n" +
                "Não inclua unidades, explicações, quebras de linha ou qualquer texto adicional.\n\n" +
                "Texto: \"" + freeText + "\"";

        String response = callGemini(prompt);
        String[] parts = parseCsvResponse(response, 11);

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
        double batteryPercent = parseDouble(parts[10]);

        batteryKwh = clamp(batteryKwh, 0, 200);
        electricRange = clamp(electricRange, 0, 1000);
        fuelTank = clamp(fuelTank, 0, 200);
        combustionRange = clamp(combustionRange, 0, 2000);
        fuelConsumption = clamp(fuelConsumption, 0, 50);
        acChargeTime = clampInt(acChargeTime, 0, 600);
        dcFastCharge = clampInt(dcFastCharge, 0, 120);
        batteryPercent = clamp(batteryPercent, 0, 100);
        if (batteryPercent <= 0) batteryPercent = 100.0;

        boolean isElectric = isElectricVehicle(fuelType, fuelTank, combustionRange);
        double electricConsumption = (electricRange > 0 && batteryKwh > 0) ? batteryKwh / electricRange : 0.15;
        electricConsumption = Math.round(electricConsumption * 10000.0) / 10000.0;
        if (electricConsumption <= 0) electricConsumption = 0.15;

        if (isElectric) {
            return new ElectricVehicle(
                    0,
                    model.isEmpty() ? "Desconhecido" : model,
                    electricRange > 0 ? electricRange : 100.0,
                    batteryPercent,
                    connector.isEmpty() ? "CCS2" : connector,
                    dcFastCharge > 0 ? dcFastCharge : 30,
                    electricConsumption,
                    acChargeTime > 0 ? acChargeTime : 60
            );
        } else {
            return new HybridVehicle(
                    0,
                    model.isEmpty() ? "Desconhecido" : model,
                    electricRange > 0 ? electricRange : 50.0,
                    batteryPercent,
                    electricConsumption,
                    acChargeTime > 0 ? acChargeTime : 60,
                    fuelTank > 0 ? fuelTank : 40.0,
                    fuelConsumption > 0 ? fuelConsumption : 12.0,
                    fuelType.isEmpty() ? "Gasolina" : fuelType,
                    connector.isEmpty() ? "CCS2" : connector,
                    dcFastCharge > 0 ? dcFastCharge : 30
            );
        }
    }

    @Override
    public City extractCityData(String freeText) throws Exception {
        String prompt = "Extraia os seguintes dados do texto abaixo e responda exatamente no formato CSV com 3 campos separados por ponto e vírgula (;):\n" +
                "1. Nome da cidade (string)\n" +
                "2. Estado (string, sigla ou nome)\n" +
                "3. Distância da capital em quilômetros (apenas número)\n\n" +
                "Se algum dado não for mencionado, use vazio ou 0.\n" +
                "Não inclua unidades, explicações, quebras de linha ou qualquer texto adicional.\n\n" +
                "Texto: \"" + freeText + "\"";

        String response = callGemini(prompt);
        String[] parts = parseCsvResponse(response, 3);
        String name = parts[0].trim();
        String state = parts[1].trim();
        double distance = parseDouble(parts[2]);
        distance = clamp(distance, 0, 10000);
        if (name.isEmpty()) name = "Cidade desconhecida";
        if (state.isEmpty()) state = "Estado desconhecido";
        return new City(0, name, state, distance);
    }

    @Override
    public ChargingStation extractStationData(String freeText) throws Exception {
        String prompt = "Extraia os seguintes dados do texto abaixo e responda exatamente no formato CSV com 7 campos separados por ponto e vírgula (;):\n" +
                "1. Nome da estação (string)\n" +
                "2. Localização (string, endereço ou referência)\n" +
                "3. Cidade onde se encontra (string, nome da cidade)\n" +
                "4. Conectores disponíveis (string, ex: CCS2, Type2, CHAdeMO)\n" +
                "5. Potência de carga em kW (apenas número)\n" +
                "6. Preço por kWh em reais (apenas número)\n" +
                "7. Número de vagas disponíveis (apenas número)\n\n" +
                "Se algum dado não for mencionado, use vazio ou 0.\n" +
                "Não inclua unidades, explicações, quebras de linha ou qualquer texto adicional.\n\n" +
                "Texto: \"" + freeText + "\"";

        String response = callGemini(prompt);
        String[] parts = parseCsvResponse(response, 7);
        String name = parts[0].trim();
        String location = parts[1].trim();
        String cityName = parts[2].trim();
        String connectors = parts[3].trim();
        double power = parseDouble(parts[4]);
        double price = parseDouble(parts[5]);
        int slots = parseInt(parts[6]);
        power = clamp(power, 0, 1000);
        price = clamp(price, 0, 100);
        slots = clampInt(slots, 0, 100);
        if (name.isEmpty()) name = "Estação desconhecida";
        if (location.isEmpty()) location = "Localização não informada";
        if (connectors.isEmpty()) connectors = "CCS2";
        return new ChargingStation(0, name, location, 0, connectors, power, price, slots);
    }

    @Override
    public String planRoute(Vehicle vehicle, City destination) {
        String prompt = "Planeje uma rota para o veículo " + vehicle.getModel() +
                " com bateria atual de " + vehicle.getCurrentBatteryCharge() + "%" +
                ", autonomia máxima de " + vehicle.getMaximumRange() + " km" +
                ", consumo de " + vehicle.getKwhConsumptionPerKm() + " kWh/km" +
                ", tempo de recarga completa de " + vehicle.getFullRechargeTime() + " minutos." +
                " Destino: " + destination.getName() + " a " + destination.getDistanceFromCapital() + " km." +
                " Considere clima, trânsito e dicas de recarga. Dê uma resposta descritiva em português.";

        try {
            return callGemini(prompt);
        } catch (Exception e) {
            return "Erro ao obter planejamento: " + e.getMessage();
        }
    }

    private boolean isElectricVehicle(String fuelType, double fuelTank, double combustionRange) {
        String normalized = Normalizer.normalize(fuelType, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        if (normalized.contains("eletrico") || normalized.contains("elétrico")) {
            return true;
        }
        return fuelTank == 0 && combustionRange == 0;
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