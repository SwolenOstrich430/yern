package com.yern.service.pattern;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yern.dto.pattern.SectionCreateRequest;
import com.yern.dto.pattern.SectionCreateResponse;
import com.yern.exceptions.NotFoundException;
import com.yern.model.pattern.Counter;
import com.yern.model.pattern.Section;
import com.yern.model.storage.FileImpl;
import com.yern.repository.pattern.CounterRepository;
import com.yern.repository.pattern.SectionRepository;
import com.yern.mapper.pattern.SectionMapper;

@Service 
public class SectionService {
    private SectionRepository sectionRepository;
    private SectionMapper sectionMapper;
    private CounterRepository counterRepository;
    
    public SectionService(
        @Autowired SectionRepository repository,
        @Autowired SectionMapper mapper,
        @Autowired CounterRepository counterRepository
    ) {
        this.sectionRepository = repository;
        this.sectionMapper = mapper;
        this.counterRepository = counterRepository;
    }

    // get a section and its related file if one exists
    public Section getSection(Long sectionId) throws NotFoundException {
        Section section = sectionRepository.getById(sectionId);

        if (section == null) {
            throw new NotFoundException("Section: " + sectionId);
        }

        return section;
    }

    // create a section without file 
    // verify that it's tied to a real pattern -- may need to just be in the pattern service 
    // need to think about auth also 
    // this should never be called directly in the api 
    // otherwise, no validation will occur
    // contextually, this should only be called through pattern service
    protected SectionCreateResponse createSection(
        SectionCreateRequest req 
    ) {
        Counter counter = createInitialCounter();
        req.setCounterId(counter.getId());
        // TODO: validate sequence not null
        Section createdSection = sectionRepository.save(
            sectionMapper.dtoToModel(req)
        );

        // TODO: assert values are set as expected 
        assert(createdSection.getCounter().getId() == counter.getId());
        return sectionMapper.modelToDto(createdSection);
    }

    // upload a file 
    // add file to that section 
    public void addFileToSection(Long sectionId, FileImpl file) {
        Section section = sectionRepository.getById(sectionId);
        assert(section instanceof Section);
        assert(section.getId() > 0);
        section.setFile(file);

        sectionRepository.save(section);
    }

    public void setInitialCounter(Section section) {
        if (section.getCounter() == null || section.getCounter().getId() <= 0) {
            Counter counter = createInitialCounter();
            section.setCounter(counter);
        } 
    }

    public Counter createInitialCounter() {
        Counter counter = new Counter();
        counter.setValue(0L);
        return counterRepository.save(counter);
    }

    public Section getById(Long id) {
        return sectionRepository.getById(id);
    }
}
