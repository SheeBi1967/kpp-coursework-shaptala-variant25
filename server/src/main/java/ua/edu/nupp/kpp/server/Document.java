package ua.edu.nupp.kpp.server;

public class Document {
    private Long id;
    private long authorId;
    private String regNumber;
    private String title;
    private String docType;
    private String dueDate;
    private int criticality;
    private int dependentProcesses;

    public Document() {}

    public Document(Long id, long authorId, String regNumber, String title, String docType,
                    String dueDate, int criticality, int dependentProcesses) {
        this.id = id;
        this.authorId = authorId;
        this.regNumber = regNumber;
        this.title = title;
        this.docType = docType;
        this.dueDate = dueDate;
        this.criticality = criticality;
        this.dependentProcesses = dependentProcesses;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getAuthorId() { return authorId; }
    public void setAuthorId(long authorId) { this.authorId = authorId; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public int getCriticality() { return criticality; }
    public void setCriticality(int criticality) { this.criticality = criticality; }
    public int getDependentProcesses() { return dependentProcesses; }
    public void setDependentProcesses(int dependentProcesses) { this.dependentProcesses = dependentProcesses; }
}
