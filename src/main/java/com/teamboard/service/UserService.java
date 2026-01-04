package com.teamboard.service;

import com.teamboard.DTO.RegisterDTO;
import com.teamboard.entity.User;
import java.util.List;

public interface UserService {
  List<User> getAllUsers();
  User getUser(Long id);
  User updateUser(User user);
  void deleteUser(Long id);
  User createUser(RegisterDTO registerDTO);
  User findByEmail(String email);
}
