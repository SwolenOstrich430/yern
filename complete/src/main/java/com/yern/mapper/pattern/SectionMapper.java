package com.yern.mapper.pattern;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.yern.dto.pattern.SectionCreateRequest;
import com.yern.dto.pattern.SectionCreateResponse;
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

        // TODO: this failed in API testing -- double check with unit tests
        section.setFile(file);
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

        if (section.getFile() != null) {
            resp.setFileId(section.getFile().getId());
        }

        return resp;
    }
}
