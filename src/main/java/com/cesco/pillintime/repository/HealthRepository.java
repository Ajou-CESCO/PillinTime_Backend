package com.cesco.pillintime.repository;

import com.cesco.pillintime.entity.Health;
import com.cesco.pillintime.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthRepository extends JpaRepository<Health, Long> {

    Optional<List<Health>> findByOwnerId(Member member);

    Optional<Health> findByDate(int date);

    void deleteByOwnerId(Long id);
}
