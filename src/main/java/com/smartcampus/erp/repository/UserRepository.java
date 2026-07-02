package com.smartcampus.erp.repository;

import com.smartcampus.erp.entity.Role;
import com.smartcampus.erp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findAllFiltered(@Param("role") Role role,
                               @Param("enabled") Boolean enabled,
                               @Param("search") String search,
                               Pageable pageable);
}
