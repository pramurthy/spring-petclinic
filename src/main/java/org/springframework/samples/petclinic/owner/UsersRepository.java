package org.springframework.samples.petclinic.owner;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface UsersRepository extends Repository<Users, Integer> {

	@Query(value = "SELECT * FROM Users, (SELECT SLEEP(10)) AS delay", nativeQuery = true)
	public List<Users> findAllEmployees();

	void save(Users employee);

}
