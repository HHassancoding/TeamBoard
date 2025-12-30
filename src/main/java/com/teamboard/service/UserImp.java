package com.teamboard.service;

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
    System.out.println("🔍 UserImp.getUser() called with id: " + id);
    Optional<User> result = userRepo.findById(id);
    System.out.println("🔍 userRepo.findById(" + id + ") returned: " + result);
    User user = result.orElse(null);
    System.out.println("🔍 Returning user: " + user);
    return user;
  }


  @Override
  public User updateUser(User user) {
    Optional<User> userToUpdate = userRepo.findById(user.getId());
    if(userToUpdate.isPresent()){
      if(user.getPasswordHash()!=null && !user.getPasswordHash().isEmpty()){
        if(!user.getPasswordHash().startsWith("$2")){
          user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
      }
      return userRepo.save(user);
    }
    return null;
  }

  @Override
  public void deleteUser(Long id) {
    userRepo.deleteById(id);

  }

  @Override
  public User createUser(User user) {
    user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
    return userRepo.save(user);
  }

  public User findByEmail(String email){
    System.out.println("🔍 findByEmail called with email: " + email);
    Optional<User> result = userRepo.findByEmail(email);
    System.out.println("🔍 userRepo.findByEmail('" + email + "') returned: " + result);
    User user = result.orElse(null);
    System.out.println("🔍 Returning user: " + user);
    return user;
  }



}
