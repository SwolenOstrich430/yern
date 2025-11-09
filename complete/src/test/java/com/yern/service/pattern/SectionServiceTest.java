package com.yern.service.pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.yern.model.common.AuditTimestamp;
import com.yern.model.pattern.Counter;
import com.yern.model.pattern.Section;
import com.yern.model.storage.FileImpl;
import com.yern.dto.pattern.SectionCreateRequest;
import com.yern.dto.pattern.SectionCreateResponse;
import com.yern.exceptions.NotFoundException;
import com.yern.mapper.pattern.SectionMapper;
import com.yern.repository.pattern.CounterRepository;
import com.yern.repository.pattern.SectionRepository;
import com.yern.service.storage.file.FileService;


public class SectionServiceTest {

    private SectionService service; 

    @Mock 
    private SectionRepository repository;

    @Mock 
    private CounterRepository counterRepository;

    @Mock 
    private SectionMapper mapper;

    @Mock 
    private FileService fileService;

    @Mock 
    private Section section;

    @Mock 
    private FileImpl file;

    @Mock 
    private Counter counter;

    @Mock 
    private AuditTimestamp auditTimestamp;

    private SectionService spy;

    private final Long userId = 1L;
    private final Long counterId = 2L;


    @BeforeEach 
    public void setup() {
        MockitoAnnotations.openMocks(this);

        service = new SectionService(
            repository, 
            mapper, 
            counterRepository, 
            fileService
        );
        
        spy = spy(service);
    }

    @Test 
    public void getSection_returnsASection() throws NotFoundException {
        Long id = 1L;
        when(repository.getById(id)).thenReturn(section);
        
        Section foundSection = service.getSection(id);
        assertInstanceOf(Section.class, foundSection);
        assertEquals(section, foundSection);
    }

    @Test 
    public void getSection_throwsNotFoundException_whenProvidedIdDoesntExist() {
        Long id = 1L;
        when(repository.getById(id)).thenReturn(null);

        assertThrows(
            NotFoundException.class,
            () -> service.getSection(id)
        );
    }

    @Test
    public void createSection_returnsASectionCreateResponse() {
        String name = UUID.randomUUID().toString();
        Long patternId = 1L;
        Long fileId = 2L;

        doReturn(counter).when(spy).createInitialCounter();
        SectionCreateRequest req = new SectionCreateRequest();
        req.setName(name);
        req.setPatternId(patternId);
        req.setFileId(fileId);

        doReturn(counter).when(spy).createInitialCounter();
        when(mapper.dtoToModel(req)).thenReturn(section);
        when(
            repository.save(section)
        )
        .thenAnswer(
            invocation -> invocation.getArgument(0)
        );
        when(mapper.modelToDto(section)).thenCallRealMethod();
        when(section.getFile()).thenReturn(file);
        when(section.getName()).thenReturn(name);
        when(section.getPatternId()).thenReturn(patternId);
        when(file.getId()).thenReturn(fileId);
        when(section.getCounter()).thenReturn(counter);
        when(counter.getId()).thenReturn(counterId);
        when(section.getAuditTimestamps()).thenReturn(auditTimestamp);
        when(auditTimestamp.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(auditTimestamp.getUpdatedAt()).thenReturn(LocalDateTime.now());

        SectionCreateResponse section = spy.createSection(
            userId, req
        );
        assertNotNull(section);
        assertInstanceOf(SectionCreateResponse.class, section);
    }

    @Test 
    public void createSection_createsASection_withProvidedDetails() {
        String name = UUID.randomUUID().toString();
        Long patternId = 1L;
        Long fileId = 2L;

        doReturn(counter).when(spy).createInitialCounter();
        SectionCreateRequest req = new SectionCreateRequest();
        req.setName(name);
        req.setPatternId(patternId);
        req.setFileId(fileId);

        when(mapper.dtoToModel(req)).thenReturn(section);
        when(
            repository.save(section)
        )
        .thenAnswer(
            invocation -> invocation.getArgument(0)
        );
        when(mapper.modelToDto(section)).thenCallRealMethod();
        when(section.getFile()).thenReturn(file);
        when(section.getName()).thenReturn(name);
        when(section.getPatternId()).thenReturn(patternId);
        when(section.getCounter()).thenReturn(counter);
        when(counter.getId()).thenReturn(counterId);
        when(file.getId()).thenReturn(fileId);
        when(section.getAuditTimestamps()).thenReturn(auditTimestamp);
        when(auditTimestamp.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(auditTimestamp.getUpdatedAt()).thenReturn(LocalDateTime.now());

        SectionCreateResponse section = spy.createSection(
            userId, req
        );
        assertEquals(section.getName(), name);
        assertEquals(section.getPatternId(), patternId);
        assertEquals(section.getFileId(), fileId);

        verify(
            repository, 
            times(1)
        ).save(any(Section.class));
    }

    @Test 
    public void createSection_assertsFileIdMatchesDBEntry_andCurrUserHasAccess() {
        String name = UUID.randomUUID().toString();
        Long patternId = 1L;
        Long fileId = 2L;

        doReturn(counter).when(spy).createInitialCounter();
        SectionCreateRequest req = new SectionCreateRequest();
        req.setName(name);
        req.setPatternId(patternId);
        req.setFileId(fileId);

        when(mapper.dtoToModel(req)).thenReturn(section);
        when(
            repository.save(section)
        )
        .thenAnswer(
            invocation -> invocation.getArgument(0)
        );
        when(mapper.modelToDto(section)).thenCallRealMethod();
        when(section.getFile()).thenReturn(file);
        when(section.getName()).thenReturn(name);
        when(section.getPatternId()).thenReturn(patternId);
        when(file.getId()).thenReturn(fileId);
        when(section.getCounter()).thenReturn(counter);
        when(counter.getId()).thenReturn(counterId);
        when(section.getAuditTimestamps()).thenReturn(auditTimestamp);
        when(auditTimestamp.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(auditTimestamp.getUpdatedAt()).thenReturn(LocalDateTime.now());

         SectionCreateResponse section = spy.createSection(
            userId, req
        );
        assertEquals(section.getName(), name);
        assertEquals(section.getPatternId(), patternId);
        assertEquals(section.getFileId(), fileId);

        verify(
            fileService, 
            times(1)
        ).getFileById(userId, fileId);
    }

    @Test 
    public void addFileToSection_setsFileImplOnProvidedSection() {
        Long patternId = 1L;
        when(repository.getById(patternId)).thenReturn(section);
        when(section.getId()).thenReturn(1L);

        service.addFileToSection(patternId, file);

        verify(
            section,
            times(1)
        ).setFile(file);

        verify(
            repository,
            times(1)
        ).save(section);
    }

    @Test 
    public void addFileToSection_throwsAssertionError_whenSectionIdDoesNotExist() {
        Long patternId = 1L;
        when(repository.getById(patternId)).thenReturn(null);

        assertThrows(
            AssertionError.class, 
            () -> service.addFileToSection(patternId, file)
        );

        verify(
            section,
            times(0)
        ).setFile(file);

        verify(
            repository,
            times(0)
        ).save(section);
    }

    @Test 
    public void addFileToSection_throwsAssertionError_whenSectionDoesNotHaveValidId() {
        Long patternId = 1L;
        when(repository.getById(patternId)).thenReturn(section);
        when(section.getId()).thenReturn(0L);

        assertThrows(
            AssertionError.class, 
            () -> service.addFileToSection(patternId, file)
        );

        verify(
            section,
            times(0)
        ).setFile(file);

        verify(
            repository,
            times(0)
        ).save(section);
    }
}
