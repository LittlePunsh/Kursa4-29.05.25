package org.kursplom.service;

import org.kursplom.model.DecisionType;
import org.kursplom.repository.DecisionTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DecisionTypeService {
    private final DecisionTypeRepository decisionTypeRepository;

    public DecisionTypeService(DecisionTypeRepository decisionTypeRepository) {
        this.decisionTypeRepository = decisionTypeRepository;
    }

    public List<DecisionType> getAllDecisionTypes() {
        return decisionTypeRepository.findAll();
    }

}
