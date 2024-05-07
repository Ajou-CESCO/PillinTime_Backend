package com.cesco.pillintime.repository;

import com.cesco.pillintime.entity.Member;
import com.cesco.pillintime.entity.Relation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RelationRepository extends JpaRepository<Relation, Long> {

    @Query("SELECT r FROM Relation r WHERE r.managerId = :id OR r.clientId = :id")
    List<Relation> findByMemberId(Long id);

}