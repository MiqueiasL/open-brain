/**
 * OpenBrain Core Types
 */

export type PlatformType = 'windows' | 'linux' | 'macos' | 'android' | 'ios' | 'unknown';

export interface DeviceInfo {
  platform: PlatformType;
  arch: string;
  osRelease: string;
  hostname: string;
  isRootOrAdmin: boolean;
  isTermuxOrShizuku: boolean;
  isKaliLinux: boolean;
  availableTools: string[];
}

export interface CommandExecutionOptions {
  cwd?: string;
  timeoutMs?: number;
  elevated?: boolean;
}

export interface CommandResult {
  stdout: string;
  stderr: string;
  exitCode: number;
  durationMs: number;
}

export interface ToolParameter {
  type: 'string' | 'number' | 'boolean' | 'object' | 'array';
  description: string;
  required?: boolean;
}

export interface ToolDefinition {
  name: string;
  description: string;
  parameters: Record<string, ToolParameter>;
  execute: (args: Record<string, any>, context: AgentExecutionContext) => Promise<any>;
}

export interface MemoryEntry {
  id: string;
  title: string;
  content: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  platform?: PlatformType;
}

export interface AgentMessage {
  role: 'system' | 'user' | 'assistant' | 'tool';
  content: string;
  toolCalls?: Array<{
    id: string;
    name: string;
    arguments: Record<string, any>;
  }>;
  toolCallId?: string;
}

export interface AgentExecutionContext {
  device: DeviceInfo;
  workingDirectory: string;
  memory: any;
  tools: Map<string, ToolDefinition>;
  log: (message: string, level?: 'info' | 'warn' | 'error' | 'step') => void;
}

export interface LLMProviderConfig {
  provider: 'gemini' | 'claude' | 'openai' | 'ollama' | 'mock';
  apiKey?: string;
  baseUrl?: string;
  modelName: string;
  temperature?: number;
}
