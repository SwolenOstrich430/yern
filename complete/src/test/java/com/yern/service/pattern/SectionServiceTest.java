package com.yern.service.pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.yern.model.pattern.Section;
import com.yern.model.storage.FileImpl;
import com.yern.dto.pattern.SectionCreateRequest;
import com.yern.dto.pattern.SectionCreateResponse;
import com.yern.exceptions.NotFoundException;
import com.yern.mapper.pattern.SectionMapper;
import com.yern.repository.pattern.SectionRepository;


public class SectionServiceTest {
    private SectionRepository repository;
    private SectionService service; 
    private SectionMapper mapper;
    private Section section;
    private FileImpl file;

    @BeforeEach 
    public void setup() {
        repository = mock(SectionRepository.class);
        mapper = mock(SectionMapper.class);
        service = new SectionService(repository, mapper);
        section = mock(Section.class);
        file = mock(FileImpl.class);
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

        SectionCreateResponse section = service.createSection(req);
        assertNotNull(section);
        assertInstanceOf(SectionCreateResponse.class, section);
    }

    @Test 
    public void createSection_createsASection_withProvidedDetails() {
        String name = UUID.randomUUID().toString();
        Long patternId = 1L;
        Long fileId = 2L;

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

        SectionCreateResponse section = service.createSection(req);
        assertEquals(section.getName(), name);
        assertEquals(section.getPatternId(), patternId);
        assertEquals(section.getFileId(), fileId);

        verify(
            repository, 
            times(1)
        ).save(any(Section.class));
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
