package model;
public class VisitRecord {
    private int id;
    private int orderId;
    private int elderlyId;
    private int doctorId;
    private String vitalSigns;
    private Integer bpSystolic;
    private Integer bpDiastolic;
    private Integer heartRate;
    private Double bloodGlucose;
    private String bodyCondition;
    private String dailyLife;
    private String medicationSituation;
    private String subjectiveSymptoms;
    private String aiAdvice;
    private String createTime;
    private String orderNo;
    private String elderlyName;
    private String doctorName;
    public VisitRecord() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getElderlyId() { return elderlyId; }
    public void setElderlyId(int elderlyId) { this.elderlyId = elderlyId; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(String vitalSigns) { this.vitalSigns = vitalSigns; }
    public Integer getBpSystolic() { return bpSystolic; }
    public void setBpSystolic(Integer bpSystolic) { this.bpSystolic = bpSystolic; }
    public Integer getBpDiastolic() { return bpDiastolic; }
    public void setBpDiastolic(Integer bpDiastolic) { this.bpDiastolic = bpDiastolic; }
    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }
    public Double getBloodGlucose() { return bloodGlucose; }
    public void setBloodGlucose(Double bloodGlucose) { this.bloodGlucose = bloodGlucose; }
    public String getBodyCondition() { return bodyCondition; }
    public void setBodyCondition(String bodyCondition) { this.bodyCondition = bodyCondition; }
    public String getDailyLife() { return dailyLife; }
    public void setDailyLife(String dailyLife) { this.dailyLife = dailyLife; }
    public String getMedicationSituation() { return medicationSituation; }
    public void setMedicationSituation(String medicationSituation) { this.medicationSituation = medicationSituation; }
    public String getSubjectiveSymptoms() { return subjectiveSymptoms; }
    public void setSubjectiveSymptoms(String subjectiveSymptoms) { this.subjectiveSymptoms = subjectiveSymptoms; }
    public String getAiAdvice() { return aiAdvice; }
    public void setAiAdvice(String aiAdvice) { this.aiAdvice = aiAdvice; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getElderlyName() { return elderlyName; }
    public void setElderlyName(String elderlyName) { this.elderlyName = elderlyName; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
}
