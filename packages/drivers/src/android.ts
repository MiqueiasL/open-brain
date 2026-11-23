/**
 * OpenBrain Android OS Driver (Termux & Shizuku)
 */

import { exec } from 'child_process';
import * as fs from 'fs';
import * as path from 'path';
import { CommandExecutionOptions, CommandResult, DeviceInfo, ToolDefinition } from '@openbrain/core';
import { OSDriver } from './base.js';
import { PlatformDetector } from './detector.js';

export class AndroidDriver implements OSDriver {
  public getPlatform() {
    return 'android' as const;
  }

  public async getDeviceInfo(): Promise<DeviceInfo> {
    const info = await PlatformDetector.detect();
    info.platform = 'android';
    info.isTermuxOrShizuku = true;
    info.availableTools = ['sh', 'pm', 'am', 'input', 'termux-api'];
    return info;
  }

  public async executeCommand(command: string, options?: CommandExecutionOptions): Promise<CommandResult> {
    const start = Date.now();
    return new Promise((resolve) => {
      // In Android/Termux, use standard sh or rish if elevated
      const shellPath = fs.existsSync('/system/bin/sh') ? '/system/bin/sh' : '/bin/sh';

      exec(command, {
        shell: shellPath,
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
        name: 'run_android_shell',
        description: 'Executa um comando de terminal no Android (compatível com Termux, Shizuku e comandos pm/am).',
        parameters: {
          command: {
            type: 'string',
            description: 'Comando shell no Android (ex: "pm list packages", "dumpsys battery", "am start ...").',
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
        name: 'install_apk',
        description: 'Instala um arquivo APK no smartphone Android via pm install.',
        parameters: {
          apkPath: {
            type: 'string',
            description: 'Caminho absoluto do arquivo .apk no armazenamento do Android.',
            required: true
          }
        },
        execute: async (args) => {
          const cmd = `pm install -r -d "${args.apkPath}"`;
          const res = await this.executeCommand(cmd);
          return {
            result: res.stdout.trim() || res.stderr.trim(),
            success: res.exitCode === 0
          };
        }
      },
      {
        name: 'launch_app',
        description: 'Inicia um aplicativo no Android pelo nome do pacote.',
        parameters: {
          packageName: {
            type: 'string',
            description: 'Nome do pacote do app (ex: "com.whatsapp", "com.google.android.youtube").',
            required: true
          }
        },
        execute: async (args) => {
          const cmd = `monkey -p ${args.packageName} -c android.intent.category.LAUNCHER 1`;
          const res = await this.executeCommand(cmd);
          return {
            success: res.exitCode === 0,
            output: res.stdout.trim()
          };
        }
      }
    ];
  }
}
