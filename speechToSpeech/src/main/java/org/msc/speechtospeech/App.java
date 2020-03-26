package org.msc.speechtospeech;

import static spark.Spark.post;
import static spark.Spark.redirect;
import static spark.Spark.staticFiles;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;

import spark.Request;

public class App {
	
	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) {
		logger.debug("OS temporary directory is {}", TEMP_DIR);
		staticFiles.location("/public");
		staticFiles.externalLocation(TEMP_DIR);
		
		//before(new BasicAuthenticationFilter("/*", new AuthenticationDetails("demo", "Accenture2020")));

		redirect.get("/", "demo.html");

		post("/upload", (request, response) -> {
			try {
				request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(TEMP_DIR));
				
				Language source = Language.valueOf(new String(readPartData(request, "sourceLanguage")));
				Language target = Language.valueOf(new String(readPartData(request, "targetLanguage")));			
				logger.debug("Request translation from {} to {}", source, target);		
	
				byte[] audioData = readPartData(request, "audio_data");
				Path audioPath = safeFile(audioData);
				String orgText = speechToText(audioData, source);
				String translatedText = translateText(orgText, source, target);
				Path translatedSpeech = textToSpeech(translatedText, target);
				
				JsonObject res = new JsonObject();
				res.addProperty("org_audio", audioPath.getFileName().toString());
				res.addProperty("org_text", orgText);
				res.addProperty("translated_text", translatedText);
				res.addProperty("translated_audio", translatedSpeech.getFileName().toString());
				
				return res.toString();
			} catch (IndexOutOfBoundsException e) {
				response.status(400);
				return "Unable to perform speech-to-text";
			}
		});
	}
	
	public static byte[] readPartData(Request request, String name) throws IOException, ServletException {
		try (InputStream is = request.raw().getPart(name).getInputStream()) {

			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			return buffer;
		}		
	}
	
	public static Path safeFile(byte[] audioData) throws IOException {
		Path path = getFileName("org");
		try (OutputStream outStream = new FileOutputStream(path.toFile());) {
			outStream.write(audioData);
			return path;
		}
	}
	
	public static String speechToText(byte[] audioData, Language language) throws IOException {
		
		 try (SpeechClient speechClient = SpeechClient.create()) {
			   RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.LINEAR16;
			   RecognitionConfig config = RecognitionConfig.newBuilder()
			     .setEncoding(encoding)
			     .setLanguageCode(language.getLanguageCode()) 
			     .build();

			   RecognitionAudio audio = RecognitionAudio.newBuilder()
			     .setContent(ByteString.copyFrom(audioData))
			     .build();
			   RecognizeResponse response = speechClient.recognize(config, audio);
			   return response.getResults(0).getAlternatives(0).getTranscript();
			 }
	}
	
	public static String translateText(String orgText, Language source, Language target) {
		Translate translate = TranslateOptions.getDefaultInstance().getService();
		Translation translation = translate.translate(
				orgText,
			    TranslateOption.sourceLanguage(source.getLanguageCode()),
			    TranslateOption.targetLanguage(target.getLanguageCode()));
		
		
		return translation.getTranslatedText();
	}
	
	private static Path getFileName(String suffix) {
		return Paths.get(TEMP_DIR, new Date().getTime() + "_" + suffix + ".wav");
	}
	
	public static Path textToSpeech(String translatedText, Language target) throws IOException {		
		 try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
		      // Set the text input to be synthesized
		      SynthesisInput input = SynthesisInput.newBuilder()
		            .setText(translatedText)
		            .build();

		      // Build the voice request, select the language code ("en-US") and the ssml voice gender
		      // ("neutral")
		      VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
		          .setLanguageCode(target.getLanguageCode())
		          .setSsmlGender(SsmlVoiceGender.NEUTRAL)
		          .build();
		      
		      

		      // Select the type of audio file you want returned
		      AudioConfig audioConfig = AudioConfig.newBuilder()
		          .setAudioEncoding(AudioEncoding.LINEAR16)
		          .build();

		      // Perform the text-to-speech request on the text input with the selected voice parameters and
		      // audio file type
		      SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice,
		          audioConfig);

		      // Get the audio contents from the response
		      ByteString audioContents = response.getAudioContent();

		      // Write the response to the output file.
		      Path path = getFileName("output");
		      try (OutputStream out = new FileOutputStream(path.toFile())) {
		        out.write(audioContents.toByteArray());
		      }
		      return path;
		    }
		
	}
	
}
