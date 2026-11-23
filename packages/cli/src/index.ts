/**
 * OpenBrain CLI Entrypoint
 */

import * as readline from 'readline';
import { DriverFactory, PlatformDetector } from '../../drivers/src/index.js';
import { UniversalMemory } from '../../memory/src/index.js';
import { OpenBrainAgent, UniversalLLM, ToolDefinition } from '../../core/src/index.js';

export async function main(args: string[]) {
  const command = args[0]?.toLowerCase();

  const driver = await DriverFactory.getDriver();
  const device = await driver.getDeviceInfo();
  const memory = new UniversalMemory();

  // Print OpenBrain Header
  const printBanner = () => {
    console.log('\n======================================================');
    console.log('       🧠 OPENBRAIN — UNIVERSAL AUTONOMOUS AI CLI     ');
    console.log('         (Windows · macOS · Linux · Kali · Android)    ');
    console.log('======================================================\n');
  };

  switch (command) {
    case 'device':
    case 'status': {
      printBanner();
      console.log('📱 DIAGNÓSTICO DO DISPOSITIVO ATUAL:');
      console.log(`- Plataforma:          ${device.platform.toUpperCase()}`);
      console.log(`- Sistema Operacional: ${device.osRelease}`);
      console.log(`- Arquitetura:         ${device.arch}`);
      console.log(`- Hostname:            ${device.hostname}`);
      console.log(`- Kali Linux Ativo:    ${device.isKaliLinux ? '🟢 SIM' : '⚪ NÃO'}`);
      console.log(`- Permissões Root:     ${device.isRootOrAdmin ? '🟢 SIM' : '⚪ NÃO'}`);
      console.log(`- Modo Android/Termux: ${device.isTermuxOrShizuku ? '🟢 SIM' : '⚪ NÃO'}`);
      console.log(`- Ferramentas Nativas: [${device.availableTools.join(', ')}]`);
      console.log(`- Memória Persistente: ${memory.getStoragePath()}\n`);
      break;
    }

    case 'tools': {
      printBanner();
      console.log(`🛠️ FERRAMENTAS ATIVAS PARA O AMBIENTE [${device.platform.toUpperCase()}]:\n`);
      const nativeTools = driver.getNativeTools();
      for (const tool of nativeTools) {
        console.log(`• \x1b[36m${tool.name}\x1b[0m`);
        console.log(`  Descrição: ${tool.description}`);
        console.log(`  Parâmetros: ${Object.keys(tool.parameters).join(', ') || 'Nenhum'}\n`);
      }
      break;
    }

    case 'memory': {
      printBanner();
      const subCommand = args[1]?.toLowerCase();
      const query = args.slice(2).join(' ');

      if (subCommand === 'save' && query) {
        const entry = await memory.save({
          title: `Nota-${Date.now()}`,
          content: query,
          platform: device.platform
        });
        console.log(`✅ Memória gravada com sucesso! [ID: ${entry.id}] em ${memory.getStoragePath()}`);
      } else if (subCommand === 'search' && query) {
        const results = await memory.query(query);
        console.log(`🔍 Encontradas ${results.length} memórias para "${query}":\n`);
        for (const res of results) {
          console.log(`• [${res.id}] ${res.title} (${res.updatedAt})`);
          console.log(`  ${res.content}\n`);
        }
      } else {
        const all = await memory.listAll();
        console.log(`📚 ${all.length} memórias universais salvas em ${memory.getStoragePath()}:\n`);
        for (const res of all.slice(0, 10)) {
          console.log(`• [${res.id}] ${res.title}`);
          console.log(`  ${res.content.substring(0, 100)}${res.content.length > 100 ? '...' : ''}\n`);
        }
      }
      break;
    }

    case 'run': {
      const taskPrompt = args.slice(1).join(' ');
      if (!taskPrompt) {
        console.error('❌ Uso incorreto. Especifique a tarefa: openbrain run "<instrução>"');
        process.exit(1);
      }

      printBanner();
      await executeAutonomousTask(taskPrompt, driver, device, memory);
      break;
    }

    case 'chat':
    case 'repl':
    default: {
      printBanner();
      console.log(`[Modo Interativo Aberto] Dispositivo: ${device.platform.toUpperCase()} (${device.arch})`);
      console.log('Digite sua instrução para o OpenBrain ou digite "sair" para encerrar.\n');

      const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
      });

      const promptUser = () => {
        rl.question('\x1b[32mopenbrain>\x1b[0m ', async (input) => {
          const trimmed = input.trim();
          if (!trimmed) {
            promptUser();
            return;
          }

          if (trimmed.toLowerCase() === 'sair' || trimmed.toLowerCase() === 'exit') {
            console.log('\n👋 Até logo! Sessão do OpenBrain encerrada.');
            rl.close();
            return;
          }

          if (trimmed.toLowerCase() === 'device') {
            console.log(`[Status] ${device.platform.toUpperCase()} · ${device.osRelease}`);
            promptUser();
            return;
          }

          await executeAutonomousTask(trimmed, driver, device, memory);
          promptUser();
        });
      };

      promptUser();
      break;
    }
  }
}

async function executeAutonomousTask(goal: string, driver: any, device: any, memory: any) {
  // Provider detection: check environment
  const provider = process.env.GEMINI_API_KEY
    ? 'gemini'
    : process.env.ANTHROPIC_API_KEY
    ? 'claude'
    : process.env.OPENAI_API_KEY
    ? 'openai'
    : 'mock';

  const llm = new UniversalLLM({
    provider,
    modelName: provider === 'gemini' ? 'gemini-2.5-flash' : 'claude-3-7-sonnet-20250219'
  });

  // Tools: Native OS tools + Memory tool
  const tools: ToolDefinition[] = [
    ...driver.getNativeTools(),
    {
      name: 'query_universal_memory',
      description: 'Pesquisa na memória persistente universal do OpenBrain por tópicos, regras ou histórico.',
      parameters: {
        query: {
          type: 'string',
          description: 'Termo de busca na memória.',
          required: true
        }
      },
      execute: async (args) => {
        const memories = await memory.query(args.query);
        return memories.map((m: any) => ({
          title: m.title,
          content: m.content,
          date: m.updatedAt
        }));
      }
    },
    {
      name: 'save_universal_memory',
      description: 'Salva uma nova decisão, regra ou conhecimento na memória universal permanente.',
      parameters: {
        title: {
          type: 'string',
          description: 'Título descritivo da memória.',
          required: true
        },
        content: {
          type: 'string',
          description: 'Conteúdo detalhado a ser lembrado.',
          required: true
        }
      },
      execute: async (args) => {
        const saved = await memory.save({
          title: args.title,
          content: args.content,
          platform: device.platform
        });
        return `Memória salva com sucesso [ID: ${saved.id}]`;
      }
    }
  ];

  const agent = new OpenBrainAgent({
    llm,
    device,
    memory,
    tools,
    maxSteps: 12
  });

  const result = await agent.run(goal, (progress) => {
    console.log(`\x1b[90m${progress}\x1b[0m`);
  });

  console.log('\n\x1b[32m=== RESPOSTA DO OPENBRAIN ===\x1b[0m');
  console.log(result);
  console.log('\x1b[32m=============================\x1b[0m\n');
}
