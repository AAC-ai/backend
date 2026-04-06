package com.aac.backend.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

    List<Word> findByCategory_Name(String categoryName);
}
