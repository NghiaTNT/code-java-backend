package com.ie303m22.laptopweb.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ie303m22.laptopweb.models.Brand;
import com.ie303m22.laptopweb.models.EProductBrand;
import com.ie303m22.laptopweb.payload.request.BrandRequest;
import com.ie303m22.laptopweb.payload.response.BrandResponse;
import com.ie303m22.laptopweb.payload.response.MessageResponse;
import com.ie303m22.laptopweb.services.BrandServiceImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/brands")
public class BrandController {

	@Autowired
	BrandServiceImpl brandService;

	@GetMapping
	ResponseEntity<?> getAllBrand() {
		List<BrandResponse> brandResponse = brandService.findAll().stream().map(brand -> new BrandResponse(brand))
				.collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(brandResponse);
	}

	@GetMapping("/{id}")
	ResponseEntity<Brand> getBrand(@PathVariable Long id) {
		Optional<Brand> brandOptional = brandService.findById(id);

		return brandOptional.map(tempBrand -> new ResponseEntity<>(tempBrand, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PostMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	ResponseEntity<?> createNewCategory(@RequestBody BrandRequest brandRequest) {
		EProductBrand brandName = EProductBrand.valueOf(brandRequest.getName());

		if (brandService.existsBrandName(brandName.toString())) {
			return ResponseEntity.badRequest().body(new MessageResponse("That brand already exists"));
		}

		Brand brand = new Brand(brandName);
		brandService.save(brand);

		return new ResponseEntity<>(new MessageResponse("Add brand '" + brand.getName() + "' successfully!"),
				HttpStatus.OK);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	ResponseEntity<?> updateBrand(@PathVariable Long id, @RequestBody BrandRequest brandRequest) {
		EProductBrand brandName = EProductBrand.valueOf(brandRequest.getName());

		if (brandService.existsBrandName(brandName.toString())) {
			return ResponseEntity.badRequest().body(new MessageResponse("That brand already exists"));
		}

		Optional<Brand> brandOptional = brandService.findById(id);

		return brandOptional.map(oldBrand -> {
			Brand brand = new Brand(brandName);
			String oldBrandName = oldBrand.getName().toString();

			brand.setId(oldBrand.getId());
			brandService.save(brand);

			return ResponseEntity.ok()
					.body(new MessageResponse("Updated brand '" + oldBrandName + "' to '" + brand.getName() + "'"));
		}).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	ResponseEntity<Brand> deleteBrand(@PathVariable Long id) {
		Optional<Brand> brandOptional = brandService.findById(id);

		return brandOptional.map(tempBrand -> {
			brandService.remove(id);

			return new ResponseEntity<>(tempBrand, HttpStatus.OK);
		}).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
