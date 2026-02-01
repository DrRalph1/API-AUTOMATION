package com.usg.apiAutomation.repositories;

import com.usg.apiAutomation.dtos.user.AppUserDTO;
import com.usg.apiAutomation.entities.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUserEntity, String>,
        JpaSpecificationExecutor<AppUserEntity> {

    // ✅ Already used elsewhere
    Optional<AppUserEntity> findByUsername(String username);

    Optional<AppUserEntity> findByEmailAddress(String emailAddress);

    // ✅ Used in AccountLockService
    Optional<AppUserEntity> findByUserIdIgnoreCase(String userId);

    @Query("SELECT u.username FROM AppUserEntity u WHERE u.userId = :userId")
    String getUsernameByUserId(@Param("userId") String userId);

    @Query("SELECT u.emailAddress FROM AppUserEntity u WHERE u.userId = :userId")
    String getEmailAddressByUserId(@Param("userId") String userId);

    // Query to update the password for an existing user by userId
    @Modifying
    @Query("UPDATE AppUserEntity u SET " +
            "u.password = COALESCE(:new_password, u.password), " +
            "u.isDefaultPassword = true, u.lastModifiedDate = :updatedAt " +
            "WHERE u.userId = :userId " +
            "AND u.password = :old_password")
    void updatePassword(@Param("userId") String userId,
                        @Param("old_password") String old_password,
                        @Param("new_password") String new_password,
                        @Param("updatedAt") LocalDateTime updatedAt);


    @Modifying
    @Query("UPDATE AppUserEntity u SET u.password = :newPassword, " +
            "u.isDefaultPassword = false, " +
            "u.lastModifiedDate = :updatedAt " +
            "WHERE u.userId = :userId")
    void updatePasswordAndResetDefaultFlag(
            @Param("userId") String userId,
            @Param("newPassword") String newPassword,
            @Param("updatedAt") LocalDateTime updatedAt);


    @Query("SELECT u.phoneNumber FROM AppUserEntity u WHERE u.userId = :userId")
    String getPhoneNumberByUserId(@Param("userId") String userId);

    @Query("SELECT u.role.roleName FROM AppUserEntity u WHERE u.userId = :userId")
    String getUserRoleByUserId(@Param("userId") String userId);

    @Query("SELECT u FROM AppUserEntity u JOIN FETCH u.role WHERE LOWER(u.userId) = LOWER(:userId)")
    Optional<AppUserEntity> findByUserIdWithRole(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("""
    UPDATE AppUserEntity u 
    SET u.lastLogin = :lastLogin, 
        u.lastModifiedDate = :lastModifiedDate,
        u.failedLoginAttempts = 0
    WHERE LOWER(u.userId) = LOWER(:userId)
""")
    void updateLastLogin(
            @Param("userId") String userId,
            @Param("lastLogin") LocalDateTime lastLogin,
            @Param("lastModifiedDate") LocalDateTime lastModifiedDate
    );

    @Query("""
    SELECT NEW com.usg.apiAutomation.dtos.user.AppUserDTO(
        u.userId,
        u.username,
        u.password,
        u.fullName,
        u.role.roleId,
        u.role.roleName,
        u.isActive,
        u.isDefaultPassword,
        u.failedLoginAttempts,
        u.accountLockedUntil,
        u.createdDate,
        u.lastModifiedDate,
        u.staffId,
        u.emailAddress,
        u.phoneNumber,
        u.lastLogin
    )
    FROM AppUserEntity u
    WHERE LOWER(u.userId) = LOWER(:userId)
""")
    Optional<AppUserDTO> getUserByUserId(@Param("userId") String userId);


    // ✅ Add this query to reset failed login attempts
    @Modifying
    @Transactional
    @Query("""
        UPDATE AppUserEntity u 
        SET u.failedLoginAttempts = 0,
            u.accountLockedUntil = null,
            u.lastModifiedDate = CURRENT_TIMESTAMP
        WHERE LOWER(u.userId) = LOWER(:userId)
    """)
    void resetFailedLoginAttempts(@Param("userId") String userId);

    // ✅ Used when locking the account
    @Modifying
    @Transactional
    @Query("""
        UPDATE AppUserEntity u 
        SET u.accountLockedUntil = :lockUntil, 
            u.failedLoginAttempts = :maxAttempts,
            u.lastModifiedDate = CURRENT_TIMESTAMP
        WHERE LOWER(u.userId) = LOWER(:userId)
    """)
    void lockUserAccount(
            @Param("userId") String userId,
            @Param("lockUntil") Instant lockUntil,
            @Param("maxAttempts") int maxAttempts
    );

    /*
     * New queries to support "unique filters" for the users list (similar to AuditLogRepository):
     * - distinct role names
     * - distinct user groups
     * - distinct usernames (trimmed)
     *
     * These are used to populate filter dropdowns on the UI when listing users.
     */

    @Query("SELECT DISTINCT TRIM(u.role.roleName) FROM AppUserEntity u WHERE u.role.roleName IS NOT NULL AND TRIM(u.role.roleName) <> '' ORDER BY TRIM(u.role.roleName)")
    List<String> findDistinctRoleNames();

    @Query("""
    SELECT DISTINCT u.isActive 
    FROM AppUserEntity u
    WHERE u.role IS NOT NULL
""")
    List<Boolean> findDistinctStatuses();


    @Query("SELECT DISTINCT TRIM(u.username) FROM AppUserEntity u WHERE u.username IS NOT NULL AND TRIM(u.username) <> '' ORDER BY TRIM(u.username)")
    List<String> findDistinctUsernames();
}