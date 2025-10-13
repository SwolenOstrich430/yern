package com.yern.mapper.pattern;

import org.mapstruct.Mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yern.dto.pattern.PatternCreateRequest;
import com.yern.dto.pattern.PatternCreateResponse;
import com.yern.model.pattern.Pattern;

import lombok.NoArgsConstructor;

@Mapper
@NoArgsConstructor
public class PatternMapper extends ObjectMapper {

    public Pattern dtoToModel(PatternCreateRequest req) {
        Pattern pattern = new Pattern();
        pattern.setName(req.getName());
        pattern.setDescription(req.getDescription());

        return pattern;
    }

    public PatternCreateResponse modelToDto(Pattern pattern) {
        PatternCreateResponse resp = new PatternCreateResponse();
        resp.setId(pattern.getId());
        resp.setName(pattern.getName());
        resp.setDescription(pattern.getDescription());

        return resp;
    }
}
