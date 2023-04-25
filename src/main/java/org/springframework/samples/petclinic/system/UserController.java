package org.springframework.samples.petclinic.system;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.samples.petclinic.system.User;
import org.springframework.samples.petclinic.system.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserController {

	@Autowired
	UserRepository urepo;

	Logger logger = LoggerFactory.getLogger(UserController.class);

	@RequestMapping("/signup")
	public String getSignup() {
		return "signup";
	}

	@RequestMapping("/login")
	public String getLogin() {
		return "login";
	}

	@PostMapping("/addUser")
	public String addUser(@RequestBody String requestBody, @RequestParam("user_email") String user_email, User user) {
		urepo.save(user);
		logger.info("Request Body: " + requestBody);
		logger.info("New user creation successfully");
		return "success";
	}

	@PostMapping("/login")
	public String login_user(@RequestBody String requestBody, @RequestParam("username") String username,
			@RequestParam("password") String password, HttpSession session, ModelMap modelMap) {
		logger.info("Request Body: " + requestBody);
		User auser = urepo.findByUsernamePassword(username, password);

		if (auser != null) {
			String uname = auser.getUser_email();
			String upass = auser.getUser_pass();

			if (username.equalsIgnoreCase(uname) && password.equalsIgnoreCase(upass)) {
				session.setAttribute("username", username);
				logger.info("User:" + username + " logged in successfully");
				return "welcome";
			}
			else {
				modelMap.put("error", "Invalid Account");
				logger.info("Invalid user account credential");
				return "login";
			}
		}
		else {
			modelMap.put("error", "Invalid Account");
			logger.info("Invalid user account credential");
			return "login";
		}
	}

	@GetMapping(value = "/logout")
	public String logout_user(HttpSession session) {
		logger.info("User:" + session.getAttribute("username") + " made the request GET /logout");
		logger.info("User:" + session.getAttribute("username") + " logged out successfully");
		session.removeAttribute("username");
		session.invalidate();
		logger.info("Redirecting to login page");
		return "login";
	}

	@GetMapping(value = "/home")
	public String home(HttpSession session) {
		if (session.getAttribute("username") == null) {
			return "login";
		}
		else {
			return "welcome";
		}
	}

}
