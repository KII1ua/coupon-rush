package com.example.fcfm.coupon.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long>{
    @Query("select count(u) > 0 from UserEntity u where u.name = :name")
    boolean existsByUserName(@Param("name") String name);

    @Query("select u from UserEntity u where u.name = :name")
    Optional<UserEntity> findByUserName(@Param("name") String name);
}
