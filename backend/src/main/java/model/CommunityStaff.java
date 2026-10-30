package model;
public class CommunityStaff {
    private int id;
    private int communityId;
    private int userId;
    private String startDate;
    private String position;
    private String createTime;
    private String communityName;
    private String userName;
    private String realName;
    private String role;
    public CommunityStaff() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCommunityId() { return communityId; }
    public void setCommunityId(int communityId) { this.communityId = communityId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
