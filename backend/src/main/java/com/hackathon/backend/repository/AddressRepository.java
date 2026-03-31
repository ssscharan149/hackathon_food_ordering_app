package com.hackathon.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.backend.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserUserId(Long userId);
}
