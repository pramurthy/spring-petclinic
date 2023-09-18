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

	private static final Logger logger = LoggerFactory.getLogger(PerformanceResource.class);

	List<Users> memoryLeakList = new ArrayList<>();

	private volatile boolean stopFlag = false;
	
	private volatile boolean memoryLeakRunning = false; // Flag to track whether memory leak is running

	private ScheduledExecutorService executorService;

	private Thread memroyLeak;

	@GetMapping("/memoryleak")
	public String createMemoryLeak(Model model) throws InterruptedException {
		logger.info("Memory Leak API is invoked");
		/*
		 * long startTime = System.currentTimeMillis(); long targetDuration = 10 * 60 *
		 * 1000; // 10 minutes in milliseconds long targetHeapSize = 100 * 1024 * 1024; //
		 * 120MB in bytes
		 */ // long initialHeapSize = Runtime.getRuntime().totalMemory();
		if (!memoryLeakRunning) { // Check if memory leak is not already running
	        memoryLeakRunning = true; // Set the flag to indicate memory leak is running
		memoryLeakList.clear();
		memroyLeak = new Thread(() -> {
			List<Object> memoryLeakList = new ArrayList<>();
			int i = 0;
			Thread.currentThread().setName("Pet-MemoryLeak");
			while (i <= 110) {
				memoryLeakList.add(new byte[1024 * 1024]);
				try {
					Thread.sleep(5);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				i++;
			}
			try {
				Thread.sleep(600000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.schedule(this::stopCpuUsage, 10, TimeUnit.MINUTES);
			memoryLeakList.clear();
			System.gc();
			memoryLeakRunning = false; // Reset the flag
		});
		memroyLeak.start();
		}
		return "performance/performance";
	}

	@GetMapping("/stopmemoryusage")
	public String stopMemoryLoad() {
		logger.info("Memory Load API is stopped");
		stopFlag = true;

		if (memroyLeak != null) {
			logger.info("ArrayList object size::" + memoryLeakList.size());
			memroyLeak.interrupt();
		}

		if (executorService != null) {
			executorService.shutdownNow();
			memoryLeakList.clear();
			System.gc();
			logger.info("ArrayList object size after clear::" + memoryLeakList.size());
		}
		return "performance/performance";
	}

	@GetMapping("/increasecpuusage")
	public String increaseCpuUsage(Model model) {
		logger.info("CPU load API is invoked");
		stopFlag = false;
		for (int i = 0; i < 1; i++) {
			Thread CPUIncentiveThread = new Thread(() -> {

				while (!stopFlag) {
					Thread.currentThread().setName("Pet-CPUIncentiveThread");
					for (int j = 0; j < 200000; j++) {

						Math.sqrt(Math.random());
					}
				}
			});
			CPUIncentiveThread.start();

		}

		// Schedule a task to stop the execution after 5 minutes
		executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.schedule(this::stopCpuUsage, 5, TimeUnit.MINUTES);
		return "performance/performance";

	}

	@GetMapping("/stopcpuusage")
	public String stopCpuUsage() {
		logger.info("CPU load API is stopped");
		stopFlag = true;
		/*
		 * // Wait for all CPU threads to finish for (Thread thread : cpuThreads) { try {
		 * thread.join(); } catch (InterruptedException e) { e.printStackTrace(); } }
		 */
		// Clear the list of CPU threads
		if (executorService != null) {
			executorService.shutdownNow();
			logger.info("cleared the memory");
		}
		return "performance/performance";
	}

	@Async
	@GetMapping("/process")
	public void processRequest() throws InterruptedException {

		logger.info("Process request method is invoked");

		synchronized (this) {
			logger.info("inside the synchronization method");
			Thread.currentThread().setName("Pet-Thread-blocked");
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
