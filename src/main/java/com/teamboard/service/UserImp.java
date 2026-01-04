package com.teamboard.service;

import com.teamboard.DTO.RegisterDTO;
import com.teamboard.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.teamboard.repository.UserRepo;
@Service
public class UserImp implements UserService {
  private final UserRepo userRepo;
  private final PasswordEncoder passwordEncoder;

  public UserImp(UserRepo userRepo, PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public List<User> getAllUsers() {
     return userRepo.findAll();
  }

  @Override
  public User getUser(Long id) {
    Optional<User> result = userRepo.findById(id);
    return result.orElse(null);
  }


  @Override
  public User updateUser(User user) {
    Optional<User> userToUpdate = userRepo.findById(user.getId());
    if(userToUpdate.isPresent()){
      User existing = userToUpdate.get();
      // Do not allow password changes through generic update
      existing.setName(user.getName());
      existing.setAvatarInitials(user.getAvatarInitials());
      existing.setEmail(user.getEmail());
      return userRepo.save(existing);
    }
    return null;
  }

  @Override
  public void deleteUser(Long id) {
    userRepo.deleteById(id);

  }

  @Override
  public User createUser(RegisterDTO registerDTO) {
    if (registerDTO.getPassword() == null || registerDTO.getPassword().isBlank()) {
      throw new IllegalArgumentException("Password is required");
    }
    if (registerDTO.getEmail() == null || registerDTO.getEmail().isBlank()){
      throw new IllegalArgumentException("Email is required");
    }

    // create entity and ensure we only set hashed password on entity
    User user = new User();
    user.setName(registerDTO.getName());
    user.setEmail(registerDTO.getEmail());
    user.setAvatarInitials(registerDTO.getAvatarInitials());
    String hashed = passwordEncoder.encode(registerDTO.getPassword());
    user.setPasswordHash(hashed);
    return userRepo.save(user);
  }

  public User findByEmail(String email){
    Optional<User> result = userRepo.findByEmail(email);
    return result.orElse(null);
  }

  // Additional method to change password explicitly
  public User changePassword(Long userId, String rawPassword){
    if(rawPassword==null || rawPassword.isBlank()){
      throw new IllegalArgumentException("Password is required");
    }
    Optional<User> userOpt = userRepo.findById(userId);
    if(userOpt.isEmpty()) return null;
    User user = userOpt.get();
    user.setPasswordHash(passwordEncoder.encode(rawPassword));
    return userRepo.save(user);
  }


}
