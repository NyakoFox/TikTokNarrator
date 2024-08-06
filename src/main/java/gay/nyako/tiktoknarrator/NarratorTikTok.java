package gay.nyako.tiktoknarrator;

import com.mojang.text2speech.Narrator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.random.Random;

public class NarratorTikTok implements Narrator {
    @Override
    public void say(String msg, boolean interrupt) {
        TTSSoundInstance instance = new TTSSoundInstance(msg, Random.create());
        MinecraftClient.getInstance().getSoundManager().play(instance);
    }

    @Override
    public void clear() {

    }

    @Override
    public void destroy() {

    }
}
