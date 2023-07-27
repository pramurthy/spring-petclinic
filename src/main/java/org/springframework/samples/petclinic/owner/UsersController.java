package org.springframework.samples.petclinic.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsersController {

	private final UsersRepository usersRepository;

	public UsersController(UsersRepository usersRepository) {
		super();
		this.usersRepository = usersRepository;
	}

	Logger logger = LoggerFactory.getLogger(UsersController.class);

	@GetMapping("/userexception")
	public String saveUsers(Model model) {
		Users users = new Users(0, "Snappy", 23);
		try {
			usersRepository.save(users);
			usersRepository.save(users);
		}
		catch (Exception e) {
			logger.error("Exception occurred due to:", e);
			model.addAttribute("excp", "Duplicate entry 'Snappy' for key 'name'");
		}

		return "performance/performance";

	}

}
