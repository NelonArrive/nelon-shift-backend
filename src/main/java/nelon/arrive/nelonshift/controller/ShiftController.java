package nelon.arrive.nelonshift.controller;


import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.ShiftDto;
import nelon.arrive.nelonshift.entity.Shift;
import nelon.arrive.nelonshift.mappers.ShiftMapper;
import nelon.arrive.nelonshift.request.CreateShiftRequest;
import nelon.arrive.nelonshift.request.UpdateShiftRequest;
import nelon.arrive.nelonshift.response.ApiResponse;
import nelon.arrive.nelonshift.services.ShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/${api.prefix}/shifts")
@RequiredArgsConstructor
public class ShiftController {
	
	private final ShiftService shiftService;
	
	@GetMapping
	public ResponseEntity<ApiResponse> getShifts(@RequestParam() Long projectId) {
		List<ShiftDto> shiftDtos = shiftService.getShiftsByProjectId(projectId);
		return ResponseEntity.ok(new ApiResponse("Success", shiftDtos));
	}
	
	@PostMapping
	public ResponseEntity<ApiResponse> createShift(
		@RequestParam Long projectId,
		@RequestBody CreateShiftRequest request
	) {
		ShiftDto shiftDto = shiftService.createShift(projectId, request);
		return ResponseEntity.status(CREATED).body(new ApiResponse("Create shift successfully", shiftDto));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse> updateShift(
		@PathVariable Long id,
		@RequestBody UpdateShiftRequest request
	) {
		ShiftDto shiftDto = shiftService.updateShift(id, request);
		return ResponseEntity.ok(new ApiResponse("Update shift successfully", shiftDto));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> deleteShift(@PathVariable Long id) {
		shiftService.deleteShift(id);
		return ResponseEntity.ok(new ApiResponse("Delete shift successfully", null));
	}
}
