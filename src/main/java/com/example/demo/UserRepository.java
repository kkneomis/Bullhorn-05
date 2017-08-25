package com.example.demo;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<Author, Long> {
    Author findByUsername(String username);
    Author findByEmail(String email);
    Long countByEmail(String email);
    Long countByUsername(String username);
}