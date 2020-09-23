// package csd.week5.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // define a derived query to find user by username
    Optional<User> findByUsername(String username);

    @Modifying
    @Query("update User u set u.isActive = 0 where u.id = :id") // 0 -> false
    void deactivateUser(@Param(value = "id") long id); 
}