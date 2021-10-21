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

import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
class VisitController {

	public static final String PETS_CREATE_OR_UPDATE_VISIT_FORM = "pets/createOrUpdateVisitForm";
	private final VisitRepository visits;
    private boolean isCancel=false;
	private final PetRepository pets;
	private  final VetRepository vets;

	public VisitController(VisitRepository visits, PetRepository pets, VetRepository vets) {
		this.visits = visits;
		this.pets = pets;
		this.vets = vets;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("petId") int petId, Map<String, Object> model) {
		Pet pet = this.pets.findById(petId);
		pet.setVisitsInternal(this.visits.findByPetId(petId));
		model.put("pet", pet);
		Visit visit = new Visit();
		pet.addVisit(visit);
		return visit;
	}
	@ModelAttribute("veterenars")
	public Collection<Vet> populateVeterenarTypes() {
		return this.vets.findAll();
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
	@GetMapping("/owners/*/pets/{petId}/visits/new")
	public String initNewVisitForm(@PathVariable("petId") int petId, Map<String, Object> model) {
		Pet pet = this.pets.findById(petId);
		pet.setVisitsInternal(this.visits.findByPetId(petId));
		model.put("pet", pet);
		Visit visit = new Visit();
		pet.addVisit(visit);
		return PETS_CREATE_OR_UPDATE_VISIT_FORM;
	}
	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@Valid Visit visit, BindingResult result) {
		if (result.hasErrors()) {
			return PETS_CREATE_OR_UPDATE_VISIT_FORM;
		}
		else {
			this.visits.save(visit);
			return "redirect:/owners/{ownerId}";
		}
	}

	@GetMapping("/owners/*/pets/{petId}/visits/{visitId}/edit")
	public String initUpdateForm(@PathVariable("petId") int petId,
								 @PathVariable("visitId") int visitId,
								 Map<String, Object> model) {
		Pet pet = this.pets.findById(petId);
		Visit visit= this.visits.findById(visitId);
		model.put("pet", pet);
		model.put("visit",visit);
		pet.addVisit(visit);
		return PETS_CREATE_OR_UPDATE_VISIT_FORM;
	}
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit")
	public String processUpdateVisitForm(@Valid Visit visit, BindingResult result,
										 @PathVariable("visitId") int visitId,
										 Pet pet, Map<String, Object> model) {
		if (result.hasErrors()) {
		model.put("visit", visit);
			return PETS_CREATE_OR_UPDATE_VISIT_FORM;
		}
		else {
			visit.setId(visitId);
			this.visits.save(visit);
			return "redirect:/owners/{ownerId}";
		}
	}

	//It's my cancel order and it deletes the visit from database.
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/cancel")
	public String initCanselForm(@PathVariable("petId") int petId,
								 @PathVariable("visitId") int visitId,
								 Map<String, Object> model) {
		Pet pet = this.pets.findById(petId);
		Visit visit= this.visits.findById(visitId);
		pet.deleteVisit(visit);
        this.visits.deleteById(visitId);
		return "redirect:/owners/{ownerId}";
	}
}
