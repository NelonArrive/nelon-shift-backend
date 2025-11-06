package nelon.arrive.nelonshift.service.interfaces;

import nelon.arrive.nelonshift.dto.PageResponse;
import nelon.arrive.nelonshift.dto.ShiftDTO;
import nelon.arrive.nelonshift.entity.Shift;

import java.time.LocalDate;

public interface IShiftService {
	public PageResponse<ShiftDTO> getShifts(
		Long projectId,
		LocalDate startDate,
		LocalDate endDate,
		Integer minHours,
		int page,
		int size,
		String sortBy,
		String sortDirection
	);
	
	ShiftDTO getShiftById(Long id);
	
	ShiftDTO createShift(Long projectId, Shift shift);
	
	ShiftDTO updateShift(Long id, Shift shiftDetails);
	
	void deleteShift(Long id);
}
