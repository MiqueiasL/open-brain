/**
 * OpenBrain Driver Factory
 */

import { OSDriver } from './base.js';
import { PlatformDetector } from './detector.js';
import { WindowsDriver } from './windows.js';
import { LinuxDriver } from './linux.js';
import { AndroidDriver } from './android.js';

export * from './base.js';
export * from './detector.js';
export * from './windows.js';
export * from './linux.js';
export * from './android.js';

export class DriverFactory {
  public static async getDriver(): Promise<OSDriver> {
    const info = await PlatformDetector.detect();

    switch (info.platform) {
      case 'windows':
        return new WindowsDriver();
      case 'android':
        return new AndroidDriver();
      case 'linux':
      default:
        return new LinuxDriver();
    }
  }
}
