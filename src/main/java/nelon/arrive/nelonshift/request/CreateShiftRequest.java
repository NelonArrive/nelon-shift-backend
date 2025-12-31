package nelon.arrive.nelonshift.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nelon.arrive.nelonshift.entity.Project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateShiftRequest {
	@NotNull(message = "Project ID is required")
	private Long projectId;
	
	@NotNull(message = "Date is required")
	private LocalDate date;
	
	private LocalTime startTime;
	
	private LocalTime endTime;
	
	@NotNull(message = "Hours are required")
	@Min(value = 0, message = "Hours must be at least 0")
	@Max(value = 24, message = "Hours cannot exceed 24")
	private Integer hours;
	
	@DecimalMin(value = "0.0", inclusive = true, message = "Base pay must be positive")
	private BigDecimal basePay;
	
	@Min(value = 0, message = "Overtime hours must be at least 0")
	private Integer overtimeHours;
	
	@DecimalMin(value = "0.0", inclusive = true, message = "Overtime pay must be positive")
	private BigDecimal overtimePay;
	
	@DecimalMin(value = "0.0", inclusive = true, message = "Per diem must be positive")
	private BigDecimal perDiem;
	
	private Project project;
}
