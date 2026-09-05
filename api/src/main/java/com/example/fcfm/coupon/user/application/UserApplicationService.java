package com.example.fcfm.coupon.user.application;

import com.example.fcfm.coupon.user.application.dto.UserCommand;
import com.example.fcfm.coupon.user.application.dto.UserResponse;
import com.example.fcfm.coupon.user.domain.User;
import com.example.fcfm.coupon.user.domain.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserApplicationService {
    private final Users users;

    @Transactional
    public UserResponse register(UserCommand command) {
        if(users.existsByUserName(command.name())) throw new IllegalArgumentException("이미 존재하는 사용자가 있습니다.");
        User user = users.save(command.name());

        return new UserResponse(user.getName(), user.getCoupons());
    }

    public UserResponse getUser(UserCommand command) {
        User user = users.findByUserName(command.name());

        return new UserResponse(user.getName(), user.getCoupons());
    }
}
