package com.chatbot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class representing a chatbot Intent with patterns, responses, rules, and context.
 */
public class Intent {
    private String tag;
    private String category;
    private List<String> patterns;
    private List<String> responses;
    private String regexRule; // Optional regex rule for rule-based matching
    private String contextSet; // Context set by this intent
    private String contextFilter; // Context required for this intent to trigger

    public Intent() {
        this.patterns = new ArrayList<>();
        this.responses = new ArrayList<>();
    }

    public Intent(String tag, String category, List<String> patterns, List<String> responses) {
        this.tag = tag;
        this.category = category;
        this.patterns = patterns != null ? patterns : new ArrayList<>();
        this.responses = responses != null ? responses : new ArrayList<>();
    }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getPatterns() { return patterns; }
    public void setPatterns(List<String> patterns) { this.patterns = patterns; }

    public List<String> getResponses() { return responses; }
    public void setResponses(List<String> responses) { this.responses = responses; }

    public String getRegexRule() { return regexRule; }
    public void setRegexRule(String regexRule) { this.regexRule = regexRule; }

    public String getContextSet() { return contextSet; }
    public void setContextSet(String contextSet) { this.contextSet = contextSet; }

    public String getContextFilter() { return contextFilter; }
    public void setContextFilter(String contextFilter) { this.contextFilter = contextFilter; }

    public String getRandomResponse() {
        if (responses == null || responses.isEmpty()) {
            return "I'm sorry, I don't have a response configured for that.";
        }
        int index = (int) (Math.random() * responses.size());
        return responses.get(index);
    }
}
