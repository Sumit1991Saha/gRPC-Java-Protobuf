package com.github.saha.grpc.greet.server;

import com.proto.greet.*;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        System.out.println("Greet Unary request received");

        // extract the fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();
        String lastName = greeting.getLastName();

        // create the response
        String result = "Hello" + firstName + lastName;
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        // send the response
        responseObserver.onNext(response);

        // complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        System.out.println("Greet Many times request received");

        // extract the fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();
        String lastName = greeting.getLastName();

        // create the response
        String result = "Hello" + firstName + lastName;
        GreetManyTimesResponse response;

        try {
            for (int i = 0; i < 10; ++i) {
                response = GreetManyTimesResponse.newBuilder()
                        .setResult(result + ", response no. " + i)
                        .build();
                // send the response
                responseObserver.onNext(response);
                System.out.println("Response sent :- " + response.toString());
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // complete the RPC call
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {
        // we create the requestObserver that we'll return in this function
        return new StreamObserver<LongGreetRequest>() {
            String result = "";

            @Override
            public void onNext(LongGreetRequest value) {
                System.out.println("Received request from client :- " + value);
                Greeting greeting = value.getGreeting();
                String firstName = greeting.getFirstName();
                String lastName = greeting.getLastName();

                // client sends a message
                result += "Hello " + firstName + " " + lastName + "! ";
            }

            @Override
            public void onError(Throwable t) {
                // client sends an error
            }

            @Override
            public void onCompleted() {
                System.out.println("Client is done");
                // client is done
                responseObserver.onNext(
                        LongGreetResponse.newBuilder()
                                .setResult(result)
                                .build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        return new StreamObserver<GreetEveryoneRequest>() {
            @Override
            public void onNext(GreetEveryoneRequest value) {
                System.out.println("Received request from client :- " + value);
                Greeting greeting = value.getGreeting();
                String firstName = greeting.getFirstName();

                // client sends a message
                String result = "Hello " + firstName + "! ";
                GreetEveryoneResponse greetEveryoneResponse = GreetEveryoneResponse.newBuilder()
                        .setResult(result)
                        .build();

                responseObserver.onNext(greetEveryoneResponse);
            }

            @Override
            public void onError(Throwable t) {
                // do nothing
            }

            @Override
            public void onCompleted() {
                System.out.println("Client is done");
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        Integer number = request.getNumber();
        if (number >= 0) {
            double numberRoot = Math.sqrt(number);
            SquareRootResponse response = SquareRootResponse.newBuilder()
                    .setNumberRoot(numberRoot)
                    .build();

            // send the response
            responseObserver.onNext(response);

            // complete the RPC call
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("The number being sent is negative")
                            .augmentDescription("Number sent :- " + number)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request, StreamObserver<GreetWithDeadlineResponse> responseObserver) {

        Context currentContext = Context.current();
        try {
            for (int i =0; i < 3; ++i) {
                if (!currentContext.isCancelled()) {
                    System.out.println("Sleep for 100ms");
                    Thread.sleep(100);
                } else {
                    System.out.println("Deadline expired, hence returning");
                    responseObserver.onError(
                            Status.CANCELLED.
                                    withDescription("Cancelled by client")
                                    .asRuntimeException()
                    );
                    return;
                }
            }
            System.out.println("Sending Response");
            // create the response
            GreetWithDeadlineResponse response = GreetWithDeadlineResponse.newBuilder()
                    .setResult("Hello" + request.getGreeting().getFirstName() + request.getGreeting().getLastName())
                    .build();

            // send the response
            responseObserver.onNext(response);

            // complete the RPC call
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
