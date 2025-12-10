package service;

import entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import repository.UserRepo;
@Service
public class UserImp implements UserService {
  private final UserRepo userRepo;

  public UserImp(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

  @Override
  public List<User> getAllUsers() {
     return userRepo.findAll();
  }

  @Override
  public User getUser(Long id) {
    return userRepo.findById(id).orElse(null);
  }

  @Override
  public User updateUser(User user) {
    Optional<User> userToUpdate = userRepo.findById(user.getId());
    if(userToUpdate.isPresent()){
      return userRepo.save(user);
    }

    return null;
  }

  @Override
  public void deleteUser(Long id) {
    userRepo.findById(id);


  }

  @Override
  public User createUser(User user) {
    return userRepo.save(user);
  }
}
