/**
 * OpenBrain Universal LLM Provider
 * Pure Fetch implementation - zero heavy dependencies.
 */

import { AgentMessage, LLMProviderConfig, ToolDefinition } from './types.js';

export interface LLMResponse {
  content: string;
  toolCalls?: Array<{
    id: string;
    name: string;
    arguments: Record<string, any>;
  }>;
}

export class UniversalLLM {
  private config: LLMProviderConfig;

  constructor(config: LLMProviderConfig) {
    this.config = config;
  }

  public async generate(messages: AgentMessage[], tools: ToolDefinition[]): Promise<LLMResponse> {
    switch (this.config.provider) {
      case 'gemini':
        return this.callGemini(messages, tools);
      case 'claude':
        return this.callClaude(messages, tools);
      case 'openai':
      case 'ollama':
        return this.callOpenAICompatible(messages, tools);
      case 'mock':
      default:
        return this.mockResponse(messages, tools);
    }
  }

  private async callGemini(messages: AgentMessage[], tools: ToolDefinition[]): Promise<LLMResponse> {
    const apiKey = this.config.apiKey || process.env.GEMINI_API_KEY;
    if (!apiKey) {
      throw new Error('GEMINI_API_KEY não configurada no ambiente.');
    }

    const model = this.config.modelName || 'gemini-2.5-flash';
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;

    const formattedContents = messages
      .filter(m => m.role !== 'system')
      .map(m => ({
        role: m.role === 'assistant' ? 'model' : 'user',
        parts: [{ text: m.content }]
      }));

    const systemInstruction = messages.find(m => m.role === 'system');

    const body: any = {
      contents: formattedContents,
      generationConfig: {
        temperature: this.config.temperature ?? 0.2
      }
    };

    if (systemInstruction) {
      body.systemInstruction = {
        parts: [{ text: systemInstruction.content }]
      };
    }

    // Tools definition for Gemini
    if (tools.length > 0) {
      body.tools = [
        {
          functionDeclarations: tools.map(t => ({
            name: t.name,
            description: t.description,
            parameters: {
              type: 'OBJECT',
              properties: Object.fromEntries(
                Object.entries(t.parameters).map(([key, param]) => [
                  key,
                  {
                    type: param.type.toUpperCase(),
                    description: param.description
                  }
                ])
              ),
              required: Object.entries(t.parameters)
                .filter(([_, param]) => param.required)
                .map(([key]) => key)
            }
          }))
        }
      ];
    }

    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });

    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`Erro na API Gemini (${res.status}): ${errText}`);
    }

    const data = await res.json() as any;
    const candidate = data.candidates?.[0];
    const parts = candidate?.content?.parts || [];

    let textContent = '';
    const toolCalls: any[] = [];

    for (const part of parts) {
      if (part.text) {
        textContent += part.text;
      }
      if (part.functionCall) {
        toolCalls.push({
          id: `call_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
          name: part.functionCall.name,
          arguments: part.functionCall.args || {}
        });
      }
    }

    return {
      content: textContent,
      toolCalls: toolCalls.length > 0 ? toolCalls : undefined
    };
  }

  private async callClaude(messages: AgentMessage[], tools: ToolDefinition[]): Promise<LLMResponse> {
    const apiKey = this.config.apiKey || process.env.ANTHROPIC_API_KEY;
    if (!apiKey) {
      throw new Error('ANTHROPIC_API_KEY não configurada no ambiente.');
    }

    const model = this.config.modelName || 'claude-3-7-sonnet-20250219';
    const url = 'https://api.anthropic.com/v1/messages';

    const systemMsg = messages.find(m => m.role === 'system')?.content || '';
    const nonSystemMsgs = messages.filter(m => m.role !== 'system');

    const formattedMessages = nonSystemMsgs.map(m => ({
      role: m.role === 'assistant' ? 'assistant' : 'user',
      content: m.content
    }));

    const body: any = {
      model,
      max_tokens: 4096,
      system: systemMsg,
      messages: formattedMessages
    };

    if (tools.length > 0) {
      body.tools = tools.map(t => ({
        name: t.name,
        description: t.description,
        input_schema: {
          type: 'object',
          properties: Object.fromEntries(
            Object.entries(t.parameters).map(([key, param]) => [
              key,
              { type: param.type, description: param.description }
            ])
          ),
          required: Object.entries(t.parameters)
            .filter(([_, param]) => param.required)
            .map(([key]) => key)
        }
      }));
    }

    const res = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': apiKey,
        'anthropic-version': '2023-06-01'
      },
      body: JSON.stringify(body)
    });

    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`Erro na API Claude (${res.status}): ${errText}`);
    }

    const data = await res.json() as any;
    let textContent = '';
    const toolCalls: any[] = [];

    for (const block of data.content || []) {
      if (block.type === 'text') {
        textContent += block.text;
      } else if (block.type === 'tool_use') {
        toolCalls.push({
          id: block.id,
          name: block.name,
          arguments: block.input || {}
        });
      }
    }

    return {
      content: textContent,
      toolCalls: toolCalls.length > 0 ? toolCalls : undefined
    };
  }

  private async callOpenAICompatible(messages: AgentMessage[], tools: ToolDefinition[]): Promise<LLMResponse> {
    const isOllama = this.config.provider === 'ollama';
    const baseUrl = this.config.baseUrl || (isOllama ? 'http://localhost:11434/v1' : 'https://api.openai.com/v1');
    const apiKey = this.config.apiKey || (isOllama ? 'ollama' : process.env.OPENAI_API_KEY);

    const body: any = {
      model: this.config.modelName || (isOllama ? 'llama3' : 'gpt-4o'),
      messages: messages.map(m => ({
        role: m.role,
        content: m.content
      })),
      temperature: this.config.temperature ?? 0.2
    };

    if (tools.length > 0) {
      body.tools = tools.map(t => ({
        type: 'function',
        function: {
          name: t.name,
          description: t.description,
          parameters: {
            type: 'object',
            properties: Object.fromEntries(
              Object.entries(t.parameters).map(([key, param]) => [
                key,
                { type: param.type, description: param.description }
              ])
            ),
            required: Object.entries(t.parameters)
              .filter(([_, param]) => param.required)
              .map(([key]) => key)
          }
        }
      }));
    }

    const res = await fetch(`${baseUrl}/chat/completions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${apiKey}`
      },
      body: JSON.stringify(body)
    });

    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`Erro na API OpenAI/Ollama (${res.status}): ${errText}`);
    }

    const data = await res.json() as any;
    const msg = data.choices?.[0]?.message;

    const toolCalls = msg?.tool_calls?.map((tc: any) => ({
      id: tc.id,
      name: tc.function.name,
      arguments: JSON.parse(tc.function.arguments || '{}')
    }));

    return {
      content: msg?.content || '',
      toolCalls
    };
  }

  private mockResponse(messages: AgentMessage[], tools: ToolDefinition[]): Promise<LLMResponse> {
    const lastMsg = messages[messages.length - 1]?.content || '';
    return Promise.resolve({
      content: `[OpenBrain Mock Mode] Processei a instrução: "${lastMsg}". Para ativar a inteligência real, configure GEMINI_API_KEY ou ANTHROPIC_API_KEY.`
    });
  }
}
