package com.hackathon.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
