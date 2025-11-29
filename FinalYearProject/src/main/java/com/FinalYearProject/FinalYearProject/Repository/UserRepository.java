package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByEmail(String email);
    public Boolean existsByEmail(String email);
    public Optional<User> findByName(String name);
    public List<User> findByRole(String role);
    @Query("SELECT u FROM  User u WHERE u.id=(SELECT MIN(us.id) FROM User us)")
    public User findFirstByOrderByIdAsc();
    @Query("SELECT u FROM  User u WHERE u.id=(SELECT MAX(us.id) FROM User us)")
    public User findFirstByOrderByIdDesc();

}
