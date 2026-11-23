/**
 * OpenBrain Platform Detector
 */

import * as os from 'os';
import * as fs from 'fs';
import { DeviceInfo, PlatformType } from '@openbrain/core';

export class PlatformDetector {
  public static async detect(): Promise<DeviceInfo> {
    const rawPlatform = os.platform();
    let platform: PlatformType = 'unknown';
    let isKaliLinux = false;
    let isTermuxOrShizuku = false;

    // Check Android / Termux
    if (process.env.TERMUX_VERSION || process.env.PREFIX?.includes('com.termux')) {
      platform = 'android';
      isTermuxOrShizuku = true;
    } else if (rawPlatform === 'win32') {
      platform = 'windows';
    } else if (rawPlatform === 'darwin') {
      platform = 'macos';
    } else if (rawPlatform === 'linux') {
      platform = 'linux';
      // Check if Kali Linux
      try {
        if (fs.existsSync('/etc/os-release')) {
          const osRelease = fs.readFileSync('/etc/os-release', 'utf-8').toLowerCase();
          if (osRelease.includes('kali')) {
            isKaliLinux = true;
          }
        }
      } catch {
        // Ignored
      }
    }

    // Check root / admin
    let isRootOrAdmin = false;
    if (platform === 'windows') {
      isRootOrAdmin = false; // can check via whoami if needed
    } else if (typeof process.getuid === 'function') {
      isRootOrAdmin = process.getuid() === 0;
    }

    return {
      platform,
      arch: os.arch(),
      osRelease: `${os.type()} ${os.release()}`,
      hostname: os.hostname(),
      isRootOrAdmin,
      isTermuxOrShizuku,
      isKaliLinux,
      availableTools: []
    };
  }
}
