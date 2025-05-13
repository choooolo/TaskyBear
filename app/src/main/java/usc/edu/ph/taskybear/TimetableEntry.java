package usc.edu.ph.taskybear;

public class TimetableEntry {
    private int id;
    private String className;
    private String type;
    private String day;
    private String startTime;
    private String endTime;
    private String location;

    public TimetableEntry(String className, String type, String day,
                          String startTime, String endTime, String location) {
        this.className = className;
        this.type = type;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}