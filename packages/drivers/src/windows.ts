/**
 * OpenBrain Windows OS Driver
 */

import { exec } from 'child_process';
import * as fs from 'fs';
import * as path from 'path';
import { CommandExecutionOptions, CommandResult, DeviceInfo, ToolDefinition } from '@openbrain/core';
import { OSDriver } from './base.js';
import { PlatformDetector } from './detector.js';

export class WindowsDriver implements OSDriver {
  public getPlatform() {
    return 'windows' as const;
  }

  public async getDeviceInfo(): Promise<DeviceInfo> {
    const info = await PlatformDetector.detect();
    info.availableTools = ['powershell', 'cmd', 'explorer', 'winget'];
    return info;
  }

  public async executeCommand(command: string, options?: CommandExecutionOptions): Promise<CommandResult> {
    const start = Date.now();
    return new Promise((resolve) => {
      // Execute in powershell.exe by default
      const psCommand = `powershell.exe -NoProfile -NonInteractive -ExecutionPolicy Bypass -Command "${command.replace(/"/g, '`"')}"`;

      exec(psCommand, {
        cwd: options?.cwd || process.cwd(),
        timeout: options?.timeoutMs || 60000,
        maxBuffer: 10 * 1024 * 1024
      }, (err, stdout, stderr) => {
        resolve({
          stdout: stdout || '',
          stderr: stderr || (err ? err.message : ''),
          exitCode: err && typeof err.code === 'number' ? err.code : (err ? 1 : 0),
          durationMs: Date.now() - start
        });
      });
    });
  }

  public async readFile(filePath: string): Promise<string> {
    return fs.readFileSync(path.resolve(filePath), 'utf-8');
  }

  public async writeFile(filePath: string, content: string): Promise<void> {
    const resolved = path.resolve(filePath);
    fs.mkdirSync(path.dirname(resolved), { recursive: true });
    fs.writeFileSync(resolved, content, 'utf-8');
  }

  public async listDirectory(dirPath: string): Promise<string[]> {
    return fs.readdirSync(path.resolve(dirPath));
  }

  public getNativeTools(): ToolDefinition[] {
    return [
      {
        name: 'run_powershell',
        description: 'Executa um comando no PowerShell do Windows e retorna a saída (stdout/stderr).',
        parameters: {
          command: {
            type: 'string',
            description: 'O script ou comando PowerShell para executar.',
            required: true
          }
        },
        execute: async (args) => {
          const res = await this.executeCommand(args.command);
          return {
            stdout: res.stdout.trim(),
            stderr: res.stderr.trim(),
            exitCode: res.exitCode
          };
        }
      },
      {
        name: 'read_file',
        description: 'Lê o conteúdo de um arquivo de texto no Windows.',
        parameters: {
          path: {
            type: 'string',
            description: 'Caminho absoluto ou relativo do arquivo.',
            required: true
          }
        },
        execute: async (args) => {
          return await this.readFile(args.path);
        }
      },
      {
        name: 'write_file',
        description: 'Grava ou substitui o conteúdo de um arquivo no Windows.',
        parameters: {
          path: {
            type: 'string',
            description: 'Caminho do arquivo a ser criado ou atualizado.',
            required: true
          },
          content: {
            type: 'string',
            description: 'O conteúdo de texto a gravar.',
            required: true
          }
        },
        execute: async (args) => {
          await this.writeFile(args.path, args.content);
          return `Arquivo gravado com sucesso em: ${args.path}`;
        }
      }
    ];
  }
}
