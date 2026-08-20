package com.example.fcfm.coupon.coupon.interfaces;

import com.example.fcfm.coupon.coupon.application.CouponApplicationService;
import com.example.fcfm.coupon.coupon.application.dto.*;
import com.example.fcfm.coupon.coupon.domain.Coupon;
import com.example.fcfm.coupon.coupon.domain.IssueCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponApplicationService service;

    @PostMapping("/{couponId}/stock/init")      // 재고 초기화
    public ResponseEntity<Void> getStock(@PathVariable Long couponId) {
        service.deleteStock(couponId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{couponId}/stock")        // 재고 조회
    public ResponseEntity<CouponResponse> checkStock(@PathVariable Long couponId) {
        int stockCount = service.couponCount(couponId);

        return ResponseEntity.ok(new CouponResponse(couponId, stockCount));
    }

    @PostMapping("/{id}/issue")     // 발급
    public ResponseEntity<IssueCoupon> issue(@PathVariable("id") Long couponId, @RequestBody IssueRequest req) {
        return ResponseEntity.ok(service.saveIssueCoupon(new IssueCouponCommand(req.userId(), couponId)));
    }

    @PostMapping("/save/coupon")
    public ResponseEntity<Coupon> save(@RequestBody CouponSaveRequest req) {
        return ResponseEntity.ok(service.save(new CouponCommand(req.quantity())));
    }
}
