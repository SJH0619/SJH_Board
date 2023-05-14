package com.example.demo.repository;

import com.example.demo.dao.BoardDAO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardDAO, Integer> {
    Long countAllBy();
    Long countAllByBoardContentContaining(String word);
    List<BoardDAO> findByBoardContentIsContaining(String word);
}
