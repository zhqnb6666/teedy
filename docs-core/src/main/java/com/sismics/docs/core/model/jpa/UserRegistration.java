package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * User registration request entity.
 * 
 * @author claude
 */
@Entity
@Table(name = "T_USER_REGISTRATION")
public class UserRegistration implements Loggable {
    /**
     * Registration request ID.
     */
    @Id
    @Column(name = "URG_ID_C", length = 36)
    private String id;
    
    /**
     * User's username.
     */
    @Column(name = "URG_USERNAME_C", nullable = false, length = 50)
    private String username;
    
    /**
     * User's password (hashed).
     */
    @Column(name = "URG_PASSWORD_C", nullable = false, length = 100)
    private String password;

    /**
     * Email address.
     */
    @Column(name = "URG_EMAIL_C", nullable = false, length = 100)
    private String email;
    
    /**
     * Status: PENDING, APPROVED, REJECTED
     */
    @Column(name = "URG_STATUS_C", nullable = false, length = 10)
    private String status;
    
    /**
     * Creation date.
     */
    @Column(name = "URG_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Approval/Rejection date.
     */
    @Column(name = "URG_PROCESSDATE_D")
    private Date processDate;
    
    /**
     * Delete date.
     */
    @Column(name = "URG_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public UserRegistration setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserRegistration setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserRegistration setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRegistration setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserRegistration setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public UserRegistration setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public UserRegistration setProcessDate(Date processDate) {
        this.processDate = processDate;
        return this;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public UserRegistration setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("email", email)
                .toString();
    }

    @Override
    public String toMessage() {
        return username;
    }
} 