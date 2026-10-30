package model;
public class FollowUpPlan {
    private int id;
    private int elderlyId;
    private int communityId;
    private int creatorId;
    private String planType;
    private Integer cycleDays;
    private String startDate;
    private String nextVisitDate;
    private String status;
    private String createTime;
    private String elderlyName;
    private String communityName;
    private String creatorName;
    public FollowUpPlan() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getElderlyId() { return elderlyId; }
    public void setElderlyId(int elderlyId) { this.elderlyId = elderlyId; }
    public int getCommunityId() { return communityId; }
    public void setCommunityId(int communityId) { this.communityId = communityId; }
    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public Integer getCycleDays() { return cycleDays; }
    public void setCycleDays(Integer cycleDays) { this.cycleDays = cycleDays; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getNextVisitDate() { return nextVisitDate; }
    public void setNextVisitDate(String nextVisitDate) { this.nextVisitDate = nextVisitDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getElderlyName() { return elderlyName; }
    public void setElderlyName(String elderlyName) { this.elderlyName = elderlyName; }
    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
}
