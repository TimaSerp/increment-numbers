package ru.serpov.incrementnumbers.server;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import ru.serpov.incrementnumbers.IncrementServiceGrpc;
import ru.serpov.incrementnumbers.IncrementServiceOuterClass.*;
import ru.serpov.incrementnumbers.util.Util;

import static ru.serpov.incrementnumbers.IncrementServiceOuterClass.ErrorCode.FIRST_BIGGER_THAN_LAST;
import static ru.serpov.incrementnumbers.IncrementServiceOuterClass.ErrorCode.INTERNAL_ERROR;

@Slf4j
@GRpcService
public class ServerImpl extends IncrementServiceGrpc.IncrementServiceImplBase {

    @Override
    public void getStreamIncrement(Request request, StreamObserver<Response> responseObserver) {
        int firstValue = request.getFirstValue();
        int lastValue = request.getLastValue();
        log.info("GRPc call: receive getStreamIncrement(firstValue={}, lastValue={})", firstValue, lastValue);
        try {
            if (firstValue > lastValue) {
                responseObserver.onNext(getErrorResponse(FIRST_BIGGER_THAN_LAST,
                        String.format("%s is bigger than %s", firstValue, lastValue)));
            } else {
                for (int i = 1; firstValue + i <= lastValue; i++) {
                    long start = System.currentTimeMillis();
                    log.info("GRPc call: send response getStreamIncrement: newValue={}", firstValue + i);
                    responseObserver.onNext(getSuccessResponse(firstValue + i));
                    Util.wait(2000, System.currentTimeMillis() - start);
                }
            }
        } catch (Exception e) {
            log.error("GRPc call: got error on getStreamIncrement", e);
            responseObserver.onNext(getErrorResponse(INTERNAL_ERROR, e.getMessage()));
        } finally {
            log.info("GRPc call: getStreamIncrement ended");
            responseObserver.onCompleted();
        }
    }

    private Response getErrorResponse(ErrorCode code, String desc) {
        ErrorResponse errorResponse = ErrorResponse.newBuilder()
                .setErrorCode(code)
                .setDesc(desc)
                .build();
        return Response.newBuilder()
                .setErrorResponse(errorResponse)
                .build();
    }

    private Response getSuccessResponse(int newValue) {
        SuccessResponse successResponse = SuccessResponse.newBuilder()
                .setNewValue(newValue)
                .build();
        return Response.newBuilder()
                .setSuccessResponse(successResponse)
                .build();
    }
}
