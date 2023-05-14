package com.example.demo.dao;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "board")
@EntityListeners(AuditingEntityListener.class)
public class BoardDAO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer boardId;

    @Column(length = 200)
    private String boardTitle;

    @Column(columnDefinition = "TEXT")
    private String boardContent;

    @CreatedDate
    @Column
    private LocalDateTime createdDate;

    @Column
    @ElementCollection
    private List<String> relateWordList;

    @Builder
    public BoardDAO(String boardTitle, String boardContent,
                    LocalDateTime createdDate, List<String> relateWordList) {
        this.boardTitle = boardTitle;
        this.boardContent = boardContent;
        this.createdDate = createdDate;
        this.relateWordList = relateWordList;
    }
}
