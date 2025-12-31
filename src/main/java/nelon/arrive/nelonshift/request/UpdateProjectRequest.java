package nelon.arrive.nelonshift.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import nelon.arrive.nelonshift.enums.ProjectStatus;

import java.time.LocalDate;

@Data
public class UpdateProjectRequest {
	@Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
	private String name;
	private ProjectStatus status;
	private LocalDate startDate;
	private LocalDate endDate;
}
