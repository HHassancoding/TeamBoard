package Interfaces;

import entity.User;
import java.util.List;

public interface UserService {
  public List<User> getAllUsers();
  public User getUser(Long id);
  User updateUser(User user);
  void deleteUser(Long id);
  public User createUser(User user);

}
