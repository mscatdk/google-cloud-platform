URL = window.URL || window.webkitURL;

var gumStream; 						
var rec;
var input;
 
var AudioContext = window.AudioContext || window.webkitAudioContext;
var audioContext

var recordButton = document.getElementById("recordButton");
var stopButton = document.getElementById("stopButton");
var pauseButton = document.getElementById("pauseButton");

var spinner = document.getElementById("spinner");
spinner.style.display = "none";

var messageBox = document.getElementById("messageBox");

var ua = window.navigator.userAgent;
var isIE = /MSIE|Trident/.test(ua);

if ( isIE ) {
	printErrorMessage("Please use a more recent browser e.g. Google Chrome.");
} else {
	printMessage("Press Record")
}

var targetLanguage = document.getElementById("targetLanguage");
var sourceLanguage = document.getElementById("sourceLanguage");

recordButton.addEventListener("click", startRecording);
stopButton.addEventListener("click", stopRecording);
pauseButton.addEventListener("click", pauseRecording);
uploadButton.addEventListener("click", uploadFile);

function uploadFile() {
	let data = document.getElementById("audioUpload").files[0];
	processData(data)
}

function startRecording() {    
    var constraints = { audio: true, video:false }

	recordButton.disabled = true;
	stopButton.disabled = false;
	pauseButton.disabled = false

	navigator.mediaDevices.getUserMedia(constraints).then(function(stream) {
		audioContext = new AudioContext();
		gumStream = stream;
		
		input = audioContext.createMediaStreamSource(stream);
		rec = new Recorder(input,{numChannels:1})
		rec.record()

		printMessage("Speak now and then press stop.");

	}).catch(function(err) {
    	recordButton.disabled = false;
    	stopButton.disabled = true;
    	pauseButton.disabled = true
    	
    	console.log("Error");
    	console.log(err);
	});
}

function pauseRecording(){
	if (rec.recording){
		rec.stop();
		pauseButton.innerHTML="Resume";
	}else{
		rec.record()
		pauseButton.innerHTML="Pause";

	}
}

function stopRecording() {
	console.log("stopButton clicked");
	spinner.style.display = "block";
	
	stopButton.disabled = true;
	recordButton.disabled = false;
	pauseButton.disabled = true;

	pauseButton.innerHTML="Pause";
	
	rec.stop();
	
	gumStream.getAudioTracks()[0].stop();
	printMessage("Processing...");
	rec.exportWAV(processData);
}

function processData(blob) {
	var filename = new Date().toISOString();

	  var xhr=new XMLHttpRequest();
	  xhr.onload=function(e) {
	      if(this.readyState === 4 && this.status === 200) {
	    	  var obj = JSON.parse(e.target.responseText);
	    	  spinner.style.display = "none";
	    	  
	    	  document.getElementById("resultHeading").innerHTML = '<p class="h3">Result</p>';
	    	  document.getElementById("rawSpeech").innerHTML = '<b>Original Recording: </b><input type="button" class="btn btn-info btn-sm" value="Play" onclick="play(\'rawSpeechAudio\')"><audio id="rawSpeechAudio" src="' + obj.org_audio + '"></audio>';
	    	  document.getElementById("rawText").innerHTML = '<b>Speech-to-text output: </b>' + obj.org_text;
	    	  document.getElementById("translatedText").innerHTML = '<b>Translated text: </b>' + obj.translated_text;
	    	  document.getElementById("translatedSpeech").innerHTML = '<b>Translation: </b><input type="button" class="btn btn-info btn-sm" value="Play" onclick="play(\'translatedSpeechAudio\')"><audio id="translatedSpeechAudio" src="' + obj.translated_audio + '"></audio>';
	    	  
	    	  printMessage("Press Record");
	    	  
	          console.log("Server returned: ", obj);
	      } else if (this.readyState === 4 && this.status === 400) {
	    	  printErrorMessage("Unable to perform speech-to-text. Check your microphone.")	    	  
	      } else {
	    	  printErrorMessage("Unexpected error! Please try again.")
	      }
	  };
	  xhr.onerror = function() {
		  printErrorMessage("Unexpected error! Please try again.")
	  };
	  var fd=new FormData();
	  fd.append("sourceLanguage", sourceLanguage.options[sourceLanguage.selectedIndex].value);
	  fd.append("targetLanguage", targetLanguage.options[targetLanguage.selectedIndex].value);
	  fd.append("audio_data",blob, filename);
	  xhr.open("POST","upload",true);
	  xhr.send(fd);
}

function printErrorMessage(msg) {
	  spinner.style.display = "none";
	  messageBox.innerHTML = '<p class="h3">' + msg + '</p>';
	  messageBox.className = "alert alert-danger";
}

function printMessage(msg) {
	messageBox.innerHTML = '<p class="h3">' + msg + '</p>';
	messageBox.className = "alert alert-secondary";
}

function play(id) {
    var audio = document.getElementById(id);
    audio.play();
 }