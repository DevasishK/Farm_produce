package com.farmproduce.repository;

import com.farmproduce.entity.User;
import com.farmproduce.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findFirstByRole(UserRole role);
}
