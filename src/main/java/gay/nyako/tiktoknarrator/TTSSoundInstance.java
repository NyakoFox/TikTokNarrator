package gay.nyako.tiktoknarrator;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.random.Random;
import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class TTSSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    protected final String msg;
    protected boolean stopped = false;

    public TTSSoundInstance(String msg, Random random) {
        super(
                new Identifier("tiktoknarrator", "tts/" + Hashing.sha1().hashUnencodedChars(msg)),
                SoundCategory.VOICE,
                random
        );
        this.msg = msg;
    }

    @Override
    public WeightedSoundSet getSoundSet(SoundManager soundManager) {
        WeightedSoundSet soundEvents = new WeightedSoundSet(getId(), null);
        soundEvents.add(
                new Sound(
                        getId().toString(),
                        ConstantFloatProvider.create(1.0F),
                        ConstantFloatProvider.create(1.0F),
                        1,
                        Sound.RegistrationType.FILE,
                        true,
                        false,
                        0
                )
        );
        this.sound = soundEvents.getSound(this.random);
        return soundEvents;
    }

    @Override
    public boolean isDone() {
        return stopped;
    }

    @Override
    public void tick() {
    }

    public CompletableFuture<AudioStream> getStream() {
        try {
            URL url = new URL("https://tiktok-tts.weilnet.workers.dev/api/generation");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "TikTokNarrator");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            JsonObject json = new JsonObject();
            json.addProperty("text", msg);
            json.addProperty("voice", "en_us_001");
            String jsonInputString = json.toString();
            connection.getOutputStream().write(jsonInputString.getBytes());
            connection.connect();

            if (connection.getResponseCode() != 200) {
                throw new IOException("Failed to connect to TikTok TTS API");
            }

            Gson gson = new Gson();

            var br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            var str = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                str.append(output);
            }

            JsonObject response = gson.fromJson(str.toString(), JsonObject.class);
            String audioData = response.get("data").getAsString();
            byte[] decoded = Base64.decodeBase64(audioData);

            InputStream decodedStream = new ByteArrayInputStream(decoded);
            AudioStream audioStream = new Mp3AudioStream(decodedStream);

            return CompletableFuture.completedFuture(audioStream);
        }
        catch (IOException e) {
            e.printStackTrace();
            stopped = true;
            return CompletableFuture.completedFuture(null);
        }
    }

    // THIS IS USED BY FABRIC, THIS IS A SOFT OVERRIDE DO NOT REMOVE
    @SuppressWarnings("unused")
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader loader, Identifier id, boolean loop) {
        return getStream();
    }
}
