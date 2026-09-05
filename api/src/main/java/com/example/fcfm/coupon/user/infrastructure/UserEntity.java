package com.example.fcfm.coupon.user.infrastructure;

import com.example.fcfm.coupon.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer coupons;

    private UserEntity(String name) {
        this.name = name;
        this.coupons = 0;
    }

    public static UserEntity from(User user) {
        return new UserEntity(user.getName());
    }

    public User toDomain() {
        return User.from(id, name, coupons);
    }
}
