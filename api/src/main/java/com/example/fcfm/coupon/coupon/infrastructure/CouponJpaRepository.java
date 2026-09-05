package com.example.fcfm.coupon.coupon.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CouponEntity c where c.id = :id")
    Optional<CouponEntity> findByIdForUpdate(@Param("id") Long id);

    // 재고 확인 + 차감을 한 문장으로 (원자적 조건부 UPDATE). 반환값 = 수정된 행 수 (1: 성공, 0: 품절)
    @Modifying
    @Query("update CouponEntity c set c.remainQuantity = c.remainQuantity - 1 where c.id = :id and c.remainQuantity > 0")
    int decreaseRemain(@Param("id") Long id);

    @Modifying
    @Query("update CouponEntity c set c.remainQuantity = c.totalQuantity where c.id = :id")
    void resetRemain(@Param("id") Long id);
}
