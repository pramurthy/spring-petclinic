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
}
