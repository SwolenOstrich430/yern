package com.yern.controller.pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yern.dto.pattern.SectionCreateResponse;
import com.yern.model.user.User;
import com.google.rpc.context.AttributeContext.Response;
import com.yern.dto.pattern.PatternCreateRequest;
import com.yern.dto.pattern.PatternCreateResponse;
import com.yern.dto.pattern.SectionCreateRequest;
import com.yern.service.pattern.PatternService;
import com.yern.service.user.UserService;
import com.yern.exceptions.AccessDeniedException;


@RestController
@RequestMapping("${api.patterns-endpoint}")
public class PatternController {
    private PatternService patternService;
    private UserService userService;

    public PatternController(
        @Autowired PatternService patternService,
        @Autowired UserService userService
    ) {
        this.patternService = patternService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<PatternCreateResponse> createPattern (
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody PatternCreateRequest req
    ) {
        User user = getUserFromDetails(userDetails);
        
        return ResponseEntity.ok(
            patternService.createPattern(
                user.getId(),
                req
            )
        );
    }
    
    @PostMapping("/sections/create")
    public ResponseEntity<SectionCreateResponse> createSection(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody SectionCreateRequest req
    ) {
        User user = getUserFromDetails(userDetails);
        
        return ResponseEntity.ok(
            patternService.addSection(user.getId(), req)
        );
    }

    public User getUserFromDetails(
        UserDetails userDetails
    ) throws AccessDeniedException {
        User user = userService.getUserByEmail(
            userDetails.getUsername()
        );

        if (user == null) {
            throw new AccessDeniedException(null);
        }

        return user;
    }
}
