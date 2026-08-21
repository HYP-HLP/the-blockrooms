// 批量解析结构 NBT：输出每个模板的尺寸、方块总数、方块类型分布。
// 用法：node docs/analyze_nbt.cjs
const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const DIR = process.argv[2] || path.join(__dirname, '..', 'src', 'main', 'resources', 'data', 'blockrooms', 'structure');

function parse(buf) {
  const data = buf[0] === 0x1f && buf[1] === 0x8b ? zlib.gunzipSync(buf) : buf;
  let pos = 0;
  function readTAG() { return data[pos++]; }
  function readString() {
    const len = data.readUInt16BE(pos); pos += 2;
    const s = data.toString('utf8', pos, pos + len); pos += len; return s;
  }
  function skipPayload(type) {
    switch (type) {
      case 1: pos += 1; break;
      case 2: pos += 2; break;
      case 3: pos += 4; break;
      case 4: pos += 8; break;
      case 5: pos += 4; break;
      case 6: pos += 8; break;
      case 7: { const n = data.readInt32BE(pos); pos += 4 + n; break; }
      case 8: readString(); break;
      case 9: { const t = data[pos]; pos++; const n = data.readInt32BE(pos); pos += 4; for (let i = 0; i < n; i++) skipPayload(t); break; }
      case 10: { while (true) { const t = readTAG(); if (t === 0) break; readString(); skipPayload(t); } break; }
      case 11: { const n = data.readInt32BE(pos); pos += 4 + n * 4; break; }
      case 12: { const n = data.readInt32BE(pos); pos += 4 + n * 8; break; }
    }
  }
  readTAG(); readString(); // 根 compound
  function parseCompound() {
    const keys = {};
    while (true) {
      const t = readTAG(); if (t === 0) break;
      const name = readString();
      keys[name] = { type: t, start: pos };
      skipPayload(t);
      keys[name].end = pos;
    }
    return keys;
  }
  const root = parseCompound();
  function listInfo(entry) {
    if (!entry) return null;
    const save = pos; pos = entry.start;
    const t = data[pos]; pos++;
    const n = data.readInt32BE(pos); pos += 4;
    pos = save;
    return { type: t, count: n };
  }
  const blocks = listInfo(root.blocks);
  const palette = listInfo(root.palette);
  const palettes = listInfo(root.palettes);
  const entities = listInfo(root.entities);
  const sizeEntry = root.size;
  let size = null;
  if (sizeEntry) {
    const save = pos; pos = sizeEntry.start;
    const t = data[pos]; pos++;
    const n = data.readInt32BE(pos); pos += 4;
    size = [];
    for (let i = 0; i < n; i++) { size.push(data.readInt32BE(pos)); pos += 4; }
    pos = save;
  }
  // 读取 palette 方块名（元素是 compound，含 "Name" 字符串）
  let names = [];
  const palEntry = palettes || palette;
  if (palEntry && palEntry.type === 10 && palEntry.start) {
    const save = pos; pos = palEntry.start;
    data[pos]; pos++; // 元素类型
    const n = data.readInt32BE(pos); pos += 4;
    for (let i = 0; i < n && i < 60; i++) {
      const keys = {};
      while (true) {
        const t = data[pos]; pos++;
        if (t === 0) break;
        const name = readString();
        keys[name] = { start: pos, type: t };
        skipPayload(t);
      }
      if (keys.Name && keys.Name.type === 8) {
        const s2 = pos; pos = keys.Name.start;
        names.push(readString());
        pos = s2;
      } else {
        names.push('?');
      }
    }
    pos = save;
  }
  return { size, blocks, palette, palettes, entities, names };
}

for (const f of fs.readdirSync(DIR).filter(f => f.endsWith('.nbt')).sort()) {
  try {
    const r = parse(fs.readFileSync(path.join(DIR, f)));
    const size = r.size ? r.size.join('x') : '?';
    const nameStr = r.names.length ? r.names.join(', ') : '';
    console.log(`${f}: size=${size} blocks=${r.blocks ? r.blocks.count : 0} palette=[${nameStr}] entities=${r.entities ? r.entities.count : 0}`);
  } catch (e) {
    console.log(`${f}: PARSE ERROR ${e.message}`);
  }
}
