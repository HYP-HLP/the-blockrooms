// 输出 void_boat.nbt 的 y 层 xz 布局，用于找物品展示框等挂点位置。
const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const FILE = path.join(__dirname, '..', 'src', 'main', 'resources', 'data', 'blockrooms', 'structure', 'void_boat.nbt');

const data = zlib.gunzipSync(fs.readFileSync(FILE));
let pos = 0;
function rt() { return data[pos++]; }
function rs() { const l = data.readUInt16BE(pos); pos += 2; const s = data.toString('utf8', pos, pos + l); pos += l; return s; }
function sp(t) {
  switch (t) {
    case 1: pos += 1; break;
    case 2: pos += 2; break;
    case 3: pos += 4; break;
    case 4: pos += 8; break;
    case 5: pos += 4; break;
    case 6: pos += 8; break;
    case 7: { const n = data.readInt32BE(pos); pos += 4 + n; break; }
    case 8: rs(); break;
    case 9: { const t = data[pos]; pos++; const n = data.readInt32BE(pos); pos += 4; for (let i = 0; i < n; i++) sp(t); break; }
    case 10: { while (true) { const t = rt(); if (t === 0) break; rs(); sp(t); } break; }
    case 11: { const n = data.readInt32BE(pos); pos += 4 + n * 4; break; }
    case 12: { const n = data.readInt32BE(pos); pos += 4 + n * 8; break; }
  }
}
rt(); rs();
function pc() {
  const k = {};
  while (true) { const t = rt(); if (t === 0) break; const n = rs(); k[n] = { type: t, start: pos }; sp(t); k[n].end = pos; }
  return k;
}
const root = pc();
function li(e) { if (!e) return null; const s = pos; pos = e.start; const t = data[pos]; pos++; const n = data.readInt32BE(pos); pos += 4; pos = s; return { t, n }; }

const pal = li(root.palette);
console.log('palette entry:', pal, 'palette start:', root.palette && root.palette.start);
const names = [];
if (pal) {
  pos = root.palette.start; data[pos]; pos++; const n = data.readInt32BE(pos); pos += 4;
  for (let i = 0; i < n; i++) {
    const k = pc();
    if (k.Name) { const s2 = pos; pos = k.Name.start; names.push(rs()); pos = s2; } else names.push('?');
  }
}
const bl = li(root.blocks);
const blocks = [];
if (bl) {
  pos = bl.start; const t = data[pos]; pos++; const n = data.readInt32BE(pos); pos += 4;
  for (let i = 0; i < n; i++) {
    const k = pc();
    let p = null;
    if (k.pos) {
      const s2 = pos; pos = k.pos.start;
      const lt = data[pos]; pos++;
      const cnt = data.readInt32BE(pos); pos += 4;
      p = [];
      if (lt === 3) {
        for (let j = 0; j < cnt; j++) { p.push(data.readInt32BE(pos)); pos += 4; }
      }
      pos = s2;
    }
    const state = k.state ? data[k.state.start] : 0;
    blocks.push({ p, s: state });
  }
}
console.log('palette:', names.join(', '));
console.log('blocks:', blocks.length);
const W = 13, H = 28;
for (let y = 4; y <= 14; y++) {
  console.log('--- y=' + y + ' ---');
  for (let z = 0; z < H; z++) {
    let row = '';
    for (let x = 0; x < W; x++) {
      const b = blocks.find(b => b.p && b.p[0] === x && b.p[1] === y && b.p[2] === z);
      if (!b) { row += ' '; continue; }
      const n = names[b.s] || '?';
      row += n.includes('air') ? ' ' : n.includes('blackstone') ? 'B' : n.includes('basalt') ? 'V'
        : n.includes('ladder') ? 'L' : n.includes('torch') ? 'T' : n.includes('end_rod') ? 'E'
        : n.includes('brewing') ? 'P' : n.includes('chest') ? 'C' : '#';
    }
    console.log(row);
  }
}
