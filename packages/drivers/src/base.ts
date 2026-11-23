/**
 * OpenBrain Base OS Driver Interface
 */

import { CommandExecutionOptions, CommandResult, DeviceInfo, PlatformType, ToolDefinition } from '@openbrain/core';

export interface OSDriver {
  getPlatform(): PlatformType;
  getDeviceInfo(): Promise<DeviceInfo>;
  executeCommand(command: string, options?: CommandExecutionOptions): Promise<CommandResult>;
  getNativeTools(): ToolDefinition[];
  readFile(path: string): Promise<string>;
  writeFile(path: string, content: string): Promise<void>;
  listDirectory(path: string): Promise<string[]>;
}
