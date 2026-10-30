package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import model.Elderly;
import model.VisitRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiDiagnosisService {

    private static final Logger log = LoggerFactory.getLogger(AiDiagnosisService.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private static final String SYSTEM_PROMPT =
            "你是“社区老年人健康监护系统”的辅助诊断助手，服务对象是社区医生。"
            + "请依据老人档案、本次体征和主诉给出初步分析与健康建议。要求：\n"
            + "1. 你只是辅助工具，不做确定性诊断，结论中应提醒医生结合面诊；\n"
            + "2. 出现危急情况（收缩压≥180或<90、舒张压≥110或<60、心率>120或<50、"
            + "血糖>13.9或<3.0，或胸痛、意识障碍、呼吸困难）时，必须在诊断中明确提示立即就医或呼叫急救；\n"
            + "3. 建议分条列出，覆盖饮食、用药、监测和复诊，语言通俗易懂；\n"
            + "4. 只输出 JSON，不要输出 markdown 代码块或任何额外文字，格式为：\n"
            + "{\"diagnosis\":\"初步分析\",\"advice\":\"1. ...\\n2. ...\","
            + "\"confidence\":0.0到1.0的小数,\"riskLevel\":\"低|中|高|危急\"}";

    @Value("${ai.enabled:true}")
    private boolean enabled;

    @Value("${ai.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String baseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:glm-4-flash}")
    private String model;

    @Value("${ai.timeout-seconds:20}")
    private int timeoutSeconds;

    @Value("${ai.temperature:0.3}")
    private double temperature;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    @PostConstruct
    public void init() {
        if (isConfigured()) {
            log.info("AI 辅助诊断已启用，模型：{}，接口：{}", model, baseUrl);
        } else {
            log.info("AI 辅助诊断未配置 api-key，将使用规则引擎兜底");
        }
    }

    public Map<String, Object> diagnose(Elderly elderly, VisitRecord latest,
                                        Integer bpSystolic, Integer bpDiastolic,
                                        Integer heartRate, Double bloodGlucose,
                                        String bodyCondition, String subjectiveSymptoms) {
        if (!isConfigured()) {
            return null;
        }
        try {
            String content = callModel(buildUserPrompt(elderly, latest, bpSystolic, bpDiastolic,
                    heartRate, bloodGlucose, bodyCondition, subjectiveSymptoms));
            Map<String, Object> parsed = parseModelOutput(content);
            if (parsed == null) {
                return null;
            }
            parsed.put("source", "ai");
            parsed.put("model", model);
            return parsed;
        } catch (Exception e) {
            log.warn("AI 诊断调用失败，回退到规则引擎：{}", e.getMessage());
            return null;
        }
    }

    private String callModel(String userPrompt) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", temperature);
        body.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt)
        ));

        String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        JSON.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IllegalStateException("模型接口返回 HTTP " + response.statusCode()
                    + "：" + abbreviate(response.body(), 200));
        }
        JsonNode root = JSON.readTree(response.body());
        return root.path("choices").path(0).path("message").path("content").asText("");
    }

    private String buildUserPrompt(Elderly elderly, VisitRecord latest,
                                   Integer bpSystolic, Integer bpDiastolic,
                                   Integer heartRate, Double bloodGlucose,
                                   String bodyCondition, String subjectiveSymptoms) {
        StringBuilder sb = new StringBuilder("请分析以下老人情况：\n");
        if (elderly != null) {
            sb.append("【老人档案】\n");
            sb.append("姓名：").append(text(elderly.getName())).append("\n");
            sb.append("性别：").append(text(elderly.getGender())).append("\n");
            sb.append("年龄：").append(elderly.getAge() == null ? "未提供" : elderly.getAge() + " 岁").append("\n");
            sb.append("既往病史：").append(text(elderly.getChronicDiseases())).append("\n");
            sb.append("过敏史：").append(text(elderly.getAllergies())).append("\n");
            sb.append("现病史：").append(text(elderly.getCurrentIllness())).append("\n");
            sb.append("用药情况：").append(text(elderly.getMedications())).append("\n");
            sb.append("身体现状：").append(text(elderly.getBodyStatus())).append("\n");
        }
        sb.append("【本次体征】\n");
        sb.append("血压：").append(vital(bpSystolic, bpDiastolic)).append("\n");
        sb.append("心率：").append(heartRate == null ? "未提供" : heartRate + " 次/分").append("\n");
        sb.append("血糖：").append(bloodGlucose == null ? "未提供" : bloodGlucose + " mmol/L").append("\n");
        sb.append("身体状态：").append(text(bodyCondition)).append("\n");
        sb.append("主观症状：").append(text(subjectiveSymptoms)).append("\n");
        if (latest != null) {
            sb.append("【最近一次巡访体征（仅作参考）】\n");
            sb.append("血压：").append(vital(latest.getBpSystolic(), latest.getBpDiastolic())).append("\n");
            sb.append("心率：").append(latest.getHeartRate() == null ? "未提供" : latest.getHeartRate() + " 次/分").append("\n");
            sb.append("血糖：").append(latest.getBloodGlucose() == null ? "未提供" : latest.getBloodGlucose() + " mmol/L").append("\n");
        }
        sb.append("请按约定 JSON 格式输出结果。");
        return sb.toString();
    }

    private Map<String, Object> parseModelOutput(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String json = content.trim();
        if (json.startsWith("```")) {
            int firstNewline = json.indexOf('\n');
            if (firstNewline > 0) json = json.substring(firstNewline + 1);
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
        }
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        json = json.substring(start, end + 1);
        try {
            JsonNode node = JSON.readTree(json);
            String diagnosis = node.path("diagnosis").asText("");
            String advice = node.path("advice").asText("");
            if (diagnosis.isBlank() || advice.isBlank()) {
                return null;
            }
            double confidence = node.path("confidence").asDouble(0.8);
            if (confidence > 1) confidence = confidence / 100.0;
            confidence = Math.max(0.1, Math.min(1.0, confidence));
            String riskLevel = node.path("riskLevel").asText("中");
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("diagnosis", diagnosis);
            result.put("advice", advice);
            result.put("confidence", confidence);
            result.put("riskLevel", riskLevel);
            return result;
        } catch (Exception e) {
            log.warn("无法解析模型返回内容：{}", abbreviate(content, 200));
            return null;
        }
    }

    private String text(String value) {
        return (value == null || value.isBlank()) ? "未提供" : value;
    }

    private String vital(Integer systolic, Integer diastolic) {
        if (systolic == null && diastolic == null) return "未提供";
        return (systolic == null ? "-" : systolic) + "/" + (diastolic == null ? "-" : diastolic) + " mmHg";
    }

    private String abbreviate(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }
}
