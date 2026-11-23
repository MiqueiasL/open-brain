/**
 * OpenBrain Autonomous Agent Engine
 */

import { AgentExecutionContext, AgentMessage, DeviceInfo, ToolDefinition } from './types.js';
import { UniversalLLM } from './llm.js';

export class OpenBrainAgent {
  private llm: UniversalLLM;
  private tools: Map<string, ToolDefinition> = new Map();
  private device: DeviceInfo;
  private memory: any;
  private maxSteps: number;

  constructor(options: {
    llm: UniversalLLM;
    device: DeviceInfo;
    memory: any;
    tools?: ToolDefinition[];
    maxSteps?: number;
  }) {
    this.llm = options.llm;
    this.device = options.device;
    this.memory = options.memory;
    this.maxSteps = options.maxSteps ?? 15;

    if (options.tools) {
      for (const tool of options.tools) {
        this.registerTool(tool);
      }
    }
  }

  public registerTool(tool: ToolDefinition): void {
    this.tools.set(tool.name, tool);
  }

  public async run(goal: string, onProgress?: (msg: string) => void): Promise<string> {
    const log = (msg: string) => {
      if (onProgress) onProgress(msg);
    };

    log(`[OpenBrain] 🧠 Iniciando tarefa: "${goal}"`);
    log(`[OpenBrain] 📱 Dispositivo: ${this.device.platform.toUpperCase()} (${this.device.arch})`);

    const context: AgentExecutionContext = {
      device: this.device,
      workingDirectory: process.cwd(),
      memory: this.memory,
      tools: this.tools,
      log: (m) => log(m)
    };

    // System prompt with device context & rules
    const systemPrompt = `Você é o OpenBrain, um agente autônomo e cérebro universal de I.A que opera em qualquer dispositivo.
Ambiente Atual:
- Sistema Operacional: ${this.device.platform.toUpperCase()} (${this.device.osRelease})
- Arquitetura: ${this.device.arch}
- Hostname: ${this.device.hostname}
- Kali Linux detectado: ${this.device.isKaliLinux ? 'SIM' : 'NÃO'}
- Permissões elevadas / Root: ${this.device.isRootOrAdmin ? 'SIM' : 'NÃO'}
- Modo Android / Termux: ${this.device.isTermuxOrShizuku ? 'SIM' : 'NÃO'}

Diretrizes de Operação:
1. Você tem ferramentas reais para executar comandos no sistema operacional, ler e gravar arquivos e consultar memória.
2. Pense passo a passo (ReAct: Raciocinar -> Chamar Ferramenta -> Observar -> Responder).
3. Seja conciso, direto e reporte o status de cada ação.
4. Quando a tarefa estiver concluída com sucesso, resuma o que foi feito.`;

    const messages: AgentMessage[] = [
      { role: 'system', content: systemPrompt },
      { role: 'user', content: goal }
    ];

    let step = 0;
    const toolList = Array.from(this.tools.values());

    while (step < this.maxSteps) {
      step++;
      log(`[OpenBrain] 🔄 Ciclo ${step}/${this.maxSteps}...`);

      const response = await this.llm.generate(messages, toolList);

      // If there are tool calls, execute them
      if (response.toolCalls && response.toolCalls.length > 0) {
        messages.push({
          role: 'assistant',
          content: response.content || 'Executando ferramentas...',
          toolCalls: response.toolCalls
        });

        for (const tc of response.toolCalls) {
          log(`[OpenBrain] 🛠️ Chamando ferramenta: ${tc.name} (${JSON.stringify(tc.arguments)})`);
          const tool = this.tools.get(tc.name);

          let resultStr = '';
          if (!tool) {
            resultStr = `Erro: Ferramenta "${tc.name}" não encontrada no OpenBrain.`;
          } else {
            try {
              const res = await tool.execute(tc.arguments, context);
              resultStr = typeof res === 'string' ? res : JSON.stringify(res, null, 2);
            } catch (err: any) {
              resultStr = `Falha na execução de "${tc.name}": ${err?.message || String(err)}`;
            }
          }

          log(`[OpenBrain] 📥 Resposta de ${tc.name}: ${resultStr.substring(0, 150)}${resultStr.length > 150 ? '...' : ''}`);

          messages.push({
            role: 'tool',
            content: resultStr,
            toolCallId: tc.id
          });
        }
      } else {
        // No tool calls: final answer
        log(`[OpenBrain] ✅ Tarefa finalizada no ciclo ${step}.`);
        return response.content;
      }
    }

    return `[OpenBrain] ⚠️ Limite de ${this.maxSteps} passos atingido antes da conclusão total da tarefa.`;
  }
}
