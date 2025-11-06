package nelon.arrive.nelonshift.service;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.PageResponse;
import nelon.arrive.nelonshift.dto.ProjectDTO;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Project.ProjectStatus;
import nelon.arrive.nelonshift.exception.AlreadyExistsException;
import nelon.arrive.nelonshift.exception.BadRequestException;
import nelon.arrive.nelonshift.exception.ResourceNotFoundException;
import nelon.arrive.nelonshift.repository.ProjectRepository;
import nelon.arrive.nelonshift.service.interfaces.IProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {
	private final ProjectRepository projectRepository;
	
	@Transactional(readOnly = true)
	public PageResponse<ProjectDTO> getProjects(
		String name,
		ProjectStatus status,
		LocalDate startDate,
		LocalDate endDate,
		int page,
		int size,
		String sortBy,
		String sortDirection
	) {
		if (page < 0) {
			throw new BadRequestException("Page number cannot be negative");
		}
		if (size <= 0 || size > 100) {
			throw new BadRequestException("Page size must be between 1 and 100");
		}
		
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			throw new BadRequestException("Start date cannot be after end date");
		}
		
		Sort sort = sortDirection.equalsIgnoreCase("desc")
			? Sort.by(sortBy).descending()
			: Sort.by(sortBy).ascending();
		
		Pageable pageable = PageRequest.of(page, size, sort);
		
		Page<Project> projectPage = projectRepository.findByFilters(
			name, status, startDate, endDate, pageable
		);
		
		Page<ProjectDTO> dtoPage = projectPage.map(ProjectDTO::new);
		return new PageResponse<>(dtoPage);
	}
	
	@Transactional(readOnly = true)
	public ProjectDTO getProjectById(Long id) {
		Project project = projectRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
		return new ProjectDTO(project);
	}
	
	@Transactional
	public ProjectDTO createProject(Project project) {
		if (project.getName() == null || project.getName().trim().isEmpty()) {
			throw new BadRequestException("Project name cannot be empty");
		}
		
		boolean exists = projectRepository.existsByName(project.getName());
		if (exists) {
			throw new AlreadyExistsException("Already exist project by name");
		}
		
		if (project.getStartDate() != null && project.getEndDate() != null) {
			if (project.getStartDate().isAfter(project.getEndDate())) {
				throw new BadRequestException("Start date cannot be after end date");
			}
		}
		
		Project savedProject = projectRepository.save(project);
		return new ProjectDTO(savedProject);
	}
	
	@Transactional
	public ProjectDTO updateProject(Long id, Project projectDetails) {
		Project project = projectRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
		
		if (projectDetails.getName() != null && projectDetails.getName().trim().isEmpty()) {
			throw new BadRequestException("Project name cannot be empty");
		}
		
		if (projectDetails.getStartDate() != null && projectDetails.getEndDate() != null) {
			if (projectDetails.getStartDate().isAfter(projectDetails.getEndDate())) {
				throw new BadRequestException("Start date cannot be after end date");
			}
		}
		
		if (projectDetails.getName() != null) {
			project.setName(projectDetails.getName());
		}
		if (projectDetails.getStatus() != null) {
			project.setStatus(projectDetails.getStatus());
		}
		if (projectDetails.getStartDate() != null) {
			project.setStartDate(projectDetails.getStartDate());
		}
		if (projectDetails.getEndDate() != null) {
			project.setEndDate(projectDetails.getEndDate());
		}
		
		Project updatedProject = projectRepository.save(project);
		return new ProjectDTO(updatedProject);
	}
	
	@Transactional
	public void deleteProject(Long id) {
		Project project = projectRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
		
		if (!project.getShifts().isEmpty()) {
			throw new BadRequestException(
				"Cannot delete project with existing shifts. Delete shifts first."
			);
		}
		
		projectRepository.deleteById(id);
	}
}
