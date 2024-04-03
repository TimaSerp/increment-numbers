package ru.serpov.incrementnumbers;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Util {

    public static void wait(long millisToWait, long millisAlreadyGone) {
        if (millisAlreadyGone < millisToWait) {
            try {
                Thread.sleep(millisToWait - millisAlreadyGone);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
