package com.learnkafka.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.entity.LibraryEventType;
import com.learnkafka.jpa.LibraryEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LibraryEventService  {

    @Autowired
    ObjectMapper objectMapper;

    private final LibraryEventRepository libraryEventRepository;

    public LibraryEventService(LibraryEventRepository libraryEventRepository) {
        this.libraryEventRepository = libraryEventRepository;
    }

    public void processLibraryEvent(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {

        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent : {} ", libraryEvent);

        temporyNetworkIssue(libraryEvent);

        if(libraryEvent.getLibraryEventType().equals(LibraryEventType.NEW)){
            save(libraryEvent);
        }
        else {
            validate(libraryEvent);
            save(libraryEvent);
        }


    }

    private static void temporyNetworkIssue(LibraryEvent libraryEvent) {
        if(libraryEvent !=null && libraryEvent.getLibraryEventId() == 999){
            throw new RecoverableDataAccessException("Temporary Network Issue");
        }
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Successfully Persisted the library Event {} ", libraryEvent);
    }

    private void validate(LibraryEvent libraryEvent) {
        if(libraryEvent.getLibraryEventId() == null){
            throw new IllegalArgumentException("Library Event Id is missing");
        }

        libraryEventRepository.findById(libraryEvent.getLibraryEventId())
                        .orElseThrow(()-> new IllegalArgumentException("Not a valid Library Event"));

        log.info("Validation is successful for the library event {} ", libraryEvent);
        save(libraryEvent);
    }
}
