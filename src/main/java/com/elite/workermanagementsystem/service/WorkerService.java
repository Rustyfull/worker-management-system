package com.elite.workermanagementsystem.service;

import com.elite.workermanagementsystem.dto.CreateWorkerRequest;
import com.elite.workermanagementsystem.dto.WorkerResponse;
import com.elite.workermanagementsystem.exceptions.EmailAlreadyExistsException;
import com.elite.workermanagementsystem.exceptions.WorkerNotFoundException;
import com.elite.workermanagementsystem.mapper.WorkerMapper;
import com.elite.workermanagementsystem.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerService {

    private final WorkerRepository workerRepository;


    public WorkerResponse addWorker(CreateWorkerRequest createWorkerRequest){
        if(workerRepository.existsByEmail(createWorkerRequest.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists");
        }

        var newWorker = WorkerMapper.toWorker(createWorkerRequest);
        var addedWorker = workerRepository.save(newWorker);
        log.info("Worker added: {}", addedWorker.getId().toString());
        return WorkerMapper.toWorkerResponse(addedWorker);
    }



    public WorkerResponse getWorkerById(String id){
        return workerRepository.findById(UUID.fromString(id)).map(WorkerMapper::toWorkerResponse).orElseThrow(
                () -> new WorkerNotFoundException("Worker not found")
        );
    }


    public void deleteWorkerById(String id){
        var worker = workerRepository.findById(UUID.fromString(id)).orElseThrow(
                () -> new WorkerNotFoundException("Worker not found")
        );
        workerRepository.delete(worker);
    }



    public WorkerResponse updateWorker(String id, CreateWorkerRequest updateWorkerRequest){
        var worker = workerRepository.findById(UUID.fromString(id)).orElseThrow(
                () -> new WorkerNotFoundException("Worker not found")
        );


        if(updateWorkerRequest.getFirstName() != null){
            worker.setFirstName(updateWorkerRequest.getFirstName());
        }

        if (updateWorkerRequest.getLastName() != null){
            worker.setLastName(updateWorkerRequest.getLastName());
        }

        if (updateWorkerRequest.getPhoneNumber() != null){
            worker.setPhoneNumber(updateWorkerRequest.getPhoneNumber());
        }

        if (updateWorkerRequest.getAddress() != null){
            worker.setAddress(updateWorkerRequest.getAddress());
        }


        if(updateWorkerRequest.getRole() != null){
            worker.setRole(updateWorkerRequest.getRole());
        }


        if(updateWorkerRequest.getEmail() != null){
            worker.setEmail(updateWorkerRequest.getEmail());
        }

        var updatedWorker = workerRepository.save(worker);
        return WorkerMapper.toWorkerResponse(updatedWorker);
    }


    public List<WorkerResponse> getAllWorkers(){
        return workerRepository.findAll().stream()
                .map(WorkerMapper::toWorkerResponse)
                .toList();
    }


}
