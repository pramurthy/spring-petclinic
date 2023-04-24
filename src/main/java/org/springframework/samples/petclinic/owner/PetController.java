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

import java.util.Collection;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	private final OwnerRepository owners;

	Logger logger = LoggerFactory.getLogger(PetController.class);

	public PetController(OwnerRepository owners) {
		this.owners = owners;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.owners.findPetTypes();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {
		return this.owners.findById(ownerId);
	}

	@ModelAttribute("pet")
	public Pet findPet(@PathVariable("ownerId") int ownerId,
			@PathVariable(name = "petId", required = false) Integer petId) {
		return petId == null ? new Pet() : this.owners.findById(ownerId).getPet(petId);
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
	}

	@GetMapping("/pets/new")
	public String initCreationForm(HttpSession session, Owner owner, ModelMap model) {
		if (session.getAttribute("username") == null) {
			return "login";
		}
		Pet pet = new Pet();
		owner.addPet(pet);
		model.put("pet", pet);
		logger.info("User:" + session.getAttribute("username") + " GET /pets/new - Request called");
		logger.info("Create or update pet details form rendered");
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/new")
	public String processCreationForm(@RequestBody String requestBody, HttpSession session, Owner owner, @Valid Pet pet,
			BindingResult result, ModelMap model) {

		logger.info("Request Body: " + requestBody);
		if (session.getAttribute("username") == null) {
			return "login";
		}
		if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			logger.info("Entered details of Pet match with existing Pet data in db");
			result.rejectValue("name", "duplicate", "already exists");
		}

		owner.addPet(pet);
		if (result.hasErrors()) {
			model.put("pet", pet);
			logger.error("Error occured while adding Pet");
			logger.info("Create or update pet details form rendered");
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		logger.info("User:" + session.getAttribute("username") + " POST /pets/new - Request called");
		logger.info("Pet detail is successfully added to the database");
		logger.info("Fetching updated owner details from db - /owners/" + owner.getId());
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm(HttpSession session, Owner owner, @PathVariable("petId") int petId, ModelMap model) {
		if (session.getAttribute("username") == null) {
			return "login";
		}
		Pet pet = owner.getPet(petId);
		model.put("pet", pet);
		logger.info("User:" + session.getAttribute("username") + " GET /pets/{petId}/edit - Request called");
		logger.info("Update pet details form is rendered");
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(@RequestBody String requestBody, HttpSession session, @Valid Pet pet,
			BindingResult result, Owner owner, ModelMap model) {
		logger.info("Request Body: " + requestBody);
		if (session.getAttribute("username") == null) {
			return "login";
		}
		if (result.hasErrors()) {
			model.put("pet", pet);
			logger.error("Error is updating Pet details");
			logger.info("Update pet details form is rendered");
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		owner.addPet(pet);
		this.owners.save(owner);
		logger.info("User:" + session.getAttribute("username") + " POST /pets/{petId}/edit - Request called");
		logger.info("Pet details successfully updated to database");
		logger.info("Fetching updated owner details from db - /owners/" + owner.getId());
		return "redirect:/owners/{ownerId}";
	}

}
