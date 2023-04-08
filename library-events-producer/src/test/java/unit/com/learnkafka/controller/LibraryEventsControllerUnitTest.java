package com.learnkafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.producer.LibraryEventProducer;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc
public class LibraryEventsControllerUnitTest {
    
    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    LibraryEventProducer libraryEventsProducer;

    @Test
    void postLibraryEvent() throws Exception {

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

        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventsProducer.sendLibraryEvent_Approach2(isA(LibraryEvent.class))).thenReturn(null);
        //when
        mockMvc.perform(post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        //then
    }


    @Test
    void postLibraryEvent_4xx() throws Exception {

        //given

        Book book = Book.builder()
                .bookId(null)
                .bookAuthor(null)
                .bookName("Kafka using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent
                .builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventsProducer.sendLibraryEvent_Approach2(isA(LibraryEvent.class))).thenReturn(null);

        //expect
        String  expectedErrorMessage = "book.bookAuthor - must not be blank , book.bookId - must not be null";
        mockMvc.perform(post("/v1/libraryevent")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedErrorMessage));

        //then
    }
    @Test
    void putLibraryEvent() throws Exception{
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
        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventsProducer.sendLibraryEvent_Approach2(isA(LibraryEvent.class))).thenReturn(null);

        //when
        mockMvc.perform(put("/v1/libraryevent")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
    }

    @Test
    void putLibraryEvent_4xx() throws Exception{
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
        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventsProducer.sendLibraryEvent_Approach2(isA(LibraryEvent.class))).thenReturn(null);

        //when
        String expectedErrorMessage = "Please pass the LibraryEventId";
        ResultActions resultActions = mockMvc.perform(put("/v1/libraryevent")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedErrorMessage));

        //then

        assert(resultActions.andReturn().getResponse().getContentAsString().equals(expectedErrorMessage));

    }
}

/*

@WebMvcTest Spring test çerçevesi altında, MVC controller sınıflarını test etmek için kullanılır.
Bu anotasyon kullanıldığında Spring, sadece test edilen controller sınıfını yükler ve yönlendirmelerle
ilgili tüm diğer bileşenleri yok sayar. Bu sayede testlerin daha hızlı ve daha az karmaşık hale gelir.

@AutoConfigureMockMvc ise, MockMvc sınıfını otomatik olarak yapılandırmak için kullanılır. Bu sınıf,
Spring MVC uygulamalarının testlerinde, HTTP istekleri göndermek ve cevapları test etmek için kullanılır.
 Bu anotasyon sayesinde testlerde MockMvc nesnesini kullanarak HTTP istekleri göndermek ve karşılık gelen
HTTP cevaplarını test etmek mümkün olur.

Bu kod, Spring Boot framework'ünü kullanarak bir Library Event uygulaması için yazılmış bir test sınıfıdır. Test sınıfı, LibraryEventsController sınıfının davranışlarını
test eder.

Test sınıfının içindeki postLibraryEvent() ve postLibraryEvent_4xx() isimli iki adet test metodu bulunur. Bu test metodları,
HTTP POST isteği göndererek ilgili API'ların doğru çalışıp çalışmadığını kontrol eder.

İlk test metodu, bir LibraryEvent nesnesi oluşturur ve oluşturulan bu nesneyi JSON formatına dönüştürerek HTTP POST isteği gönderir.
İsteğin gönderildiği controller sınıfındaki ilgili metod, oluşturulan nesneyi doğru şekilde işleyip işlemediğini kontrol eder ve HTTP status kodunu kontrol eder.
Test metodunun içindeki doNothing() fonksiyonu, gerçek Kafka clusterına mesaj gönderme işleminin yapılmamasını sağlar.

İkinci test metodu ise, geçersiz bir LibraryEvent nesnesi oluşturur ve bu nesneyi JSON formatına dönüştürerek HTTP POST isteği gönderir. Bu test metodu,
HTTP status kodunun doğru şekilde dönüp dönmediğini ve dönen hata mesajının doğru olup olmadığını kontrol eder.

Test sınıfındaki MockMvc nesnesi, Spring Boot uygulamasının API'larını test etmek için kullanılır. ObjectMapper nesnesi,
 Java nesnelerini JSON formatına dönüştürmek için kullanılır. LibraryEventProducer nesnesi ise, Kafka mesajlarını göndermek için kullanılan bir nesnedir
 ve test sınıfında bu nesne yerine mock bir nesne kullanılır.
 */