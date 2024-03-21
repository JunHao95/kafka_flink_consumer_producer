import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;

import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.*;

public class SensorDataConsumer {
    public static void main(String[] args) {
        KafkaReceiver<Integer, Double> receiver = createReceiver(List.of("sensor-data-adjusted"), "simple-group");
        receiver.receive().subscribe(event -> {
            System.out.printf("adjusted reading %d: %.2fC\n", event.key(), event.value());
            event.receiverOffset().commit().subscribe();
        });
        KafkaReceiver<Integer, Double> averages = createReceiver(List.of("sensor-data-avg"), "avg-group");
        averages.receive().subscribe(event -> {
            System.out.printf("average reading %d: %.2fC\n", event.key(), event.value());
            event.receiverOffset().commit().subscribe();
        });
        KafkaReceiver<Integer, Double> alerts = createReceiver(List.of("sensor-data-high-alert"), "alert-group");
        alerts.receive().doOnNext(event -> {
            long timestamp = event.timestamp();
            Date date = new Date(timestamp);
            System.out.printf("ALERT: potential fire at %s: sensor %d read %f\n", date, event.key(), event.value());
            event.receiverOffset().commit().subscribe();
        }).blockLast();

    }
    public static KafkaReceiver<Integer, Double> createReceiver(Collection<String> topics, String groupId) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        p.put(ConsumerConfig.GROUP_ID_CONFIG, "simple-group");
        p.put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer");
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DoubleDeserializer.class);
        ReceiverOptions<Integer, Double> receiverOptions = ReceiverOptions.create(p);
        return KafkaReceiver.create(receiverOptions.subscription(topics));
    }
}
