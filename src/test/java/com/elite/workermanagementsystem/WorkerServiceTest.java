package com.elite.workermanagementsystem;


import com.elite.workermanagementsystem.dto.CreateWorkerRequest;
import com.elite.workermanagementsystem.dto.WorkerResponse;
import com.elite.workermanagementsystem.exceptions.EmailAlreadyExistsException;
import com.elite.workermanagementsystem.exceptions.WorkerNotFoundException;
import com.elite.workermanagementsystem.model.Worker;
import com.elite.workermanagementsystem.repository.WorkerRepository;
import com.elite.workermanagementsystem.service.WorkerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkerService")
class WorkerServiceTest {

    private static final UUID WORKER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String EMAIL = "john.doe@example.com";

    // ADAPT: two different values of your Role enum
    private static final String ROLE_A = "ROLE_A";
    private static final String ROLE_B = "ROLE_B";

    @Mock
    private WorkerRepository workerRepository;

    @InjectMocks
    private WorkerService workerService;

    // ------------------------------------------------------------------ helpers

    private static Worker aWorker() {
        return Worker.builder()
                .id(WORKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email(EMAIL)
                .phoneNumber("+49 123 456789")
                .address("Wiesenstraße 1, 35390 Gießen")
                .role(ROLE_A)
                .build();
    }

    private static CreateWorkerRequest aCreateRequest() {
        return CreateWorkerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email(EMAIL)
                .phoneNumber("+49 123 456789")
                .address("Wiesenstraße 1, 35390 Gießen")
                .role(ROLE_A)
                .build();
    }

    // ------------------------------------------------------------------ addWorker

    @Nested
    @DisplayName("addWorker")
    class AddWorker {

        @Test
        @DisplayName("saves the worker and returns the mapped response when the email is free")
        void shouldSaveWorker_whenEmailDoesNotExist() {
            // given
            var request = aCreateRequest();
            when(workerRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(workerRepository.save(any(Worker.class))).thenReturn(aWorker());

            // when
            WorkerResponse response = workerService.addWorker(request);

            // then
            var captor = ArgumentCaptor.forClass(Worker.class);
            verify(workerRepository).save(captor.capture());
            assertThat(captor.getValue())
                    .extracting(Worker::getFirstName, Worker::getLastName, Worker::getEmail,
                            Worker::getPhoneNumber, Worker::getAddress, Worker::getRole)
                    .containsExactly("John", "Doe", EMAIL, "+49 123 456789",
                            "Wiesenstraße 1, 35390 Gießen", ROLE_A);

            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            assertThat(response.getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("throws EmailAlreadyExistsException and never saves when the email is taken")
        void shouldThrowException_whenEmailAlreadyExists() {
            // given
            var request = aCreateRequest();
            when(workerRepository.existsByEmail(EMAIL)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> workerService.addWorker(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("Email already exists");

            verify(workerRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------ getWorkerById

    @Nested
    @DisplayName("getWorkerById")
    class GetWorkerById {

        @Test
        @DisplayName("returns the mapped worker when it exists")
        void shouldReturnWorker_whenFound() {
            // given
            when(workerRepository.findById(WORKER_ID)).thenReturn(Optional.of(aWorker()));

            // when
            WorkerResponse response = workerService.getWorkerById(WORKER_ID.toString());

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            verify(workerRepository).findById(WORKER_ID);
        }

        @Test
        @DisplayName("throws WorkerNotFoundException when the worker does not exist")
        void shouldThrowException_whenNotFound() {
            // given
            when(workerRepository.findById(WORKER_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> workerService.getWorkerById(WORKER_ID.toString()))
                    .isInstanceOf(WorkerNotFoundException.class)
                    .hasMessage("Worker not found");
        }

        @Test
        @DisplayName("throws IllegalArgumentException for a malformed UUID without touching the repository")
        void shouldThrowException_whenIdIsNotAValidUuid() {
            assertThatThrownBy(() -> workerService.getWorkerById("not-a-uuid"))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoMoreInteractions(workerRepository);
        }
    }

    // ------------------------------------------------------------------ deleteWorkerById

    @Nested
    @DisplayName("deleteWorkerById")
    class DeleteWorkerById {

        @Test
        @DisplayName("deletes the worker when it exists")
        void shouldDeleteWorker_whenFound() {
            // given
            var worker = aWorker();
            when(workerRepository.findById(WORKER_ID)).thenReturn(Optional.of(worker));

            // when
            workerService.deleteWorkerById(WORKER_ID.toString());

            // then
            verify(workerRepository).delete(worker);
        }

        @Test
        @DisplayName("throws WorkerNotFoundException and never deletes when the worker does not exist")
        void shouldThrowException_whenNotFound() {
            // given
            when(workerRepository.findById(WORKER_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> workerService.deleteWorkerById(WORKER_ID.toString()))
                    .isInstanceOf(WorkerNotFoundException.class)
                    .hasMessage("Worker not found");

            verify(workerRepository, never()).delete(any());
        }
    }

    // ------------------------------------------------------------------ updateWorker

    @Nested
    @DisplayName("updateWorker")
    class UpdateWorker {

        @Test
        @DisplayName("updates all fields when all are provided")
        void shouldUpdateAllFields_whenAllProvided() {
            // given
            var existing = aWorker();
            var request = CreateWorkerRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .phoneNumber("+49 987 654321")
                    .address("Neue Straße 5, 60311 Frankfurt")
                    .role(ROLE_B)
                    .build();
            when(workerRepository.findById(WORKER_ID)).thenReturn(Optional.of(existing));
            when(workerRepository.save(any(Worker.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            WorkerResponse response = workerService.updateWorker(WORKER_ID.toString(), request);

            // then
            var captor = ArgumentCaptor.forClass(Worker.class);
            verify(workerRepository).save(captor.capture());
            assertThat(captor.getValue())
                    .extracting(Worker::getFirstName, Worker::getLastName, Worker::getEmail,
                            Worker::getPhoneNumber, Worker::getAddress, Worker::getRole)
                    .containsExactly("Jane", "Smith", "jane.smith@example.com",
                            "+49 987 654321", "Neue Straße 5, 60311 Frankfurt", ROLE_B);

            assertThat(response.getEmail()).isEqualTo("jane.smith@example.com");
        }

        @Test
        @DisplayName("keeps existing values for fields that are null in the request (partial update)")
        void shouldKeepExistingValues_whenFieldsAreNull() {
            // given
            var existing = aWorker();
            var request = CreateWorkerRequest.builder()
                    .firstName("Jane")   // only the first name changes
                    .build();
            when(workerRepository.findById(WORKER_ID)).thenReturn(Optional.of(existing));
            when(workerRepository.save(any(Worker.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            workerService.updateWorker(WORKER_ID.toString(), request);

            // then
            var captor = ArgumentCaptor.forClass(Worker.class);
            verify(workerRepository).save(captor.capture());
            assertThat(captor.getValue())
                    .extracting(Worker::getFirstName, Worker::getLastName, Worker::getEmail,
                            Worker::getPhoneNumber, Worker::getAddress, Worker::getRole)
                    .containsExactly("Jane", "Doe", EMAIL, "+49 123 456789",
                            "Wiesenstraße 1, 35390 Gießen", ROLE_A);
        }

        @Test
        @DisplayName("changes nothing when the request is empty, but still saves")
        void shouldNotChangeWorker_whenRequestIsEmpty() {
            // given
            var existing = aWorker();
            when(workerRepository.findById(WORKER_ID)).thenReturn(Optional.of(existing));
            when(workerRepository.save(any(Worker.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            workerService.updateWorker(WORKER_ID.toString(), CreateWorkerRequest.builder().build());

            // then
            var captor = ArgumentCaptor.forClass(Worker.class);
            verify(workerRepository).save(captor.capture());
            assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(aWorker());
        }

        @Test
        @DisplayName("throws WorkerNotFoundException and never saves when the worker does not exist")
        void shouldThrowException_whenNotFound() {
            // given
            when(workerRepository.findById(WORKER_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> workerService.updateWorker(WORKER_ID.toString(), aCreateRequest()))
                    .isInstanceOf(WorkerNotFoundException.class)
                    .hasMessage("Worker not found");

            verify(workerRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------ getAllWorkers

    @Nested
    @DisplayName("getAllWorkers")
    class GetAllWorkers {

        @Test
        @DisplayName("returns all workers mapped to responses")
        void shouldReturnAllWorkers() {
            // given
            var second = Worker.builder()
                    .id(UUID.randomUUID())
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .role(ROLE_B)
                    .build();
            when(workerRepository.findAll()).thenReturn(List.of(aWorker(), second));

            // when
            List<WorkerResponse> responses = workerService.getAllWorkers();

            // then
            assertThat(responses)
                    .hasSize(2)
                    .extracting(WorkerResponse::getEmail)
                    .containsExactly(EMAIL, "jane.smith@example.com");
        }

        @Test
        @DisplayName("returns an empty list when there are no workers")
        void shouldReturnEmptyList_whenNoWorkers() {
            // given
            when(workerRepository.findAll()).thenReturn(List.of());

            // when
            List<WorkerResponse> responses = workerService.getAllWorkers();

            // then
            assertThat(responses).isEmpty();
        }
    }
}