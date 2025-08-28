package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class PolicySchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(PolicySchedulerService.class);
    
    private final InsurancePolicyRepository policyRepository;
    
    private final Set<Long> loggedPolicyIds = new HashSet<>();
    
    public PolicySchedulerService(InsurancePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Scheduled(fixedRate = 3600000)
    public void logExpiredPolicies() {

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<InsurancePolicy> expiredPolicies = policyRepository.findByEndDate(yesterday);
        
        for (InsurancePolicy policy : expiredPolicies) {
            if (!loggedPolicyIds.contains(policy.getId())) {
                logger.info("Policy {} for car {} expired on {}", 
                    policy.getId(), policy.getCar().getId(), policy.getEndDate());
                
                loggedPolicyIds.add(policy.getId());
            }
        }
    }
}