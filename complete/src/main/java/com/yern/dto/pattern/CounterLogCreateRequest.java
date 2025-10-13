package com.yern.dto.pattern;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yern.dto.messaging.MessagePayload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CounterLogCreateRequest implements MessagePayload {
    private int value; 
    private Long counterId;
    private String externalId;
    // TODO: for now this is limited to sections 
    //       in the future, you could implement some 
    //       kind of Countable interface and use a 
    //       factory or something to produce the repo 
    //       for checking access
    private Long sectionId;
    private Long patternId;
}
