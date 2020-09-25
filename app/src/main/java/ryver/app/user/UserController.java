package ryver.app.user;

import java.util.*;

import javax.validation.Valid;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    // private UserService userService;
    private UserRepository users;
    private BCryptPasswordEncoder encoder;

    public UserController(UserRepository users, BCryptPasswordEncoder encoder){
        // this.userService = us;
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return users.findAll();
    }

    /**
     * Search for users with the given id
     * If there is no user with the given "id", throw a UserNotFoundException
     * @param UID
     * @return user with the given id
     */
    @GetMapping("/users/{UID}")
    public Optional<User> getUser(long UID) {
        Optional<User> user = users.findById(UID);
        if (user == null) throw new UserNotFoundException(UID);

        return user;
    }

    // @PutMapping("/users/{id}")
    // // update phone, password, address
    // // role_user cannot update other things like username
    // // how?
    // public User updateUser(@PathVariable Long id, @Valid @RequestBody User newUserInfo){
    //     // return users.findById(id).map(user -> {user.setUsername(newUserInfo.getUsername());
    //     //     return users.save(user);
    //     // }).orElse(null);
    // }

    /**
    * Using BCrypt encoder to encrypt the password for storage 
    * @param user
     * @return
     */
    @PostMapping("/users")
    public User addUser(@Valid @RequestBody User user){
        user.setPassword(encoder.encode(user.getPassword()));
        return users.save(user);
    }

    // deactivate using the isActive
    // @PutMapping("/users/{id}")
    // public void deactivateUser(@PathVariable Long id){
    //     users.deactivateUser(id);
    // }
   
}