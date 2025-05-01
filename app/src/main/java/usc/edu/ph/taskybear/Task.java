package usc.edu.ph.taskybear;

public class Task {
    private int id;
    private String title;
    private String details;
    private String date;
    private String resource;
    private String category;

    // Constructor
    public Task(String title, String details, String date, String resource, String category) {
        this.title = title;
        this.details = details;
        this.date = date;
        this.resource = resource;
        this.category = category;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
