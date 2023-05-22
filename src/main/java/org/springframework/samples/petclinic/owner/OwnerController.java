/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.List;
import java.util.Map;
import java.lang.String;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.CaptureSpan;
import co.elastic.apm.api.Span;
import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private final OwnerRepository owners;

	Logger logger = LoggerFactory.getLogger(OwnerController.class);

	public OwnerController(OwnerRepository clinicService) {
		this.owners = clinicService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner() : this.owners.findById(ownerId);
	}

	@GetMapping("/owners/new")
	@CaptureSpan
	public String initCreationForm(HttpSession session, Map<String, Object> model) {
		if (session.getAttribute("username") == null) {
			return "login";
		}
		Owner owner = new Owner();
		model.put("owner", owner);
		logger.info("User:" + session.getAttribute("username") + " made the request GET /owners/new");
		logger.info("Create or Update form rendered");
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/new")
	@CaptureSpan
	public String processCreationForm(@RequestBody String requestBody, HttpSession session, @Valid Owner owner,
			BindingResult result) {
		logger.info("Request Body: " + requestBody);
		if (session.getAttribute("username") == null) {
			return "login";
		}
		if (result.hasErrors()) {
			logger.error("Error in creating new owner");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		Span span = ElasticApm.currentSpan();
		span.addLabel("_tag_user", String.valueOf(session.getAttribute("username")));
		this.owners.save(owner);
		logger.info("User:" + session.getAttribute("username") + " made the request POST /owners/new");
		logger.info("New owner with owner id :" + owner.getId() + " created and added to database successfully");
		logger.info("Fetching new owner details from db - /owners/" + owner.getId());
		return "redirect:/owners/" + owner.getId();
	}

	@GetMapping("/owners/find")
	@CaptureSpan
	public String initFindForm(HttpSession session) {
		if (session.getAttribute("username") == null) {
			return "login";
		}
		Span span = ElasticApm.currentSpan();
		span.addLabel("_tag_user", String.valueOf(session.getAttribute("username")));
		logger.info("User:" + session.getAttribute("username") + " made the request  GET /owners/find");
		logger.info("Find owner page is requested");
		return "owners/findOwners";
	}

	@GetMapping("/owners")
	@CaptureSpan
	public String processFindForm(HttpSession session, @RequestParam(defaultValue = "1") int page, Owner owner,
			BindingResult result, Model model) {
		if (session.getAttribute("username") == null) {
			return "login";
		}
		Span span = ElasticApm.currentSpan();
		span.addLabel("_tag_user", String.valueOf(session.getAttribute("username")));
		logger.info("User:" + session.getAttribute("username") + " made the request  GET /owners");
		// allow parameterless GET request for /owners to return all records
		if (owner.getLastName() == null) {
			owner.setLastName(""); // empty string signifies broadest possible search
		}
		else {
			logger.info("User:" + session.getAttribute("username") + " requested the details of owner with lastname:"
					+ owner.getLastName());
		}
		// find owners by last name
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, owner.getLastName());
		if (ownersResults.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			logger.info("No owners found in database");
			logger.info("Redirecting to find owner page");
			return "owners/findOwners";
		}
		logger.info("Number of owners in the database: " + ownersResults.getTotalElements());
		if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			owner = ownersResults.iterator().next();
			logger.info("Redirecting to /owners/" + owner.getId());
			return "redirect:/owners/" + owner.getId();
		}

		// multiple owners found
		return addPaginationModel(page, model, ownersResults);
	}

	private String addPaginationModel(int page, Model model, Page<Owner> paginated) {
		model.addAttribute("listOwners", paginated);
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		return "owners/ownersList";
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		if (lastname != "") {
			logger.info("Searching for owner details with lastname:" + lastname);
		}
		return owners.findByLastName(lastname, pageable);
	}

	@GetMapping("/owners/{ownerId}/edit")
	@CaptureSpan
	public String initUpdateOwnerForm(HttpSession session, @PathVariable("ownerId") int ownerId, Model model) {
		if (session.getAttribute("username") == null) {
			return "login";
		}
		Span span = ElasticApm.currentSpan();
		span.addLabel("_tag_user", String.valueOf(session.getAttribute("username")));
		Owner owner = this.owners.findById(ownerId);
		model.addAttribute(owner);
		logger.info("User:" + session.getAttribute("username") + " made the request  GET /owners/" + ownerId + "/edit");
		logger.info("Update owner form  for owner id " + ownerId + " is rendered");
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/{ownerId}/edit")
	@CaptureSpan
	public String processUpdateOwnerForm(@RequestBody String requestBody, HttpSession session, @Valid Owner owner,
			BindingResult result, @PathVariable("ownerId") int ownerId) {
		logger.info("Request Body " + requestBody);
		if (session.getAttribute("username") == null) {
			return "login";
		}
		Span span = ElasticApm.currentSpan();
		span.addLabel("_tag_user", String.valueOf(session.getAttribute("username")));
		if (result.hasErrors()) {
			logger.error("Error occured in updating owner details");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		logger
			.info("User:" + session.getAttribute("username") + " made the request  POST /owners/" + ownerId + "/edit");
		logger.info("Owner details with ownerId :" + ownerId + " updated sucessfully in db");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/owners/{ownerId}")
	@CaptureSpan
	public ModelAndView showOwner(HttpSession session, @PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("owners/ownerDetails");
		Owner owner = this.owners.findById(ownerId);
		mav.addObject(owner);
		Span span = ElasticApm.currentSpan();
		span.addLabel("_tag_user", String.valueOf(session.getAttribute("username")));
		logger.info("User:" + session.getAttribute("username") + " made the request  GET /owners/" + ownerId);
		logger.info("Owner details for ownerId: " + ownerId + " rendered");
		return mav;
	}

}
