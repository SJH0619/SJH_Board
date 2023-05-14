package com.example.demo.service;

import com.example.demo.dao.BoardDAO;
import com.example.demo.dto.BoardDTO;
import com.example.demo.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public void create(BoardDTO boardDTO) {
        // 생성한 게시글 내에서 40%이하의 빈도로 사용되었으며, 전체 게시글에서 60% 이상이 사용하고있는 자주쓰이는 단어가 아닌 단어 리스트
        List<String> relateWordList = defineRelateWord(boardDTO);

        BoardDAO boardDAO = BoardDAO.builder()
                .boardTitle(boardDTO.getBoardTitle())
                .boardContent(boardDTO.getBoardContent())
                .createdDate(LocalDateTime.now())
                .relateWordList(relateWordList)
                .build();

        this.boardRepository.save(boardDAO);
    }

    public BoardDTO read(BoardDTO boardDTO) throws RuntimeException {
        Optional<BoardDAO> optBoardDAO = this.boardRepository.findById(boardDTO.getBoardId());
        if (optBoardDAO.isPresent()) {
            BoardDTO returnBoardDTO = new BoardDTO();
            returnBoardDTO.setBoardId(optBoardDAO.get().getBoardId());
            returnBoardDTO.setBoardTitle(optBoardDAO.get().getBoardTitle());
            returnBoardDTO.setBoardContent(optBoardDAO.get().getBoardContent());
            returnBoardDTO.setCreatedDate(optBoardDAO.get().getCreatedDate());
            returnBoardDTO.setRelateWordList(optBoardDAO.get().getRelateWordList());

            return returnBoardDTO;
        } else throw new RuntimeException("데이터가 존재하지 않습니다.");
    }

    public List<BoardDTO> readRelateBoard(BoardDTO boardDTO) {
        // 연관 단어 가져오기
        List<String> relateWordList = boardDTO.getRelateWordList();
        // 연관 단어가 들어가있는 게시글 리스트가 들어갈 리스트
        List<List<BoardDAO>> searchResultList = new ArrayList<>();
        // 연관 단어가 1개여서 삭제되어야할 게시글 리스트
        List<BoardDAO> removeResultList = new ArrayList<>();
        List<BoardDTO> returnList = new ArrayList<>();

        // 연관 단어 개수를 체크하기 위한 Map
        Map<BoardDAO, Integer> getHighScore = new HashMap<>();

        // 연관 단어로 지정된게 1개 뿐이라면 아무것도 반환하지 않음
        if (relateWordList.size() < 2) {
            return new ArrayList<>();
        } else {
            // 각 연관 단어가 들어가는 모든 게시글을 찾음
            for (String relateWord : relateWordList) {
                // 리스트로 더함
                searchResultList.add(this.boardRepository.findByBoardContentIsContaining(relateWord));
            }

            for (List<BoardDAO> boardDAOList : searchResultList) {
                for (BoardDAO boardDAO : boardDAOList) {
                    // 각각 1개씩 체크하며 1씩 더해줌
                    getHighScore.put(boardDAO, getHighScore.getOrDefault(boardDAO, 0) + 1);
                }
            }

            // Map을 Key단위로 반복하며 그 값이 2보다 작은 경우 연관 단어가 1개 뿐이므로 키(DAO)를 제거할 DAO리스트에 추가
            for (BoardDAO boardDAO : getHighScore.keySet()) {
                if (getHighScore.get(boardDAO) < 2) {
                    removeResultList.add(boardDAO);
                }
            }

            // 실제 제거할 DAO리스트를 통해 Map에서 해당하는 키를 제거
            for (BoardDAO boardDAO : removeResultList) {
                getHighScore.remove(boardDAO);
            }

            // Value를 기준으로 정렬하기 위한 Map.Entry 리스트 작성
            List<Map.Entry<BoardDAO, Integer>> entryList = new ArrayList<>(getHighScore.entrySet());

            // Value를 기준으로 내림차순 정렬
            entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            // 내림차순 기준으로 정렬된 BoardDAO를 DTO로 변환하여 반환할 연관게시글 리스트에 순차 추가
            for (Map.Entry<BoardDAO, Integer> entry : entryList) {
                if (!boardDTO.getBoardId().equals(entry.getKey().getBoardId())) {
                    BoardDTO anotherBoardDTO = new BoardDTO();

                    anotherBoardDTO.setBoardId(entry.getKey().getBoardId());
                    anotherBoardDTO.setBoardTitle(entry.getKey().getBoardTitle());
                    anotherBoardDTO.setBoardContent(entry.getKey().getBoardContent());
                    anotherBoardDTO.setCreatedDate(entry.getKey().getCreatedDate());
                    anotherBoardDTO.setRelateWordList(entry.getKey().getRelateWordList());
                    returnList.add(anotherBoardDTO);
                }
            }

            return returnList;
        }
    }

    public List<BoardDTO> getList() {
        List<BoardDAO> boardDAOList = this.boardRepository.findAll(Sort.by(Sort.Direction.DESC, "boardId"));
        List<BoardDTO> boardDTOList = new ArrayList<>();

        for(BoardDAO boardDAO : boardDAOList) {
            BoardDTO boardDTO = new BoardDTO();
            boardDTO.setBoardId(boardDAO.getBoardId());
            boardDTO.setBoardTitle(boardDAO.getBoardTitle());
            boardDTO.setBoardContent(boardDAO.getBoardContent());
            boardDTO.setCreatedDate(boardDAO.getCreatedDate());

            boardDTOList.add(boardDTO);
        }

        return boardDTOList;
    }

    private double rateCheck(List<String> doc, String term) {
        double result = 0;
        for (String word : doc) {
            if (term.equalsIgnoreCase(word))
                result++;
        }
        return result / doc.size();
    }

    private List<String> defineRelateWord(BoardDTO boardDTO) {
        // 만든 게시글의 내용을 공백문자열(띄어쓰기, 줄바꿈) 기준으로 분리
        List<String> splitWordList = Arrays.asList(boardDTO.getBoardContent().split("\\s+"));

        // 분리된 리스트의 중복이 제거된 리스트
        List<String> dedupeSplitWordList = new ArrayList<>(splitWordList.stream().distinct().toList());

        // 조건에 부합하지 않은 단어를 제거하기위한 리스트
        List<String> removeWordList = new ArrayList<>();

        // 전체 게시글에서 60% 이상 사용된 것들 필터링하기 위한 작업
        // 전체 게시글 수
        int totalCount = this.boardRepository.countAllBy().intValue();

        for (String dedupeSplitWord : dedupeSplitWordList) {
            // 해당 단어를 가진 모든 게시글의 수
            int targetCount = this.boardRepository.countAllByBoardContentContaining(dedupeSplitWord).intValue();
            if (((double) targetCount / totalCount) * 100 > 60.0) {
                removeWordList.add(dedupeSplitWord);
            }
        }

        // 해당 게시글에서 40%이하로 사용된 단어가 아니면 제거 예정 리스트에 추가
        for (String dedupeSplitWord : dedupeSplitWordList) {
            if (rateCheck(splitWordList, dedupeSplitWord) * 100 > 40.0) {
                removeWordList.add(dedupeSplitWord);
            }
        }

        // 제거 단어 리스트에서 중복 제거
        removeWordList = removeWordList.stream().distinct().toList();

        // 중복 제거된 리스트에서 제거목록에 올라온 단어를 제거
        for (String removeWord : removeWordList) {
            dedupeSplitWordList.remove(removeWord);
        }

        // 제거 이후 남은 연관 단어 리스트를 반환
        return dedupeSplitWordList;
    }
}
