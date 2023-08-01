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

	private volatile boolean stopFlag = false;

	private List<Thread> cpuThreads = new ArrayList<>();

	private ScheduledExecutorService executorService;

	@GetMapping("/increasecpuusage")
	public String increaseCpuUsage(Model model) {
		List<Thread> cpuThreads = new ArrayList<>();
		logger.info("CPU load API is invoked");
		// model.addAttribute("cpu", "This API will excute 5 mins");
		stopFlag = false;
		double targetCpuUsage = 2.4; // Target CPU usage (25%
		int numberOfThreads = Runtime.getRuntime().availableProcessors(); // Number of
																			// available
																			// CPU cores
		int targetThreads = (int) (numberOfThreads * targetCpuUsage); // Number of threads
																		// to achieve //
		// // target CPU
		// usage
		System.out.println(targetCpuUsage);
		for (int i = 0; i < targetThreads; i++) {
			logger.info("CPU load API is inside loop");
			Thread thread = new Thread(() -> {
				logger.info("CPU load API is inside thread");
				Thread.currentThread().setName("CPU-Thread-Util");
				while (!stopFlag) {
					for (int j = 0; j < 1_000_000; j++) {
						Math.sqrt(Math.random());
					}
				}
			});

			cpuThreads.add(thread);
			thread.start();
		}

		// Schedule a task to stop the execution after 5 minutes
		executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.schedule(this::stopCpuUsage, 5, TimeUnit.MINUTES);
		return "performance/performance";

	}

	@GetMapping("/stopcpuusage")
	public String stopCpuUsage() {
		stopFlag = true;
		// Wait for all CPU threads to finish
		logger.info("CPU load API is stopped");
		for (Thread thread : cpuThreads) {
			try {
				thread.join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Clear the list of CPU threads
		cpuThreads.clear();
		executorService.shutdownNow();
		return "performance/performance";
	}

	@Async

	@GetMapping("/process")

	public void processRequest() throws InterruptedException {

		logger.info("Process request method is invoked");

		synchronized (this) {

			logger.info("inside the synchronization method");

			Thread.currentThread().setName("Thread-blocked");

			Thread.sleep(10 * 60 * 1000); // Simulating 10 minutes of processing

		}

		// Process completed

		logger.info("process request method is completed");

	}

	@GetMapping("/performance")
	public String memoryLeakPage(HttpSession httpSession) {
		if (httpSession.getAttribute("username") == null) {
			return "login";
		}
		return "performance/performance";
	}

}
