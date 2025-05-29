package org.kursplom.service;

import org.kursplom.model.SaveConfiguration;
import org.kursplom.repository.SaveConfigurationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SaveConfigurationService {
    private final SaveConfigurationRepository saveConfigurationRepository;

    public SaveConfigurationService(SaveConfigurationRepository saveConfigurationRepository) {
        this.saveConfigurationRepository = saveConfigurationRepository;
    }

    public List<SaveConfiguration> getAllSaveConfigurations() {
        return saveConfigurationRepository.findAll();
    }

    public Optional<SaveConfiguration> getSaveConfigurationById(Long id) {
        return saveConfigurationRepository.findById(id);
    }

    public long getSaveConfigurationCount() {
        return saveConfigurationRepository.count();
    }

}