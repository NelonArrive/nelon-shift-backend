package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Shift;
import nelon.arrive.nelonshift.request.CreateShiftRequest;
import nelon.arrive.nelonshift.request.UpdateShiftRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IShiftService {
	List<Shift> getShiftsByProjectId(Long projectId);
	
	Shift createShift(Long projectId, CreateShiftRequest shift);
	
	Shift updateShift(Long id, UpdateShiftRequest shiftDetails);
	
	void deleteShift(Long id);
	
	void validateShiftCreate(CreateShiftRequest request);
	
	void validateShiftUpdate(UpdateShiftRequest request);
	
	void validateShiftDateAgainstProject(LocalDate shiftDate, Project project);
	
	long calculateHoursBetween(LocalTime start, LocalTime end);
}
