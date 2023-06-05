package org.springframework.samples.petclinic.system;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.samples.petclinic.system.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	@Query("from User where user_email=?1")
	public List<User> findByEMAIL(String email);

	@Query("from User where user_email=?1 and user_pass=?2")
	public User findByUsernamePassword(String username, String password);

	@Query(value = "select * ,sleep(80) from user where user_fname=?1", nativeQuery = true)
	public User findByFname(String firstname);

}