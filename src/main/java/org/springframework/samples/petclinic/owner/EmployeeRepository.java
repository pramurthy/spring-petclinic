package org.springframework.samples.petclinic.owner;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface EmployeeRepository extends Repository<Employee, Integer> {

	@Query(value = "SELECT * FROM Employee, (SELECT SLEEP(10)) AS delay", nativeQuery = true)
	public List<Employee> findAllEmployees();

	void save(Employee employee);

}
