package service;
import model.Elderly;
import model.VisitRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class HealthScoreService {
    public static Map<String, Object> compute(Elderly e, VisitRecord v) {
        int ageScore = ageScore(e.getAge());
        int chronicScore = chronicScore(e.getChronicDiseases());
        boolean hasVisit = (v != null);
        int bpScore = 0, hrScore = 0, glucoseScore = 0;
        String bpText, hrText, glucoseText;
        if (hasVisit) {
            bpScore = bpScore(v.getBpSystolic(), v.getBpDiastolic());
            bpText = bpText(v.getBpSystolic(), v.getBpDiastolic());
            hrScore = hrScore(v.getHeartRate());
            hrText = hrText(v.getHeartRate());
            glucoseScore = glucoseScore(v.getBloodGlucose());
            glucoseText = glucoseText(v.getBloodGlucose());
        } else {
            bpText = "暂无巡访记录";
            hrText = "暂无巡访记录";
            glucoseText = "暂无巡访记录";
        }
        int total = ageScore + chronicScore + bpScore + hrScore + glucoseScore;
        String level = levelOf(total);
        String levelText = levelTextOf(level);
        List<Map<String, Object>> details = new ArrayList<>();
        details.add(detail("年龄", (e.getAge() == null ? "未知" : e.getAge() + " 岁"), ageScore, 10));
        String chronicBasis = (e.getChronicDiseases() == null || e.getChronicDiseases().trim().isEmpty()
                || "无".equals(e.getChronicDiseases().trim())) ? "无慢性病" : e.getChronicDiseases();
        details.add(detail("既往病史（慢性病）", chronicBasis, chronicScore, 20));
        details.add(detail("血压", bpText, bpScore, 20));
        details.add(detail("心率", hrText, hrScore, 10));
        details.add(detail("血糖", glucoseText, glucoseScore, 10));
        Map<String, Object> result = new HashMap<>();
        result.put("hasVisit", hasVisit);
        result.put("total", hasVisit ? total : null);
        result.put("level", hasVisit ? level : null);
        result.put("levelText", hasVisit ? levelText : "暂无巡访记录");
        result.put("details", details);
        result.put("formula",
                "健康评分 = 年龄分(0~10) + 慢性病分(0~20) + 血压分(0~20) + 心率分(0~10) + 血糖分(0~10)，满分 100 分");
        result.put("basis", "依据老人档案与最近一次巡访记录的体征数据自动计算");
        return result;
    }
    private static int ageScore(Integer age) {
        if (age == null) return 5;
        if (age < 70) return 10;
        if (age < 75) return 9;
        if (age < 80) return 8;
        if (age < 85) return 7;
        return 5;
    }
    private static int chronicScore(String chronic) {
        int count = 0;
        if (chronic != null && !chronic.trim().isEmpty() && !"无".equals(chronic.trim())) {
            count = chronic.split("[,，、;；]").length;
        }
        return Math.max(0, 20 - 5 * count);
    }
    private static int bpScore(Integer sys, Integer dia) {
        if (sys == null || dia == null) return 0;
        if (sys < 90 || dia < 60) return 8;
        if (sys < 120 && dia < 80) return 20;
        if (sys < 140 && dia < 90) return 16;
        if (sys < 160 && dia < 100) return 10;
        if (sys < 180 && dia < 110) return 5;
        return 0;
    }
    private static String bpText(Integer sys, Integer dia) {
        if (sys == null || dia == null) return "暂无数据";
        if (sys < 90 || dia < 60) return sys + "/" + dia + " mmHg（偏低）";
        if (sys < 120 && dia < 80) return sys + "/" + dia + " mmHg（正常）";
        if (sys < 140 && dia < 90) return sys + "/" + dia + " mmHg（正常高值）";
        if (sys < 160 && dia < 100) return sys + "/" + dia + " mmHg（高血压1级）";
        if (sys < 180 && dia < 110) return sys + "/" + dia + " mmHg（高血压2级）";
        return sys + "/" + dia + " mmHg（高血压3级）";
    }
    private static int hrScore(Integer hr) {
        if (hr == null) return 0;
        if (hr >= 60 && hr <= 100) return 10;
        if ((hr >= 50 && hr < 60) || (hr > 100 && hr <= 110)) return 6;
        return 2;
    }
    private static String hrText(Integer hr) {
        if (hr == null) return "暂无数据";
        if (hr >= 60 && hr <= 100) return hr + " 次/分（正常）";
        if (hr >= 50 && hr < 60) return hr + " 次/分（偏慢）";
        if (hr > 100 && hr <= 110) return hr + " 次/分（偏快）";
        return hr + " 次/分（异常）";
    }
    private static int glucoseScore(Double g) {
        if (g == null) return 0;
        if (g >= 3.9 && g <= 6.1) return 10;
        if (g > 6.1 && g <= 7.0) return 6;
        if (g > 7.0) return 3;
        return 5;
    }
    private static String glucoseText(Double g) {
        if (g == null) return "暂无数据";
        if (g >= 3.9 && g <= 6.1) return g + " mmol/L（正常）";
        if (g > 6.1 && g <= 7.0) return g + " mmol/L（偏高）";
        if (g > 7.0) return g + " mmol/L（偏高明显）";
        return g + " mmol/L（偏低）";
    }
    private static String levelOf(int total) {
        if (total >= 85) return "excellent";
        if (total >= 70) return "good";
        if (total >= 55) return "fair";
        if (total >= 40) return "poor";
        return "danger";
    }
    private static String levelTextOf(String level) {
        switch (level) {
            case "excellent": return "优";
            case "good": return "良好";
            case "fair": return "一般";
            case "poor": return "较差";
            default: return "需关注";
        }
    }
    private static Map<String, Object> detail(String label, String basis, int score, int max) {
        Map<String, Object> m = new HashMap<>();
        m.put("label", label);
        m.put("basis", basis);
        m.put("score", score);
        m.put("max", max);
        return m;
    }
}
