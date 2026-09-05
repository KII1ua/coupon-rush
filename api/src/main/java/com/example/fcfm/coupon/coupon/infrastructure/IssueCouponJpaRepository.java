package com.example.fcfm.coupon.coupon.infrastructure;

import com.example.fcfm.coupon.coupon.domain.IssueCoupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssueCouponJpaRepository extends JpaRepository<IssueCouponEntity, Long> {

    @Query("select count(i) from IssueCouponEntity i where i.couponId = :couponId")
    Integer countByCouponId(@Param("couponId") Long couponId);

    @Modifying
    @Query("delete from IssueCouponEntity i where i.couponId = :couponId")
    void deleteAllByCouponId(@Param("couponId") Long couponId);
}
