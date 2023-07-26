package org.springframework.samples.petclinic.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployeeController {

	private final EmployeeRepository employeeRepository;

	public EmployeeController(EmployeeRepository employeeRepository) {
		super();
		this.employeeRepository = employeeRepository;
	}

	Logger logger = LoggerFactory.getLogger(EmployeeController.class);

	@GetMapping("/empexception")
	public String saveEmployee(Model model) {
		Employee employee = new Employee(0, "Snappy", 23);
		try {
			employeeRepository.save(employee);
			employeeRepository.save(employee);
		}
		catch (Exception e) {
			logger.error("Exception occurred due to:", e);
			model.addAttribute("excp", "Duplicate entry 'Snappy' for key 'name'");
		}

		return "performance/performance";

	}

}
