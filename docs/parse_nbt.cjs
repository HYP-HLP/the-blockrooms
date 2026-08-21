// Minimal gzipped NBT (named binary tag, big-endian) parser for structure templates.
const zlib = require('zlib');
const fs = require('fs');

const buf = zlib.gunzipSync(fs.readFileSync(process.argv[2]));
let off = 0;
function u8() { return buf[off++]; }
function u16() { const v = buf.readUInt16BE(off); off += 2; return v; }
function u32() { const v = buf.readUInt32BE(off); off += 4; return v; }
function s32() { const v = buf.readInt32BE(off); off += 4; return v; }
function f64() { const v = buf.readDoubleBE(off); off += 8; return v; }
function name() { const len = u16(); const s = buf.toString('utf8', off, off + len); off += len; return s; }

const TAG_END = 0, TAG_BYTE = 1, TAG_SHORT = 2, TAG_INT = 3, TAG_LONG = 4, TAG_FLOAT = 5,
  TAG_DOUBLE = 6, TAG_BYTE_ARRAY = 7, TAG_STRING = 8, TAG_LIST = 9, TAG_COMPOUND = 10,
  TAG_INT_ARRAY = 11, TAG_LONG_ARRAY = 12;

function readPayload(type) {
  switch (type) {
    case TAG_BYTE: return u8();
    case TAG_SHORT: { const v = buf.readInt16BE(off); off += 2; return v; }
    case TAG_INT: return s32();
    case TAG_LONG: { const v = buf.readBigInt64BE(off); off += 8; return v; }
    case TAG_FLOAT: { const v = buf.readFloatBE(off); off += 4; return v; }
    case TAG_DOUBLE: return f64();
    case TAG_BYTE_ARRAY: { const n = u32(); const v = [...buf.subarray(off, off + n)]; off += n; return v; }
    case TAG_STRING: return name();
    case TAG_LIST: {
      const elemType = u8(); const count = u32();
      const arr = [];
      for (let i = 0; i < count; i++) arr.push(readPayload(elemType));
      return arr;
    }
    case TAG_COMPOUND: {
      const obj = {};
      for (;;) {
        const t = u8();
        if (t === TAG_END) break;
        const n = name();
        obj[n] = readPayload(t);
      }
      return obj;
    }
    case TAG_INT_ARRAY: { const n = u32(); const v = []; for (let i = 0; i < n; i++) v.push(s32()); return v; }
    case TAG_LONG_ARRAY: { const n = u32(); const v = []; for (let i = 0; i < n; i++) v.push(buf.readBigInt64BE(off)), off += 8; return v; }
    default: throw new Error('unknown tag type ' + type);
  }
}

const rootType = u8();
const rootName = name();
const root = readPayload(rootType);
console.log('root:', rootType === TAG_COMPOUND ? 'Compound(' + rootName + ')' : '?');

if (root.size) {
  console.log('size (x,y,z):', JSON.stringify(root.size));
  const [sx, sy, sz] = root.size;
  console.log('within 16x8x16:', sx <= 16 && sy <= 8 && sz <= 16 ? 'YES' : 'NO!');
}
if (root.palette) {
  console.log('palette entries:', root.palette.length);
  const counts = {};
  for (const p of root.palette) {
    const name_ = p.Name || '?';
    counts[name_] = (counts[name_] || 0) + 1;
    if (p.Properties) console.log('  state:', name_, JSON.stringify(p.Properties));
    else console.log('  state:', name_);
  }
  const total = root.blocks ? root.blocks.length : 0;
  console.log('block entries:', total);
  const stateCount = {};
  for (const b of root.blocks || []) {
    const sn = root.palette[b.state] ? root.palette[b.state].Name : '?' + b.state;
    stateCount[sn] = (stateCount[sn] || 0) + 1;
  }
  console.log('block usage:', JSON.stringify(stateCount));
  const withNbt = (root.blocks || []).filter(b => b.nbt !== undefined);
  console.log('blocks with NBT (block entities):', withNbt.length);
  for (const b of withNbt.slice(0, 5)) {
    console.log('  BE at', JSON.stringify(b.pos), '->', root.palette[b.state] ? root.palette[b.state].Name : '?', JSON.stringify(b.nbt).slice(0, 200));
  }
}
if (root.entities) console.log('entities:', root.entities.length);
