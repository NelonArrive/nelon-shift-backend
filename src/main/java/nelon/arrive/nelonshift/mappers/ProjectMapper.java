package nelon.arrive.nelonshift.mappers;

import nelon.arrive.nelonshift.dto.ProjectDto;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.response.PageResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
	
	ProjectDto toDto(Project project);
	
	Project toEntity(ProjectDto projectDto);
	
}
