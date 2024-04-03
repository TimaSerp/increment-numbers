package ru.serpov.incrementnumbers;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import ru.serpov.incrementnumbers.IncrementServiceOuterClass.ErrorResponse;
import ru.serpov.incrementnumbers.IncrementServiceOuterClass.Request;
import ru.serpov.incrementnumbers.IncrementServiceOuterClass.Response;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.serpov.incrementnumbers.IncrementServiceGrpc.newStub;

@Slf4j
public class MainClient {

    private static final Request request = Request.newBuilder()
            .setFirstValue(0)
            .setLastValue(30)
            .build();

    public static void main(String[] args) {
        try {
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress("localhost", 6565)
                    .usePlaintext()
                    .build();
            var stub = newStub(channel);

            final AtomicBoolean hasError = new AtomicBoolean(false);
            final AtomicInteger newValue = new AtomicInteger(0);
            log.info("Client: send request");
            stub.getStreamIncrement(request, new StreamObserver<>() {
                @Override
                public void onNext(Response value) {
                    if (value == null) {
                        log.error("response == null");
                        hasError.set(true);
                        return;
                    }
                    if (value.hasErrorResponse()) {
                        ErrorResponse err = value.getErrorResponse();
                        log.error("{}: {}", err.getErrorCode(), err.getDesc());
                        hasError.set(true);
                        return;
                    }
                    newValue.set(value.getSuccessResponse().getNewValue());
                    log.info("число от сервера:{}", newValue.get());
                }

                @Override
                public void onError(Throwable t) {
                    log.error("got error", t);
                    hasError.set(true);
                }

                @Override
                public void onCompleted() {
                    log.info("completed");
                }
            });
            int currentValue = 0;
            for (int i = 0; i <= 50; i++) {
                long start = System.currentTimeMillis();
                if (hasError.get()) {
                    break;
                }
                currentValue = currentValue + 1 + newValue.getAndSet(0);
                log.info("currentValue:{}", currentValue);
                Util.wait(1000, System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            log.error("Got error", e);
            throw new RuntimeException(e);
        }
    }
}
