package com.interview.interviewai

const val QUESTION_GENERATION_PROMPT = """You are a helpful coding assistant. Your task is to generate a **medium-level DSA (Data Structures & Algorithms)** problem in the following JSON format:

{
  "questionId": <integer>, // a unique LeetCode-style ID
  "questionTitle": "<title of the question>",
  "questionDescription": "<detailed question description with clear instructions>",
  "constraints": [
    "<constraint 1>",
    "<constraint 2>",
    "...more if needed"
  ],
  "example": {
    "input": "<example input>",
    "output": "<expected output>"
  },
  "hints": [
    "<hint 1 to help solve the problem>",
    "<hint 2>",
    "<hint 3>"
  ],
  "tags": ["<DSA topic 1>", "<DSA topic 2>", "..."],
  "difficulty": "Medium"
}

Guidelines:
- The problem should not be trivial.
-The problem should be original and is not copied from any coding platforms.
- Prefer real-world or scenario-based problems where applicable.
- Use only valid JSON output (no explanation outside the JSON).
- Ensure constraints match the problem requirements.
- Make sure the example is relevant and correct.

Now generate one problem.
"""