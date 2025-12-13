package service;

import entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.UserRepo;
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
    return userRepo.findById(id).orElse(null);
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


}
