package com.example.gk09;

import java.io.Serializable;
import java.util.Date;

public class Certificate implements Serializable {

    private String id;
    private String name;
    private String description;
    private String issuedBy;
    private Date issueDate;
    private Date expiryDate;

    public Certificate() {}

    public Certificate(String id, String name, String description, String issuedBy, Date issueDate, Date expiryDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.issuedBy = issuedBy;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }

    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

}
