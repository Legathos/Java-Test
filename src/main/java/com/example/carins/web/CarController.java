package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.ClaimDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    public CarController(CarService service) {
        this.service = service;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId, @RequestParam String date) {
        // TODO: validate date format and handle errors consistently
        LocalDate d = LocalDate.parse(date);
        boolean valid = service.isInsuranceValid(carId, d);
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, d.toString(), valid));
    }

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}

    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<ClaimDto> registerClaim(@PathVariable Long carId, @Valid @RequestBody ClaimDto claimDto) {
        try {
            Claim claim = service.registerInsuranceClaim(
                carId, 
                claimDto.getClaimDate(), 
                claimDto.getDescription(), 
                claimDto.getAmount()
            );

            ClaimDto responseDto = new ClaimDto(
                claim.getId(),
                claim.getCar().getId(),
                claim.getClaimDate(),
                claim.getDescription(),
                claim.getAmount()
            );

            URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(claim.getId())
                .toUri();

            return ResponseEntity.created(location).body(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cars/{carId}/history")
    public List<ClaimDto> getCars(@PathVariable Long carId) {
        return service.listCarClaims(carId).stream().map(c -> new ClaimDto(c.getId(), c.getCar().getId(), c.getClaimDate(), c.getDescription(), c.getAmount())).toList();
    }
    }
