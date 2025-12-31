package nelon.arrive.nelonshift.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.dto.ProjectDto;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.enums.ProjectStatus;
import nelon.arrive.nelonshift.exception.BadRequestException;
import nelon.arrive.nelonshift.exception.BusinessLogicException;
import nelon.arrive.nelonshift.exception.ResourceNotFoundException;
import nelon.arrive.nelonshift.mappers.ProjectMapper;
import nelon.arrive.nelonshift.repository.ProjectRepository;
import nelon.arrive.nelonshift.request.CreateProjectRequest;
import nelon.arrive.nelonshift.request.UpdateProjectRequest;
import nelon.arrive.nelonshift.response.PageResponse;
import nelon.arrive.nelonshift.services.interfaces.IProjectService;
import nelon.arrive.nelonshift.services.interfaces.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService implements IProjectService {
	
	private final ProjectRepository projectRepository;
	private final IUserService userService;
	private final ProjectMapper projectMapper;
	
	private static final int MAX_NAME_LENGTH = 100;
	private static final List<String> VALID_SORT_FIELDS = Arrays.asList(
		"id", "name", "status", "startDate", "endDate", "createdAt", "updatedAt"
	);
	
	@Override
	@Transactional(readOnly = true)
	public PageResponse<ProjectDto> getProjects(
		String name,
		ProjectStatus status,
		LocalDate startDate,
		LocalDate endDate,
		int page,
		int size,
		String sortBy,
		String sortDirection
	) {
		validateSorting(sortBy, sortDirection);
		
		if (name != null && name.trim().length() > MAX_NAME_LENGTH) {
			throw new BadRequestException("Search name is too long (max " + MAX_NAME_LENGTH + " characters)");
		}
		
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			throw new BadRequestException("Start date cannot be after end date");
		}
		
		if (startDate != null && startDate.isBefore(LocalDate.now().minusYears(10))) {
			throw new BadRequestException("Start date cannot be more than 10 years in the past");
		}
		
		if (endDate != null && endDate.isAfter(LocalDate.now().plusYears(10))) {
			throw new BadRequestException("End date cannot be more than 10 years in the future");
		}
		
		Sort sort = sortDirection.equalsIgnoreCase("desc")
			? Sort.by(sortBy).descending()
			: Sort.by(sortBy).ascending();
		
		Pageable pageable = PageRequest.of(page, size, sort);
		
		Page<Project> projectPage = projectRepository.findByFilters(
			name, status, startDate, endDate, pageable
		);
		
		Page<ProjectDto> projectDtoPage = projectPage.map(projectMapper::toDto);
		return new PageResponse<>(projectDtoPage);
	}
	
	@Override
	@Transactional(readOnly = true)
	public ProjectDto getProjectById(Long id) {
		Project project = projectRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		return projectMapper.toDto(project);
	}
	
	@Override
	public ProjectDto createProject(CreateProjectRequest request) {
		User user = userService.getAuthenticatedUser();
		
		validateProjectDates(request.getStartDate(), request.getEndDate());
		
		if (request.getStartDate() != null && request.getStartDate().isBefore(LocalDate.now())) {
			log.warn("Creating project with start date in the past: {}", request.getStartDate());
		}
		
		Project project = new Project();
		project.setName(request.getName());
		project.setStatus(request.getStatus());
		project.setStartDate(request.getStartDate());
		project.setEndDate(request.getEndDate());
		project.setUser(user);
		
		Project savedProject = projectRepository.save(project);
		
		log.info("Created project with id: {} and name: '{}'", project.getId(), project.getName());
		
		return projectMapper.toDto(savedProject);
	}
	
	@Override
	public ProjectDto updateProject(Long id, UpdateProjectRequest request) {
		Project project = projectRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		
		project.setName(request.getName().trim());
		project.setStatus(request.getStatus());
		LocalDate newStartDate = request.getStartDate() != null ?
			request.getStartDate() : project.getStartDate();
		LocalDate newEndDate = request.getEndDate() != null ?
			request.getEndDate() : project.getEndDate();
		
		validateProjectDates(newStartDate, newEndDate);
		
		if (request.getStartDate() != null || request.getEndDate() != null) {
			validateDateChangeWithShifts(project, newStartDate, newEndDate);
		}
		
		project.setStartDate(request.getStartDate());
		project.setEndDate(request.getEndDate());
		
		Project updatedProject = projectRepository.save(project);
		return projectMapper.toDto(updatedProject);
	}
	
	public void deleteProject(Long id) {
		projectRepository.deleteById(id);
		log.info("Deleted project with id: {}", id);
	}
	
	// ===== ПРИВАТНЫЕ МЕТОДЫ ВАЛИДАЦИИ =====
	
	private void validateProjectDates(LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null) {
			if (startDate.isAfter(endDate)) {
				throw new BadRequestException("Start date cannot be after end date");
			}
			
			if (startDate.plusYears(5).isBefore(endDate)) {
				throw new BadRequestException("Project duration cannot exceed 5 years");
			}
		}
		
		if (startDate != null && startDate.isBefore(LocalDate.now().minusYears(10))) {
			throw new BadRequestException("Start date cannot be more than 10 years in the past");
		}
		
		if (endDate != null && endDate.isAfter(LocalDate.now().plusYears(10))) {
			throw new BadRequestException("End date cannot be more than 10 years in the future");
		}
	}
	
	private void validateSorting(String sortBy, String sortDirection) {
		if (sortBy == null || sortBy.trim().isEmpty()) {
			throw new BadRequestException("Sort field cannot be empty");
		}
		
		if (!VALID_SORT_FIELDS.contains(sortBy)) {
			throw new BadRequestException("Invalid sort field: " + sortBy +
				". Valid fields: " + String.join(", ", VALID_SORT_FIELDS));
		}
		
		if (sortDirection == null || sortDirection.trim().isEmpty()) {
			throw new BadRequestException("Sort direction cannot be empty");
		}
		
		if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
			throw new BadRequestException("Sort direction must be 'asc' or 'desc'");
		}
	}
	
	private void validateDateChangeWithShifts(Project project, LocalDate newStartDate, LocalDate newEndDate) {
		if (project.getShifts().isEmpty()) {
			return;
		}
		
		boolean hasShiftsOutsideRange = project.getShifts().stream()
			.anyMatch(shift -> {
				LocalDate shiftDate = shift.getDate();
				boolean outsideStart = newStartDate != null && shiftDate.isBefore(newStartDate);
				boolean outsideEnd = newEndDate != null && shiftDate.isAfter(newEndDate);
				return outsideStart || outsideEnd;
			});
		
		if (hasShiftsOutsideRange) {
			throw new BusinessLogicException(
				"Cannot change project dates: some shifts fall outside the new date range. " +
					"Update or delete those shifts first."
			);
		}
	}
}
