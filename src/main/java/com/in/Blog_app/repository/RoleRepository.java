package com.in.Blog_app.repository;

import com.in.Blog_app.entity.Role;
import com.in.Blog_app.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
