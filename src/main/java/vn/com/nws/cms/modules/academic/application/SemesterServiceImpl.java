package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.model.Semester;
import vn.com.nws.cms.modules.academic.domain.repository.SemesterRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SemesterServiceImpl implements SemesterService {

    private final SemesterRepository semesterRepository;

    @Override
    public PageResponse<SemesterResponse> getSemesters(SemesterFilterRequest request) {
        List<Semester> semesters = semesterRepository.search(request.getKeyword(), request.getActive(), request.getPage(), request.getSize());
        long totalElements = semesterRepository.count(request.getKeyword(), request.getActive());
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        List<SemesterResponse> responses = semesters.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<SemesterResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .data(responses)
                .build();
    }

    @Override
    public SemesterResponse getSemesterById(Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Semester not found"));
        return toResponse(semester);
    }

    @Override
    @Transactional
    public SemesterResponse createSemester(SemesterCreateRequest request) {
        if (semesterRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Semester code already exists");
        }

        if (request.isActive()) {
            deactivateAllSemesters();
        }

        Semester semester = Semester.builder()
                .name(request.getName())
                .code(request.getCode())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.isActive())
                .build();

        semester = semesterRepository.save(semester);
        return toResponse(semester);
    }

    @Override
    @Transactional
    public SemesterResponse updateSemester(Long id, SemesterUpdateRequest request) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Semester not found"));

        if (request.getCode() != null && !request.getCode().equals(semester.getCode())) {
            if (semesterRepository.existsByCode(request.getCode())) {
                throw new BusinessException("Semester code already exists");
            }
            semester.setCode(request.getCode());
        }

        if (request.getName() != null) semester.setName(request.getName());
        if (request.getStartDate() != null) semester.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) semester.setEndDate(request.getEndDate());
        
        if (request.getActive() != null) {
            if (request.getActive() && !semester.isActive()) {
                deactivateAllSemesters();
            }
            semester.setActive(request.getActive());
        }

        semester = semesterRepository.save(semester);
        return toResponse(semester);
    }

    @Override
    @Transactional
    public void deleteSemester(Long id) {
        if (!semesterRepository.findById(id).isPresent()) {
            throw new BusinessException("Semester not found");
        }
        semesterRepository.deleteById(id);
    }

    @Override
    public SemesterResponse getActiveSemester() {
        Semester semester = semesterRepository.findActiveSemester()
                .orElseThrow(() -> new BusinessException("No active semester found"));
        return toResponse(semester);
    }

    private void deactivateAllSemesters() {
        semesterRepository.findActiveSemester().ifPresent(s -> {
            s.setActive(false);
            semesterRepository.save(s);
        });
    }

    private SemesterResponse toResponse(Semester semester) {
        return SemesterResponse.builder()
                .id(semester.getId())
                .name(semester.getName())
                .code(semester.getCode())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .active(semester.isActive())
                .createdAt(semester.getCreatedAt())
                .updatedAt(semester.getUpdatedAt())
                .build();
    }
}
