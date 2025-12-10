package controller;

import service.UserService;
import entity.User;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController  {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public List<User> getAllUsers(){
    return userService.getAllUsers();

  }
  @GetMapping("/{id}")
  public User getUser(@PathVariable long id){
    return userService.getUser(id);
  }
  @DeleteMapping("/delete/{id}")
  public void deleteUser(@PathVariable long id){
    userService.deleteUser(id);
  }

  @PostMapping("/create")
  public User createUser(@RequestBody User user){
    return userService.createUser(user);
  }

  @PutMapping("/update/{id}")
  public User updateUser(@PathVariable long id, @RequestBody User user){
    user.setId(id);
    return userService.updateUser(user);
  }


}
