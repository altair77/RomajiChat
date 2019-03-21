package altair.romajichat;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.moji4j.MojiConverter;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

class Translation {
	private static final ExecutorService ES = Executors.newWorkStealingPool();
	private static final String API_URL = "http://www.google.com/transliterate";

	void convert(String sourceText, Consumer<String> converted) {
		ES.execute(() -> converted.accept(toKanji(toHira(sourceText))));
	}

	private String toHira(String sourceText) {
		sourceText = sourceText
				.replaceAll("nn", "n'")
				.replaceAll(",", "、")
				.replaceAll(" ", ",")
				.replaceAll("\\.", "。");
		MojiConverter converter = new MojiConverter();
		return converter.convertRomajiToHiragana(sourceText.toLowerCase());
	}

	private String toKanji(String sourceText) {
		try {
			HttpUrl url = HttpUrl.parse(API_URL);
			if (url == null) {
				return sourceText;
			}
			url.newBuilder()
					.addQueryParameter("langpair", "ja-Hira|ja")
					.addQueryParameter("text", sourceText)
					.build();
			Request request = new Request.Builder().url(url).build();
			OkHttpClient client = new OkHttpClient.Builder().build();
			ResponseBody body = client.newCall(request).execute().body();
			if (body == null) {
				return sourceText;
			}
			String jsonText = body.string();

			JsonElement root = new JsonParser().parse(jsonText);
			StringBuilder builder = new StringBuilder();
			if (root.isJsonArray()) {
				root.getAsJsonArray().forEach((node) -> builder.append(node
						.getAsJsonArray().get(1)
						.getAsJsonArray().get(0)
						.getAsJsonPrimitive().getAsString()));
			}

			return builder.toString();
		} catch (IOException e) {
			return sourceText;
		}
	}
}
