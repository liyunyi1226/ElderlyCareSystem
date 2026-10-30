package model;
public class Device {
    private int id;
    private String deviceNo;
    private String deviceType;
    private String installTime;
    private String status;
    private int elderlyId;
    private String createTime;
    private String elderlyName;
    public Device() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDeviceNo() { return deviceNo; }
    public void setDeviceNo(String deviceNo) { this.deviceNo = deviceNo; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getInstallTime() { return installTime; }
    public void setInstallTime(String installTime) { this.installTime = installTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getElderlyId() { return elderlyId; }
    public void setElderlyId(int elderlyId) { this.elderlyId = elderlyId; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getElderlyName() { return elderlyName; }
    public void setElderlyName(String elderlyName) { this.elderlyName = elderlyName; }
}
