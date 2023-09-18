package org.springframework.samples.petclinic.owner;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.*;
import org.springframework.stereotype.Controller;
@Controller
public class AboutController {
	
	@GetMapping("/about")
	public String DisplayAbout(){
		return "about";
	}
}