package com.example.fcfm.coupon.coupon.infrastructure;

import com.example.fcfm.coupon.coupon.domain.CouponIssuer;
import com.example.fcfm.coupon.coupon.domain.IssueResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisCouponIssuer implements CouponIssuer {

    private static final RedisScript<Long> ISSUE_SCRIPT = RedisScript.of(new ClassPathResource("scripts/issue.lua"), Long.class);

    private final StringRedisTemplate redis;

    @Override
    public void open(Long couponId, int totalQuantity) {
        redis.opsForValue().set(limitKey(couponId), String.valueOf(totalQuantity));
        redis.delete(issuedKey(couponId));
    }

    @Override
    public IssueResult tryIssue(Long couponId, Long userId) {
        Long result = redis.execute(
                ISSUE_SCRIPT,
                List.of(issuedKey(couponId), limitKey(couponId)),
                String.valueOf(userId)
        );
        return switch (result.intValue()) {
            case 1 -> IssueResult.ISSUED;
            case 0 -> IssueResult.SOLD_OUT;
            case -1 -> IssueResult.DUPLICATED;
            default -> IssueResult.NOT_FOUND;
        };
    }

    @Override
    public void cancel(Long couponId, Long userId) {
        redis.opsForSet().remove(issuedKey(couponId), String.valueOf(userId));
    }

    @Override
    public int issuedCount(Long couponId) {
        Long size = redis.opsForSet().size(issuedKey(couponId));
        return size == null? 0 : size.intValue();
    }

    private String issuedKey(Long couponId) {
        return "coupon:" + couponId + ":issued";
    }

    private String limitKey(Long couponId) {
        return "coupon:" + couponId + ":limit";
    }
}
