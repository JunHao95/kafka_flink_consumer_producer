import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.*;

public class UserConsumer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the name for the consumer:");
        String consumerName = scanner.nextLine().trim();

        KafkaReceiver<String, String> receiver = createReceiver(List.of("filtered-conversation"), "balance-consumer");
        receiver.receive().subscribe(event -> {
          if (event.key().equals(consumerName)) {
            System.out.printf("%s: %s\n", event.key(), event.value());
            event.receiverOffset().commit().subscribe();
          }
        });
//        KafkaReceiver<String, String> txnAlerts = createReceiver(List.of("user-alerts"), "alert-consumer");
//        txnAlerts.receive().doOnNext(event -> {
//            long timestamp = event.timestamp();
//            Date date = new Date(timestamp);
//            System.out.printf("ALERT: potential fraudulent transaction at %s: user %s amount %s\n", date, event.key(), event.value());
//            event.receiverOffset().commit().subscribe();
//        }).blockLast();
    }

    public static KafkaReceiver<String, String> createReceiver(Collection<String> topics, String groupId) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        p.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        p.put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer-users");
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(p);
        return KafkaReceiver.create(receiverOptions.subscription(topics));
    }
}
