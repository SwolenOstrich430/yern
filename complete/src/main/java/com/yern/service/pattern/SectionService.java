package com.yern.service.pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yern.model.pattern.Section;
import com.yern.model.storage.FileImpl;
import com.yern.repository.pattern.SectionRepository;

@Service 
public class SectionService {
    private SectionRepository sectionRepository;

    public SectionService(
        @Autowired SectionRepository repository
    ) {
        this.sectionRepository = repository;
    }

    // create a section without file 
    // verify that it's tied to a real pattern -- may need to just be in the pattern service 
    // need to think about auth also 
    public Section createSection(String name, Long patternId) {
        Section section = new Section();
        section.setName(name);
        section.setPatternId(patternId);

        return sectionRepository.save(section);
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
    // get a section raw 
    // get a section and its related file
}
