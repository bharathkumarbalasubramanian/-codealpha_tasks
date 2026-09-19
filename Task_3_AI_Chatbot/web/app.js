/**
 * Java AI Chatbot - Web Application Controller
 */

document.addEventListener('DOMContentLoaded', () => {
    // --- STATE VARIABLES ---
    let isSpeechEnabled = true;
    let isRecording = false;
    let recognition = null;
    let currentFaqs = [];

    // --- DOM ELEMENTS ---
    const navButtons = document.querySelectorAll('.nav-btn');
    const tabPanes = document.querySelectorAll('.tab-pane');

    // Chat Elements
    const chatMessagesContainer = document.getElementById('chatMessages');
    const userChatInput = document.getElementById('userChatInput');
    const btnSendMessage = document.getElementById('btnSendMessage');
    const btnMicInput = document.getElementById('btnMicInput');
    const btnToggleSpeech = document.getElementById('btnToggleSpeech');
    const speechStateText = document.getElementById('speechStateText');
    const btnClearChat = document.getElementById('btnClearChat');
    const typingIndicator = document.getElementById('typingIndicator');
    const quickSuggestions = document.getElementById('quickSuggestions');

    // Inspector Elements
    const inspectIntentTag = document.getElementById('inspectIntentTag');
    const inspectConfBar = document.getElementById('inspectConfBar');
    const inspectConfValue = document.getElementById('inspectConfValue');
    const inspectMatchType = document.getElementById('inspectMatchType');
    const inspectSentiment = document.getElementById('inspectSentiment');
    const inspectTokens = document.getElementById('inspectTokens');
    const inspectStemmed = document.getElementById('inspectStemmed');

    // FAQ Trainer Elements
    const faqTableBody = document.getElementById('faqTableBody');
    const searchFaqInput = document.getElementById('searchFaqInput');
    const btnOpenAddModal = document.getElementById('btnOpenAddModal');
    const intentModal = document.getElementById('intentModal');
    const btnCloseModal = document.getElementById('btnCloseModal');
    const btnCancelModal = document.getElementById('btnCancelModal');
    const intentForm = document.getElementById('intentForm');
    const inputTag = document.getElementById('inputTag');
    const inputCategory = document.getElementById('inputCategory');
    const inputPatterns = document.getElementById('inputPatterns');
    const inputResponses = document.getElementById('inputResponses');
    const inputRegex = document.getElementById('inputRegex');

    // Analytics Elements
    const statTotalQueries = document.getElementById('statTotalQueries');
    const statMatchRate = document.getElementById('statMatchRate');
    const statTotalIntents = document.getElementById('statTotalIntents');
    const statFallbackCount = document.getElementById('statFallbackCount');
    const btnRefreshAnalytics = document.getElementById('btnRefreshAnalytics');

    // --- TAB NAVIGATION ---
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.getAttribute('data-tab');

            navButtons.forEach(b => b.classList.remove('active'));
            tabPanes.forEach(pane => pane.classList.remove('active'));

            btn.classList.add('active');
            document.getElementById(targetTab).classList.add('active');

            if (targetTab === 'tab-trainer') loadFaqs();
            if (targetTab === 'tab-analytics') loadAnalytics();
        });
    });

    // --- CHAT LOGIC ---
    btnSendMessage.addEventListener('click', handleSendMessage);
    userChatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleSendMessage();
    });

    btnClearChat.addEventListener('click', () => {
        chatMessagesContainer.innerHTML = '';
        appendSystemBubble('Chat history cleared.');
    });

    btnToggleSpeech.addEventListener('click', () => {
        isSpeechEnabled = !isSpeechEnabled;
        speechStateText.textContent = `Voice Output: ${isSpeechEnabled ? 'ON' : 'OFF'}`;
        btnToggleSpeech.querySelector('i').className = isSpeechEnabled ? 'fa-solid fa-volume-high' : 'fa-solid fa-volume-xmark';
    });

    // Quick Chips
    quickSuggestions.addEventListener('click', (e) => {
        if (e.target.classList.contains('chip-btn')) {
            const text = e.target.getAttribute('data-query');
            userChatInput.value = text;
            handleSendMessage();
        }
    });

    async function handleSendMessage() {
        const text = userChatInput.value.trim();
        if (!text) return;

        // Render User Bubble
        appendUserBubble(text);
        userChatInput.value = '';

        // Show Typing Indicator
        showTypingIndicator(true);

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: text })
            });

            const data = await response.json();
            showTypingIndicator(false);

            // Render Bot Bubble & Update Inspector
            appendBotBubble(data);
            updateInspector(data);

            // Speech Synthesis
            if (isSpeechEnabled && 'speechSynthesis' in window) {
                speakText(data.botResponse);
            }

        } catch (err) {
            showTypingIndicator(false);
            appendSystemBubble('Error connecting to backend REST server.');
            console.error(err);
        }
    }

    function appendUserBubble(text) {
        const div = document.createElement('div');
        div.className = 'chat-bubble user-bubble';
        div.innerHTML = `
            <div class="avatar"><i class="fa-solid fa-user"></i></div>
            <div class="bubble-content">
                <div class="bubble-header">
                    <span class="sender-name">You</span>
                </div>
                <div class="message-text">${escapeHtml(text)}</div>
            </div>
        `;
        chatMessagesContainer.appendChild(div);
        scrollToBottom();
    }

    function appendBotBubble(data) {
        const div = document.createElement('div');
        div.className = 'chat-bubble bot-bubble';
        
        let badgeClass = 'badge-indigo';
        if (data.matchType === 'RULE_REGEX') badgeClass = 'badge-emerald';
        if (data.matchType === 'FALLBACK') badgeClass = 'badge-rose';

        div.innerHTML = `
            <div class="avatar"><i class="fa-solid fa-robot"></i></div>
            <div class="bubble-content">
                <div class="bubble-header">
                    <span class="sender-name">AI Assistant</span>
                    <span class="badge ${badgeClass}">${data.matchType}</span>
                </div>
                <div class="message-text">${escapeHtml(data.botResponse)}</div>
                <div class="nlp-mini-meta">
                    <span><i class="fa-solid fa-tag"></i> Intent: ${escapeHtml(data.intentTag)}</span>
                    <span><i class="fa-solid fa-gauge-high"></i> Conf: ${data.confidenceScore}%</span>
                    <span><i class="fa-solid fa-face-smile"></i> ${data.sentiment}</span>
                </div>
            </div>
        `;
        chatMessagesContainer.appendChild(div);
        scrollToBottom();
    }

    function appendSystemBubble(text) {
        const div = document.createElement('div');
        div.className = 'chat-bubble bot-bubble';
        div.innerHTML = `
            <div class="avatar"><i class="fa-solid fa-circle-info"></i></div>
            <div class="bubble-content">
                <div class="message-text"><em>${escapeHtml(text)}</em></div>
            </div>
        `;
        chatMessagesContainer.appendChild(div);
        scrollToBottom();
    }

    function showTypingIndicator(show) {
        if (show) {
            typingIndicator.classList.remove('hidden');
        } else {
            typingIndicator.classList.add('hidden');
        }
        scrollToBottom();
    }

    function scrollToBottom() {
        chatMessagesContainer.scrollTop = chatMessagesContainer.scrollHeight;
    }

    function updateInspector(data) {
        inspectIntentTag.textContent = data.intentTag || 'None';
        inspectConfBar.style.width = `${Math.min(100, Math.max(0, data.confidenceScore))}%`;
        inspectConfValue.textContent = `${data.confidenceScore}%`;
        inspectMatchType.textContent = data.matchType || '-';
        
        // Sentiment badge
        inspectSentiment.textContent = data.sentiment || 'NEUTRAL';
        inspectSentiment.className = 'badge ' + 
            (data.sentiment === 'POSITIVE' ? 'badge-emerald' : 
            (data.sentiment === 'NEGATIVE' ? 'badge-rose' : 'badge-neutral'));

        // Tokens cloud
        if (data.tokens && data.tokens.length > 0) {
            inspectTokens.innerHTML = data.tokens.map(t => `<span>${escapeHtml(t)}</span>`).join('');
        } else {
            inspectTokens.innerHTML = `<span class="token-empty">No tokens processed</span>`;
        }

        // Stemmed tokens cloud
        if (data.stemmedTokens && data.stemmedTokens.length > 0) {
            inspectStemmed.innerHTML = data.stemmedTokens.map(s => `<span>${escapeHtml(s)}</span>`).join('');
        } else {
            inspectStemmed.innerHTML = `<span class="token-empty">No stems computed</span>`;
        }
    }

    function speakText(text) {
        if (!('speechSynthesis' in window)) return;
        window.speechSynthesis.cancel(); // Stop current speech
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 1.0;
        utterance.pitch = 1.0;
        window.speechSynthesis.speak(utterance);
    }

    // --- SPEECH RECOGNITION (MIC INPUT) ---
    if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
        const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;
        recognition = new SpeechRec();
        recognition.continuous = false;
        recognition.interimResults = false;

        recognition.onresult = (event) => {
            const transcript = event.results[0][0].transcript;
            userChatInput.value = transcript;
            btnMicInput.classList.remove('recording');
            isRecording = false;
            handleSendMessage();
        };

        recognition.onerror = () => {
            btnMicInput.classList.remove('recording');
            isRecording = false;
        };

        btnMicInput.addEventListener('click', () => {
            if (isRecording) {
                recognition.stop();
                btnMicInput.classList.remove('recording');
                isRecording = false;
            } else {
                recognition.start();
                btnMicInput.classList.add('recording');
                isRecording = true;
            }
        });
    } else {
        btnMicInput.style.display = 'none';
    }

    // --- FAQ TRAINER TAB ---
    async function loadFaqs() {
        try {
            const res = await fetch('/api/faqs');
            currentFaqs = await res.json();
            renderFaqTable(currentFaqs);
        } catch (err) {
            console.error('Error loading FAQs:', err);
        }
    }

    function renderFaqTable(faqs) {
        faqTableBody.innerHTML = '';
        const filter = searchFaqInput.value.toLowerCase().trim();

        faqs.forEach(faq => {
            if (filter) {
                const matchTag = faq.tag.toLowerCase().includes(filter);
                const matchCat = (faq.category || '').toLowerCase().includes(filter);
                const matchPat = faq.patterns.some(p => p.toLowerCase().includes(filter));
                if (!matchTag && !matchCat && !matchPat) return;
            }

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong class="intent-badge-box">${escapeHtml(faq.tag)}</strong></td>
                <td><span class="badge badge-indigo">${escapeHtml(faq.category || 'General')}</span></td>
                <td>
                    <ul style="padding-left:14px;">
                        ${faq.patterns.map(p => `<li>${escapeHtml(p)}</li>`).join('')}
                    </ul>
                </td>
                <td>
                    <ul style="padding-left:14px;">
                        ${faq.responses.map(r => `<li>${escapeHtml(r)}</li>`).join('')}
                    </ul>
                </td>
                <td><code>${escapeHtml(faq.regexRule || '-')}</code></td>
                <td>
                    <div class="action-btns">
                        <button class="icon-btn edit-faq-btn" data-tag="${escapeHtml(faq.tag)}" title="Edit Intent">
                            <i class="fa-solid fa-pen-to-square"></i>
                        </button>
                        <button class="icon-btn delete-faq-btn" data-tag="${escapeHtml(faq.tag)}" title="Delete Intent">
                            <i class="fa-solid fa-trash-can"></i>
                        </button>
                    </div>
                </td>
            `;
            faqTableBody.appendChild(tr);
        });

        // Attach action listeners
        document.querySelectorAll('.delete-faq-btn').forEach(btn => {
            btn.addEventListener('click', () => deleteFaq(btn.getAttribute('data-tag')));
        });

        document.querySelectorAll('.edit-faq-btn').forEach(btn => {
            btn.addEventListener('click', () => openEditModal(btn.getAttribute('data-tag')));
        });
    }

    searchFaqInput.addEventListener('input', () => renderFaqTable(currentFaqs));

    btnOpenAddModal.addEventListener('click', () => {
        intentForm.reset();
        inputTag.removeAttribute('readonly');
        document.getElementById('modalTitle').textContent = 'Create New Intent / FAQ';
        intentModal.classList.remove('hidden');
    });

    btnCloseModal.addEventListener('click', () => intentModal.classList.add('hidden'));
    btnCancelModal.addEventListener('click', () => intentModal.classList.add('hidden'));

    function openEditModal(tag) {
        const faq = currentFaqs.find(f => f.tag === tag);
        if (!faq) return;

        inputTag.value = faq.tag;
        inputTag.setAttribute('readonly', 'true');
        inputCategory.value = faq.category || 'General';
        inputPatterns.value = faq.patterns.join('\n');
        inputResponses.value = faq.responses.join('\n');
        inputRegex.value = faq.regexRule || '';

        document.getElementById('modalTitle').textContent = `Edit Intent: ${tag}`;
        intentModal.classList.remove('hidden');
    }

    intentForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const tag = inputTag.value.trim();
        const category = inputCategory.value.trim();
        const patterns = inputPatterns.value.split('\n').map(p => p.trim()).filter(p => p);
        const responses = inputResponses.value.split('\n').map(r => r.trim()).filter(r => r);
        const regexRule = inputRegex.value.trim();

        if (!tag || patterns.length === 0 || responses.length === 0) {
            alert('Tag, Patterns, and Responses are required.');
            return;
        }

        try {
            const res = await fetch('/api/train', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ tag, category, patterns, responses, regexRule })
            });
            const data = await res.json();
            if (data.success) {
                intentModal.classList.add('hidden');
                loadFaqs();
            } else {
                alert('Error: ' + data.message);
            }
        } catch (err) {
            console.error('Error saving intent:', err);
        }
    });

    async function deleteFaq(tag) {
        if (!confirm(`Are you sure you want to delete intent "${tag}"?`)) return;

        try {
            const res = await fetch('/api/delete-faq', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ tag })
            });
            const data = await res.json();
            if (data.success) {
                loadFaqs();
            }
        } catch (err) {
            console.error('Error deleting intent:', err);
        }
    }

    // --- ANALYTICS TAB ---
    async function loadAnalytics() {
        try {
            const res = await fetch('/api/analytics');
            const data = await res.json();

            statTotalQueries.textContent = data.totalQueries;
            statMatchRate.textContent = `${data.matchRatePercent}%`;
            statTotalIntents.textContent = data.totalIntents;
            statFallbackCount.textContent = data.fallbackCount;
        } catch (err) {
            console.error('Error loading analytics:', err);
        }
    }

    btnRefreshAnalytics.addEventListener('click', loadAnalytics);

    // UTILS
    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
});
