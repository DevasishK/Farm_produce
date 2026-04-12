package com.farmproduce.service;

import com.farmproduce.entity.User;
import com.farmproduce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> update(Long id, User patch) {
        return userRepository.findById(id).map(existing -> {
            if (patch.getName() != null) {
                existing.setName(patch.getName());
            }
            if (patch.getEmail() != null) {
                existing.setEmail(patch.getEmail());
            }
            if (patch.getRole() != null) {
                existing.setRole(patch.getRole());
            }
            if (patch.getPhoneNumber() != null) {
                existing.setPhoneNumber(patch.getPhoneNumber());
            }
            return userRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}
