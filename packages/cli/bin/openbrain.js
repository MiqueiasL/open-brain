#!/usr/bin/env node

/**
 * OpenBrain CLI Binary Launcher
 */

import { main } from '../src/index.js';

const args = process.argv.slice(2);

main(args).catch((err) => {
  console.error('\n\x1b[31m[OpenBrain Fatal Error]\x1b[0m', err?.message || err);
  process.exit(1);
});
