package com.Jk.ProjectMgmtSys.repository;

import com.Jk.ProjectMgmtSys.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository  extends JpaRepository<User, Long> {
    User findByEmail(String email);
    List<User> findByRole(String role);

    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String keyword, String keyword1, Pageable pageable);
    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findByRoleAndNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
            String role1, String usernameKeyword,
            String role2, String emailKeyword,
            Pageable pageable);
}
