package com.Jk.ProjectMgmtSys.service;

import com.Jk.ProjectMgmtSys.entity.User;
import com.Jk.ProjectMgmtSys.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    // Save or register a new user
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    // Find user by email (used in login, dashboard access, etc.)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Find user by ID
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get users by role (e.g., STUDENT, GUIDE)
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    // Delete a user by ID (optional for admin)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Page<User> searchAndFilter(String keyword, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.by("id").descending());

        if ((keyword == null || keyword.isBlank()) && (role==null || role.isBlank())){
            return userRepository.findAll(pageable);
        }
        if ((keyword == null || keyword.isBlank()) && role != null) {
            return userRepository.findByRole(role, pageable);
        }

        if (role == null || role.isBlank()) {
            return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        }

        // Both role and keyword present
        return userRepository.findByRoleAndNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                role, keyword, role, keyword, pageable);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }


    public void deleteUser(Long id, Long currentUserId) {
        if (!id.equals(currentUserId)) {
            userRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Admin cannot delete themselves");
        }
    }


}