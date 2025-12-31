package nelon.arrive.nelonshift.mappers;

import nelon.arrive.nelonshift.dto.ShiftDto;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Shift;
import nelon.arrive.nelonshift.request.CreateShiftRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
	
	ShiftDto toShiftDto(Shift shift);
	
	Shift toEntity(ShiftDto shiftDto);
	
	List<ShiftDto> toDtoList(List<Shift> shifts);
	
	Shift toEntity(CreateShiftRequest request, Project project);
}
