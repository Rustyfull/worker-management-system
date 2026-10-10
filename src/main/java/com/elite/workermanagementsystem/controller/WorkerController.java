package com.elite.workermanagementsystem.controller;


import com.elite.workermanagementsystem.dto.CreateWorkerRequest;
import com.elite.workermanagementsystem.dto.WorkerResponse;
import com.elite.workermanagementsystem.service.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<WorkerResponse> createWorker(@RequestBody @Valid CreateWorkerRequest workerRequest) {
        return ResponseEntity.ok(workerService.addWorker(workerRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkerResponse> getWorkerById(@PathVariable("id") String id) {
        return ResponseEntity.ok(workerService.getWorkerById(id));
    }


    @GetMapping
    public ResponseEntity<List<WorkerResponse>> getAllWorkers() {
        return ResponseEntity.ok(workerService.getAllWorkers());
    }


    @PutMapping("/{id}")
    public ResponseEntity<WorkerResponse> updateWorker(@PathVariable("id") String id, @RequestBody @Valid CreateWorkerRequest workerRequest) {
        return ResponseEntity.ok(workerService.updateWorker(id, workerRequest));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteWorker(@PathVariable("id") String id) {
        workerService.deleteWorkerById(id);
        return ResponseEntity.ok(Map.of("message", "Worker deleted successfully"));
    }

}

