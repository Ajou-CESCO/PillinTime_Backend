package com.cesco.pillintime.api.relation.repository;

import com.cesco.pillintime.api.member.entity.Member;
import com.cesco.pillintime.api.relation.entity.Relation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RelationRepository extends JpaRepository<Relation, Long> {

    @Query("SELECT r FROM Relation r WHERE r.manager = :member OR r.client = :member")
    Optional<List<Relation>> findByMember(Member member);

}