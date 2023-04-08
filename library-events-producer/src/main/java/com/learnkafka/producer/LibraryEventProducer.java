/*

@Component anotasyonu, Spring Framework'ün bu sınıfın bir bileşen olduğunu tanımasını sağlar. Bu nedenle bu sınıf, uygulamanın diğer
 bileşenleri tarafından kullanılabilir hale getirilir.

@Slf4j anotasyonu, Lombok kütüphanesi tarafından sağlanır ve uygulamanın günlük dosyasına (log file) mesaj yazmak için kullanılır.

LibraryEventProducer sınıfı, Kafka mesaj kuyruğuna bir LibraryEvent nesnesi göndermek için sendLibraryEvent metodu sağlar. Bu yöntem,
Kafka mesaj kuyruğuna gönderilen mesajı temsil eden bir anahtar (key) ve değer (value) çifti oluşturur.

ObjectMapper sınıfı, libraryEvent nesnesini JSON biçimine dönüştürmek için kullanılır.

kafkaTemplate değişkeni, Kafka mesaj kuyruğuna mesaj göndermek için kullanılan Spring Framework'ün KafkaTemplate sınıfından bir örnektir.
 sendDefault yöntemi, Kafka mesaj kuyruğuna mesaj göndermek için kullanılır.

ListenableFutureCallback arayüzü, Kafka mesaj kuyruğuna gönderilen mesajın başarısız veya başarılı olma durumunu takip etmek için kullanılır.
 addCallback yöntemi, mesajın başarısız veya başarılı gönderilmesi durumunda çağrılacak yöntemleri sağlar.

handleSuccess yöntemi, mesajın başarılı bir şekilde gönderildiğinde çağrılır ve gönderilen mesajın anahtarını, değerini ve
gönderildiği bölüm numarasını günlük dosyasına yazdırır.

fandleFailure yöntemi, mesaj gönderimi sırasında bir hata oluştuğunda çağrılır ve hatanın mesajını günlük dosyasına yazdırır.
 */
package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class LibraryEventProducer {


    String topic = "library-events";
    @Autowired
    KafkaTemplate<Integer,String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);

        listenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {

                handleSuccess(key, value, result);
            }
        });
    }

    public ListenableFuture<SendResult<Integer, String>> sendLibraryEvent_Approach2(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer,String> producerRecord = buildProducerRecord(key,value,topic);
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);

        listenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {

                handleSuccess(key, value, result);
            }
        });
        return listenableFuture;
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topic) {

        List<Header> recordHeaders = List.of(new RecordHeader("event-source","scanner".getBytes()));
        return new ProducerRecord<>(topic,null,key,value,recordHeaders);
    }


    public SendResult<Integer, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult = null;
        try {
            sendResult =  kafkaTemplate.sendDefault(key,value).get();
        }catch (ExecutionException | InterruptedException e){
            log.error("ExecutionException/InterruptedException Sending the Message and the excepiton is {}",e.getMessage());
        }catch (Exception e){
            log.error("Exception Sending the Message and the excepiton is {}",e.getMessage());
        }
        return  sendResult;
    }



    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error Sending the Message and the exception is {}",ex.getMessage());
        try{
            throw ex;

        }catch (Throwable throwable){
            log.error("Error in OnFailure: {}",throwable.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message Sent SuccessFully for the key : {} and the value is {}, partition is {}", key, value, result.getRecordMetadata().partition());
    }


}


/*
NOT :
sendLibraryEventSynchronous() metodu senkron bir gönderim yapar, yani Kafka broker'ından cevap almadan önce gönderen istemci uygulaması bloke olur.
Bu metod, Kafka'ya mesaj gönderdikten sonra, ilgili Kafka broker'ından bir onay almayı bekleyerek gönderim işleminin tamamlanmasını sağlar.
Bu sayede, gönderim işlemi başarılı bir şekilde tamamlanana kadar devam eden bir iş akışı sağlayabilirsiniz.

sendLibraryEvent() metodu ise asenkron bir gönderim yapar, yani istemci uygulaması gönderim işlemini gerçekleştirdikten hemen sonra devam eder ve c
evap almadan önce beklemek yerine, gönderilen mesajın kaydedilmesine güvenir. Bu metod, Kafka broker'ına mesajı hızlı bir şekilde göndermek için kullanılabilir ve
istemci uygulaması için blokaj veya bekleyiş gerektirmez.

sendLibraryEvent_Approach2: Bu yaklaşım, önceki yaklaşıma benzer, ancak buildProducerRecord yöntemi kullanılarak Kafka mesajı oluşturulur.
Bu yöntem, mesajın anahtarını, değerini ve hedef topic adını alır ve bir ProducerRecord nesnesi oluşturur.
Bu nesne, kafkaTemplate'ın send yöntemi aracılığıyla Kafka kuyruğuna gönderilir. Mesaj gönderme işlemi, yine ListenableFuture ve
ListenableFutureCallback kullanılarak gerçekleştirilir.

Bu metotlar, uygulamanın gereksinimlerine ve kullanım senaryolarına göre farklı durumlarda kullanılabilir. sendLibraryEvent() ve sendLibraryEvent_Approach2()
 metotları asenkron gönderim için kullanılabilirken, sendLibraryEventSynchronous() metodu senkron gönderim için kullanılır.
 Asenkron yöntemler daha hızlı yanıt verirken, senkron yöntemler daha güvenilir sonuçlar sağlar.

 */