package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks //Bu test edilmekte olan sınıfın bir örneğini oluşturmak için kullanabileceğiniz bir anotasyondur.
    LibraryEventProducer libraryEventProducer;

    @Test
    void sendLibraryEvent_Approach2_failure() throws JsonProcessingException, ExecutionException, InterruptedException {

        //given
        Book book = Book.builder()
                .bookId(null)
                .bookAuthor("olgun")
                .bookName("Kafka using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent
                .builder()
                .libraryEventId(null)
                .book(book)
                .build();

        SettableListenableFuture future = new SettableListenableFuture();
        future.setException(new RuntimeException("Exception calling Kafka"));
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        //when
        assertThrows(Exception.class, () -> libraryEventProducer.sendLibraryEvent_Approach2(libraryEvent).get());
        //then
    }

    @Test
    void sendLibraryEvent_Approach2_success() throws JsonProcessingException, ExecutionException, InterruptedException {

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
        String record = objectMapper.writeValueAsString(libraryEvent);
        SettableListenableFuture future = new SettableListenableFuture();

        ProducerRecord<Integer,String> producerRecord = new ProducerRecord("library-producer-events",libraryEvent.getLibraryEventId(),record);
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-producer-events",2),1,1,
                342,System.currentTimeMillis(),1,2);

        SendResult<Integer,String> sendResult = new SendResult<>(producerRecord,recordMetadata);

        future.set(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        //when
        ListenableFuture<SendResult<Integer, String>> listenableFuture = libraryEventProducer.sendLibraryEvent_Approach2(libraryEvent);


        //then

       SendResult<Integer, String> sendResult1 =  listenableFuture.get();
       assert  sendResult1.getRecordMetadata().partition() == 2;
    }
}



/*
- @InjectMocks ve @Mock birbirinden farklı iki Mockito anotasyonudur.

- @Mock anotasyonu bir sınıfın mock nesnesini oluşturur. Yani bir sınıftaki bir metodu çağırdığınızda, gerçek nesnenin yanıt vermesi gereken yerde bir
mock nesne çağrısı gerçekleşir. Mock nesnesi gerçek nesnenin yerine geçer ve istenildiği gibi ayarlanabilir.

- @InjectMocks anotasyonu ise mocklanmış nesneleri, mock olmayan nesnelerin içinde enjekte eder. Yani, bir sınıfın içinde kullanılan bütün mock
nesnelerini bu anotasyon yardımıyla enjekte edebiliriz.

Özetle, @Mock bir sınıfın mock nesnesini oluştururken, @InjectMocks bu mock nesneleri gerçek nesnelerin içinde kullanılabilir hale getirir.

-@Spy annotationu, Mockito kütüphanesi ile birlikte kullanılarak, gerçek bir nesneyi spy nesnesine dönüştürür. Spy nesnesi, gerçek nesnenin bir kopyasını oluşturur
 ve gerçek nesnenin tüm davranışlarını taklit eder. Ancak, spy nesnesi davranışlarına müdahale etmek için kullanılabilir. Spy nesnesi üzerindeki çağrılar, gerçek nesnede olduğu gibi işleme konulur. Ancak, spy nesnesi üzerinde yapılan değişiklikler gerçek nesneye yansımaz.



-@ExtendWith(MockitoExtension.class) anotasyonu, JUnit 5'de Mockito kullanan test sınıfları için bir JUnit uzantısı olarak kullanılır. Bu anotasyon,
Mockito sınıflarını testlerinizde kullanabilmeniz için Mockito'nun JUnit entegrasyonunu sağlar.

Bu anotasyonu kullanarak, Mockito'nun Mock ve Spy nesnelerini oluşturabilir, veri seçenekleri belirleyebilir ve davranışları doğrulayabilirsiniz. A
yrıca, bu anotasyon sayesinde, Mockito mock nesnelerini ve davranışlarını otomatik olarak temizler ve yeniden yükler, bu da testlerinizi daha okunaklı
ve sürdürülebilir hale getirir.

- SettableListenableFuture bir ListenableFuture arayüzü uygulamasıdır ve gelecekte tamamlanacak bir işlemin sonucunu temsil eder. Bu sınıf,
ListenableFuture'ın normal davranışını geçersiz kılarak, gelecekte tamamlanacak bir işlemin sonucunu programlı olarak ayarlamak için kullanılır.
Yani, future değişkeni, gelecekte tamamlanacak bir işlemin sonucunu temsil eder ve set() veya setException() yöntemleriyle bu sonuç programlı olarak ayarlanabilir.

Bu test sınıfında, sendLibraryEvent_Approach2_failure() test metodu için bir SettableListenableFuture örneği oluşturulmuştur.
Bu örnek, bir RuntimeException oluştuğu durumda Kafka'ya gönderilecek bir LibraryEvent için bir asenkron görevin sonucunu temsil eder.
Bu şekilde, test sırasında Kafka'ya bir olay gönderme işleminin sonucu programlı olarak ayarlanabilir ve böylece bir hata senaryosu simüle edilebilir.
 */