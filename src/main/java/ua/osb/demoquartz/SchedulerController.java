package ua.osb.demoquartz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/app/scheduler")
public class SchedulerController {
    private final SchedulerService schedulerService;

    @Autowired
    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/reschedule")
    public ResponseEntity rescheduleJob(@RequestBody RescheduleRequest request) {
        validateRequest(request);
        schedulerService.rescheduleJob(request.triggerName, request.triggerGroup, request.newInterval);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop")
    public ResponseEntity stopJob(@RequestBody RescheduleRequest request) {
        schedulerService.stopJob(request.triggerName, request.triggerGroup);
        return ResponseEntity.ok().build();
    }

    private void validateRequest(RescheduleRequest request) {
        final int newInterval = request.getNewInterval();
        if (newInterval < 0) {
            throw new BadRequestException(Collections.singletonList("interval=" + newInterval));
        }
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handle(BadRequestException e) {
        return ResponseEntity.badRequest().body(e.getErrorMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity handleRuntime(RuntimeException e) {
        return ResponseEntity.status(500).body(String.format("%s\nStack Trace:\n%s",
                e.getMessage(), Arrays.toString(e.getStackTrace())));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RescheduleRequest {
        private String triggerName;
        private String triggerGroup;
        private int newInterval;
    }

    private class BadRequestException extends RuntimeException {
        private List<String> failedParameters;

        BadRequestException(List<String> failedParameters) {
            this.failedParameters = failedParameters;
        }

        String getErrorMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed fields:");
            failedParameters.forEach(param -> sb.append(" ").append(param));
            return sb.toString();
        }
    }
}
