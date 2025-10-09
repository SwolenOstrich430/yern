package com.yern.service.pattern;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.yern.dto.pattern.PatternCreateRequest;
import com.yern.dto.pattern.PatternCreateResponse;
import com.yern.dto.pattern.SectionCreateRequest;
import com.yern.dto.pattern.SectionCreateResponse;
import com.yern.mapper.pattern.PatternMapper;
import com.yern.model.pattern.Pattern;
import com.yern.model.pattern.UserPattern;
import com.yern.repository.pattern.PatternRepository;
import com.yern.repository.pattern.UserPatternRepository;
import com.yern.service.storage.file.FileService;

@Service 
public class PatternService {
    private FileService fileService;
    private SectionService sectionService;
    private PatternRepository patternRepository;
    private UserPatternRepository userPatternRepository;
    private PatternMapper patternMapper;

    public PatternService(
        @Autowired FileService fileService,
        @Autowired SectionService sectionService,
        @Autowired PatternRepository patternRepository,
        @Autowired UserPatternRepository userPatternRepository,
        @Autowired PatternMapper patternMapper
    ) {
        this.fileService = fileService;
        this.sectionService = sectionService;
        this.patternRepository = patternRepository;
        this.userPatternRepository = userPatternRepository;
        this.patternMapper = patternMapper;
    }

    public PatternCreateResponse createPattern(
        Long userId, 
        PatternCreateRequest req
    ) {
        Pattern createdPattern = patternRepository.save(
            patternMapper.dtoToModel(req)
        );
        assert(createdPattern != null);
        assert(createdPattern.getId() > 0);

        // TODO: move this into a service 
        UserPattern userPattern = new UserPattern();
        userPattern.setUserId(userId);
        userPattern.setPatternId(createdPattern.getId());
        UserPattern createdUserPattern = userPatternRepository.save(
            userPattern
        );

        assert(createdUserPattern != null);
        assert(createdUserPattern.getUserId().equals(userId));
        assert(
            createdUserPattern.getPatternId().equals(
                createdPattern.getId()
            )
        );

        return patternMapper.modelToDto(createdPattern);
    }

    public SectionCreateResponse addSection(
        Long userId,
        SectionCreateRequest req 
    ) {
        validateAccess(req.getPatternId(), userId);
        fileService.validateAccess(req.getFileId(), userId);
        return sectionService.createSection(req);
    }

    public void validateAccess(
        Long patternId, 
        Long userId
    ) throws AccessDeniedException {
        Optional<UserPattern> permissions = userPatternRepository.findByUserIdAndPatternId( 
            userId, 
            patternId
        );
        
        if (permissions.isEmpty()) {
            throw new AccessDeniedException(
                "Pattern Id: " + patternId
            );
        }
    }
}
