
package com.yern.service.pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.yern.dto.pattern.PatternCreateRequest;
import com.yern.dto.pattern.PatternCreateResponse;
import com.yern.dto.pattern.SectionCreateRequest;
import com.yern.dto.pattern.SectionCreateResponse;
import com.yern.mapper.pattern.PatternMapper;
import com.yern.repository.pattern.PatternRepository;
import com.yern.repository.pattern.UserPatternRepository;
import com.yern.service.messaging.MessagePublisher;
import com.yern.service.storage.file.FileService;
import com.yern.model.pattern.Pattern;
import com.yern.model.pattern.UserPattern;

public class PatternServiceTest {
    private PatternService patternService;
    private FileService fileService;
    private SectionService sectionService;
    private PatternRepository patternRepository;
    private UserPatternRepository userPatternRepository;
    private PatternMapper patternMapper;
    private SectionCreateRequest req;
    private SectionCreateResponse resp;
    private PatternCreateRequest patternCreateRequest;
    private PatternCreateResponse patternCreateResponse;
    private Pattern pattern;
    private UserPattern userPattern;
    private MessagePublisher messagePublisher;
    private String topicName = UUID.randomUUID().toString();
    
    private final Long fileId = 3L;
    private final Long patternId = 1L;
    private final Long userId = 2L;

    @BeforeEach
    public void setup() {
        this.patternRepository = mock(PatternRepository.class);
        this.sectionService = mock(SectionService.class);
        this.fileService = mock(FileService.class);
        this.userPatternRepository = mock(UserPatternRepository.class);
        this.patternMapper = mock(PatternMapper.class);
        this.messagePublisher = mock(MessagePublisher.class);

        this.patternService = new PatternService(
            fileService,
            sectionService, 
            patternRepository,
            userPatternRepository,
            patternMapper,
            messagePublisher,
            topicName
        );

        this.patternCreateRequest = mock(PatternCreateRequest.class);
        this.patternCreateResponse = mock(PatternCreateResponse.class);
        this.req = mock(SectionCreateRequest.class);
        this.resp = mock(SectionCreateResponse.class);
        this.pattern = mock(Pattern.class);
        this.userPattern = mock(UserPattern.class);
    }

    @Test 
    public void createPattern_throwsAssertionError_ifPatternInsertFails() {
        when(patternMapper.dtoToModel(patternCreateRequest)).thenReturn(pattern);
        when(patternRepository.save(pattern)).thenReturn(null);

        assertThrows(
            AssertionError.class, 
            () -> patternService.createPattern(userId, patternCreateRequest)
        );

        when(patternMapper.dtoToModel(patternCreateRequest)).thenReturn(pattern);
        when(patternRepository.save(pattern)).thenReturn(pattern);
        when(pattern.getId()).thenReturn(0L);

        assertThrows(
            AssertionError.class, 
            () -> patternService.createPattern(userId, patternCreateRequest)
        );
    }

    @Test 
    public void createPattern_savesRequestDetailsToPatterns() {
        when(
            patternMapper.dtoToModel(patternCreateRequest)
        ).thenReturn(pattern);
        when(patternRepository.save(pattern)).thenReturn(pattern);
        when(userPatternRepository.save(any(UserPattern.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );
        when(pattern.getId()).thenReturn(1L);

        patternService.createPattern(userId, patternCreateRequest);

        verify(
            patternRepository, 
            times(1)
        ).save(pattern);
    }


    @Test 
    public void createPattern_savesUserIDAndCreatedPatternId_toUsersPatterns() {
        when(
            patternMapper.dtoToModel(patternCreateRequest)
        ).thenReturn(pattern);
        when(patternRepository.save(pattern)).thenReturn(pattern);
        when(userPatternRepository.save(any(UserPattern.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );
        when(pattern.getId()).thenReturn(1L);

        patternService.createPattern(userId, patternCreateRequest);

        verify(
            userPatternRepository, 
            times(1)
        ).save(any(UserPattern.class));
    }

    @Test 
    public void createPattern_returnsAPatterCreateResponse_basedOnCreatedPattern() {
        when(
            patternMapper.dtoToModel(patternCreateRequest)
        ).thenReturn(pattern);
        when(patternRepository.save(pattern)).thenReturn(pattern);
        when(userPatternRepository.save(any(UserPattern.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );
        when(pattern.getId()).thenReturn(1L);
        when(patternMapper.modelToDto(pattern)).thenReturn(patternCreateResponse);
        PatternCreateResponse currResp = patternService.createPattern(userId, patternCreateRequest);
        assertInstanceOf(PatternCreateResponse.class, currResp);
        assertEquals(patternCreateResponse, currResp);
    }

    @Test 
    public void addSection_returnsASectionCreateResponse() {
        when(userPatternRepository.findByUserIdAndPatternId(
            userId, patternId
        )).thenReturn(Optional.of(userPattern));
        when(req.getPatternId()).thenReturn(patternId);
        when(req.getFileId()).thenReturn(fileId);
        doNothing().when(fileService).validateAccess(any(), any());
        when(sectionService.createSection(req)).thenReturn(resp);

        SectionCreateResponse foundResp = patternService.addSection(userId, req);
        assertInstanceOf(SectionCreateResponse.class, foundResp);
        assertEquals(foundResp, resp);
    }

    @Test 
    public void addSection_throwsAccessDenied_whenContextualUserDoesntHaveAccessToThePatternId() {
        when(userPatternRepository.findByUserIdAndPatternId(
            userId, patternId
        )).thenReturn(Optional.empty());
        when(req.getPatternId()).thenReturn(patternId);
        when(req.getFileId()).thenReturn(fileId);
        assertThrows(
            AccessDeniedException.class, 
            () -> patternService.addSection(userId, req)
        );
    }

    @Test 
    public void addSection_throwsAccessDenied_whenContextualUserDoesntHaveAccessToFileId() {
        when(req.getPatternId()).thenReturn(patternId);
        when(req.getFileId()).thenReturn(fileId);
        when(userPatternRepository.findByUserIdAndPatternId(
            userId, patternId
        )).thenReturn(Optional.of(userPattern));

        doThrow(
            AccessDeniedException.class
        )
        .when(fileService)
        .validateAccess(fileId, userId);

        assertThrows(
            AccessDeniedException.class, 
            () -> patternService.addSection(userId, req)
        );
    }

    @Test 
    public void addSection_savesASectionToDB_whenAccessChecksPass() {
        when(req.getPatternId()).thenReturn(patternId);
        when(req.getFileId()).thenReturn(fileId);
        when(userPatternRepository.findByUserIdAndPatternId(
            userId, patternId
        )).thenReturn(Optional.of(userPattern));

        doNothing().when(fileService).validateAccess(fileId, userId);

        patternService.addSection(userId, req);

        verify(
            sectionService, 
            times(1)
        ).createSection(req);
    }
}
