package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BoardDTO {
    private Integer boardId;
    private String boardTitle;
    private String boardContent;
    private LocalDateTime createdDate;
    private List<String> relateWordList;
}
