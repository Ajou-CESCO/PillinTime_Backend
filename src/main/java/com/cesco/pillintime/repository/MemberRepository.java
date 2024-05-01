package com.cesco.pillintime.repository;

import com.cesco.pillintime.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByName(String name);

    Optional<Member> findByPhone(String phone); // ssn과 동일한 유저가 있으면 유저를 반환, 없으면 null

    Member findByUuid(String uuid);

    Optional<Member> findByNameAndPhone(String name, String phone);

    Member findByNameAndSsnAndPhone(String name, String ssn, String phone); // 동일한 유저가 있으면 true, 없으면 false

}