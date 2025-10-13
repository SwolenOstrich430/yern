package com.yern.mapper.pattern;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.yern.dto.pattern.SectionCreateRequest;
import com.yern.dto.pattern.SectionCreateResponse;
import com.yern.model.pattern.Counter;
import com.yern.model.pattern.Section;
import com.yern.model.storage.FileImpl;

@Mapper(componentModel = "spring")
@Component
public class SectionMapper {
    // TODO: add unit test 
    public Section dtoToModel(SectionCreateRequest req) {
        Section section = new Section();
        
        FileImpl file = new FileImpl();
        file.setId(req.getFileId());

        Counter counter = new Counter();
        assert(req.getCounterId() > 0);
        counter.setId(req.getCounterId());

        // TODO: this failed in API testing -- double check with unit tests
        section.setFile(file);
        section.setCounter(counter);
        section.setPatternId(req.getPatternId());
        section.setName(req.getName());
        section.setSequence(req.getSequence());

        return section;
    }

    // TODO: add unit tests 
    public SectionCreateResponse modelToDto(Section section) {
        SectionCreateResponse resp = new SectionCreateResponse();
        resp.setId(section.getId());
        resp.setName(section.getName());
        resp.setPatternId(section.getPatternId());
        resp.setSequence(section.getSequence());
        resp.setCreatedAt(section.getCreatedAt());
        resp.setUpdatedAt(section.getUpdatedAt());
        
        if (section.getFile() != null) {
            resp.setFileId(section.getFile().getId());
        }

        if (section.getCounter() != null) {
            resp.setCounterId(section.getCounter().getId());
        }

        return resp;
    }
}
