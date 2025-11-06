package nelon.arrive.nelonshift.service;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.PageResponse;
import nelon.arrive.nelonshift.dto.ProjectDTO;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Project.ProjectStatus;
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
			.orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
		return new ProjectDTO(project);
	}
	
	@Transactional
	public ProjectDTO createProject(Project project) {
		Project savedProject = projectRepository.save(project);
		return new ProjectDTO(savedProject);
	}
	
	@Transactional
	public ProjectDTO updateProject(Long id, Project projectDetails) {
		Project project = projectRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
		
		project.setName(projectDetails.getName());
		project.setStatus(projectDetails.getStatus());
		project.setStartDate(projectDetails.getStartDate());
		project.setEndDate(projectDetails.getEndDate());
		
		Project updatedProject = projectRepository.save(project);
		return new ProjectDTO(updatedProject);
	}
	
	@Transactional
	public void deleteProject(Long id) {
		if (!projectRepository.existsById(id)) {
			throw new RuntimeException("Project not found with id: " + id);
		}
		projectRepository.deleteById(id);
	}
}
