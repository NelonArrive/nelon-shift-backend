package nelon.arrive.nelonshift.service;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.PageResponse;
import nelon.arrive.nelonshift.dto.ShiftDTO;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.entity.Shift;
import nelon.arrive.nelonshift.repository.ProjectRepository;
import nelon.arrive.nelonshift.repository.ShiftRepository;
import nelon.arrive.nelonshift.service.interfaces.IShiftService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ShiftService implements IShiftService {
	private final ShiftRepository shiftRepository;
	private final ProjectRepository projectRepository;
	
	@Transactional(readOnly = true)
	public PageResponse<ShiftDTO> getShifts(
		Long projectId,
		LocalDate startDate,
		LocalDate endDate,
		Integer minHours,
		int page,
		int size,
		String sortBy,
		String sortDirection
	) {
		Sort sort = sortDirection.equalsIgnoreCase("desc")
			? Sort.by(sortBy).descending()
			: Sort.by(sortBy).ascending();
		
		Pageable pageable = PageRequest.of(page, size, sort);
		
		Page<Shift> shiftPage = shiftRepository.findByFilters(
			projectId, startDate, endDate, minHours, pageable
		);
		
		Page<ShiftDTO> dtoPage = shiftPage.map(ShiftDTO::new);
		return new PageResponse<>(dtoPage);
	}
	
	@Transactional(readOnly = true)
	public ShiftDTO getShiftById(Long id) {
		Shift shift = shiftRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Shift not found with id: " + id));
		return new ShiftDTO(shift);
	}
	
	@Transactional
	public ShiftDTO createShift(Long projectId, Shift shift) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new RuntimeException("Project not found!"));
		
		shift.setProject(project);
		Shift savedShift = shiftRepository.save(shift);
		return new ShiftDTO(savedShift);
	}
	
	public ShiftDTO updateShift(Long id, Shift shiftDetails) {
		Shift shift = shiftRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Shift not found with id: " + id));
		
		shift.setDate(shiftDetails.getDate());
		shift.setStartTime(shiftDetails.getStartTime());
		shift.setEndTime(shiftDetails.getEndTime());
		shift.setHours(shiftDetails.getHours());
		shift.setBasePay(shiftDetails.getBasePay());
		shift.setOvertimeHours(shiftDetails.getOvertimeHours());
		shift.setOvertimePay(shiftDetails.getOvertimePay());
		shift.setPerDiem(shiftDetails.getPerDiem());
		
		Shift updatedShift = shiftRepository.save(shift);
		return new ShiftDTO(updatedShift);
	}
	
	public void deleteShift(Long id) {
		if (!shiftRepository.existsById(id)) {
			throw new RuntimeException("Shift not found with id: " + id);
		}
		shiftRepository.deleteById(id);
	}
}
