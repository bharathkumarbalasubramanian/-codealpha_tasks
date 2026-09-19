# Task 3: Artificial Intelligence Chatbot

A Java-based AI Chatbot incorporating **Natural Language Processing (NLP)** techniques, a hybrid **Machine Learning & Rule-Based** intent matching engine, dynamic **FAQ dataset training**, an interactive **Glassmorphic Web Interface**, and a **Desktop Java Swing GUI**.

---

## 🌟 Key Features

1. **Natural Language Processing (NLP) Engine**:
   - **Tokenization & Cleaning**: Case normalization, special character stripping, token extraction.
   - **Stop-words Removal**: Custom English stop-words filter to reduce vector noise.
   - **Porter Stemmer Algorithm**: Custom implementation of Martin Porter's stemming algorithm for root word normalization.
   - **TF-IDF Vectorization**: Term Frequency-Inverse Document Frequency feature vector construction.
   - **Cosine Similarity Calculator**: Calculates angular vector similarity between user query vectors and trained intent patterns.
   - **Sentiment Analysis**: Multi-class sentiment detection (`POSITIVE`, `NEGATIVE`, `NEUTRAL`).

2. **Machine Learning & Rule-Based Hybrid Engine**:
   - **ML Classifier**: TF-IDF + Cosine Similarity matching with dynamic confidence scoring (0-100%).
   - **Rule Engine**: Regex pattern triggers for exact rule execution.
   - **Context Tracking**: Session context filters and state transitions.
   - **Smart Fallback**: Sentiment-aware response fallbacks.

3. **Trainable FAQ Repository**:
   - Pre-loaded domain datasets: **Technical (Java)**, **AI/NLP**, **E-Commerce/Order Tracking**, **Academic/Scholarships**, **General Knowledge**.
   - Live Retraining & JSON Persistence (`data/faqs.json`).

4. **Dual Interactive Interfaces**:
   - **Glassmorphic Web UI**: Real-time chat, typing indicators, audio Speech Synthesis (TTS), Web Speech API mic input, side **NLP Query Inspector**, live **FAQ Trainer Studio**, and **Analytics Dashboard**.
   - **Desktop Swing GUI**: Native Java desktop window experience.

---

## 🛠️ Project Structure

```
Task_3_AI_Chatbot/
├── src/
│   └── com/chatbot/
│       ├── nlp/               # Tokenizer, StopWords, PorterStemmer, TFIDF, Cosine Sim, Sentiment
│       ├── model/             # Intent, ChatMessage, FAQDataset
│       ├── engine/            # HybridChatEngine, TrainerEngine
│       ├── server/            # ChatbotWebServer, StaticFileHandler
│       ├── gui/               # SwingChatGUI
│       └── App.java           # Main Entry Point
├── web/
│   ├── index.html             # Web UI Layout
│   ├── styles.css             # Glassmorphism Dark Theme CSS
│   └── app.js                 # JS Client & REST Communication
├── data/
│   └── faqs.json              # Trained Intents & FAQ Dataset
├── build.bat                  # Compile script
├── run_web.bat                # Start Web Server script
└── run_swing.bat              # Start Desktop GUI script
```

---

## 🚀 How to Run

### Step 1: Compile Code
Run `build.bat` or compile with `javac`:
```cmd
build.bat
```

### Step 2: Launch Options

#### Option A: Launch Interactive Web UI (Recommended)
```cmd
run_web.bat
```
Open **`http://localhost:8080`** in your browser.

#### Option B: Launch Desktop Java Swing GUI
```cmd
run_swing.bat
```

#### Option C: Run Automated NLP Engine Tests
```cmd
java -cp bin com.chatbot.App --test
```
