package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Attendance;
import com.csims.security.CustomUserDetails;
import com.csims.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ATTENDANCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendance(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer workerId) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getAttendance(projectId, date, workerId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ATTENDANCE_RECORD') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> recordAttendance(
            @RequestBody Attendance attendance,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        attendanceService.recordAttendance(attendance, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Attendance recorded successfully", null));
    }

    @PatchMapping("/{id}/correct")
    @PreAuthorize("hasAuthority('ATTENDANCE_CORRECT') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> correctAttendance(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String status = body.get("status");
        String remarks = body.get("remarks");
        attendanceService.correctAttendance(id, status, remarks, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Attendance record corrected", null));
    }
}
