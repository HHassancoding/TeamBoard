package Interfaces;

import entity.User;
import java.util.List;

public class UserImp implements UserService{
  @Override
  public List<User> getAllUsers() {
    return List.of();
  }

  @Override
  public User getUser(Long id) {
    return null;
  }

  @Override
  public User updateUser(User user) {
    return null;
  }

  @Override
  public void deleteUser(Long id) {

  }
}
