/**
 * OpenBrain Universal Persistent Memory
 * Stores knowledge in human-readable Markdown and connects to ai-memory / Obsidian.
 */

import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import { MemoryEntry, PlatformType } from '@openbrain/core';

export class UniversalMemory {
  private storageDir: string;

  constructor(customStorageDir?: string) {
    if (customStorageDir) {
      this.storageDir = customStorageDir;
    } else {
      // Default to ~/.openbrain/memory or system appdata
      const home = os.homedir();
      this.storageDir = path.join(home, '.openbrain', 'memory');
    }

    this.ensureStorageDir();
  }

  private ensureStorageDir(): void {
    if (!fs.existsSync(this.storageDir)) {
      fs.mkdirSync(this.storageDir, { recursive: true });
    }
  }

  public getStoragePath(): string {
    return this.storageDir;
  }

  public async save(entry: {
    id?: string;
    title: string;
    content: string;
    tags?: string[];
    platform?: PlatformType;
  }): Promise<MemoryEntry> {
    const id = entry.id || this.slugify(entry.title);
    const now = new Date().toISOString();

    const fullEntry: MemoryEntry = {
      id,
      title: entry.title,
      content: entry.content,
      tags: entry.tags || [],
      createdAt: now,
      updatedAt: now,
      platform: entry.platform
    };

    const filePath = path.join(this.storageDir, `${id}.md`);
    const frontmatter = [
      '---',
      `title: "${fullEntry.title}"`,
      `id: "${fullEntry.id}"`,
      `tags: [${fullEntry.tags.map(t => `"${t}"`).join(', ')}]`,
      `createdAt: "${fullEntry.createdAt}"`,
      `updatedAt: "${fullEntry.updatedAt}"`,
      fullEntry.platform ? `platform: "${fullEntry.platform}"` : '',
      '---',
      '',
      fullEntry.content
    ].filter(Boolean).join('\n');

    fs.writeFileSync(filePath, frontmatter, 'utf-8');
    return fullEntry;
  }

  public async get(id: string): Promise<MemoryEntry | null> {
    const filePath = path.join(this.storageDir, `${id}.md`);
    if (!fs.existsSync(filePath)) {
      return null;
    }

    const content = fs.readFileSync(filePath, 'utf-8');
    return this.parseMarkdownMemory(id, content);
  }

  public async query(searchTerm: string): Promise<MemoryEntry[]> {
    this.ensureStorageDir();
    const files = fs.readdirSync(this.storageDir).filter(f => f.endsWith('.md'));
    const results: MemoryEntry[] = [];
    const termLower = searchTerm.toLowerCase();

    for (const file of files) {
      const id = file.replace(/\.md$/, '');
      const raw = fs.readFileSync(path.join(this.storageDir, file), 'utf-8');
      const entry = this.parseMarkdownMemory(id, raw);

      if (
        entry.title.toLowerCase().includes(termLower) ||
        entry.content.toLowerCase().includes(termLower) ||
        entry.tags.some(t => t.toLowerCase().includes(termLower))
      ) {
        results.push(entry);
      }
    }

    return results;
  }

  public async listAll(): Promise<MemoryEntry[]> {
    this.ensureStorageDir();
    const files = fs.readdirSync(this.storageDir).filter(f => f.endsWith('.md'));
    const entries: MemoryEntry[] = [];

    for (const file of files) {
      const id = file.replace(/\.md$/, '');
      const raw = fs.readFileSync(path.join(this.storageDir, file), 'utf-8');
      entries.push(this.parseMarkdownMemory(id, raw));
    }

    return entries.sort((a, b) => b.updatedAt.localeCompare(a.updatedAt));
  }

  private parseMarkdownMemory(id: string, markdown: string): MemoryEntry {
    const match = markdown.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n([\s\S]*)$/);
    if (!match) {
      return {
        id,
        title: id,
        content: markdown.trim(),
        tags: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
    }

    const yaml = match[1];
    const body = match[2].trim();

    const titleMatch = yaml.match(/title:\s*"(.*?)"/);
    const tagsMatch = yaml.match(/tags:\s*\[(.*?)\]/);
    const createdMatch = yaml.match(/createdAt:\s*"(.*?)"/);
    const updatedMatch = yaml.match(/updatedAt:\s*"(.*?)"/);
    const platformMatch = yaml.match(/platform:\s*"(.*?)"/);

    const tags = tagsMatch
      ? tagsMatch[1].split(',').map(s => s.trim().replace(/^"|"$/g, '')).filter(Boolean)
      : [];

    return {
      id,
      title: titleMatch ? titleMatch[1] : id,
      content: body,
      tags,
      createdAt: createdMatch ? createdMatch[1] : new Date().toISOString(),
      updatedAt: updatedMatch ? updatedMatch[1] : new Date().toISOString(),
      platform: platformMatch ? (platformMatch[1] as PlatformType) : undefined
    };
  }

  private slugify(text: string): string {
    return text
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '') || `mem-${Date.now()}`;
  }
}
