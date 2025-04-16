package dev.magadiflo.user.app.service.impl;

import dev.magadiflo.user.app.constan.UserConstant;
import dev.magadiflo.user.app.entity.Role;
import dev.magadiflo.user.app.entity.User;
import dev.magadiflo.user.app.enums.Roles;
import dev.magadiflo.user.app.model.dto.UserEnabledRequest;
import dev.magadiflo.user.app.model.dto.UserRequest;
import dev.magadiflo.user.app.model.dto.UserResponse;
import dev.magadiflo.user.app.repository.RoleRepository;
import dev.magadiflo.user.app.repository.UserRepository;
import dev.magadiflo.user.app.service.UserService;
import dev.magadiflo.user.app.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponse> findUsers() {
        return this.userRepository.findAll().stream()
                .map(this.userMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse findUser(Long userId) {
        return this.userRepository.findById(userId)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new NoSuchElementException(UserConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(userId)));
    }

    @Override
    public UserResponse findUserByUsername(String username) {
        return this.userRepository.findByUsername(username)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new NoSuchElementException(UserConstant.NO_SUCH_USERNAME_MESSAGE.formatted(username)));
    }

    @Override
    @Transactional
    public UserResponse saveUser(UserRequest userRequest) {
        User userToSave = this.userMapper.toUser(userRequest);
        String role = userRequest.isAdmin() != null && userRequest.isAdmin() ? Roles.ROLE_ADMIN.name() : Roles.ROLE_USER.name();
        Optional<Role> roleOptional = this.roleRepository.findByName(role);
        if (roleOptional.isPresent()) {
            Set<Role> roles = new HashSet<>();
            roles.add(roleOptional.get());
            userToSave.setRoles(roles);
        }
        User userDB = this.userRepository.save(userToSave);
        return this.userMapper.toUserResponse(userDB);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserRequest userRequest) {
        return this.userRepository.findById(userId)
                .map(userDB -> this.userMapper.toUpdateUser(userDB, userRequest))
                .map(this.userRepository::save)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new NoSuchElementException(UserConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(userId)));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User userDB = this.userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(UserConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(userId)));
        this.userRepository.deleteById(userDB.getId());
    }

    @Override
    @Transactional
    public UserResponse updateUserEnabled(Long userId, UserEnabledRequest userEnabledRequest) {
        return this.userRepository.findById(userId)
                .map(userDB -> this.userMapper.toUpdateUserEnabled(userDB, userEnabledRequest))
                .map(this.userRepository::save)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new NoSuchElementException(UserConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(userId)));
    }

    @Override
    @Transactional
    public UserResponse updateUserRoles(Long userId, Set<String> roleNames) {
        boolean invalidRoleExists = roleNames.stream()
                .anyMatch(roleName -> !Roles.exists(roleName));

        if (invalidRoleExists) {
            throw new IllegalStateException(UserConstant.ILLEGAL_STATE_ROLE_EXCEPTION);
        }

        Set<Role> rolesDB = this.roleRepository.findAllByNameIn(roleNames);

        return this.userRepository.findById(userId)
                .map(userDB -> this.userMapper.toUpdateUserRoles(userDB, rolesDB))
                .map(this.userRepository::save)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new NoSuchElementException(UserConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(userId)));
    }
}
