package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, ClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (carId == null || date == null) return false;
        // TODO: optionally throw NotFound if car does not exist

        // Fix policies with null endDate
        List<InsurancePolicy> badPolicies = policyRepository.findByCarIdAndEndDateIsNull(carId);
        for (InsurancePolicy policy : badPolicies) {
            policy.setEndDate(policy.getStartDate().plusYears(1));
        }
        if (!badPolicies.isEmpty()) {
            policyRepository.saveAll(badPolicies);
        }
        return policyRepository.existsActiveOnDate(carId, date);
    }

    public Claim registerInsuranceClaim(Long carId, LocalDate claimDate, String description, Double amount) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) {
            throw new IllegalArgumentException("Car not found with ID: " + carId);
        }

        Car car = carOpt.get();
        Claim claim = new Claim(car, claimDate, description, amount);
        return claimRepository.save(claim);
    }

    public List<Claim> listCarClaims(Long carId) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) {
            throw new IllegalArgumentException("Car not found with ID: " + carId);
        }
        return claimRepository.findByCarId(carId);
    }

}
