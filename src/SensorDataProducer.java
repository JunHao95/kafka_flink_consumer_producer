import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import utils.KafkaConfiguration;

import java.time.Duration;
import java.util.Random;

public class SensorDataProducer {
    private static final Random RNG = new Random();

    public static void main(String[] args) {
        KafkaSender<Integer, Double> kafkaSender = KafkaConfiguration.createSender(IntegerSerializer.class, DoubleSerializer.class, "random-sensor-data-producer");
        Flux.<ProducerRecord<Integer, Double>>generate(sink -> {
                    int sensorId = RNG.nextInt(10);
                    double sensorValue = RNG.nextDouble() < 0.2
                            ? -1
                            : RNG.nextDouble() < 0.2 && sensorId == 1
                            ? 10000
                            : RNG.nextDouble(293, 308);
                    sink.next(new ProducerRecord<Integer, Double>("sensor-data-raw", sensorId, sensorValue));
                })
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(x -> System.out.printf("%d -> %f\n", x.key(), x.value()))
                .map(x -> Mono.just(SenderRecord.create(x, System.currentTimeMillis())))
                .flatMap(kafkaSender::send)
                .blockLast();
        kafkaSender.close();
    }
}
