package com.yern.service.pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.yern.model.pattern.Section;
import com.yern.model.storage.FileImpl;
import com.yern.repository.pattern.SectionRepository;


public class SectionServiceTest {
    private SectionRepository repository;
    private SectionService service; 
    private Section section;
    private FileImpl file;

    @BeforeEach 
    public void setup() {
        repository = mock(SectionRepository.class);
        service = new SectionService(repository);
        section = mock(Section.class);
        file = mock(FileImpl.class);
    }

    @Test
    public void createSection_returnsASection() {
        String name = UUID.randomUUID().toString();
        Long patternId = 1L;

        when(repository.save(any(Section.class))).thenReturn(section);
        Section section = service.createSection(name, patternId);
        assertInstanceOf(Section.class, section);
    }

    @Test 
    public void createSection_createsASection_withProvidedDetails() {
        String name = UUID.randomUUID().toString();
        Long patternId = 1L;

        when(
            repository.save(any(Section.class))
        )
        .thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        Section section = service.createSection(name, patternId);

        assertEquals(section.getName(), name);
        assertEquals(section.getPatternId(), patternId);
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
