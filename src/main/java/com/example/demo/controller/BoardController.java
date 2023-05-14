package com.example.demo.controller;

import com.example.demo.dto.BoardDTO;
import com.example.demo.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/create")
    public String createGet(Model model, BoardDTO boardDTO) {
        model.addAttribute("boardDTO", boardDTO);
        return "create";
    }

    @PostMapping("/create")
    public String createPost(BoardDTO boardDTO) {
        this.boardService.create(boardDTO);

        return "redirect:/list";
    }

    @GetMapping("/read")
    public String readGet(Model model, BoardDTO boardDTO) throws RuntimeException {
        try {
            BoardDTO board = this.boardService.read(boardDTO);
            List<BoardDTO> relateBoard = this.boardService.readRelateBoard(board);
            model.addAttribute("board", board);
            model.addAttribute("relateBoard", relateBoard);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "read";
    }

    @GetMapping("/list")
    public String listGet(Model model) {
        List<BoardDTO> boardDTOList = this.boardService.getList();
        model.addAttribute("boardList", boardDTOList);

        return "list";
    }
}
