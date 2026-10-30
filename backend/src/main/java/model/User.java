package model;
public class User {
    private int id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private String phone;
    private int status;
    private String createTime;
    private String updateTime;
    public User() {}
    public User(int id, String username, String password, String realName, String role, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
        this.phone = phone;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getRoleName() {
        switch (role) {
            case "community_worker": return "社区人员";
            case "system_admin": return "系统管理员";
            case "doctor": return "医生";
            default: return "未知";
        }
    }
}
