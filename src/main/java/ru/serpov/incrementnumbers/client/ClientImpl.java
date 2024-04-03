package ru.serpov.incrementnumbers.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.serpov.incrementnumbers.IncrementServiceOuterClass.ErrorResponse;
import ru.serpov.incrementnumbers.IncrementServiceOuterClass.Request;
import ru.serpov.incrementnumbers.IncrementServiceOuterClass.Response;
import ru.serpov.incrementnumbers.util.Util;

import static ru.serpov.incrementnumbers.IncrementServiceGrpc.newStub;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ClientImpl {

    @GetMapping("/getIncrement/{first}/{last}")
    public void getIncrement(@PathVariable("first") int firstValue,
                             @PathVariable("last") int lastValue) {
        Request request = Request.newBuilder()
                .setFirstValue(firstValue)
                .setLastValue(lastValue)
                .build();
        try {
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress("localhost", 6565)
                    .usePlaintext()
                    .build();
            var stub = newStub(channel);

            final boolean[] hasError = {false};
            final int[] newValue = {0};
            log.info("Client: send request");
            stub.getStreamIncrement(request, new StreamObserver<>() {
                @Override
                public void onNext(Response value) {
                    if (value == null) {
                        log.error("response == null");
                        hasError[0] = true;
                        return;
                    }
                    if (value.hasErrorResponse()) {
                        ErrorResponse err = value.getErrorResponse();
                        log.error("{}: {}", err.getErrorCode(), err.getDesc());
                        hasError[0] = true;
                        return;
                    }
                    newValue[0] = value.getSuccessResponse().getNewValue();
                    log.info("число от сервера:{}", newValue[0]);
                }

                @Override
                public void onError(Throwable t) {
                    log.error("got error", t);
                    hasError[0] = true;
                }

                @Override
                public void onCompleted() {
                    log.info("completed");
                }
            });
            for (int i = 0; i <= 50; i++) {
                long start = System.currentTimeMillis();
                if (hasError[0]) break;
                log.info("currentValue:{}", i + 1 + newValue[0]);
                Util.wait(1000, System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            log.error("Got error", e);
            throw new RuntimeException(e);
        }
    }
}
