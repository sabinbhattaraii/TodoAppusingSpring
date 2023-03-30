package org.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class KafkaApp {

    private static final String TOPIC = "my-kafka-topic";

    private static final String BOOTSTRAP_SERVERS = "localhost:9093,localhost:9094,localhost:9095";

    private static void produce(){

        Properties props = new Properties();

        props.put("bootstrap.servers",BOOTSTRAP_SERVERS);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            // here, we run an infinite loop to sent a message to the cluster every second
            for (int i = 0;; i++) {
                String key = Integer.toString(i);
                String message = "this is message " + Integer.toString(i);

                producer.send(new ProducerRecord<String, String>(TOPIC, key, message));

                // log a confirmation once the message is written
                System.out.println("sent msg " + key);
                try {
                    // Sleep for a second
                    Thread.sleep(1000);
                } catch (Exception e) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not start producer: " + e);
        }
    }

    private static void consume(){
        Properties props = new Properties();

        props.setProperty("bootstrap.servers",BOOTSTRAP_SERVERS);
        props.setProperty("group.id", "my-group-id");
        props.setProperty("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");

        try(KafkaConsumer<String,String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(TOPIC));
            while (true){
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String,String> record : records){
                     System.out.printf("received message: %s\n", record.value());
                }
            }
        }
    }


    public static void main(String[] args) throws Exception{
        Thread consumerThread = new Thread(KafkaApp::consume);
        consumerThread.start();

        Thread producerThread = new Thread(KafkaApp::produce);
        producerThread.start();
    }
}
