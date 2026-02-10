package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.model.Subject;
import vn.com.nws.cms.modules.academic.domain.repository.SubjectRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    public PageResponse<SubjectResponse> getSubjects(SubjectFilterRequest request) {
        List<Subject> subjects = subjectRepository.search(request.getKeyword(), request.getActive(), request.getPage(), request.getSize());
        long totalElements = subjectRepository.count(request.getKeyword(), request.getActive());
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        List<SubjectResponse> responses = subjects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<SubjectResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .data(responses)
                .build();
    }

    @Override
    public SubjectResponse getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Subject not found"));
        return toResponse(subject);
    }

    @Override
    @Transactional
    public SubjectResponse createSubject(SubjectCreateRequest request) {
        if (subjectRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Subject code already exists");
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .code(request.getCode())
                .credit(request.getCredit())
                .description(request.getDescription())
                .active(request.isActive())
                .build();

        subject = subjectRepository.save(subject);
        return toResponse(subject);
    }

    @Override
    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectUpdateRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Subject not found"));

        if (request.getCode() != null && !request.getCode().equals(subject.getCode())) {
            if (subjectRepository.existsByCode(request.getCode())) {
                throw new BusinessException("Subject code already exists");
            }
            subject.setCode(request.getCode());
        }

        if (request.getName() != null) subject.setName(request.getName());
        if (request.getCredit() != null) subject.setCredit(request.getCredit());
        if (request.getDescription() != null) subject.setDescription(request.getDescription());
        if (request.getActive() != null) subject.setActive(request.getActive());

        subject = subjectRepository.save(subject);
        return toResponse(subject);
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        if (!subjectRepository.findById(id).isPresent()) {
            throw new BusinessException("Subject not found");
        }
        subjectRepository.deleteById(id);
    }

    private SubjectResponse toResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .code(subject.getCode())
                .credit(subject.getCredit())
                .description(subject.getDescription())
                .active(subject.isActive())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }
}
