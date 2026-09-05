package com.example.fcfm.coupon.user.domain;

public interface Users {
    User save(String name);
    User findById(Long userId);
    boolean existsByUserName(String name);
    User findByUserName(String name);
}
