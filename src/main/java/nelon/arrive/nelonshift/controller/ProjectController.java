package nelon.arrive.nelonshift.controller;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.ProjectDto;
import nelon.arrive.nelonshift.enums.ProjectStatus;
import nelon.arrive.nelonshift.request.CreateProjectRequest;
import nelon.arrive.nelonshift.request.UpdateProjectRequest;
import nelon.arrive.nelonshift.response.ApiResponse;
import nelon.arrive.nelonshift.response.PageResponse;
import nelon.arrive.nelonshift.services.interfaces.IProjectService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/${api.prefix}/projects")
@RequiredArgsConstructor
public class ProjectController {
	
	private final IProjectService projectService;
	
	@GetMapping
	public ResponseEntity<ApiResponse> getProjects(
		@RequestParam(required = false) String name,
		@RequestParam(required = false) ProjectStatus status,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "id") String sortBy,
		@RequestParam(defaultValue = "asc") String sortDirection
	) {
		PageResponse<ProjectDto> projectDtos = projectService.getProjects(
			name, status, startDate, endDate, page, size, sortBy, sortDirection
		);
		return ResponseEntity.ok(new ApiResponse("Success", projectDtos));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse> getProjectById(@PathVariable Long id) {
		ProjectDto projectDto = projectService.getProjectById(id);
		return ResponseEntity.ok(new ApiResponse("Success", projectDto));
	}
	
	@PostMapping
	public ResponseEntity<ApiResponse> createProject(@RequestBody CreateProjectRequest request) {
		ProjectDto projectDto = projectService.createProject(request);
		return ResponseEntity.status(CREATED).body(new ApiResponse("Create project successfully", projectDto));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse> updateProject(
		@PathVariable Long id,
		@RequestBody UpdateProjectRequest request
	) {
		ProjectDto projectDto = projectService.updateProject(id, request);
		return ResponseEntity.ok(new ApiResponse("Update project successfully", projectDto));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> deleteProject(@PathVariable Long id){
		projectService.deleteProject(id);
		return ResponseEntity.ok(new ApiResponse("Delete project successfully", null));
	}
}
