package com.elite.workermanagementsystem.mapper;

import com.elite.workermanagementsystem.dto.CreateWorkerRequest;
import com.elite.workermanagementsystem.dto.WorkerResponse;
import com.elite.workermanagementsystem.model.Worker;

public class WorkerMapper {

    public static WorkerResponse toWorkerResponse(Worker worker){
        return WorkerResponse.builder()
                .id(worker.getId().toString())
                .firstName(worker.getFirstName())
                .lastName(worker.getLastName())
                .email(worker.getEmail())
                .phoneNumber(worker.getPhoneNumber())
                .address(worker.getAddress())
                .role(worker.getRole())
                .build();
    }

    public static Worker toWorker(CreateWorkerRequest workerRequest){
        return Worker.builder()
                .firstName(workerRequest.getFirstName())
                .lastName(workerRequest.getLastName())
                .email(workerRequest.getEmail())
                .phoneNumber(workerRequest.getPhoneNumber())
                .address(workerRequest.getAddress())
                .role(workerRequest.getRole())
                .build();
    }
}
