package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
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
}
