package com.github.phidescode.JavaDynamoDBService;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class Logger {

    private static LambdaLogger lambdaLogger;

    public static void setLogger(LambdaLogger logger) {
        lambdaLogger = logger;
    }

    public static void log(String message) {
        if (lambdaLogger != null) {
            lambdaLogger.log(message);
        } else {
            // Fallback to standard output or another logging mechanism
            System.out.println(message);
        }

    }

    public static void logError(String message, Throwable throwable) {
        if (lambdaLogger != null) {
            lambdaLogger.log(message + ": " + throwable.getMessage());
        } else {
            System.err.println(message + ": " + throwable.getMessage());
        }
    }
}
