package com.lordgasmic.logiled;

import com.logitech.gaming.LogiLED;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import java.util.Arrays;

public class MCP {

    public static void main(final String... args) throws InterruptedException, LineUnavailableException {
        LogiLED.LogiLedInit();
        Thread.sleep(10);

        final Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

        final Mixer.Info microphone = Arrays.stream(mixerInfo)
                                            .filter(m -> m.getName().startsWith("Port Microphone"))
                                            .filter(m -> m.getName().contains("935"))
                                            .findFirst()
                                            .get();
        final Mixer mixer = AudioSystem.getMixer(microphone);
        final Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();

        final Line line = mixer.getLine(sourceLineInfo[0]);
        line.open();

        final Control[] controls = line.getControls();

        final CompoundControl compoundControl = (CompoundControl) controls[0];
        final Control[] memberControls = compoundControl.getMemberControls();
        BooleanControl mutableControl = null;
        for (final Control c : memberControls) {
            if (c instanceof BooleanControl) {
                mutableControl = (BooleanControl) c;
                break;
            }
        }

        if (mutableControl == null) {
            throw new IllegalStateException("Mutable Control must not be null");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Thread.sleep(200);
                System.out.println("shutting down");
                line.close();
                LogiLED.LogiLedShutdown();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }));
        while (true) {
            Thread.sleep(1000);

            if (mutableControl.getValue()) {
                // blue
                LogiLED.LogiLedSetLighting(0, 0, 100);
            } else {
                // red
                LogiLED.LogiLedSetLighting(100, 0, 0);
            }
        }
    }
}
