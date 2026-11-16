package ua.edu.nupp.kpp.client;

public class ApprovalRequest {
    private Long id;
    private long documentId;
    private long reviewerId;
    private ApprovalState state;
    private double f1;
    private double f2;
    private double f3;
    private double priorityIndex;
    private String createdAt;

    public ApprovalRequest() {}

    public ApprovalRequest(Long id, long documentId, long reviewerId, ApprovalState state,
                           double f1, double f2, double f3, double priorityIndex, String createdAt) {
        this.id = id;
        this.documentId = documentId;
        this.reviewerId = reviewerId;
        this.state = state;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.priorityIndex = priorityIndex;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getDocumentId() { return documentId; }
    public void setDocumentId(long documentId) { this.documentId = documentId; }
    public long getReviewerId() { return reviewerId; }
    public void setReviewerId(long reviewerId) { this.reviewerId = reviewerId; }
    public ApprovalState getState() { return state; }
    public void setState(ApprovalState state) { this.state = state; }
    public double getF1() { return f1; }
    public void setF1(double f1) { this.f1 = f1; }
    public double getF2() { return f2; }
    public void setF2(double f2) { this.f2 = f2; }
    public double getF3() { return f3; }
    public void setF3(double f3) { this.f3 = f3; }
    public double getPriorityIndex() { return priorityIndex; }
    public void setPriorityIndex(double priorityIndex) { this.priorityIndex = priorityIndex; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
