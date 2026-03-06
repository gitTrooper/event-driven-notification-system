package com.saidev.nfsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.saidev.nfsystem.entity.User;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
