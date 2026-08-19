package com.example.fcfm.coupon.user.infrastructure;

import com.example.fcfm.coupon.user.domain.User;
import com.example.fcfm.coupon.user.domain.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserJpa implements Users {
    private final UserJpaRepository delegate;

    @Override
    public User save(String name) {
        UserEntity entity = UserEntity.from(name);
        delegate.save(entity);
        return entity.toDomain();
    }

    @Override
    public User findById(Long userId) {
        return delegate.findById(userId)
                .orElseThrow().toDomain();
    }

    @Override
    public boolean existsByUserName(String name) {
        return delegate.existsByUserName(name);
    }

    @Override
    public User findByUserName(String name) {
        return delegate.findByUserName(name)
                .map(UserEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 사용자입니다."));
    }
}
