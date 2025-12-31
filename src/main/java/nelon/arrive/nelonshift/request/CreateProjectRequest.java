package nelon.arrive.nelonshift.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import nelon.arrive.nelonshift.enums.ProjectStatus;

import java.time.LocalDate;

@Data
public class CreateProjectRequest {
	@NotBlank(message = "Project name is required")
	@Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
	private String name;
	
	@NotNull(message = "Project status is required")
	private ProjectStatus status;
	
	private LocalDate startDate;
	
	private LocalDate endDate;
}
