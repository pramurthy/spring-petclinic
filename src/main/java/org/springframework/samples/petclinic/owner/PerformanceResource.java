package org.springframework.samples.petclinic.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class PerformanceResource {

	Logger logger = LoggerFactory.getLogger(PerformanceResource.class);

	@GetMapping("/performance")
	public String memoryLeakPage(HttpSession httpSession) {
		if (httpSession.getAttribute("username") == null) {
			return "login";
		}
		return "performance/performance";
	}

@GetMapping("/memoryleak")
public String createMemoryLeak(Model model) throws InterruptedException {
	logger.info("Memory Leak API is invoked");
	List<Users> memoryLeakList = new ArrayList<Users>();
	model.addAttribute("msg", "This API will excute 10 mins");
	int numberofUser = 0;
	long endTime = System.currentTimeMillis() + 10 * 60 * 1000; // 10 minutes
	while (System.currentTimeMillis() < endTime) {
		Users users = new Users();
		users.setAge(20);
		users.setId(1);
		users.setName("sai");
		Thread.currentThread().setName("Memory-Leak");
		memoryLeakList.add(users);
		Thread.sleep(1);
		numberofUser++;
	}

	/*
	 * System.err.println("CPU Usage"); Thread thread = new Thread(() -> { while
	 * (System.currentTimeMillis() < endTime) { Employee employee = new Employee();
	 * Thread.currentThread().setName("Memory-Leak"); memoryLeakList.add(employee);
	 * try { Thread.sleep(5); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } } });
	 */
	memoryLeakList.clear();
	logger.info("Total employee objects are created:" + numberofUser);
	logger.info("Memory Leak API is stopped");
	return "performance/performance";
}
}
