package com.learnkafka.controller;

import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;


import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library-producer-events",partitions = 1)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.roducer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsControllerIntegrationTest {


    @Autowired
     TestRestTemplate testRestTemplate;

    @Autowired(required = false)
      EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Integer,String> consumer;



    @BeforeEach
    void setUp(){
        Map<String,Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1","true",embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs,new IntegerDeserializer(),new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown(){
        consumer.close();
    }

    @Test
    @Timeout(5)
    void postLibraryEvent(){

        //given
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("olgun")
                .bookName("Kafka using Spring Boot")
                .build();
        LibraryEvent libraryEvent = LibraryEvent
                .builder()
                .libraryEventId(null)
                .book(book)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent);

        //when
        ResponseEntity<LibraryEvent> responseEntity =  testRestTemplate.exchange("/v1/libraryevent", HttpMethod.POST,request,LibraryEvent.class);

        //then
        assertEquals(HttpStatus.CREATED,responseEntity.getStatusCode());

        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-producer-events");
        String expectedRecord = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka using Spring Boot\",\"bookAuthor\":\"olgun\"}}";
        String value = consumerRecord.value();
        assertEquals(expectedRecord,value);

    }

    @Test
    void putLibraryEvent(){

        //given
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("olgun")
                .bookName("Kafka using Spring Boot")
                .build();
        LibraryEvent libraryEvent = LibraryEvent
                .builder()
                .libraryEventId(123)
                .book(book)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent);

        //when
        ResponseEntity<LibraryEvent> responseEntity =  testRestTemplate.exchange("/v1/libraryevent", HttpMethod.PUT,request,LibraryEvent.class);

        //then
        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());

        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-producer-events");
        String expectedRecord = "{\"libraryEventId\":123,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka using Spring Boot\",\"bookAuthor\":\"olgun\"}}";
        String value = consumerRecord.value();
        assertEquals(expectedRecord,value);

    }
}

// TODO : Sınıfın ne iş yaotğını açıklayan dokumantasyon
/*
1. com.learnkafka.controller paketindeki LibraryEventsControllerIntegrationTest sınıfı başlatılır.

2. Test sınıfı, Spring Boot test ortamıyla yapılandırılmış bir TestRestTemplate nesnesi ve bir EmbeddedKafkaBroker nesnesi kullanarak,
 Kafka ile bir web uygulamasının entegrasyon testlerini gerçekleştirir.

3.@BeforeEach işlevi, bir Kafka tüketicisi oluşturarak, tüm gömülü Kafka konularını tüketmek için hazırlanır.

4.@AfterEach işlevi, tüketicinin kapatılmasını sağlar.

5.@Test işlevi, bir LibraryEvent öğesi oluşturarak HTTP POST isteği gönderir ve Kafka tarafından gönderilen bir mesajı doğrular.
 Bu test, gönderilen mesajın doğru olup olmadığını kontrol eder

6.@EmbeddedKafka dekoratörü, test koşulunun Kafka sunucusunu yerleşik olarak çalıştırmasını sağlar.

7.@TestPropertySource dekoratörü, test koşulunda kullanılacak özellikleri ayarlar.
Bu test sınıfı, bir kitap etkinliğinin HTTP POST isteği ile Kafka'ya gönderilmesini ve Kafka'nın doğru bir şekilde tüketilmesini doğrular.
Test koşulunda kullanılan özellikler,
yerleşik bir Kafka sunucusunu kullanarak Kafka etkinliklerinin test edilmesini sağlar.


8.Elbette, postLibraryEvent() yöntemi, bir LibraryEvent öğesi oluşturarak HTTP POST isteği gönderir ve Kafka tarafından gönderilen bir mesajı doğrular.
İşlevin yaptıkları aşağıdaki gibidir:

9.Book sınıfından bir örnek oluşturulur ve LibraryEvent sınıfından bir nesne oluşturulur. Bu, gönderilecek olan kitap etkinliğini temsil eder.

10.Bir HttpHeaders nesnesi oluşturulur ve bu nesne, istek başlığına content-type parametresini ekler.

11.HttpEntity sınıfı kullanılarak, LibraryEvent nesnesi ve HttpHeaders nesnesi birleştirilerek bir HTTP isteği oluşturulur.
12. TestRestTemplate nesnesi kullanılarak, oluşturulan HTTP isteği "/v1/libraryevent" adresine gönderilir ve LibraryEvent türünde bir ResponseEntity alınır.

13.Alınan ResponseEntity'deki durum kodu (HttpStatus) CREATED olmalıdır. Bu, başarılı bir şekilde bir kitap etkinliğinin oluşturulduğunu gösterir

14.KafkaTestUtils.getSingleRecord() yöntemi kullanılarak, ConsumerRecord nesnesi elde edilir. Bu, Kafka tarafından gönderilen tek bir mesajı temsil eder.

15.Kafka mesajı, bir String değişkene atanır ve beklenen sonucu içeren bir String ile karşılaştırılır. Eğer beklenen sonuçla eşleşiyorsa, test başarılıdır.
Bu yöntem, kitap etkinliklerinin başarıyla oluşturulup oluşturulmadığını ve Kafka tarafından başarılı bir şekilde işlenip işlenmediğini doğrular.

 */