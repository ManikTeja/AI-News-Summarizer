package com.news;

import com.google.gson.Gson;
import spark.Spark;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NewsSummarizerSystem {
    
    private static final Gson gson = new Gson();
    private static final NLPSummarizer nlpSummarizer = new NLPSummarizer();
    private static final List<Article> sampleArticles = new ArrayList<>();
    
    public static void main(String[] args) {
        initializeSampleArticles();
        setupRESTAPI();
        
        System.out.println("\n==============================================");
        System.out.println("AI News Summarizer System Started!");
        System.out.println("==============================================");
        System.out.println("Access the application at: http://localhost:4567");
        System.out.println("API Endpoints:");
        System.out.println("  POST /api/summarize - Summarize article text");
        System.out.println("  GET  /api/samples - Get sample articles");
        System.out.println("  GET  / - Web Interface");
        System.out.println("==============================================\n");
    }
    
    private static void initializeSampleArticles() {
        sampleArticles.add(new Article(
            "AI Breakthrough in Healthcare",
            "Artificial intelligence has made significant strides in healthcare diagnostics. Researchers at Stanford University have developed an AI system that can detect diseases from medical images with 95% accuracy. The system uses deep learning algorithms trained on millions of medical scans. This breakthrough could revolutionize early disease detection and save countless lives. The AI can identify patterns that human doctors might miss, leading to earlier interventions. Healthcare professionals are excited about integrating this technology into clinical practice. The system has been tested across multiple hospitals with impressive results. Experts believe this is just the beginning of AI's impact on medicine.",
            "Technology"
        ));
        
        sampleArticles.add(new Article(
            "Climate Change Action Summit",
            "World leaders gathered in Geneva for the Global Climate Action Summit. The three-day conference focused on reducing carbon emissions and implementing renewable energy solutions. Over 150 countries pledged to achieve net-zero emissions by 2050. Scientists presented alarming data about rising global temperatures and melting ice caps. The summit resulted in a historic agreement to invest $500 billion in green technology. Developing nations will receive financial aid to transition to clean energy. Environmental activists praised the commitments but emphasized the need for immediate action. The next summit is scheduled for 2026 to review progress and set new targets.",
            "Environment"
        ));
        
        sampleArticles.add(new Article(
            "Tech Giant Launches Revolutionary Smartphone",
            "TechCorp unveiled its latest smartphone featuring groundbreaking holographic display technology. The device can project 3D images without requiring special glasses. It includes an advanced AI assistant that learns user preferences and anticipates needs. The phone's battery lasts up to three days on a single charge thanks to new graphene technology. The camera system uses computational photography to capture professional-quality images. Pre-orders have already exceeded expectations with over 2 million units sold in the first week. The phone will be available globally next month starting at $899. Industry analysts predict this will set new standards for smartphone innovation.",
            "Technology"
        ));
        
        System.out.println("Loaded " + sampleArticles.size() + " sample articles");
    }
    
    private static void setupRESTAPI() {
        Spark.port(4567);
        
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST");
            response.type("application/json");
        });
        
        Spark.post("/api/summarize", (req, res) -> {
            SummarizeRequest request = gson.fromJson(req.body(), SummarizeRequest.class);
            
            if (request.text == null || request.text.trim().isEmpty()) {
                return gson.toJson(Map.of("error", "Text is required"));
            }
            
            int sentenceCount = request.sentences > 0 ? request.sentences : 3;
            String summary = nlpSummarizer.summarize(request.text, sentenceCount);
            
            SummarizeResponse response = new SummarizeResponse();
            response.originalText = request.text;
            response.summary = summary;
            response.originalWordCount = countWords(request.text);
            response.summaryWordCount = countWords(summary);
            response.compressionRatio = String.format("%.1f%%", 
                (1 - (double)response.summaryWordCount / response.originalWordCount) * 100);
            response.keyPoints = nlpSummarizer.extractKeyPoints(request.text);
            
            return gson.toJson(response);
        });
        
        Spark.get("/api/samples", (req, res) -> gson.toJson(sampleArticles));
        
        Spark.get("/", (req, res) -> {
            res.type("text/html");
            return getDashboardHTML();
        });
    }
    
    private static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return text.trim().split("\\s+").length;
    }
    
    private static String getDashboardHTML() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>AI News Summarizer</title><style>" +
            "body{font-family:'Segoe UI',Arial,sans-serif;margin:0;padding:20px;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);min-height:100vh}" +
            ".container{max-width:1200px;margin:0 auto}" +
            ".header{text-align:center;color:white;margin-bottom:30px}" +
            ".header h1{font-size:42px;margin:0 0 10px 0;text-shadow:2px 2px 4px rgba(0,0,0,0.3)}" +
            ".header p{font-size:18px;opacity:0.9}" +
            ".main-content{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px}" +
            ".card{background:white;border-radius:12px;padding:25px;box-shadow:0 8px 16px rgba(0,0,0,0.2)}" +
            ".card h2{margin:0 0 20px 0;color:#667eea;font-size:24px}" +
            "textarea{width:100%;min-height:300px;padding:15px;border:2px solid #e0e0e0;border-radius:8px;font-size:14px;font-family:inherit;resize:vertical;box-sizing:border-box}" +
            "textarea:focus{outline:none;border-color:#667eea}" +
            ".controls{display:flex;gap:10px;margin-top:15px;align-items:center;flex-wrap:wrap}" +
            "button{padding:12px 24px;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:600;transition:all 0.3s}" +
            ".btn-primary{background:linear-gradient(135deg,#667eea,#764ba2);color:white;box-shadow:0 4px 8px rgba(102,126,234,0.4)}" +
            ".btn-primary:hover{transform:translateY(-2px);box-shadow:0 6px 12px rgba(102,126,234,0.6)}" +
            ".btn-secondary{background:#f0f0f0;color:#333}" +
            ".btn-secondary:hover{background:#e0e0e0}" +
            ".btn-small{padding:6px 12px;font-size:14px}" +
            "label{display:block;margin:10px 0 5px 0;font-weight:600;color:#555}" +
            "input[type='number']{padding:8px;border:2px solid #e0e0e0;border-radius:6px;width:80px;font-size:14px}" +
            ".summary-section{background:#f8f9ff;padding:20px;border-radius:8px;margin-top:15px;border-left:4px solid #667eea}" +
            ".summary-text{line-height:1.8;color:#333;font-size:15px}" +
            ".stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:10px;margin:15px 0}" +
            ".stat-item{background:white;padding:15px;border-radius:8px;text-align:center;box-shadow:0 2px 4px rgba(0,0,0,0.1)}" +
            ".stat-value{font-size:24px;font-weight:bold;color:#667eea}" +
            ".stat-label{font-size:12px;color:#666;margin-top:5px}" +
            ".key-points{margin-top:20px}" +
            ".key-points h3{color:#667eea;margin-bottom:10px}" +
            ".key-point{background:white;padding:12px;margin:8px 0;border-radius:6px;border-left:3px solid #4CAF50;box-shadow:0 2px 4px rgba(0,0,0,0.05)}" +
            ".samples{margin-top:20px}" +
            ".sample-article{background:#f8f9ff;padding:15px;margin:10px 0;border-radius:8px;cursor:pointer;transition:all 0.3s;border:2px solid transparent}" +
            ".sample-article:hover{border-color:#667eea;transform:translateX(5px);box-shadow:0 4px 8px rgba(0,0,0,0.1)}" +
            ".sample-title{font-weight:bold;color:#333;margin-bottom:5px}" +
            ".sample-category{display:inline-block;padding:4px 12px;background:#667eea;color:white;border-radius:12px;font-size:12px;margin-bottom:8px}" +
            ".sample-text{color:#666;font-size:14px;line-height:1.6}" +
            ".loading{display:none;text-align:center;padding:20px;color:#667eea}" +
            ".spinner{border:4px solid #f3f3f3;border-top:4px solid #667eea;border-radius:50%;width:40px;height:40px;animation:spin 1s linear infinite;margin:0 auto}" +
            "@keyframes spin{0%{transform:rotate(0deg)}100%{transform:rotate(360deg)}}" +
            ".full-width{grid-column:1/-1}" +
            "@media(max-width:768px){.main-content{grid-template-columns:1fr}}" +
            "</style></head><body>" +
            "<div class='container'>" +
            "<div class='header'><h1>AI News Summarizer</h1><p>Extract key insights from long articles instantly using advanced NLP</p></div>" +
            "<div class='main-content'>" +
            "<div class='card'><h2>Input Article</h2>" +
            "<textarea id='inputText' placeholder='Paste your news article here or select a sample below...'></textarea>" +
            "<div class='controls'>" +
            "<button class='btn-primary' onclick='summarize()'>Summarize Article</button>" +
            "<label>Sentences: <input type='number' id='sentences' value='3' min='1' max='10'></label>" +
            "<button class='btn-secondary btn-small' onclick='clearAll()'>Clear</button>" +
            "</div>" +
            "<div class='samples'><h3>Sample Articles:</h3><div id='samplesList'></div></div>" +
            "</div>" +
            "<div class='card'><h2>Summary & Analysis</h2>" +
            "<div class='loading' id='loading'><div class='spinner'></div><p>Analyzing article...</p></div>" +
            "<div id='output' style='display:none'>" +
            "<div class='stats'>" +
            "<div class='stat-item'><div class='stat-value' id='originalWords'>0</div><div class='stat-label'>Original Words</div></div>" +
            "<div class='stat-item'><div class='stat-value' id='summaryWords'>0</div><div class='stat-label'>Summary Words</div></div>" +
            "<div class='stat-item'><div class='stat-value' id='compression'>0%</div><div class='stat-label'>Compressed</div></div>" +
            "</div>" +
            "<div class='summary-section'><strong>Summary:</strong><div class='summary-text' id='summaryText'></div></div>" +
            "<div class='key-points' id='keyPointsSection'></div>" +
            "</div></div></div></div>" +
            "<script>" +
            "async function summarize(){" +
            "const text=document.getElementById('inputText').value.trim();" +
            "if(!text){alert('Please enter article text');return;}" +
            "document.getElementById('loading').style.display='block';" +
            "document.getElementById('output').style.display='none';" +
            "try{const response=await fetch('/api/summarize',{method:'POST',headers:{'Content-Type':'application/json'}," +
            "body:JSON.stringify({text:text,sentences:parseInt(document.getElementById('sentences').value)})});" +
            "const data=await response.json();" +
            "document.getElementById('originalWords').textContent=data.originalWordCount;" +
            "document.getElementById('summaryWords').textContent=data.summaryWordCount;" +
            "document.getElementById('compression').textContent=data.compressionRatio;" +
            "document.getElementById('summaryText').textContent=data.summary;" +
            "let keyPointsHTML='<h3>Key Points:</h3>';" +
            "data.keyPoints.forEach(point=>{keyPointsHTML+=`<div class='key-point'>${point}</div>`;});" +
            "document.getElementById('keyPointsSection').innerHTML=keyPointsHTML;" +
            "document.getElementById('loading').style.display='none';" +
            "document.getElementById('output').style.display='block';" +
            "}catch(error){alert('Error: '+error.message);document.getElementById('loading').style.display='none';}}" +
            "function clearAll(){document.getElementById('inputText').value='';document.getElementById('output').style.display='none';}" +
            "let allSamples=[];" +
            "function loadSample(index){document.getElementById('inputText').value=allSamples[index].content;document.getElementById('inputText').scrollIntoView({behavior:'smooth'});}" +
            "async function loadSamples(){try{const response=await fetch('/api/samples');allSamples=await response.json();" +
            "const list=document.getElementById('samplesList');" +
            "list.innerHTML=allSamples.map((s,i)=>`<div class='sample-article' onclick='loadSample(${i})'>" +
            "<div class='sample-category'>${s.category}</div><div class='sample-title'>${s.title}</div>" +
            "<div class='sample-text'>${s.content.substring(0,150)}...</div></div>`).join('');" +
            "}catch(error){console.error('Error loading samples:',error);}}" +
            "loadSamples();" +
            "</script></body></html>";
    }
}

class Article {
    String title;
    String content;
    String category;
    
    public Article(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }
}

class SummarizeRequest {
    String text;
    int sentences;
}

class SummarizeResponse {
    String originalText;
    String summary;
    int originalWordCount;
    int summaryWordCount;
    String compressionRatio;
    List<String> keyPoints;
}

class NLPSummarizer {
    
    public String summarize(String text, int numSentences) {
        List<String> sentences = splitIntoSentences(text);
        
        if (sentences.size() <= numSentences) {
            return text;
        }
        
        Map<String, Double> wordFrequencies = calculateWordFrequencies(text);
        Map<String, Double> sentenceScores = scoreSentences(sentences, wordFrequencies);
        
        List<String> topSentences = sentenceScores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(numSentences)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        List<String> orderedSentences = new ArrayList<>();
        for (String sentence : sentences) {
            if (topSentences.contains(sentence)) {
                orderedSentences.add(sentence);
            }
        }
        
        return String.join(" ", orderedSentences);
    }
    
    public List<String> extractKeyPoints(String text) {
        List<String> sentences = splitIntoSentences(text);
        Map<String, Double> wordFrequencies = calculateWordFrequencies(text);
        Map<String, Double> sentenceScores = scoreSentences(sentences, wordFrequencies);
        
        return sentenceScores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(Math.min(5, sentences.size()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    private List<String> splitIntoSentences(String text) {
        Pattern sentencePattern = Pattern.compile("[^.!?]+[.!?]+");
        Matcher matcher = sentencePattern.matcher(text);
        List<String> sentences = new ArrayList<>();
        
        while (matcher.find()) {
            String sentence = matcher.group().trim();
            if (sentence.length() > 10) {
                sentences.add(sentence);
            }
        }
        
        return sentences;
    }
    
    private Map<String, Double> calculateWordFrequencies(String text) {
        String[] words = text.toLowerCase()
            .replaceAll("[^a-z\\s]", "")
            .split("\\s+");
        
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "were", "been",
            "be", "have", "has", "had", "do", "does", "did", "will", "would",
            "could", "should", "may", "might", "can", "this", "that", "these",
            "those", "it", "its", "they", "them", "their", "he", "she", "his",
            "her", "we", "our", "you", "your"
        ));
        
        Map<String, Integer> wordCounts = new HashMap<>();
        for (String word : words) {
            if (word.length() > 2 && !stopWords.contains(word)) {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }
        
        int maxFreq = wordCounts.values().stream().max(Integer::compare).orElse(1);
        
        Map<String, Double> frequencies = new HashMap<>();
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            frequencies.put(entry.getKey(), (double) entry.getValue() / maxFreq);
        }
        
        return frequencies;
    }
    
    private Map<String, Double> scoreSentences(List<String> sentences, Map<String, Double> wordFrequencies) {
        Map<String, Double> scores = new HashMap<>();
        
        for (String sentence : sentences) {
            String[] words = sentence.toLowerCase()
                .replaceAll("[^a-z\\s]", "")
                .split("\\s+");
            
            double score = 0;
            int wordCount = 0;
            
            for (String word : words) {
                if (wordFrequencies.containsKey(word)) {
                    score += wordFrequencies.get(word);
                    wordCount++;
                }
            }
            
            if (wordCount > 0) {
                score = score / wordCount;
                
                if (sentence.matches(".*\\b(important|significant|critical|key|major|essential)\\b.*")) {
                    score *= 1.5;
                }
                
                if (sentence.length() < 50) {
                    score *= 0.8;
                }
                
                scores.put(sentence, score);
            }
        }
        
        return scores;
    }
}