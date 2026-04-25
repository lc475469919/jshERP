package com.yize.erp.system;

import com.yize.erp.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class HealthController {
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of(
                "name", "yize-erp",
                "status", "UP",
                "time", LocalDateTime.now()
        ));
    }
}
