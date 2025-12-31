package nelon.arrive.nelonshift.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserDto {
	private UUID id;
	private String name;
	private String email;
	private List<ProjectDto> projects;
}
