import express from "express";
import helmet from "helmet";
import rateLimit from "express-rate-limit";
import dotenv from "dotenv";
import { GoogleGenAI } from "@google/genai";

dotenv.config();

const app = express();
const PORT = 3000;
const MODEL = process.env.GEMINI_MODEL || "gemini-3.8-flash";

app.use(helmet());
app.use(express.json({ limit: "20kb" }));

app.use("/api/", rateLimit({
  windowMs: 60 * 1000,
  limit: 20,
  standardHeaders: "draft-7",
  legacyHeaders: false
}));

const personality = `
You are Loki, a funny, witty and helpful AI assistant inside an
Android launcher called BLACKHOLE.

Personality:
- Speak naturally in Hindi, English or Hinglish.
- Be funny, playful and lightly sarcastic, never insulting.
- Keep normal replies concise and useful.
- Understand phone-related requests.
- Never claim you performed a phone action unless the Android app
  confirms it.
- Never ask the user to reveal passwords, API keys or private codes.
`;

app.get("/health", (req, res) => {
  res.json({ ok: true, assistant: "Loki" });
});

app.post("/api/chat", async (req, res) => {
  try {
    const apiKey = process.env.GEMINI_API_KEY;

    if (!apiKey) {
      return res.status(503).json({
        error: "Gemini API key is not configured on the server."
      });
    }

    const messages = req.body?.messages;

    if (!Array.isArray(messages) || messages.length === 0) {
      return res.status(400).json({
        error: "Please send a chat message."
      });
    }

    const history = messages.slice(-12).map((item) => {
      if (!["user", "model"].includes(item.role)) {
        throw new Error("Invalid message role.");
      }

      if (typeof item.text !== "string" ||
          item.text.length > 4000 ||
          !item.text.trim()) {
        throw new Error("Invalid message text.");
      }

      return {
        role: item.role,
        parts: [{ text: item.text.trim() }]
      };
    });

    const ai = new GoogleGenAI({ apiKey });

    const result = await ai.models.generateContent({
      model: MODEL,
      contents: history,
      config: {
        systemInstruction: personality,
        temperature: 0.85
      }
    });

    res.json({
      reply: result.text || "Ek second... Loki ko sochne do!"
    });

  } catch (error) {
    console.error("Loki server error:", error.message);

    res.status(500).json({
      error: "Loki abhi reply nahi kar pa raha. Server logs check karo."
    });
  }
});

app.listen(PORT, "127.0.0.1", () => {
  console.log(`Loki server running at http://127.0.0.1:${PORT}`);
  console.log(`Gemini model: ${MODEL}`);
});
