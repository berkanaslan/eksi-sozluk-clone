package com.berkanaslan.eksisozlukclone.controller;


import com.berkanaslan.eksisozlukclone.model.User;
import com.berkanaslan.eksisozlukclone.repository.UserRepository;
import com.berkanaslan.eksisozlukclone.util.ExceptionMessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@RestController
@RequestMapping(path = UserController.PATH)
public class UserController extends BaseEntityController<User> {
    static final String PATH = "/user";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public String getRequestPath() {
        return PATH;
    }

    @Override
    public User save(@RequestBody final User user) {
        if (user.getId() != 0) {
            return this.updateUser(user);
        }

        if (user.getPassword() == null) {
            throw new IllegalArgumentException(ExceptionMessageUtil.getMessageByLocale("message.password_is_required"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getUsername() == null) {
            throw new IllegalArgumentException(ExceptionMessageUtil.getMessageByLocale("message.username_is_required"));
        }

        if (user.getEmail() == null) {
            throw new IllegalArgumentException(ExceptionMessageUtil.getMessageByLocale("message.email_is_required"));
        }

        final Optional<User> isUsernameAlreadyUsed = userRepository.findByUsername(user.getUsername());

        if (isUsernameAlreadyUsed.isPresent()) {
            throw new IllegalArgumentException(ExceptionMessageUtil.getMessageByLocale("message.username_already_using"));
        }

        final Optional<User> isEmailAlreadyUsed = userRepository.findByEmail(user.getEmail());

        if (isEmailAlreadyUsed.isPresent()) {
            throw new IllegalArgumentException(ExceptionMessageUtil.getMessageByLocale("message.email_already_using"));
        }

        return super.save(user);
    }

    private User updateUser(@RequestBody final User user) {
        if (user.getId() == 0) {
            return this.save(user);
        }

        final Optional<User> userOptional = userRepository.findById(user.getId());

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException(ExceptionMessageUtil.getMessageByLocale("message.user_not_found"));
        }

        final User existingUserOnDB = userOptional.get();
        copyNonNullProperties(user, existingUserOnDB);

        // Encode the new password if declared.
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return super.save(existingUserOnDB);
    }
}
