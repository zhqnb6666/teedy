package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.constant.RegistrationStatus;
import com.sismics.docs.core.dao.dto.UserRegistrationDto;
import com.sismics.docs.core.model.jpa.UserRegistration;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User registration DAO.
 * 
 * @author claude
 */
public class UserRegistrationDao {
    /**
     * Creates a new user registration request.
     * 
     * @param userRegistration User registration to create
     * @return New ID
     * @throws Exception e
     */
    public String create(UserRegistration userRegistration) throws Exception {
        // Create the user registration UUID
        userRegistration.setId(UUID.randomUUID().toString());
        
        // Checks for username unicity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
        q.setParameter("username", userRegistration.getUsername());
        List<?> l = q.getResultList();
        if (l.size() > 0) {
            throw new Exception("AlreadyExistingUsername");
        }
        
        // Check for pending registration with same username
        q = em.createQuery("select r from UserRegistration r where r.username = :username and r.status = :status and r.deleteDate is null");
        q.setParameter("username", userRegistration.getUsername());
        q.setParameter("status", RegistrationStatus.PENDING.name());
        l = q.getResultList();
        if (l.size() > 0) {
            throw new Exception("AlreadyExistingUsername");
        }
        
        // Set creation date and status
        userRegistration.setCreateDate(new Date());
        userRegistration.setStatus(RegistrationStatus.PENDING.name());
        
        // Create the registration
        em.persist(userRegistration);
        
        return userRegistration.getId();
    }
    
    /**
     * Updates a registration request status.
     * 
     * @param userRegistration Registration request to update
     * @param userId User ID making the change
     * @return Updated registration
     */
    public UserRegistration update(UserRegistration userRegistration, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the registration
        Query q = em.createQuery("select r from UserRegistration r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", userRegistration.getId());
        UserRegistration registrationDb = (UserRegistration) q.getSingleResult();

        // Update the registration
        registrationDb.setStatus(userRegistration.getStatus());
        registrationDb.setProcessDate(new Date());
        
        // Create audit log
        AuditLogUtil.create(registrationDb, AuditLogType.UPDATE, userId);
        
        return registrationDb;
    }
    
    /**
     * Gets a registration request by its ID.
     * 
     * @param id Registration request ID
     * @return Registration request
     */
    public UserRegistration getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from UserRegistration r where r.id = :id and r.deleteDate is null");
            q.setParameter("id", id);
            return (UserRegistration) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets a registration request by username.
     * 
     * @param username Username
     * @return Registration request
     */
    public UserRegistration getByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from UserRegistration r where r.username = :username and r.status = :status and r.deleteDate is null");
            q.setParameter("username", username);
            q.setParameter("status", RegistrationStatus.PENDING.name());
            return (UserRegistration) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Deletes a registration request.
     * 
     * @param id Registration request ID
     * @param userId User ID making the deletion
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the registration
        Query q = em.createQuery("select r from UserRegistration r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", id);
        UserRegistration registrationDb = (UserRegistration) q.getSingleResult();
        
        // Delete the registration
        Date dateNow = new Date();
        registrationDb.setDeleteDate(dateNow);
        
        // Create audit log
        AuditLogUtil.create(registrationDb, AuditLogType.DELETE, userId);
    }
    
    /**
     * Returns all active registration requests.
     * 
     * @return List of registration DTOs
     */
    public List<UserRegistrationDto> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<UserRegistration> q = em.createQuery("select r from UserRegistration r where r.status = :status and r.deleteDate is null order by r.createDate desc", UserRegistration.class);
        q.setParameter("status", RegistrationStatus.PENDING.name());
        List<UserRegistration> registrationList = q.getResultList();
        
        // Assemble results
        List<UserRegistrationDto> registrationDtoList = new ArrayList<>();
        for (UserRegistration registration : registrationList) {
            UserRegistrationDto dto = new UserRegistrationDto();
            dto.setId(registration.getId());
            dto.setUsername(registration.getUsername());
            dto.setEmail(registration.getEmail());
            dto.setCreateTimestamp(registration.getCreateDate().getTime());
            registrationDtoList.add(dto);
        }
        
        return registrationDtoList;
    }
} 