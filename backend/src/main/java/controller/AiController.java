package controller;

import com.elderlycare.common.ApiResult;
import com.elderlycare.common.BusinessException;
import dao.ElderlyDAO;
import dao.VisitRecordDAO;
import model.Elderly;
import model.VisitRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private service.AiDiagnosisService aiDiagnosisService;

    @PostMapping("/diagnosis")
    public Map<String, Object> aiDiagnosis(@RequestParam Map<String, String> p) {
        try {
            String vitalSigns = p.get("vitalSigns");
            String bodyCondition = p.get("bodyCondition");
            String subjectiveSymptoms = p.get("subjectiveSymptoms");
            Integer bpSystolic = parseIntOrNull(p.get("bpSystolic"));
            Integer bpDiastolic = parseIntOrNull(p.get("bpDiastolic"));
            Integer heartRate = parseIntOrNull(p.get("heartRate"));
            Double bloodGlucose = parseDoubleOrNull(p.get("bloodGlucose"));

            Elderly elderly = null;
            VisitRecord latest = null;
            Integer elderlyId = parseIntOrNull(p.get("elderlyId"));
            if (elderlyId != null) {
                elderly = ElderlyDAO.findById(elderlyId);
                latest = VisitRecordDAO.findLatestByElderlyId(elderlyId);
            }

            Map<String, Object> aiResult = aiDiagnosisService.diagnose(elderly, latest,
                    bpSystolic, bpDiastolic, heartRate, bloodGlucose, bodyCondition, subjectiveSymptoms);
            if (aiResult != null) {
                return ApiResult.success(aiResult);
            }

            String diagnosis = generateDiagnosis(vitalSigns, bodyCondition, subjectiveSymptoms,
                    bpSystolic, bpDiastolic, heartRate, bloodGlucose);
            String advice = generateAdvice(vitalSigns, bodyCondition, subjectiveSymptoms,
                    bpSystolic, bpDiastolic, heartRate, bloodGlucose);
            Map<String, Object> result = new HashMap<>();
            result.put("diagnosis", diagnosis);
            result.put("advice", advice);
            result.put("confidence", 0.6);
            result.put("riskLevel", assessRisk(bpSystolic, bpDiastolic, heartRate, bloodGlucose));
            result.put("source", "rule");
            return ApiResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("AI诊断服务暂时不可用");
        }
    }

    private String assessRisk(Integer bpSystolic, Integer bpDiastolic, Integer heartRate, Double bloodGlucose) {
        if ((bpSystolic != null && bpSystolic >= 180) || (bpDiastolic != null && bpDiastolic >= 110)
                || (heartRate != null && (heartRate > 120 || heartRate < 50))
                || (bloodGlucose != null && (bloodGlucose > 13.9 || bloodGlucose < 3.0))) {
            return "危急";
        }
        if ((bpSystolic != null && bpSystolic >= 160) || (bpDiastolic != null && bpDiastolic >= 100)
                || (heartRate != null && heartRate > 110) || (bloodGlucose != null && bloodGlucose > 11.1)) {
            return "高";
        }
        if ((bpSystolic != null && bpSystolic >= 140) || (bpDiastolic != null && bpDiastolic >= 90)
                || (heartRate != null && heartRate > 100) || (bloodGlucose != null && bloodGlucose > 7.0)) {
            return "中";
        }
        return "低";
    }

    private String generateDiagnosis(String vitalSigns, String bodyCondition, String symptoms,
                                     Integer bpSystolic, Integer bpDiastolic, Integer heartRate, Double bloodGlucose) {
        StringBuilder sb = new StringBuilder();
        if (bpSystolic != null && bpDiastolic != null) {
            if (bpSystolic >= 140 || bpDiastolic >= 90) {
                sb.append("血压偏高，");
            } else if (bpSystolic < 90 || bpDiastolic < 60) {
                sb.append("血压偏低，");
            }
        }
        if (heartRate != null) {
            if (heartRate > 100) {
                sb.append("心率过快，");
            } else if (heartRate < 60) {
                sb.append("心率偏慢，");
            }
        }
        if (bloodGlucose != null) {
            if (bloodGlucose > 7.0) {
                sb.append("血糖偏高，");
            } else if (bloodGlucose < 3.9) {
                sb.append("血糖偏低，");
            }
        }
        if (vitalSigns != null) {
            if (vitalSigns.contains("高血压") || vitalSigns.contains("血压") && vitalSigns.contains("高")) {
                sb.append("血压偏高，");
            }
            if (vitalSigns.contains("心率") && (vitalSigns.contains("快") || vitalSigns.contains("高"))) {
                sb.append("心率异常，");
            }
            if (vitalSigns.contains("血糖") && vitalSigns.contains("高")) {
                sb.append("血糖偏高，");
            }
        }
        if (symptoms != null) {
            if (symptoms.contains("头晕") || symptoms.contains("头痛")) {
                sb.append("伴有头部不适，");
            }
            if (symptoms.contains("胸闷") || symptoms.contains("心慌")) {
                sb.append("伴有心脏不适，");
            }
            if (symptoms.contains("咳嗽") || symptoms.contains("气短")) {
                sb.append("伴有呼吸道症状，");
            }
        }
        if (sb.length() == 0) {
            return "根据目前数据，老人身体状况基本稳定，建议定期复查。";
        }
        return "初步分析：" + sb.toString() + "建议进一步检查确认。";
    }

    private String generateAdvice(String vitalSigns, String bodyCondition, String symptoms,
                                  Integer bpSystolic, Integer bpDiastolic, Integer heartRate, Double bloodGlucose) {
        StringBuilder sb = new StringBuilder();
        sb.append("【AI辅助建议】\n");
        int adviceNum = 1;
        if (bpSystolic != null && bpDiastolic != null && (bpSystolic >= 140 || bpDiastolic >= 90)) {
            sb.append(adviceNum++).append(". 血压偏高：建议低盐低脂饮食，规律服用降压药物，每日早晚各测一次血压；\n");
        }
        if (bpSystolic != null && bpDiastolic != null && (bpSystolic < 90 || bpDiastolic < 60)) {
            sb.append(adviceNum++).append(". 血压偏低：建议规律三餐、适当增加优质蛋白摄入，起身动作放慢，如反复头晕需就医；\n");
        }
        if (heartRate != null && (heartRate > 100 || heartRate < 60)) {
            sb.append(adviceNum++).append(". 心率异常：建议静息休息后复测，避免剧烈活动与情绪激动，必要时进行心电图检查；\n");
        }
        if (bloodGlucose != null && bloodGlucose > 7.0) {
            sb.append(adviceNum++).append(". 血糖偏高：建议控制碳水化合物摄入，规律用餐，按医嘱服用降糖药并定期复查血糖；\n");
        }
        if (bloodGlucose != null && bloodGlucose < 3.9) {
            sb.append(adviceNum++).append(". 血糖偏低：建议及时补充糖分，规律进食，注意观察有无冷汗、心慌等低血糖症状；\n");
        }
        if (vitalSigns != null && (vitalSigns.contains("高血压") || vitalSigns.contains("血压"))) {
            sb.append(adviceNum++).append(". 建议低盐低脂饮食，规律服用降压药物，每日监测血压；\n");
        }
        if (vitalSigns != null && vitalSigns.contains("血糖")) {
            sb.append(adviceNum++).append(". 建议控制碳水摄入，规律用餐，定期监测血糖；\n");
        }
        if (symptoms != null && (symptoms.contains("头晕") || symptoms.contains("头痛"))) {
            sb.append(adviceNum++).append(". 建议安静休息，避免剧烈活动，如持续不缓解需及时就医；\n");
        }
        if (symptoms != null && (symptoms.contains("胸闷") || symptoms.contains("心慌"))) {
            sb.append(adviceNum++).append(". 建议避免劳累，保持情绪稳定，必要时进行心电图检查；\n");
        }
        if (symptoms != null && (symptoms.contains("咳嗽") || symptoms.contains("气短"))) {
            sb.append(adviceNum++).append(". 建议保持室内空气流通，适量饮水，如持续不缓解需拍胸片检查；\n");
        }
        if (adviceNum == 1) {
            sb.append(adviceNum).append(". 老人整体状态良好，建议继续保持现有生活规律，定期巡访；\n");
            sb.append(adviceNum + 1).append(". 注意观察身体变化，如有不适及时就医。");
        } else {
            sb.append(adviceNum++).append(". 建议定期复查，关注身体指标变化；\n");
            sb.append(adviceNum).append(". 如症状持续加重，请及时到医院就诊。");
        }
        return sb.toString();
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return null; }
    }
}
