package org.example.studiopick.web.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.StudioClassRequestDto;
import org.example.studiopick.application.studio.dto.StudioClassResponseDto;
import org.example.studiopick.application.studio.service.StudioClassService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studio/classes")
public class StudioClassController {

    private final StudioClassService studioClassService;

    // 클래스 등록
    @PostMapping
    public ResponseEntity<Long> createClass(@RequestBody StudioClassRequestDto dto) {
        Long classId = studioClassService.createClass(dto);
        return ResponseEntity.ok(classId);
    }

    // 클래스 목록 조회 (스튜디오 ID 기준)
    @GetMapping
    public ResponseEntity<List<StudioClassResponseDto>> getClasses(@RequestParam Long studioId) {
        List<StudioClassResponseDto> classes = studioClassService.getClassesByStudio(studioId);
        return ResponseEntity.ok(classes);
    }

    // 클래스 수정
    @PutMapping("/{classId}")
    public ResponseEntity<Void> updateClass(@PathVariable Long classId, @RequestBody StudioClassRequestDto dto) {
        studioClassService.updateClass(classId, dto);
        return ResponseEntity.ok().build();
    }

    // 클래스 삭제
    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long classId) {
        studioClassService.deleteClass(classId);
        return ResponseEntity.ok().build();
    }
}
