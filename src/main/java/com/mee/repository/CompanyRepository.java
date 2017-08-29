package com.mee.repository;

import com.mee.entity.Company;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CompanyRepository extends MongoRepository<Company, String> {
    public Optional<Company> findByEmail(String email);
}
