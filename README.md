# Java-GPT-Assistant
This is a Java-based project that uses the OpenAI API to create a chatbot assistant. The assistant can communicate via text or voice and can perform various tasks such as answering questions, performing a Google search, and interacting with the file system.

- [X] GPT with chat history
- [X] TTS with elevenlabs
- [ ] Audio input with whisper api
- [ ] Commands for AI to execute

*run src/main/java/Assistant/Main.java for CLI chats*

Commands really only work with gpt-4, as intital prompt is too long for other models to use properly



## Installation
1. Clone the repository
2. Navigate to the project directory
3. Build the project with Maven: mvn package
4. Run the project: java -jar target/Java-GPT-Assistant-1.0-SNAPSHOT.jar


## Usage
The assistant can communicate via text or voice. When prompted, type 't' to communicate via text or 'v' to communicate via voice.

Once the assistant is running, you can ask it questions or give it commands. The following commands are supported:
- [X] write_to_file: write the assistant's response to a file
- [ ] download_file: download a file from a URL
- [X] self_prompt: generate a prompt from the assistant's previous responses
- [X] read_file: read a file from the file system
- [X] read_directory: read a directory from the file system
- [ ] google_search: perform a Google search
- [ ] search_website: search a website
To exit the assistant, type 'q'.


## Environment Variables
This project requires several environment variables to be set in order to function properly:
```
OPEN_AI_API_KEY: API key for the OpenAI API
OPEN_AI_MODEL: name of the OpenAI GPT model to use
ELEVENLABS_API_KEY: API key for the ElevenLabs TTS API
ELEVENLABS_VOICE_ID: voice ID for the ElevenLabs TTS API
WORKING_DIRECTORY: path to the working directory for the assistant
```
To set these environment variables, remove .template from .env.template and your keys

## Dependencies
This project uses the following dependencies:
```
Maven: for build management
OpenAI API: for natural language processing
ElevenLabs TTS API: for text-to-speech functionality
RxJava: for reactive programming
Dotenv: for loading environment variables from a .env file
Beads: for audio processing
```


_This readme is written by gpt3.5_
