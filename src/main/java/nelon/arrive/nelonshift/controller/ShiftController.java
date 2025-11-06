package nelon.arrive.nelonshift.controller;


import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.PageResponse;
import nelon.arrive.nelonshift.dto.ShiftDTO;
import nelon.arrive.nelonshift.entity.Shift;
import nelon.arrive.nelonshift.service.ShiftService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("${api.prefix}/shifts")
@RequiredArgsConstructor
public class ShiftController {
	private final ShiftService shiftService;
	
	@GetMapping
	public ResponseEntity<PageResponse<ShiftDTO>> getShifts(
		@RequestParam(required = false) Long projectId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
		@RequestParam(required = false) Integer minHours,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "date") String sortBy,
		@RequestParam(defaultValue = "desc") String sortDirection
		) {
		PageResponse<ShiftDTO> response = shiftService.getShifts(
			projectId, startDate, endDate, minHours, page, size, sortBy, sortDirection
		);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ShiftDTO> getShiftById(
		@PathVariable Long id
	) {
		ShiftDTO shift = shiftService.getShiftById(id);
		return ResponseEntity.ok(shift);
	}
	
	@PostMapping
	public ResponseEntity<ShiftDTO> createShift(
		@RequestParam Long projectId,
		@RequestBody Shift shift
		) {
		ShiftDTO updatedShift = shiftService.createShift(projectId, shift);
		return ResponseEntity.status(HttpStatus.CREATED).body(updatedShift);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ShiftDTO> updateShift(
		@PathVariable Long id,
		@RequestBody Shift shift
	) {
		ShiftDTO updatedShift = shiftService.updateShift(id, shift);
		return ResponseEntity.ok(updatedShift);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
		shiftService.deleteShift(id);
		return ResponseEntity.noContent().build();
	}
}
