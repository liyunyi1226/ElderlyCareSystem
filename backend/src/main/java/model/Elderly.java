package model;
import java.util.Map;
public class Elderly {
    private int id;
    private String name;
    private String gender;
    private Integer age;
    private String idCard;
    private String phone;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String chronicDiseases;
    private String allergies;
    private String currentIllness;
    private String medications;
    private String bodyStatus;
    private Integer communityId;
    private String createTime;
    private String updateTime;
    private String communityName;
    private Map<String, Object> healthScore;
    public Elderly() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }
    public String getChronicDiseases() { return chronicDiseases; }
    public void setChronicDiseases(String chronicDiseases) { this.chronicDiseases = chronicDiseases; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getCurrentIllness() { return currentIllness; }
    public void setCurrentIllness(String currentIllness) { this.currentIllness = currentIllness; }
    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }
    public String getBodyStatus() { return bodyStatus; }
    public void setBodyStatus(String bodyStatus) { this.bodyStatus = bodyStatus; }
    public Integer getCommunityId() { return communityId; }
    public void setCommunityId(Integer communityId) { this.communityId = communityId; }
    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public Map<String, Object> getHealthScore() { return healthScore; }
    public void setHealthScore(Map<String, Object> healthScore) { this.healthScore = healthScore; }
}
