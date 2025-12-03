package com.FinalYearProject.FinalYearProject.Repository;

import com.FinalYearProject.FinalYearProject.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByEmail(String email);
    public Boolean existsByEmail(String email);
    public List<User> findByRole(String role);
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.email =:email")
    public void deleteByEmail(String email);
    @Transactional
    @Modifying
    @Query("UPDATE User u set u.is_enable=true, u.locked=false,u.expired=false where u.email=:email")
    public  void updateIsEnableLockedExpiredToTrue(String email);
}
